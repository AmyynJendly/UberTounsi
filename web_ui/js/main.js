
(async function boot() {
    DB.init();
    await window.API.init();
    const isAuthPage = window.location.pathname.includes('auth.html');
    const isAdminPage = window.location.pathname.includes('admin.html');
    if (isAuthPage || isAdminPage) return;
    
    const u = window.API.getCurrentUser();
    if (!u) { window.location.href = 'auth.html'; return; }

    if (window.loadData) await window.loadData();

    initMap();
    if (window.setupSidebar) setupSidebar();
    if (window.updateMoneyPill) updateMoneyPill();
    if (window.Router) Router.navigate('trips');
})();
