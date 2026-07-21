(async function guardAndRenderNav() {
    try {
        const response = await fetch('/api/auth/me');
        if (response.ok) {
            const user = await response.json();
            renderNav(user);
            return;
        }
        if (response.status === 401) {
            redirectToLogin();
        }
    } catch (err) {
        // provavelmente offline: login tambem exige rede, entao nao redireciona.
        // deixa a pagina tentar mostrar o que estiver em cache.
        renderOfflineNav();
    }
})();

function redirectToLogin() {
    window.location.href = '/login.html';
}

function renderNav(user) {
    const container = document.getElementById('app-nav');
    if (!container) {
        return;
    }
    const path = window.location.pathname;
    container.innerHTML = `
        <nav class="app-nav">
            <a href="/index.html" class="app-nav-link ${path.endsWith('index.html') || path === '/' ? 'active' : ''}">🔍 Buscar</a>
            <a href="/colecao.html" class="app-nav-link ${path.endsWith('colecao.html') && !window.location.search ? 'active' : ''}">❤️ Coleção</a>
            <a href="/amigos.html" class="app-nav-link ${path.endsWith('amigos.html') ? 'active' : ''}">👥 Amigos</a>
            <span class="app-nav-user">${escapeHtml(user.displayName)}</span>
            <button type="button" id="logout-btn" class="app-nav-logout">Sair</button>
        </nav>
    `;
    document.getElementById('logout-btn').addEventListener('click', logout);
}

function renderOfflineNav() {
    const container = document.getElementById('app-nav');
    if (!container) {
        return;
    }
    container.innerHTML = `
        <nav class="app-nav">
            <a href="/index.html" class="app-nav-link">🔍 Buscar</a>
            <a href="/colecao.html" class="app-nav-link">❤️ Coleção</a>
            <a href="/amigos.html" class="app-nav-link">👥 Amigos</a>
            <span class="app-nav-user">📴 Offline</span>
        </nav>
    `;
}

async function logout() {
    await fetch('/api/auth/logout', { method: 'POST' });
    redirectToLogin();
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
}
