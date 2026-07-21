# Changelog

Histórico das mudanças relevantes do projeto. Formato baseado em
[Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/). Entradas novas vão no topo.

## 2026-07-20 — Persistência, autenticação, PWA e deploy no Render

Migração do app de protótipo em memória (single-user, sem persistência) para um app
privado multi-usuário, instalável como PWA, hospedado no Render com Postgres do Supabase.

### Added

- **Persistência (Postgres via Supabase)**
  - Entidades JPA: `AppUser` (`br.com.bg.user`), `Game` (`br.com.bg.game` — cache local dos
    dados da Ludopedia, id = id da Ludopedia), `CollectionEntry` (`br.com.bg.collection` —
    liga usuário ↔ jogo, constraint única no par).
  - `Game.mechanics/categories/themes/designers/artists` mapeados via `@ElementCollection`
    (tabelas auxiliares simples, sem dependência extra de jsonb).
  - `ddl-auto: update` — schema criado automaticamente pelo Hibernate, sem Flyway por ora.
  - Datasource lido via env vars (`DATABASE_URL`/`DATABASE_USERNAME`/`DATABASE_PASSWORD`).

- **Cache local dos dados da Ludopedia**
  - `LudopediaGameService.getOrFetchGame(id)`: consulta o `GameRepository` primeiro; só
    bate na Ludopedia (API + scraper de descrição) se o jogo ainda não estiver cacheado, e
    grava o resultado. Sem política de expiração/refresh (dados de jogo de tabuleiro
    raramente mudam) — se precisar forçar atualização de um jogo específico no futuro, é
    manual (deletar a linha em `game` e o cache re-popula sozinho).
  - A busca (`/api/ludopedia/games/search`) continua sempre ao vivo, nunca cacheada
    (resultado efêmero, muitos jogos por busca).

- **Autenticação por sessão (Spring Security)**
  - `POST /api/auth/login` (JSON, não é o form-login padrão do Spring), `GET /api/auth/me`,
    `POST /api/auth/logout`. Sessão via cookie `HttpOnly`, `SameSite=Lax`.
  - Todo `/api/**` exige autenticação, exceto `/api/auth/login`. Páginas estáticas
    continuam públicas (proteção real está nas chamadas de API, não no HTML).
  - CSRF desabilitado deliberadamente: `SameSite=Lax` + ausência de CORS já bloqueia o
    vetor clássico nesse cenário (app privado, sem forms de terceiros). Ver comentário em
    `SecurityConfig`.
  - **Sem endpoint de cadastro** (decisão consciente — grupo fechado). Usuários são
    criados via `INSERT` manual no Supabase (SQL Editor). Gerar o hash da senha com:
    ```
    mvn exec:java -Dexec.args="senha-da-pessoa"
    ```
    (usa o `PasswordHashGenerator`, plugin `exec-maven-plugin` configurado só pra isso).

- **Coleção multi-usuário**
  - `CollectionController` agora opera sobre o usuário autenticado
    (`@AuthenticationPrincipal`).
  - `GET /api/collection/users/{username}` — ver a coleção de outro usuário do grupo
    (somente leitura).
  - `GET /api/users` — lista os usuários do grupo (usado pela tela "Amigos").
  - Payload de adicionar à coleção simplificado: `POST /api/collection` agora recebe só
    `{ "gameId": ... }` (antes recebia o objeto do jogo inteiro vindo do front) — o backend
    já busca/cacheia os dados da Ludopedia sozinho.

- **Front: telas de autenticação e navegação**
  - `login.html`/`login.js` — tela de login.
  - `nav.js` — script compartilhado incluído em toda página protegida: checa
    `/api/auth/me` ao carregar (401 → redireciona pro login; falha de rede → mostra nav
    "offline" em vez de redirecionar, já que o login também exigiria rede) e renderiza a
    barra de navegação (Buscar / Coleção / Amigos / usuário / Sair).
  - `amigos.html`/`amigos.js` — lista os usuários do grupo, cada um linkando pra
    `colecao.html?user=<username>`.
  - `colecao.html`/`colecao.js` — aceita `?user=X`: sem o parâmetro mostra a coleção do
    usuário logado (editável); com o parâmetro mostra a de outra pessoa (somente leitura,
    sem botão de remover).

