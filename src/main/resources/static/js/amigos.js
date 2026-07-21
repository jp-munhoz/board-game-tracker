const status = document.getElementById('status');
const results = document.getElementById('results');

loadFriends();

async function loadFriends() {
    status.classList.remove('error');
    status.textContent = 'Carregando...';
    try {
        const [usersResponse, meResponse] = await Promise.all([
            fetch('/api/users'),
            fetch('/api/auth/me')
        ]);
        if (!usersResponse.ok) {
            throw new Error(`Erro ${usersResponse.status}`);
        }
        const allUsers = await usersResponse.json();
        const me = meResponse.ok ? await meResponse.json() : null;
        const users = me ? allUsers.filter(user => user.username !== me.username) : allUsers;
        if (users.length === 0) {
            status.textContent = 'Nenhum amigo cadastrado ainda.';
            results.innerHTML = '';
            return;
        }
        status.textContent = `${users.length} pessoa(s) no grupo:`;
        results.innerHTML = users.map((user, index) => renderFriend(user, index)).join('');
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar amigos: ${err.message}`;
    }
}

function renderFriend(user, index) {
    const delay = Math.min(index * 0.05, 0.4);
    return `
        <li class="result-item" style="animation-delay: ${delay}s">
            <a class="result-link" href="/colecao.html?user=${encodeURIComponent(user.username)}&name=${encodeURIComponent(user.displayName)}">
                <span class="result-name">${escapeHtml(user.displayName)}</span>
            </a>
        </li>
    `;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
}
