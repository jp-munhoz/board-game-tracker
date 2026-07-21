if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js').catch(() => {
            // ambiente sem suporte a service worker: app segue funcionando online normalmente
        });
    });
}
