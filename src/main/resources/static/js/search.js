const form = document.getElementById('search-form');
const input = document.getElementById('search-input');
const status = document.getElementById('status');
const results = document.getElementById('results');

let collectionIds = new Set();

loadCollectionIds();

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const name = input.value.trim();
    if (!name) {
        return;
    }

    results.innerHTML = '';
    status.classList.remove('error');
    status.textContent = 'Buscando...';

    try {
        const response = await fetch(`/api/ludopedia/games/search?name=${encodeURIComponent(name)}`);
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || `Erro ${response.status}`);
        }

        const games = await response.json();

        if (games.length === 0) {
            status.textContent = 'Nenhum jogo encontrado.';
            return;
        }

        status.textContent = `${games.length} jogo(s) encontrado(s):`;
        results.innerHTML = games.map((game, index) => renderResult(game, index)).join('');
        results.querySelectorAll('.heart-btn').forEach(btn => btn.addEventListener('click', onHeartClick));
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao buscar: ${err.message}`;
    }
});

async function loadCollectionIds() {
    try {
        const response = await fetch('/api/collection');
        if (!response.ok) {
            return;
        }
        const games = await response.json();
        collectionIds = new Set(games.map(game => game.id));
    } catch (err) {
        // colecao indisponivel no momento, coracoes comecam vazios
    }
}

async function onHeartClick(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    btn.disabled = true;
    try {
        if (collectionIds.has(id)) {
            const response = await fetch(`/api/collection/${id}`, { method: 'DELETE' });
            if (!response.ok) {
                throw new Error(`Erro ${response.status}`);
            }
            collectionIds.delete(id);
            btn.classList.remove('active');
            btn.textContent = '🤍';
        } else {
            const response = await fetch('/api/collection', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ gameId: id })
            });
            if (!response.ok) {
                throw new Error(`Erro ${response.status}`);
            }
            collectionIds.add(id);
            btn.classList.add('active');
            btn.textContent = '❤️';
            btn.classList.add('pop');
            btn.addEventListener('animationend', () => btn.classList.remove('pop'), { once: true });
        }
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao atualizar coleção: ${err.message}`;
    } finally {
        btn.disabled = false;
    }
}

function renderResult(game, index) {
    const inCollection = collectionIds.has(game.id);
    const delay = Math.min(index * 0.05, 0.4);
    return `
        <li class="result-item" style="animation-delay: ${delay}s">
            <a class="result-link" href="/jogo.html?id=${encodeURIComponent(game.id)}">
                ${game.imageUrl ? `<img class="result-thumb" src="${escapeHtml(game.imageUrl)}" alt="">` : ''}
                <span class="result-name">${escapeHtml(game.name ?? '')}</span>
                ${game.yearPublished ? `<span class="result-year">(${game.yearPublished})</span>` : ''}
                <span class="result-id">#${game.id}</span>
            </a>
            <button type="button" class="heart-btn ${inCollection ? 'active' : ''}" data-id="${game.id}" aria-label="Adicionar à coleção">${inCollection ? '❤️' : '🤍'}</button>
        </li>
    `;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
