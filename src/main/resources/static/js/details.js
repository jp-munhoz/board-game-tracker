const content = document.getElementById('content');
const params = new URLSearchParams(window.location.search);
const id = params.get('id');

if (!id) {
    content.innerHTML = '<p class="status error">Nenhum id informado. Volte e busque um jogo.</p>';
} else {
    loadGame(id);
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
    content.innerHTML = `
    <div class="fade-in">
        ${game.imageUrl ? `<img class="game-image" src="${escapeHtml(game.imageUrl)}" alt="Capa de ${escapeHtml(game.name ?? '')}">` : ''}
        <h1>🎯 ${escapeHtml(game.name ?? '')}</h1>
        <p class="subtitle">
            ${game.yearPublished ? `Publicado em ${game.yearPublished}` : ''}
            ${game.nationalYear ? ` &middot; Lançado no Brasil em ${game.nationalYear}` : ''}
        </p>

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
