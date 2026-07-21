const form = document.getElementById('login-form');
const status = document.getElementById('status');

redirectIfAlreadyLoggedIn();

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    status.classList.remove('error');
    status.textContent = 'Entrando...';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) {
            throw new Error('Usuário ou senha inválidos');
        }
        window.location.href = '/index.html';
    } catch (err) {
        status.classList.add('error');
        status.textContent = err.message;
    }
});

async function redirectIfAlreadyLoggedIn() {
    try {
        const response = await fetch('/api/auth/me');
        if (response.ok) {
            window.location.href = '/index.html';
        }
    } catch (err) {
        // segue na tela de login
    }
}