- **PWA**
  - `manifest.json` (`display: standalone`, tema verde do app, ícones 192/512/512-maskable).
  - Ícones gerados programaticamente (não havia asset de design pronto) — um dado
    estilizado nas cores do tema, via `Graphics2D` em Java (emoji foi descartado por risco
    de renderizar mal dependendo da fonte do SO). Script usado ficou fora do repo
    (foi um utilitário de uso único em `/tmp`); se precisar regenerar os ícones no futuro,
    reescrever é rápido — ver estrutura em `static/icons/`.
  - `service-worker.js`: cache-first pro app shell (HTML/CSS/JS/ícones), network-first com
    fallback pro cache em `/api/**` (só GET). Mutações (POST/DELETE) sempre vão direto pra
    rede, sem fila de sincronização em background — suficiente pro objetivo de "funcionar
    minimamente offline", não é um app offline-first completo.
  - CSS revisado pra mobile-first: truncamento de nomes de jogos longos (bug real —
    sem isso, nomes longos da Ludopedia estouravam o layout horizontal em telas
    pequenas), badge de id escondido abaixo de 480px, suporte a
    `env(safe-area-inset-*)` pra não ficar conteúdo atrás do notch/home indicator no
    iOS em modo standalone.

- **Containerização**
  - `Dockerfile` multi-stage: build com `maven:3.9-eclipse-temurin-21`, runtime com
    `eclipse-temurin:21-jre-alpine` (imagem final leve, roda como usuário não-root).
    `-XX:MaxRAMPercentage=75.0` pra aproveitar melhor a RAM limitada do free tier do
    Render.
  - `.dockerignore`, `.env.example` (documenta `DATABASE_URL`, `DATABASE_USERNAME`,
    `DATABASE_PASSWORD`, `LUDOPEDIA_ACCESS_TOKEN`, `PORT`).
  - `server.port` já lia `${PORT:8080}` — exigência do Render de portar via env var.

### Changed

- `LudopediaGameService`/`LudopediaGameController` mantidos como fonte de verdade da
  Ludopedia, mas agora com camada de cache local por baixo (ver acima).
- `.gitignore` passou a ignorar `.env`/`.env.*` (mantendo `.env.example` rastreado).

### Fixed

- Resposta 401 inconsistente: o filtro de segurança (usuário não autenticado) devolvia um
  JSON diferente do `GlobalExceptionHandler` (senha errada). Padronizado pro mesmo formato
  `ProblemDetail`-like nos dois casos.

### Notas operacionais (gotchas descobertos durante essa migração)

- **Supabase: use a connection pooler, não a conexão direta.** O host direto
  (`db.<ref>.supabase.co`) costuma exigir IPv6, que hosts de deploy nem sempre suportam.
  Usamos a **Session pooler** (porta 5432, host `aws-<n>-<região>.pooler.supabase.com`,
  usuário no formato `postgres.<project-ref>`) — funciona com prepared statements do
  Hibernate sem flag extra. Se algum dia precisar trocar pra **Transaction pooler** (porta
  6543), tem que adicionar `?prepareThreshold=0` na `DATABASE_URL` (pgbouncer em modo
  transaction não suporta prepared statements do jeito que o Hibernate usa por padrão).
- A UI do Supabase pra achar essas credenciais mudou de nome algumas vezes — procurar o
  botão **"Connect"** no topo do dashboard do projeto, aba **"Session pooler"**, copiar a
  connection string URI de lá.
- Ambiente de dev deste projeto é Windows sem Maven no PATH (nem `mvnw` no repo) — o build
  foi validado usando o Maven embutido no plugin do IntelliJ
  (`plugins/maven-plugin/lib/maven3/bin/mvn.cmd`). Vale considerar adicionar um Maven
  Wrapper (`mvnw`/`mvnw.cmd`) pra não depender disso.
- Não há Docker nem `chromium-cli`/Playwright disponíveis neste ambiente de
  desenvolvimento — o `Dockerfile` foi validado só por leitura + confirmação de que as
  imagens base (`maven:3.9-eclipse-temurin-21`, `eclipse-temurin:21-jre-alpine`) existem
  no Docker Hub, e o front foi validado via chamadas HTTP diretas (curl), não visualmente
  num navegador real. Recomendado testar `docker build` e a UI num navegador de verdade
  antes de considerar o deploy 100% validado.

### Pendências (fora do escopo desta sessão)

- Inicializar o repositório git e publicar num remoto (necessário pro Render buildar).
- Criar o Web Service no Render apontando pro `Dockerfile`, configurando as env vars.
- Criar os demais usuários do grupo no Supabase (mesmo processo usado pro usuário de teste
  `pedro`).
- Testar a instalação do PWA ("Adicionar à tela inicial") e o comportamento offline num
  celular de verdade.
