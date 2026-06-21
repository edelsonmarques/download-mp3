# Deploy no Render

Este projeto está configurado para deploy no Render com as seguintes características:
- Dockerfile com yt-dlp e FFmpeg incluídos
- render.yaml para configuração automática
- Endpoint de health check para keep-alive
- Variáveis de ambiente configuradas

## Pré-requisitos

1. **Conta no Render** - Crie em https://render.com
2. **Repositório GitHub** - O projeto deve estar no GitHub
3. **Docker** - Opcional, para testes locais

## Passos para Deploy

### Opção 1: Deploy Automático (Recomendado)

1. **Fazer push do projeto para GitHub**
   ```bash
   git add .
   git commit -m "Preparado para deploy no Render"
   git push origin main
   ```

2. **Conectar no Render**
   - Acesse https://dashboard.render.com
   - Clique em "New +"
   - Selecione "Web Service"
   - Conecte seu repositório GitHub
   - Selecione o repositório yt-dlp-java

3. **Configurar o Web Service**
   - Render detectará automaticamente o `render.yaml`
   - Nome: `yt-dlp-java`
   - Branch: `main`
   - Region: `Oregon` (ou mais próxima)
   - Plan: `Free`

4. **Variáveis de Ambiente**
   - Render configurará automaticamente as variáveis do `render.yaml`
   - Verifique se estão presentes:
     - `FFMPEG_PATH=/usr/bin/ffmpeg`
     - `YTDLP_PATH=/usr/local/bin/yt-dlp`
     - `TEMP_DIR=/tmp/downloads`
     - `JAVA_OPTS=-Xmx512m -Xms256m`

5. **Deploy**
   - Clique em "Create Web Service"
   - Render fará o build automaticamente
   - Aguarde o deploy (pode levar 5-10 minutos)

### Opção 2: Deploy Manual via Blueprint

1. **Fazer push do projeto para GitHub**
   ```bash
   git add .
   git commit -m "Preparado para deploy no Render"
   git push origin main
   ```

2. **Usar Blueprint do Render**
   - Acesse https://dashboard.render.com
   - Clique em "New +"
   - Selecione "Blueprint"
   - Cole a URL do seu repositório GitHub
   - Render lerá o `render.yaml` automaticamente
   - Clique em "Apply"

## Configurar Keep-Alive (Evitar Sleep)

O plano gratuito do Render dorme após 15 minutos de inatividade. Para evitar isso:

### 1. UptimeRobot (Gratuito)

1. Acesse https://uptimerobot.com
2. Crie conta gratuita
3. Adicione novo monitor:
   - **Monitor Type:** HTTP(s)
   - **URL:** `https://seu-app.onrender.com/api/health`
   - **Monitoring Interval:** 5 minutos
   - **Alert Contacts:** Seu email

### 2. Freshping.io (Alternativa Gratuita)

1. Acesse https://freshping.io
2. Crie conta gratuita
3. Configure monitor similar ao UptimeRobot

### 3. Pingdom (Alternativa)

1. Acesse https://pingdom.com
2. Plano gratuito com 1 monitor
3. Configure para pingar `/api/health` a cada 1 minuto

## Verificar Deploy

### 1. Verificar Status
- Acesse o dashboard do Render
- Veja se o serviço está "Live"
- Verifique os logs se houver erro

### 2. Testar API
```bash
# Testar health check
curl https://seu-app.onrender.com/api/health

# Testar download
curl -X POST https://seu-app.onrender.com/api/download \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.youtube.com/watch?v=VIDEO_ID"}'
```

### 3. Verificar Logs
- No dashboard do Render
- Clique no serviço
- Aba "Logs"

## Endpoints da API

Após o deploy, a API estará disponível em:
- `https://seu-app.onrender.com/api/health` - Health check
- `https://seu-app.onrender.com/api/download` - Download assíncrono
- `https://seu-app.onrender.com/api/download-stream` - Download streaming

## Configurações Importantes

### Plano Gratuito Render
- **512MB RAM** - Suficiente para Java + yt-dlp
- **0.1 CPU** - Pode ser lento para downloads
- **Sleep após 15min inatividade** - Resolvido com keep-alive
- **Cold start ~30s** - Primeira requisição após sleep
- **750h/mês** - Suficiente para uso moderado

### Variáveis de Ambiente
Configuradas automaticamente pelo `render.yaml`:
- `FFMPEG_PATH=/usr/bin/ffmpeg`
- `YTDLP_PATH=/usr/local/bin/yt-dlp`
- `TEMP_DIR=/tmp/downloads`
- `JAVA_OPTS=-Xmx512m -Xms256m`

### Região
Recomendada: `Oregon` (us-west) ou região mais próxima dos usuários

## Troubleshooting

### Erro: Build Failed
- Verifique os logs no dashboard do Render
- Confirme que o Dockerfile está correto
- Verifique se o yt-dlp está instalando corretamente

### Erro: Service Crashed
- Verifique logs de runtime
- Pode ser falta de memória (plano gratuito)
- Considere upgrade para plano Starter ($7/mês)

### App dorme apesar do keep-alive
- Verifique se o UptimeRobot está funcionando
- Aumente frequência dos pings (mínimo 5 minutos)
- Verifique se o endpoint `/api/health` está acessível

### Download muito lento
- Plano gratuito tem CPU limitada
- Considere upgrade para plano com mais CPU
- Otimize as opções do yt-dlp

### Erro de permissão
- O Dockerfile já configura permissões corretas
- Verifique se as variáveis de ambiente estão corretas

## Upgrade de Plano

Se precisar de mais recursos:

### Plano Starter ($7/mês)
- 512MB RAM → 2GB RAM
- 0.1 CPU → 0.5 CPU
- Sem sleep por inatividade
- Melhor performance para downloads

### Plano Standard ($25/mês)
- 2GB RAM → 4GB RAM
- 0.5 CPU → 1 CPU
- Performance ainda melhor

## Monitoramento

### Métricas no Dashboard
- CPU usage
- Memory usage
- Response times
- Error rates

### Logs
- Logs em tempo real
- Histórico de logs
- Filtros e busca

## Custos

### Plano Gratuito
- **$0/mês** (com keep-alive configurado)
- Limitações: CPU, RAM, sleep
- Ideal para testes e uso pessoal

### Plano Starter ($7/mês)
- Sem sleep por inatividade
- Mais recursos
- Melhor para produção

## Backup e Restore

Render não faz backup automático de volumes no plano gratuito. Para dados importantes:
- Considere upgrade para plano pago
- Use armazenamento externo (S3, etc)
- Implemente download imediato após processamento

## Suporte

- **Documentação Render:** https://render.com/docs
- **Community:** https://community.render.com
- **Status:** https://status.render.com

## Comparação com Outras Plataformas

| Plataforma | Plano Gratuito | Sleep | CPU | RAM | Recomendado |
|------------|----------------|-------|-----|-----|-------------|
| Render | $0 | Sim (15min) | 0.1 | 512MB | ✅ Sim |
| Railway | $1/mês | Não | 1 | 0.5GB | ❌ Caro |
| Fly.io | $0 | Não | 1 | 256MB | ❌ Pouca RAM |
| Heroku | $0 | Sim | 0.5 | 512MB | ⚠️ Eco |

**Render é a melhor opção gratuita** com keep-alive configurado.
