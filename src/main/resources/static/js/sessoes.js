const backLink = document.getElementById('sessions-back-link');
const title = document.getElementById('sessions-title');
const subtitle = document.getElementById('sessions-subtitle');
const status = document.getElementById('status');
const body = document.getElementById('sessions-body');

const params = new URLSearchParams(window.location.search);
const gameId = params.get('game');
const gameName = params.get('name');
const sharedSessionId = params.get('session');

let myUsername = null;
let allUsers = [];

init();

async function init() {
    myUsername = await resolveMyUsername();
    if (sharedSessionId) {
        initSharedSession();
    } else if (gameId) {
        initGameSessions();
    } else {
        initHome();
    }
}

async function resolveMyUsername() {
    try {
        const response = await fetch('/api/auth/me');
        if (!response.ok) {
            return null;
        }
        const me = await response.json();
        return me.username;
    } catch (err) {
        return null;
    }
}

/* ---------- Modo inicial: meus jogos + sessões que participo ---------- */

async function initHome() {
    body.innerHTML = `
        <section>
            <h2 class="sessions-section-heading">🗂️ Meus jogos</h2>
            <ul class="results" id="my-games-list"></ul>
        </section>
        <section>
            <h2 class="sessions-section-heading">🤝 Sessões que eu participo</h2>
            <ul class="results" id="shared-sessions-list"></ul>
        </section>
    `;
    status.classList.remove('error');
    status.textContent = 'Carregando...';
    try {
        const [gamesResponse, sharedResponse] = await Promise.all([
            fetch('/api/collection'),
            fetch('/api/sessions/shared')
        ]);
        if (!gamesResponse.ok) {
            throw new Error(`Erro ${gamesResponse.status}`);
        }
        const games = await gamesResponse.json();
        const shared = sharedResponse.ok ? await sharedResponse.json() : [];
        status.textContent = '';

        const myGamesList = document.getElementById('my-games-list');
        myGamesList.innerHTML = games.length === 0
            ? '<li class="status">Sua coleção está vazia. Adicione jogos para começar a registrar sessões.</li>'
            : games.map((game, index) => renderGameLink(game, index)).join('');

        const sharedList = document.getElementById('shared-sessions-list');
        sharedList.innerHTML = shared.length === 0
            ? '<li class="status">Nenhum amigo te chamou pra uma sessão ainda.</li>'
            : shared.map((session, index) => renderSharedSessionLink(session, index)).join('');
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar: ${err.message}`;
    }
}

function renderGameLink(game, index) {
    const delay = Math.min(index * 0.05, 0.4);
    return `
        <li class="result-item" style="animation-delay: ${delay}s">
            <a class="result-link" href="/sessoes.html?game=${encodeURIComponent(game.id)}&name=${encodeURIComponent(game.name ?? '')}">
                ${game.imageUrl ? `<img class="result-thumb" src="${escapeHtml(game.imageUrl)}" alt="">` : ''}
                <span class="result-name">${escapeHtml(game.name ?? '')}</span>
                ${game.yearPublished ? `<span class="result-year">(${game.yearPublished})</span>` : ''}
                <span class="result-id">#${game.id}</span>
            </a>
        </li>
    `;
}

function renderSharedSessionLink(session, index) {
    const delay = Math.min(index * 0.05, 0.4);
    const isOngoing = session.status === 'ONGOING';
    return `
        <li class="result-item" style="animation-delay: ${delay}s">
            <a class="result-link" href="/sessoes.html?session=${session.sessionId}">
                ${session.gameImageUrl ? `<img class="result-thumb" src="${escapeHtml(session.gameImageUrl)}" alt="">` : ''}
                <span class="result-name">${escapeHtml(session.gameName ?? '')} <span class="session-owner-tag">de ${escapeHtml(session.ownerDisplayName)}</span></span>
                <span class="session-badge ${isOngoing ? 'ongoing' : 'completed'}">${isOngoing ? '🟢' : '✅'}</span>
            </a>
        </li>
    `;
}

/* ---------- Modo dono: sessões de um jogo da minha coleção ---------- */

async function initGameSessions() {
    backLink.classList.remove('hidden');
    let name = gameName;
    if (!name) {
        name = await resolveGameName(gameId);
    }
    title.textContent = `🎲 ${name}`;
    subtitle.textContent = 'Registre sessões e convide amigos para anotar também';
    document.title = `Sessões de ${name} - Board Game Tracker`;

    allUsers = await loadAllUsers();

    body.innerHTML = `
        <div id="new-session-form-wrap"></div>
        <ul class="sessions-list" id="sessions-list"></ul>
    `;
    renderNewSessionForm();
    document.getElementById('new-session-form').addEventListener('submit', onCreateSession);

    await loadSessions();
}

async function resolveGameName(id) {
    try {
        const response = await fetch(`/api/ludopedia/games/${encodeURIComponent(id)}`);
        if (!response.ok) {
            return `Jogo #${id}`;
        }
        const game = await response.json();
        return game.name ?? `Jogo #${id}`;
    } catch (err) {
        return `Jogo #${id}`;
    }
}

