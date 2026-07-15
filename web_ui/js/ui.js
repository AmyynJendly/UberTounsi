window.Router = {
    current: 'trips',
    _views: {},
    register: function(name, renderFn) {
        this._views[name] = renderFn;
    },
    navigate: function(view) {
        this.current = view;
        document.querySelectorAll('.panel-view').forEach(v => v.classList.remove('active'));
        document.querySelectorAll('.sidebar-btn[data-view]').forEach(b => b.classList.remove('active'));
        const panel = document.getElementById('view-' + view);
        const btn = document.querySelector('.sidebar-btn[data-view="' + view + '"]');
        if (panel) { panel.classList.add('active'); renderView(view); }
        if (btn) btn.classList.add('active');

        document.body.classList.toggle('admin-mode', view === 'admin');

        // Clear inline grid in admin mode (so CSS rule wins); restore on exit
        const appEl = document.querySelector('.app');
        if (view === 'admin') {
            appEl.dataset.savedGrid = appEl.style.gridTemplateColumns || '';
            appEl.style.gridTemplateColumns = '';
        } else if (appEl.dataset.savedGrid !== undefined) {
            appEl.style.gridTemplateColumns = appEl.dataset.savedGrid;
            delete appEl.dataset.savedGrid;
        }

        if (view !== 'trips' && view !== 'search') { if (window.clearRoute) clearRoute(); const db = document.getElementById('detail-bar'); if(db) db.classList.remove('visible'); }
        document.getElementById('panel').scrollTo(0, 0);
        if (window.updateMoneyPill) updateMoneyPill();
    }
};

async function renderView(v) {
    if (window.Router._views[v]) await window.Router._views[v]();
}

window.Sidebar = {
    _items: [],
    register: function(item) {
        this._items.push(item);
    }
};

async function setupSidebar() {
    const u = window.API.getCurrentUser();
    if (!u) return;
    const nav = document.getElementById('sidebar-nav');
    const bottom = document.getElementById('sidebar-bottom');
    
    const visibleItems = window.Sidebar._items
        .filter(i => !i.role || i.role === u.role || u.role === 'ADMIN')
        .sort((a, b) => (a.order || 99) - (b.order || 99));

    nav.innerHTML = '';
    for (const i of visibleItems) {
        let hasBadge = false;
        if (typeof i.badge === 'function') {
            hasBadge = await i.badge();
        } else if (i.badge) {
            hasBadge = true;
        }
        
        let tip = typeof i.tip === 'function' ? i.tip() : i.tip;
        
        nav.innerHTML += `<button class="sidebar-btn ${i.view === window.Router.current ? 'active' : ''}" data-view="${i.view}" data-tooltip="${tip}" onclick="Router.navigate('${i.view}')">${i.icon}${hasBadge ? '<span class="sidebar-badge"></span>' : ''}</button>`;
    }
    
    const _savedForSwitch = (function() {
        try { return JSON.parse(localStorage.getItem('cov_saved_accounts')) || []; } catch { return []; }
    })().filter(a => a.email !== u.email);

    bottom.innerHTML = `
        <img class="sidebar-avatar" src="${u.avatar || 'https://ui-avatars.com/api/?name=' + u.name.replace(' ', '+') + '&background=1a1a19&color=c3f832'}" onclick="Router.navigate('profile')" title="${u.name}">
        ${_savedForSwitch.length > 0 ? `<button class="sidebar-btn" data-tooltip="Changer de compte" onclick="openAccountSwitcherModal()" style="font-size:18px;font-weight:900"><span style="display:inline-flex;width:18px;height:18px">${ICO.switch}</span></button>` : ''}
        <button class="sidebar-btn" data-tooltip="${window.t('logout')}" onclick="window.API.logout()">${ICO.logout}</button>`;
}

