package com.jfposton.ytdlp;

import java.util.Scanner;

public class YtDlpMain {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        String videoUrl = "";
        String pastaDestino = System.getProperty("java.io.tmpdir");

        System.out.println("Digite o URL do vídeo: ");
        videoUrl = scanner.nextLine();

        System.out.println("Processando...");

        // Configura a requisição
        YtDlpRequest request = new YtDlpRequest(videoUrl, pastaDestino);
        request.setOption("no-playlist"); // Desabilita o download de playlists
        request.setOption("ignore-errors");
        request.setOption("extract-audio"); // Extrai apenas áudio
        request.setOption("audio-format", "mp3"); // Define formato MP3
        request.setOption("audio-quality", "0"); // Melhor qualidade
        
        // Usar variável de ambiente ou caminho padrão do sistema
        String ffmpegPath = System.getenv().getOrDefault("FFMPEG_PATH", "ffmpeg");
        request.setOption("ffmpeg-location", ffmpegPath);

        try {
            YtDlpResponse response = YtDlp.execute(request, new DownloadProgressCallback() {
                @Override
                public void onProgressUpdate(String progress) {
                    // Parse the progress string to extract percentage and ETA if available
                    System.out.println(progress);
                }
            });
            System.out.println("Download concluído: " + response.getOut());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