async function loadAllUsers() {
    try {
        const response = await fetch('/api/users');
        if (!response.ok) {
            return [];
        }
        const users = await response.json();
        return users.filter(user => user.username !== myUsername);
    } catch (err) {
        return [];
    }
}

function renderNewSessionForm() {
    const today = new Date().toISOString().slice(0, 10);
    document.getElementById('new-session-form-wrap').innerHTML = `
        <form id="new-session-form" class="session-form">
            <input type="date" id="new-session-date" required value="${today}">
            <textarea id="new-session-notes" rows="2" placeholder="Suas anotações iniciais (opcional)"></textarea>
            <button type="submit">+ Iniciar nova sessão</button>
        </form>
    `;
}

async function onCreateSession(event) {
    event.preventDefault();
    const dateInput = document.getElementById('new-session-date');
    const notesInput = document.getElementById('new-session-notes');
    const submitBtn = event.currentTarget.querySelector('button[type="submit"]');
    if (!dateInput.value) {
        return;
    }

    submitBtn.disabled = true;
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/collection/${gameId}/sessions`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playedAt: dateInput.value, notes: notesInput.value.trim() || null })
        });
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || `Erro ${response.status}`);
        }
        notesInput.value = '';
        await loadSessions();
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao iniciar sessão: ${err.message}`;
    } finally {
        submitBtn.disabled = false;
    }
}

async function loadSessions() {
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/collection/${gameId}/sessions`);
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        const sessions = await response.json();
        const list = document.getElementById('sessions-list');
        if (sessions.length === 0) {
            status.textContent = 'Nenhuma sessão registrada ainda. Que tal começar uma agora?';
            list.innerHTML = '';
            return;
        }
        status.textContent = '';
        list.innerHTML = sessions.map((session, index) => renderOwnerSession(session, index)).join('');
        attachSessionHandlers();
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar sessões: ${err.message}`;
    }
}

function renderOwnerSession(session, index) {
    const delay = Math.min(index * 0.05, 0.4);
    const isOngoing = session.status === 'ONGOING';
    const participantUsernames = new Set(session.notes.map(n => n.authorUsername));
    const candidates = allUsers.filter(user => !participantUsernames.has(user.username));

    const dateBlock = isOngoing
        ? `<input type="date" class="session-edit-date" value="${session.playedAt}">`
        : `<span class="session-date">${formatDate(session.playedAt)}</span>`;

    const participantsBlock = `
        <div class="session-participants">
            ${session.notes.map(note => renderParticipantChip(note, isOngoing)).join('')}
            ${isOngoing && candidates.length > 0 ? `
                <div class="session-add-participant">
                    <select class="session-participant-select">
                        ${candidates.map(user => `<option value="${escapeHtml(user.username)}">${escapeHtml(user.displayName)}</option>`).join('')}
                    </select>
                    <button type="button" class="session-add-participant-btn" data-session-id="${session.id}">+ Adicionar</button>
                </div>
            ` : ''}
        </div>
    `;

    const notesBlock = `
        <div class="session-notes-mural">
            ${session.notes.map(note => renderNoteCard(note, session.id)).join('')}
        </div>
    `;

    const actions = isOngoing ? `
        <div class="session-actions">
            <button type="button" class="session-save-btn" data-id="${session.id}">💾 Salvar data</button>
            <button type="button" class="session-complete-btn" data-id="${session.id}">✅ Concluir sessão</button>
            <button type="button" class="session-remove-btn" data-id="${session.id}" aria-label="Remover sessão">🗑️</button>
        </div>
    ` : `
        <div class="session-actions">
            <button type="button" class="session-remove-btn" data-id="${session.id}" aria-label="Remover sessão">🗑️</button>
        </div>
    `;

    return `
        <li class="session-item ${isOngoing ? 'session-ongoing' : 'session-completed'}" style="animation-delay: ${delay}s" data-session-id="${session.id}">
            <span class="session-badge ${isOngoing ? 'ongoing' : 'completed'}">${isOngoing ? '🟢 Em andamento' : '✅ Concluída'}</span>
            ${dateBlock}
            ${participantsBlock}
            ${notesBlock}
            ${actions}
        </li>
    `;
}

