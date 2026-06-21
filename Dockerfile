# Multi-stage build para Fly.io
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Instalar Maven
RUN apk add --no-cache maven

# Copiar pom.xml e baixar dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Buildar o projeto
RUN mvn clean package -DskipTests -B

# Imagem final com yt-dlp e FFmpeg
FROM eclipse-temurin:17-jre-alpine

# Instalar dependências do sistema
RUN apk add --no-cache \
    python3 \
    py3-pip \
    ffmpeg \
    curl \
    && rm -rf /var/cache/apk/*

# Instalar yt-dlp
RUN pip3 install --no-cache-dir --break-system-packages yt-dlp

WORKDIR /app

# Copiar o JAR do builder (Spring Boot plugin gera JAR executável)
COPY --from=builder /app/target/*.jar app.jar

# Criar diretório para downloads temporários
RUN mkdir -p /tmp/downloads

# Expor porta
EXPOSE 8080

# Variáveis de ambiente
ENV FFMPEG_PATH=/usr/bin/ffmpeg
ENV YTDLP_PATH=/usr/local/bin/yt-dlp
ENV TEMP_DIR=/tmp/downloads

# Command para rodar a aplicação Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
