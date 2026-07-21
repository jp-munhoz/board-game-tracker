const status = document.getElementById('status');
const results = document.getElementById('results');
const titleEl = document.getElementById('category-title');
const paginationControls = document.getElementById('pagination-controls');
const prevBtn = document.getElementById('prev-page');
const nextBtn = document.getElementById('next-page');
const pageInfo = document.getElementById('page-info');

const PAGE_SIZE = 10;

const params = new URLSearchParams(window.location.search);
const idCategoria = params.get('id');
const nomeCategoria = params.get('nome') || 'Categoria';

let allGames = [];
let currentPage = 1;

titleEl.textContent = nomeCategoria;
document.title = `${nomeCategoria} - Board Game Tracker`;

if (!idCategoria) {
    status.classList.add('error');
    status.textContent = 'Nenhuma categoria informada.';
} else {
    loadRanking(idCategoria);
}

prevBtn.addEventListener('click', () => goToPage(currentPage - 1));
nextBtn.addEventListener('click', () => goToPage(currentPage + 1));

async function loadRanking(id) {
    status.classList.remove('error');
    status.textContent = 'Carregando...';
    paginationControls.classList.add('hidden');
    try {
        const response = await fetch(`/api/ludopedia/games/ranking?idCategoria=${encodeURIComponent(id)}`);
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || `Erro ${response.status}`);
        }
        allGames = await response.json();
        if (allGames.length === 0) {
            status.textContent = 'Nenhum jogo encontrado para essa categoria.';
            results.innerHTML = '';
            return;
        }
        goToPage(1);
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar ranking: ${err.message}`;
    }
}

function goToPage(page) {
    const totalPages = Math.ceil(allGames.length / PAGE_SIZE);
    if (page < 1 || page > totalPages) {
        return;
    }
    currentPage = page;

    const start = (page - 1) * PAGE_SIZE;
    const pageGames = allGames.slice(start, start + PAGE_SIZE);

    status.textContent = `${allGames.length} jogo(s) encontrado(s):`;
    results.innerHTML = pageGames.map((game, index) => renderResult(game, index)).join('');

    pageInfo.textContent = `Página ${page} de ${totalPages}`;
    prevBtn.disabled = page === 1;
    nextBtn.disabled = page === totalPages;
    paginationControls.classList.toggle('hidden', totalPages <= 1);
}

function renderResult(game, index) {
    const delay = Math.min(index * 0.05, 0.4);
    return `
        <li class="result-item" style="animation-delay: ${delay}s">
            <a class="result-link" href="/jogo.html?id=${encodeURIComponent(game.id)}">
                ${game.rank ? `<span class="rank-badge">${game.rank}º</span>` : ''}
                ${game.imageUrl ? `<img class="result-thumb" src="${escapeHtml(game.imageUrl)}" alt="">` : ''}
                <span class="result-name">${escapeHtml(game.name ?? '')}</span>
                ${game.yearPublished ? `<span class="result-year">(${game.yearPublished})</span>` : ''}
                <span class="result-id">#${game.id}</span>
            </a>
        </li>
    `;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
