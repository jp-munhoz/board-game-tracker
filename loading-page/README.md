# Página de loading (cold start do Render)

Site estático simples que fica sempre ativo (Render Static Site não hiberna) e mostra uma
tela de "acordando o servidor" enquanto o backend (que hiberna no plano free) sobe. Assim
que o backend responde, redireciona automaticamente para ele.

## Como funciona

`index.html` fica pingando um asset leve e público do backend (`/icons/favicon.png`, via
`<img>`, sem precisar de CORS) a cada poucos segundos. Quando o ping funciona, redireciona
o usuário para `BACKEND_URL + /login.html`. Se demorar demais, mostra um link manual.

## Configuração antes de publicar

Edite `index.html` e preencha a constante no topo do `<script>`:

```js
const BACKEND_URL = "https://SEU-APP.onrender.com";
```

Use a URL pública real do seu Web Service no Render (a que já está no ar hoje).

## Deploy no Render

1. No dashboard do Render: **New > Static Site**, apontando para este mesmo repositório.
2. **Root Directory**: `loading-page`
3. **Build Command**: (deixe em branco, não há build)
4. **Publish Directory**: `.` (a própria pasta `loading-page`)
5. Depois de criado, vá em **Settings > Custom Domains** desse Static Site e aponte seu
   domínio (o que hoje aponta para o Web Service) para ele.
6. O Web Service continua existindo normalmente, só deixa de ser o dono do domínio custom —
   ele passa a ser acessado apenas pela URL `.onrender.com`, que é a que a página de loading usa.
