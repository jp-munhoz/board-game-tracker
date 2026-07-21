const form = document.getElementById('register-form');
const status = document.getElementById('status');
const passwordInput = document.getElementById('password');

const passwordRules = {
    length: password => password.length >= 8,
    uppercase: password => /[A-Z]/.test(password),
    special: password => /[^A-Za-z0-9]/.test(password)
};

redirectIfAlreadyLoggedIn();

passwordInput.addEventListener('input', () => updatePasswordRequirements(passwordInput.value));

function updatePasswordRequirements(password) {
    document.querySelectorAll('#password-requirements li').forEach(li => {
        const rule = passwordRules[li.dataset.rule];
        li.classList.toggle('met', rule(password));
    });
}

function isPasswordValid(password) {
    return Object.values(passwordRules).every(rule => rule(password));
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const displayName = document.getElementById('display-name').value.trim();
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('password-confirm').value;

    status.classList.remove('error');

    if (!isPasswordValid(password)) {
        status.classList.add('error');
        status.textContent = 'A senha não atende aos requisitos acima';
        return;
    }

    if (password !== passwordConfirm) {
        status.classList.add('error');
        status.textContent = 'As senhas não coincidem';
        return;
    }

    status.textContent = 'Criando conta...';

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, displayName, password })
        });
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || 'Não foi possível criar a conta');
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
        // segue na tela de cadastro
    }
}
