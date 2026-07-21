const content = document.getElementById('content');
const status = document.getElementById('status');
const params = new URLSearchParams(window.location.search);
const id = params.get('id');

let collectionIds = new Set();
let wishlistIds = new Set();

if (!id) {
    content.innerHTML = '<p class="status error">Nenhum id informado. Volte e busque um jogo.</p>';
} else {
    init();
}

async function init() {
    await Promise.all([loadCollectionIds(), loadWishlistIds()]);
    await loadGame(id);
}

async function loadCollectionIds() {
    try {
        const response = await fetch('/api/collection');
        if (!response.ok) {
            return;
        }
        const games = await response.json();
        collectionIds = new Set(games.map(game => game.id));
    } catch (err) {
        // colecao indisponivel no momento, coracao comeca vazio
    }
}

async function loadWishlistIds() {
    try {
        const response = await fetch('/api/wishlist');
        if (!response.ok) {
            return;
        }
        const games = await response.json();
        wishlistIds = new Set(games.map(game => game.id));
    } catch (err) {
        // lista de desejos indisponivel no momento, estrela comeca vazia
    }
}

async function loadGame(gameId) {
    content.innerHTML = '<p class="status">Carregando...</p>';
    try {
        const response = await fetch(`/api/ludopedia/games/${encodeURIComponent(gameId)}`);
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || `Erro ${response.status}`);
        }
        const game = await response.json();
        render(game);
    } catch (err) {
        content.innerHTML = `<p class="status error">Erro ao carregar jogo: ${escapeHtml(err.message)}</p>`;
    }
}

function render(game) {
    const inCollection = collectionIds.has(game.id);
    const inWishlist = wishlistIds.has(game.id);
    const heartBtn = `
        <button type="button" class="heart-btn ${inCollection ? 'active' : ''}" data-id="${game.id}"
                aria-label="${inCollection ? 'Remover da coleção' : 'Adicionar à coleção'}">${inCollection ? '❤️' : '🤍'}</button>
    `;
    const wishlistBtn = `
        <button type="button" class="heart-btn wishlist-btn ${inWishlist ? 'active' : ''}" data-id="${game.id}"
                aria-label="${inWishlist ? 'Remover da lista de desejos' : 'Adicionar à lista de desejos'}">${inWishlist ? '⭐' : '☆'}</button>
    `;
    const actions = heartBtn + wishlistBtn;

    content.innerHTML = `
    <div class="fade-in">
        <div class="game-cover-row">
            <div class="game-image-wrap">
                ${game.imageUrl ? `<img class="game-image" src="${escapeHtml(game.imageUrl)}" alt="Capa de ${escapeHtml(game.name ?? '')}">` : ''}
            </div>
            <div class="game-actions">${actions}</div>
        </div>
        <h1>🎯 ${escapeHtml(game.name ?? '')}</h1>
        <p class="subtitle">
            ${game.yearPublished ? `Publicado em ${game.yearPublished}` : ''}
            ${game.nationalYear ? ` &middot; Lançado no Brasil em ${game.nationalYear}` : ''}
        </p>

        <a id="sessions-link" class="sessions-link ${inCollection ? '' : 'hidden'}"
           href="/sessoes.html?game=${encodeURIComponent(game.id)}&name=${encodeURIComponent(game.name ?? '')}">🎲 Ver sessões deste jogo</a>

        <dl class="details-grid">
            <div class="stat">
                <span class="stat-icon">👥</span>
                <dt>Jogadores</dt>
                <dd>${formatRange(game.minPlayers, game.maxPlayers)}</dd>
            </div>
            <div class="stat">
                <span class="stat-icon">⏱️</span>
                <dt>Tempo de jogo</dt>
                <dd>${game.playingTimeMinutes ? `${game.playingTimeMinutes} min` : '-'}</dd>
            </div>
            <div class="stat">
                <span class="stat-icon">🎂</span>
                <dt>Idade minima</dt>
                <dd>${game.minAge ? `${game.minAge}+` : '-'}</dd>
            </div>
        </dl>

        ${renderDescription(game.description)}

        ${renderTags('🧠 Mecanicas', game.mechanics, 'tags-mechanics')}
        ${renderTags('🏷️ Categorias', game.categories, 'tags-categories')}
        ${renderTags('🎭 Temas', game.themes, 'tags-themes')}
        ${renderTags('✏️ Designers', game.designers, 'tags-designers')}
        ${renderTags('🎨 Artistas', game.artists, 'tags-artists')}

        ${game.link ? `<a class="ludopedia-link" href="${game.link}" target="_blank" rel="noopener">Ver na Ludopedia &#8599;</a>` : ''}
    </div>
    `;

    content.querySelector('.heart-btn:not(.wishlist-btn)').addEventListener('click', onHeartClick);
    content.querySelector('.wishlist-btn').addEventListener('click', onWishlistClick);
}

async function onWishlistClick(event) {
    const btn = event.currentTarget;
    const gameId = Number(btn.dataset.id);
    btn.disabled = true;
    status.classList.remove('error');
    status.textContent = '';
    try {
        if (wishlistIds.has(gameId)) {
            const response = await fetch(`/api/wishlist/${gameId}`, { method: 'DELETE' });
            if (!response.ok) {
                throw new Error(`Erro ${response.status}`);
            }
            wishlistIds.delete(gameId);
            btn.classList.remove('active');
            btn.textContent = '☆';
            btn.setAttribute('aria-label', 'Adicionar à lista de desejos');
        } else {
            const response = await fetch('/api/wishlist', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ gameId })
            });
            if (!response.ok) {
                throw new Error(`Erro ${response.status}`);
            }
            wishlistIds.add(gameId);
            btn.classList.add('active');
            btn.textContent = '⭐';
            btn.setAttribute('aria-label', 'Remover da lista de desejos');
            btn.classList.add('pop');
            btn.addEventListener('animationend', () => btn.classList.remove('pop'), { once: true });
        }
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao atualizar lista de desejos: ${err.message}`;
    } finally {
        btn.disabled = false;
    }
}

