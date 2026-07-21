const status = document.getElementById('status');
const results = document.getElementById('results');

loadWishlist();

async function loadWishlist() {
    status.classList.remove('error');
    status.textContent = 'Carregando...';
    try {
        const response = await fetch('/api/wishlist');
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        const games = await response.json();
        if (games.length === 0) {
            status.textContent = 'Sua lista de desejos está vazia. Busque jogos e clique na estrela para adicionar.';
            results.innerHTML = '';
            return;
        }
        status.textContent = `${games.length} jogo(s) na lista de desejos:`;
        results.innerHTML = games.map((game, index) => renderResult(game, index)).join('');
        results.querySelectorAll('.wishlist-remove-btn').forEach(btn => btn.addEventListener('click', onRemoveClick));
        results.querySelectorAll('.wishlist-note-save-btn').forEach(btn => btn.addEventListener('click', onSaveNoteClick));
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar lista de desejos: ${err.message}`;
    }
}

async function onRemoveClick(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    btn.disabled = true;
    try {
        const response = await fetch(`/api/wishlist/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        const item = btn.closest('.wishlist-card');
        item.classList.add('removing');
        item.addEventListener('transitionend', () => {
            item.remove();
            if (!results.children.length) {
                status.textContent = 'Sua lista de desejos está vazia. Busque jogos e clique na estrela para adicionar.';
            }
        }, { once: true });
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao remover jogo: ${err.message}`;
        btn.disabled = false;
    }
}

async function onSaveNoteClick(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    const input = results.querySelector(`.wishlist-note-input[data-id="${id}"]`);
    btn.disabled = true;
    btn.classList.remove('saved');
    try {
        const response = await fetch(`/api/wishlist/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ note: input.value })
        });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        btn.textContent = 'Salvo ✓';
        btn.classList.add('saved');
        setTimeout(() => {
            btn.textContent = 'Salvar';
            btn.classList.remove('saved');
        }, 1500);
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao salvar anotação: ${err.message}`;
    } finally {
        btn.disabled = false;
    }
}

function renderResult(game, index) {
    const delay = Math.min(index * 0.05, 0.4);
    return `
        <li class="wishlist-card fade-in" style="animation-delay: ${delay}s">
            <div class="wishlist-card-main">
                <a class="result-link" href="/jogo.html?id=${encodeURIComponent(game.id)}">
                    ${game.imageUrl ? `<img class="result-thumb" src="${escapeHtml(game.imageUrl)}" alt="">` : ''}
                    <span class="result-name">${escapeHtml(game.name ?? '')}</span>
                    ${game.yearPublished ? `<span class="result-year">(${game.yearPublished})</span>` : ''}
                    <span class="result-id">#${game.id}</span>
                </a>
                <button type="button" class="heart-btn wishlist-btn active wishlist-remove-btn" data-id="${game.id}" aria-label="Remover da lista de desejos">⭐</button>
            </div>
            <div class="wishlist-note-row">
                <textarea class="wishlist-note-input" data-id="${game.id}" rows="2" placeholder="Descrição ou link de onde comprar">${escapeHtml(game.note ?? '')}</textarea>
                <button type="button" class="wishlist-note-save-btn" data-id="${game.id}">Salvar</button>
            </div>
        </li>
    `;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
}