function renderParticipantChip(note, isOngoing) {
    const isMe = note.authorUsername === myUsername;
    const removeBtn = (!isMe && isOngoing)
        ? `<button type="button" class="session-remove-participant-btn" data-username="${escapeHtml(note.authorUsername)}" aria-label="Remover ${escapeHtml(note.authorDisplayName)}">✕</button>`
        : '';
    return `<span class="session-participant-chip">${escapeHtml(note.authorDisplayName)}${removeBtn}</span>`;
}

function attachSessionHandlers() {
    const list = document.getElementById('sessions-list');
    list.querySelectorAll('.session-save-btn').forEach(btn => btn.addEventListener('click', onSaveDate));
    list.querySelectorAll('.session-complete-btn').forEach(btn => btn.addEventListener('click', onCompleteSession));
    list.querySelectorAll('.session-remove-btn').forEach(btn => btn.addEventListener('click', onRemoveSession));
    list.querySelectorAll('.session-add-participant-btn').forEach(btn => btn.addEventListener('click', onAddParticipant));
    list.querySelectorAll('.session-remove-participant-btn').forEach(btn => btn.addEventListener('click', onRemoveParticipant));
    list.querySelectorAll('.session-note-save-btn').forEach(btn => btn.addEventListener('click', onSaveNote));
}

async function onSaveDate(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    const card = btn.closest('.session-item');
    const dateInput = card.querySelector('.session-edit-date');
    btn.disabled = true;
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/collection/${gameId}/sessions/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playedAt: dateInput.value, notes: null })
        });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        btn.textContent = 'Salvo ✓';
        setTimeout(() => {
            btn.textContent = '💾 Salvar data';
        }, 1500);
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao salvar sessão: ${err.message}`;
    } finally {
        btn.disabled = false;
    }
}

async function onCompleteSession(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    const card = btn.closest('.session-item');
    const dateInput = card.querySelector('.session-edit-date');
    btn.disabled = true;
    status.classList.remove('error');
    try {
        if (dateInput) {
            const saveResponse = await fetch(`/api/collection/${gameId}/sessions/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ playedAt: dateInput.value, notes: null })
            });
            if (!saveResponse.ok) {
                throw new Error(`Erro ${saveResponse.status}`);
            }
        }
        const response = await fetch(`/api/collection/${gameId}/sessions/${id}/complete`, { method: 'POST' });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        await loadSessions();
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao concluir sessão: ${err.message}`;
        btn.disabled = false;
    }
}

async function onRemoveSession(event) {
    const btn = event.currentTarget;
    const id = Number(btn.dataset.id);
    btn.disabled = true;
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/collection/${gameId}/sessions/${id}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        await loadSessions();
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao remover sessão: ${err.message}`;
        btn.disabled = false;
    }
}

async function onAddParticipant(event) {
    const btn = event.currentTarget;
    const sessionId = btn.dataset.sessionId;
    const card = btn.closest('.session-item');
    const select = card.querySelector('.session-participant-select');
    const username = select.value;
    if (!username) {
        return;
    }
    btn.disabled = true;
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/collection/${gameId}/sessions/${sessionId}/participants`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username })
        });
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || `Erro ${response.status}`);
        }
        await loadSessions();
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao adicionar participante: ${err.message}`;
        btn.disabled = false;
    }
}

