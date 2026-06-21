package com.jfposton.ytdlp.controller;

import com.jfposton.ytdlp.YtDlp;
import com.jfposton.ytdlp.YtDlpRequest;
import com.jfposton.ytdlp.YtDlpResponse;
import com.jfposton.ytdlp.dto.DownloadRequest;
import com.jfposton.ytdlp.dto.DownloadResponse;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/download")
    public ResponseEntity<DownloadResponse> download(@RequestBody DownloadRequest downloadRequest) {
        String videoUrl = downloadRequest.getUrl();
        String pastaDestino = downloadRequest.getDestinationPath();

        if (pastaDestino == null || pastaDestino.isEmpty()) {
            pastaDestino = System.getProperty("java.io.tmpdir");
        }

        // Configura a requisição
        YtDlpRequest request = new YtDlpRequest(videoUrl, pastaDestino);
        request.setOption("no-playlist");
        request.setOption("ignore-errors");
        request.setOption("extract-audio");
        request.setOption("audio-format", "mp3");
        request.setOption("audio-quality", "0");
        
        request.setOption("user-agent", "\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36\"");
        request.setOption("no-check-certificates");
        request.setOption("prefer-insecure");
        
        // Usar variável de ambiente ou caminho padrão do sistema
        String ffmpegPath = System.getenv().getOrDefault("FFMPEG_PATH", "ffmpeg");
        request.setOption("ffmpeg-location", ffmpegPath);

        try {
            YtDlpResponse response = YtDlp.execute(request, System.out::println);

            DownloadResponse downloadResponse = new DownloadResponse(
                "Download concluído com sucesso",
                response.getOut(),
                true
            );

            return ResponseEntity.ok(downloadResponse);
        } catch (Exception e) {
            DownloadResponse downloadResponse = new DownloadResponse(
                "Erro ao processar download: " + e.getMessage(),
                e.getMessage(),
                false
            );

            return ResponseEntity.status(500).body(downloadResponse);
        }
    }

    @PostMapping("/download-stream")
    public ResponseEntity<Resource> downloadStream(@RequestBody DownloadRequest downloadRequest) {
        String videoUrl = downloadRequest.getUrl();
        String tempDir = System.getProperty("java.io.tmpdir");

        // Criar um subdiretório único para este download
        String uniqueDir = tempDir + File.separator + "ytdlp_" + System.currentTimeMillis();
        Path downloadDir = Paths.get(uniqueDir);
        try {
            Files.createDirectories(downloadDir);
        } catch (Exception e) {
            logger.error("Erro ao criar diretório temporário: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }

        // Listar arquivos antes do download
        File[] filesBefore = downloadDir.toFile().listFiles();

        // Configura a requisição
        YtDlpRequest request = new YtDlpRequest(videoUrl, uniqueDir);
        request.setOption("no-playlist");
        request.setOption("ignore-errors");
        request.setOption("extract-audio");
        request.setOption("audio-format", "mp3");
        request.setOption("audio-quality", "0");
        
        request.setOption("user-agent", "\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36\"");
        request.setOption("no-check-certificates");
        request.setOption("prefer-insecure");
        
        // Usar variável de ambiente ou caminho padrão do sistema
        String ffmpegPath = System.getenv().getOrDefault("FFMPEG_PATH", "ffmpeg");
        request.setOption("ffmpeg-location", ffmpegPath);
        request.setOption("output", "%(title)s.%(ext)s");

        try {
            YtDlpResponse response = YtDlp.execute(request, System.out::println);

            // Listar arquivos após o download para encontrar o novo arquivo
            File downloadedFile = getFile(downloadDir, filesBefore);

            if (downloadedFile == null) {
                return ResponseEntity.status(500).build();
            }

            // Ler o arquivo e criar o Resource
            byte[] fileContent = Files.readAllBytes(downloadedFile.toPath());
            ByteArrayResource resource = new ByteArrayResource(fileContent);

            // Sanitizar o nome do arquivo para remover caracteres problemáticos mas preservar acentos
            String originalFileName = downloadedFile.getName();
            System.out.println("Nome original do arquivo: " + originalFileName);
            
            // Remove apenas caracteres realmente problemáticos para HTTP headers
            // Preserva letras, números, espaços, acentos, hífens, underscores e pontos
            String sanitizedFileName = originalFileName.replaceAll("[^ -~\u00A0-ÿ]", "_");
            System.out.println("Nome sanitizado do arquivo: " + sanitizedFileName);

            try {
                // Tentar codificar o nome do arquivo para URL
                String encodedFileName = URLEncoder.encode(sanitizedFileName, StandardCharsets.UTF_8);
                
                // Limpar o arquivo temporário
                Files.delete(downloadedFile.toPath());
                Files.delete(downloadDir);

                // Retornar o arquivo como blob
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"" + sanitizedFileName + "\"")
                        .header("Access-Control-Expose-Headers", "Content-Disposition")  // ← Adicione isto!
                        .body(resource);
            } catch (UnsupportedEncodingException e) {
                // Fallback para nome simplificado
                Files.delete(downloadedFile.toPath());
                Files.delete(downloadDir);
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=\"audio.mp3\"")
                        .body(resource);
            }

        } catch (Exception e) {
            logger.error("Erro ao processar download-stream: {}", e.getMessage(), e);
            // Limpar diretório temporário em caso de erro
            try {
                if (Files.exists(downloadDir)) {
                    try (Stream<Path> stream = Files.walk(downloadDir)) {
                        stream.sorted(Comparator.reverseOrder())
                              .map(Path::toFile)
                              .forEach(File::delete);
                    }
                }
            } catch (Exception ex) {
                logger.error("Erro ao limpar diretório temporário: {}", ex.getMessage(), ex);
            }
            return ResponseEntity.status(500).build();
        }
    }

    @Nullable
    private static File getFile(Path downloadDir, File[] filesBefore) {
        File[] filesAfter = downloadDir.toFile().listFiles();
        File downloadedFile = null;

        if (filesAfter != null && filesBefore != null) {
            for (File file : filesAfter) {
                boolean isNew = true;
                for (File beforeFile : filesBefore) {
                    if (file.getName().equals(beforeFile.getName())) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew && file.isFile() && file.getName().endsWith(".mp3")) {
                    downloadedFile = file;
                    break;
                }
            }
        }

        // Se não encontrou pelo método de comparação, pega o primeiro arquivo MP3
        if (downloadedFile == null && filesAfter != null) {
            for (File file : filesAfter) {
                if (file.isFile() && file.getName().endsWith(".mp3")) {
                    downloadedFile = file;
                    break;
                }
            }
        }
        return downloadedFile;
    }
}
