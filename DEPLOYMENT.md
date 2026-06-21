# Deploy em Plataformas Cloud

Este projeto está configurado para deploy em múltiplas plataformas cloud com Dockerfile otimizado:
- Dockerfile com yt-dlp e FFmpeg incluídos
- Variáveis de ambiente configuradas
- Configuração otimizada para ambiente Linux

## Plataformas Disponíveis

### 1. Render (Recomendado - Plano Gratuito)
- **Custo:** $0/mês (com keep-alive)
- **Sleep:** Sim (15min inatividade) - resolvido com UptimeRobot
- **Recursos:** 512MB RAM, 0.1 CPU
- **Documentação:** Veja [RENDER_DEPLOY.md](./RENDER_DEPLOY.md)

### 2. Fly.io (Alternativa)
- **Custo:** $0/mês (limitado) ou $5-7/mês
- **Sleep:** Não
- **Recursos:** 256MB RAM (grátis) ou 512MB RAM (pago)
- **Documentação:** Veja abaixo

---

## Deploy no Render (Recomendado)

Para instruções detalhadas de deploy no Render, veja [RENDER_DEPLOY.md](./RENDER_DEPLOY.md).

### Resumo Rápido

1. **Fazer push para GitHub**
   ```bash
   git add .
   git commit -m "Preparado para deploy no Render"
   git push origin main
   ```

2. **Deploy no Render**
   - Acesse https://dashboard.render.com
   - New + → Web Service → Conecte seu repositório
   - Render detectará `render.yaml` automaticamente
   - Clique em "Create Web Service"

3. **Configurar Keep-Alive**
   - Acesse https://uptimerobot.com
   - Adicione monitor: `https://seu-app.onrender.com/api/health`
   - Intervalo: 5 minutos

---

## Deploy no Fly.io (Alternativa)

## Pré-requisitos

1. **Instalar Fly CLI:**
   ```bash
   # Windows (PowerShell)
   iwr https://fly.io/install.ps1 -useb | iex
   
   # Linux/Mac
   curl -L https://fly.io/install.sh | sh
   ```

2. **Fazer login no Fly:**
   ```bash
   flyctl auth login
   ```

## Passos para Deploy

### 1. Criar aplicação no Fly.io
```bash
flyctl apps create yt-dlp-java
```

### 2. Configurar volume para downloads temporários
```bash
flyctl volumes create temp_downloads --size 1 --region gru
```

### 3. Deploy da aplicação
```bash
flyctl deploy
```

### 4. Verificar status do deploy
```bash
flyctl status
```

### 5. Verificar logs
```bash
flyctl logs
```

## Configurações Importantes

### Variáveis de Ambiente
O projeto usa as seguintes variáveis de ambiente (configuradas no fly.toml):
- `FFMPEG_PATH=/usr/bin/ffmpeg` - Caminho do FFmpeg
- `YTDLP_PATH=/usr/local/bin/yt-dlp` - Caminho do yt-dlp
- `TEMP_DIR=/tmp/downloads` - Diretório temporário
- `JAVA_OPTS=-Xmx512m -Xms256m` - Configurações de memória JVM

### Região
A região padrão é `gru` (São Paulo). Para mudar:
```bash
flyctl regions set gru
```

### Escalabilidade
O app está configurado para:
- Auto-start: Sim
- Auto-stop: Sim
- Máquinas mínimas: 0 (scale to zero quando não em uso)
- Memória: 512MB
- CPU: 1 vCPU (shared)

## Endpoints da API

Após o deploy, a API estará disponível em:
- Fly.io: `https://yt-dlp-java.fly.dev/api/download` - Download assíncrono
- Fly.io: `https://yt-dlp-java.fly.dev/api/download-stream` - Download streaming
- Render: `https://seu-app.onrender.com/api/download` - Download assíncrono
- Render: `https://seu-app.onrender.com/api/download-stream` - Download streaming

## Monitoramento Fly.io

### Ver métricas
```bash
flyctl monitor
```

### Ver máquinas
```bash
flyctl machines list
```

### Reiniciar aplicação
```bash
flyctl apps restart yt-dlp-java
```

## Troubleshooting Fly.io

### Erro de permissão no volume
```bash
flyctl ssh shell
cd /tmp/downloads
chmod 777 .
```

### Ver logs em tempo real
```bash
flyctl logs --tail
```

### Rebuild completo
```bash
flyctl deploy --remote-only
```

## Limitações do Plano Gratuito Fly.io

- 3 apps gratuitos
- 256MB RAM por app (configuramos 512MB, pode exceder limite)
- 2GB volumes
- Apps dormem após 24h de inatividade
- Requer cartão de crédito para cadastro

## Custos Adicionais Fly.io

Se precisar de mais recursos:
- Upgrade para plano pago (~$5-10/mês para 512MB RAM)
- Volume adicional se necessário
- Bandwidth extra se muito uso

## Comparação de Plataformas

| Plataforma | Plano Gratuito | Sleep | CPU | RAM | Recomendado |
|------------|----------------|-------|-----|-----|-------------|
| Render | $0 | Sim (15min) | 0.1 | 512MB | ✅ Sim |
| Fly.io | $0 | Não | 1 | 256MB | ⚠️ Pouca RAM |
| Railway | $1/mês | Não | 1 | 0.5GB | ❌ Caro |

**Render é a melhor opção gratuita** para este projeto, com instruções detalhadas em [RENDER_DEPLOY.md](./RENDER_DEPLOY.md).