async function onRemoveParticipant(event) {
    const btn = event.currentTarget;
    const username = btn.dataset.username;
    const card = btn.closest('.session-item');
    const sessionId = card.dataset.sessionId;
    btn.disabled = true;
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/collection/${gameId}/sessions/${sessionId}/participants/${encodeURIComponent(username)}`, { method: 'DELETE' });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        await loadSessions();
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao remover participante: ${err.message}`;
        btn.disabled = false;
    }
}

/* ---------- Modo participante: uma sessão de outra pessoa ---------- */

async function initSharedSession() {
    backLink.classList.remove('hidden');
    backLink.textContent = '← Voltar';
    status.classList.remove('error');
    status.textContent = 'Carregando...';
    try {
        const response = await fetch(`/api/sessions/${sharedSessionId}`);
        if (!response.ok) {
            const problem = await response.json().catch(() => null);
            throw new Error(problem?.detail || `Erro ${response.status}`);
        }
        const session = await response.json();
        status.textContent = '';
        title.textContent = `🎲 ${session.gameName}`;
        subtitle.textContent = `Sessão de ${session.ownerDisplayName} — ${formatDate(session.playedAt)}`;
        document.title = `Sessões de ${session.gameName} - Board Game Tracker`;

        const isOngoing = session.status === 'ONGOING';
        body.innerHTML = `
            <div class="session-item ${isOngoing ? 'session-ongoing' : 'session-completed'}">
                <span class="session-badge ${isOngoing ? 'ongoing' : 'completed'}">${isOngoing ? '🟢 Em andamento' : '✅ Concluída'}</span>
                <div class="session-notes-mural">
                    ${session.notes.map(note => renderNoteCard(note, session.sessionId)).join('')}
                </div>
            </div>
        `;
        body.querySelectorAll('.session-note-save-btn').forEach(btn => btn.addEventListener('click', onSaveNote));
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao carregar sessão: ${err.message}`;
    }
}

/* ---------- Mural de anotações (compartilhado entre os dois modos) ---------- */

function renderNoteCard(note, sessionId) {
    const isMine = myUsername && note.authorUsername === myUsername;
    if (isMine) {
        return `
            <div class="session-note-card mine">
                <span class="session-note-author">Você</span>
                <textarea class="session-note-text" rows="2" placeholder="Suas anotações sobre a sessão">${escapeHtml(note.text ?? '')}</textarea>
                <button type="button" class="session-note-save-btn" data-session-id="${sessionId}">💾 Salvar anotação</button>
            </div>
        `;
    }
    return `
        <div class="session-note-card">
            <span class="session-note-author">${escapeHtml(note.authorDisplayName)}</span>
            <p class="session-note-readonly">${note.text ? escapeHtml(note.text) : 'Sem anotações ainda.'}</p>
        </div>
    `;
}

async function onSaveNote(event) {
    const btn = event.currentTarget;
    const sessionId = btn.dataset.sessionId;
    const card = btn.closest('.session-note-card');
    const textarea = card.querySelector('.session-note-text');
    btn.disabled = true;
    status.classList.remove('error');
    try {
        const response = await fetch(`/api/sessions/${sessionId}/notes/me`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text: textarea.value.trim() || null })
        });
        if (!response.ok) {
            throw new Error(`Erro ${response.status}`);
        }
        btn.textContent = 'Salvo ✓';
        setTimeout(() => {
            btn.textContent = '💾 Salvar anotação';
        }, 1500);
    } catch (err) {
        status.classList.add('error');
        status.textContent = `Erro ao salvar anotação: ${err.message}`;
    } finally {
        btn.disabled = false;
    }
}

/* ---------- Utilitários ---------- */

function formatDate(isoDate) {
    if (!isoDate) {
        return '';
    }
    const [year, month, day] = isoDate.split('-');
    return `${day}/${month}/${year}`;
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str ?? '';
    return div.innerHTML;
}