async function onHeartClick(event) {
    const btn = event.currentTarget;
    const gameId = Number(btn.dataset.id);
    btn.disabled = true;
    status.classList.remove('error');
    status.textContent = '';
    try {
        if (collectionIds.has(gameId)) {
            const response = await fetch(`/api/collection/${gameId}`, { method: 'DELETE' });
            if (!response.ok) {
                throw new Error(`Erro ${response.status}`);
            }
            collectionIds.delete(gameId);
            btn.classList.remove('active');
            btn.textContent = '🤍';
            btn.setAttribute('aria-label', 'Adicionar à coleção');
            document.getElementById('sessions-link')?.classList.add('hidden');
        } else {
            const response = await fetch('/api/collection', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ gameId })
            });
            if (!response.ok) {
                throw new Error(`Erro ${response.status}`);
            }
            collectionIds.add(gameId);
            btn.classList.add('active');
            btn.textContent = '❤️';
            btn.setAttribute('aria-label', 'Remover da coleção');
            btn.classList.add('pop');
            btn.addEventListener('animationend', () => btn.classList.remove('pop'), { once: true });
            document.getElementById('sessions-link')?.classList.remove('hidden');
        }
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao atualizar coleção: ${err.message}`;
    } finally {
        btn.disabled = false;
    }
}

function formatRange(min, max) {
    if (!min && !max) {
        return '-';
    }
    if (min && max && min !== max) {
        return `${min} - ${max}`;
    }
    return `${min || max}`;
}

function renderDescription(description) {
    if (!description) {
        return '';
    }
    const paragraphs = description
        .split(/\n{2,}/)
        .map(paragraph => `<p>${escapeHtml(paragraph).replace(/\n/g, '<br>')}</p>`)
        .join('');
    return `
        <section class="description-section">
            <h2>📖 Sobre o jogo</h2>
            ${paragraphs}
        </section>
    `;
}

function renderTags(label, items, cssClass) {
    if (!items || items.length === 0) {
        return '';
    }
    return `
        <section class="tag-section">
            <h2>${label}</h2>
            <div class="tags ${cssClass}">
                ${items.map(item => `<span class="tag">${escapeHtml(item)}</span>`).join('')}
            </div>
        </section>
    `;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