// ── Account switcher modal (usable from any page while logged in) ─────────────
window.openAccountSwitcherModal = function() {
    const accounts = (function() {
        try { return JSON.parse(localStorage.getItem('cov_saved_accounts')) || []; } catch { return []; }
    })();
    const current = window.API.getCurrentUser();
    const others = accounts.filter(a => a.email !== current?.email);
    if (others.length === 0) { Toast.show('Aucun autre compte enregistré', 'error'); return; }

    const modal = document.getElementById('generic-modal');
    if (!modal) { window.location.href = 'auth.html'; return; }

    document.getElementById('generic-modal-body').innerHTML = `
        <div style="padding:24px">
            <h2 style="font-size:18px;font-weight:800;margin-bottom:4px"><span style="display:inline-flex;width:20px;height:20px;margin-right:4px">${ICO.switch}</span>Changer de compte</h2>
            <p style="font-size:13px;color:var(--text-muted);margin-bottom:20px">Connecté en tant que <b>${window.esc ? window.esc(current?.name || current?.email || '') : (current?.name || '')}</b></p>
            <div style="display:flex;flex-direction:column;gap:8px">
                ${others.map(a => {
                    const initials = (a.name || a.email).split(' ').map(w=>w[0]).join('').slice(0,2).toUpperCase();
                    const emailSafe = (a.email||'').replace(/\\/g,'\\\\').replace(/'/g,"\\'");
                    return `<div style="display:flex;align-items:center;gap:12px;padding:12px 14px;background:var(--bg);border:1px solid var(--border);border-radius:10px;cursor:pointer;transition:border-color 0.15s"
                                 onmouseover="this.style.borderColor='var(--lime)'" onmouseout="this.style.borderColor='var(--border)'"
                                 onclick="switchToAccount('${emailSafe}')">
                        <div style="width:36px;height:36px;border-radius:50%;background:var(--lime);color:#000;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:900;flex-shrink:0">${initials}</div>
                        <div style="flex:1;min-width:0">
                            <div style="font-size:14px;font-weight:700">${window.esc ? window.esc(a.name || a.email) : (a.name || a.email)}</div>
                            <div style="font-size:11px;color:var(--text-muted)">${window.esc ? window.esc(a.email) : a.email}</div>
                        </div>
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="color:var(--text-muted)"><polyline points="9 18 15 12 9 6"/></svg>
                    </div>`;
                }).join('')}
            </div>
            <button class="btn btn-outline btn-full" style="margin-top:16px;color:var(--text-muted)"
                onclick="document.getElementById('generic-modal').classList.remove('open')">Annuler</button>
        </div>`;
    modal.classList.add('open');
};

window.switchToAccount = async function(email) {
    const accounts = (function() {
        try { return JSON.parse(localStorage.getItem('cov_saved_accounts')) || []; } catch { return []; }
    })();
    const account = accounts.find(a => a.email === email);

    document.getElementById('generic-modal')?.classList.remove('open');

    if (!account || !account._p) {
        // No password — go to login page with email pre-filled
        sessionStorage.setItem('cov_prefill_email', email);
        await window.API.logout();
        return;
    }

    const password = atob(account._p);
    Toast.show('Connexion en cours…');
    await fetch(`${window.API.API_BASE}/api/auth/logout`).catch(() => {});
    if (window.WS) window.WS.disconnect();
    const res = await window.API.login(email, password);
    if (res.error) {
        Toast.show(res.error, 'error');
    } else {
        await window.API.fetchCurrentUser();
        Toast.show('Connecté : ' + (res.user?.name || email));
        setupSidebar();
        window.Router.navigate('trips');
    }
};


// ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━  PANELS ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ ━ 
function skeletonCards(n) {
    return Array(n).fill(0).map(() =>
        '<div class="skeleton-card"><div class="skeleton skeleton-line wide"></div><div class="skeleton skeleton-line medium"></div><div class="skeleton skeleton-line narrow"></div></div>'
    ).join('');
}