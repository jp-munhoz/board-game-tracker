const status = document.getElementById('status');
const results = document.getElementById('results');
const titleEl = document.getElementById('collection-title');
const subtitleEl = document.getElementById('collection-subtitle');

const params = new URLSearchParams(window.location.search);
const viewedUsername = params.get('user');
const isOwnCollection = !viewedUsername;

if (!isOwnCollection) {
    titleEl.textContent = `Coleção de ${viewedUsername}`;
    subtitleEl.textContent = 'Somente leitura';
    document.title = `Coleção de ${viewedUsername} - Board Game Tracker`;
}

loadCollection();

async function loadCollection() {
    status.classList.remove('error');
    status.textContent = 'Carregando...';
    try {
        const url = isOwnCollection
            ? '/api/collection'
            : `/api/collection/users/${encodeURIComponent(viewedUsername)}`;
        const response = await fetch(url);
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        const games = await response.json();
        if (games.length === 0) {
            status.textContent = isOwnCollection
                ? 'Sua coleção está vazia. Busque jogos e clique no coração para adicionar.'
                : 'Essa coleção ainda está vazia.';
            results.innerHTML = '';
            return;
        }
        status.textContent = `${games.length} jogo(s) na coleção:`;
        results.innerHTML = games.map((game, index) => renderResult(game, index)).join('');
        if (isOwnCollection) {
            results.querySelectorAll('.heart-btn').forEach(btn => btn.addEventListener('click', onRemoveClick));
        }
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar coleção: ${err.message}`;
    }
}

async function onRemoveClick(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    btn.disabled = true;
    try {
        const response = await fetch(`/api/collection/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        const item = btn.closest('.result-item');
        item.classList.add('removing');
        item.addEventListener('transitionend', () => {
            item.remove();
            if (!results.children.length) {
                status.textContent = 'Sua coleção está vazia. Busque jogos e clique no coração para adicionar.';
            }
        }, { once: true });
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao remover jogo: ${err.message}`;
        btn.disabled = false;
    }
}

function renderResult(game, index) {
    const delay = Math.min(index * 0.05, 0.4);
    const heart = isOwnCollection
        ? `<button type="button" class="heart-btn active" data-id="${game.id}" aria-label="Remover da coleção">❤️</button>`
        : '';
    return `
        <li class="result-item collection-item" style="animation-delay: ${delay}s">
            <a class="result-link" href="/jogo.html?id=${encodeURIComponent(game.id)}">
                ${game.imageUrl ? `<img class="result-thumb" src="${escapeHtml(game.imageUrl)}" alt="">` : ''}
                <span class="result-name">${escapeHtml(game.name ?? '')}</span>
                ${game.yearPublished ? `<span class="result-year">(${game.yearPublished})</span>` : ''}
                <span class="result-id">#${game.id}</span>
            </a>
            ${heart}
        </li>
    `;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
