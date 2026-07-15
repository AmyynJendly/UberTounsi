async function renderProfilePanel(tab) {
    if (tab) window._profileTab = tab;
    if (!window._profileTab) window._profileTab = 'general';

    const u = window.API.getCurrentUser();
    if (!u) { window.location.href = 'auth.html'; return; }

    // Load data needed per tab (only what's required)
    const stats     = await window.API.getUserStats(u.id);
    const rating    = await window.API.getUserRating(u.id) || 5.0;
    const rawCars   = window._profileTab === 'general' ? await window.API.getCars() : [];
    const cars      = Array.isArray(rawCars) ? rawCars : [];
    const favorites = window._profileTab === 'general'
        ? await window.API.getMyFavoriteDrivers().catch(() => []) : [];
    const history   = window._profileTab === 'history'
        ? await window.API.getTripHistory().catch(() => []) : [];

    document.getElementById('view-profile').innerHTML = `
        <div class="panel-header">
            <h1 class="panel-title">${t('profile')}</h1>
            <div class="tabs" style="margin-top:12px; margin-bottom:0; overflow-x:auto; white-space:nowrap">
                <button class="tab ${window._profileTab === 'general'  ? 'active' : ''}" onclick="renderProfilePanel('general')">${t('general')}</button>
                <button class="tab ${window._profileTab === 'history'  ? 'active' : ''}" onclick="renderProfilePanel('history')"><span style="display:inline-flex;width:14px;height:14px;vertical-align:middle;margin-right:4px">${ICO.calendar}</span>Historique</button>
                <button class="tab ${window._profileTab === 'settings' ? 'active' : ''}" onclick="renderProfilePanel('settings')">${t('settings')}</button>
            </div>
        </div>
        <div class="panel-body" style="padding-top:16px">
            ${window._profileTab === 'general'  ? renderGeneralTab(u, stats, cars, rating, favorites)
            : window._profileTab === 'history'  ? renderHistoryTab(history, u)
            : renderSettingsTab(u)}
        </div>`;
}

function renderGeneralTab(u, stats, cars, rating, favorites = []) {
    return `
        <div class="profile-header" style="margin-top:0">
            <div style="position:relative; width:92px; height:92px; margin:0 auto; display:block;">
                <div class="profile-avatar-wrapper" style="width:100%; height:100%; border-radius:50%; border:2.5px solid var(--lime); padding:3px; box-shadow:0 0 15px rgba(195, 248, 50, 0.15)">
                    <img class="profile-avatar-lg" src="${u.avatar || 'https://ui-avatars.com/api/?name=' + u.name.replace(' ', '+')}" style="margin:0; width:100%; height:100%; object-fit: cover; border-radius:50%">
                </div>
                <button class="btn btn-icon" onClick="openEditProfileModal()" style="position:absolute; bottom:-4px; right:-4px; width:32px; height:32px; background:var(--white); border-radius:50%; border:2px solid var(--border); color:var(--text); display:flex; padding:0; justify-content:center; align-items:center; box-shadow:0 4px 10px rgba(0,0,0,0.15); transition: all 0.3s">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M12 20h9M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
                </button>
                <div class="profile-rating-badge">
                    <svg viewBox="0 0 24 24"><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/></svg>
                    ${rating.toFixed(1)}
                </div>
            </div>
            <div class="profile-name">${u.name}</div>
            <div class="profile-role">
                <div class="profile-info-line">
                    <span class="icon-inline">${u.role === 'DRIVER' ? ICO.car : u.role === 'ADMIN' ? ICO.shield : ICO.user}</span>
                    <span style="font-weight:800; color:var(--text)">${u.role === 'DRIVER' ? 'Chauffeur' : u.role === 'ADMIN' ? 'Admin' : 'Passager'}</span>
                    <span style="color:var(--text-muted)">·</span>
                    <span>${u.email}</span>
                </div>
                
                <div class="profile-badges-line">
                    ${u.isVerified 
                        ? `<span class="badge badge-success" style="font-size:10px; padding:4px 10px; display:flex; align-items:center; gap:5px"><span class="icon-inline" style="margin:0">${ICO.check}</span> Vérifié</span>` 
                        : `<button class="btn btn-xs btn-outline" style="font-size:10px; height:28px" onclick="openOtpModal()">Vérifier mon compte</button>`}
                    
                    ${u.role === 'DRIVER' ? (u.idVerified
                        ? `<span class="badge badge-info" style="font-size:10px; padding:4px 10px; display:flex; align-items:center; gap:5px; background:rgba(59,130,246,0.1); color:var(--info); border:1px solid rgba(59,130,246,0.2)"><span class="icon-inline" style="margin:0">${ICO.idcard}</span> ID Vérifié</span>`
                        : `<button class="btn btn-xs btn-outline" style="font-size:10px; height:28px; color:var(--info); border-color:rgba(59,130,246,0.3)" onclick="openUploadIdModal()">🪪 Soumettre identité</button>`)
                        : ''}
                </div>
            </div>
            
            <div style="position:relative; background:var(--bg); border:1px solid var(--border); border-radius:var(--radius-sm); padding:14px; margin-top:16px">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px">
                    <span style="font-size:10px; font-weight:800; color:var(--text-muted); text-transform:uppercase">${t('bio')}</span>
                    <button class="btn btn-xs" onclick="openEditProfileModal()" style="padding:4px; min-width:unset">${ICO.book}</button>
                </div>
                <div style="font-size:13px; color:var(--text-secondary); line-height:1.5; font-style:italic">
                    ${u.bio || t('bio_placeholder')}
                </div>
            </div>
        </div>
        
        <div class="stats-row">
            <div class="stat-pill"><div class="stat-pill-icon icon-md" style="color:var(--info);background:rgba(59,130,246,0.12)">${ICO.car}</div><div class="stat-pill-val">${stats?.totalTrips || 0}</div><div class="stat-pill-label">Trajets</div></div>
            <div class="stat-pill"><div class="stat-pill-icon icon-md" style="color:var(--success);background:rgba(34,197,94,0.12)">${ICO.co2}</div><div class="stat-pill-val">${(stats?.co2Saved || 0).toFixed(1)}</div><div class="stat-pill-label">kg CO₂</div></div>
            <div class="stat-pill"><div class="stat-pill-icon icon-md" style="color:var(--warning);background:rgba(245,158,11,0.12)">${ICO.tnd}</div><div class="stat-pill-val">${(stats?.moneySaved || 0).toFixed(0)}</div><div class="stat-pill-label">TND éco.</div></div>
        </div>
        
        <div class="divider"></div>
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px"><div class="section-title" style="margin:0">Mes Véhicules</div><button class="btn btn-lime btn-xs" onclick="openCarModal()">+ Ajouter</button></div>
        <div style="display:grid; grid-template-columns: 1fr; gap:12px; margin-bottom:16px">
        ${cars.length === 0 ? '<p style="font-size:13px;color:var(--text-muted)">Aucun véhicule enregistré</p>' : cars.map(c => {
        const cl = CAR_BRANDS.find(b => b.name === c.brand);
        return `
            <div class="car-card" style="display:flex; align-items:center; padding:10px; background:var(--bg); border:1px solid var(--border); border-radius:var(--radius-sm)">
                <div class="car-photo-box" style="width:70px; height:46px; border-radius:4px; overflow:hidden; background:var(--white); flex-shrink:0; margin-right:12px">
                    ${c.image ? `<img src="${c.image}" style="width:100%; height:100%; object-fit:cover">` :
                (cl ? `<img src="${cl.logo}" style="width:100%; height:100%; object-fit:contain; padding:6px">` : '🚗')}
                </div>
                <div class="car-info" style="flex:1">
                    <div class="car-model" style="font-weight:800; font-size:13px">${c.brand} ${c.model}</div>
                    <div class="car-plate" style="font-size:10px; color:var(--text-muted); text-transform:uppercase">${c.plate} · ${c.color}</div>
                </div>
                <div style="display:flex; flex-direction:column; gap:4px; margin-left:8px">
                    <button class="btn btn-outline btn-xs" style="padding:0; width:28px; height:24px" onclick='openEditCarModal(${JSON.stringify(c).replace(/'/g, "&apos;")})'>✏️ </button>
                    <button class="btn btn-danger btn-xs" style="padding:0; width:28px; height:24px; background:rgba(239,68,68,0.1); color:var(--error)" onclick="deleteCarReq(${c.id})">${ICO.trash}</button>
                </div>
            </div>`}).join('')}
        </div>
        <div class="divider"></div>
        <div id="eco-badges-section"></div>

        ${favorites.length > 0 ? `
        <div class="divider"></div>
        <div class="section-title">Chauffeurs Favoris</div>
        <div style="display:flex;flex-direction:column;gap:8px;margin-bottom:8px">
            ${favorites.map(f => `
                <div class="car-card" style="padding:10px 14px">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(f.driverName || 'Driver')}" style="width:32px;height:32px;border-radius:50%">
                    <div class="car-info"><div class="car-model">${esc(f.driverName || 'Chauffeur')}</div></div>
                    <button class="btn btn-danger btn-xs" style="background:rgba(239,68,68,0.1);color:var(--error)" onclick="removeFavDriver(${f.driverId})">Retirer</button>
                </div>
            `).join('')}
        </div>
        ` : ''}

        <div class="divider"></div>
        <button class="btn btn-danger btn-full" onclick="window.API.logout()">${t('logout')}</button>`;
    setTimeout(() => renderEcoBadges(stats, rating), 0);
}

// ── Feature 4: OTP Verification modal ───────────────────────────────────────
window.openOtpModal = async function () {
    const res = await window.API.generateOtp();
    if (!res || res.error) { Toast.show('Erreur lors de la génération du code', 'error'); return; }
    const html = `
        <div style="padding:24px; text-align:center">
            <div class="icon-inline" style="width:48px; height:48px; margin-bottom:16px; color:var(--lime)">${ICO.msg}</div>
            <h2 style="font-size:20px; font-weight:800; margin-bottom:8px">Vérification du compte</h2>
            <p style="font-size:13px; color:var(--text-muted); margin-bottom:4px">Un code à 6 chiffres a été généré :</p>
            <div style="font-size:32px; font-weight:900; letter-spacing:8px; text-align:center; color:var(--lime); margin:16px 0; font-family:monospace">${res.code}</div>
            <p style="font-size:12px; color:var(--text-muted); margin-bottom:20px">(En production, ce code serait envoyé par email/SMS)</p>
            <input type="text" id="otp-input" class="input" maxlength="6" placeholder="Entrez le code" style="letter-spacing:6px; font-size:20px; text-align:center; margin-bottom:20px; border-radius:var(--radius-sm)">
            <button class="btn btn-lime btn-full" style="height:48px; font-weight:800" onclick="submitOtp()">Confirmer</button>
        </div>`;
    const modal = document.getElementById('generic-modal');
    if (modal) { document.getElementById('generic-modal-body').innerHTML = html; modal.classList.add('open'); }
};

// ── ID Document upload modal ─────────────────────────────────────────────────
window.openUploadIdModal = function () {
    const html = `
        <div style="padding:24px; text-align:center">
            <div class="icon-inline" style="width:48px; height:48px; margin-bottom:16px; color:var(--info)">${ICO.idcard}</div>
            <h2 style="font-size:20px; font-weight:800; margin-bottom:8px">Soumettre une pièce d'identité</h2>
            <p style="font-size:13px; color:var(--text-muted); margin-bottom:20px">Téléchargez une photo lisible de votre CIN ou passeport. L'administrateur l'examinera sous 24h.</p>
            
            <div class="image-upload-box" onclick="document.getElementById('id-doc-picker').click()"
                 style="height:160px; background:var(--bg); border:2px dashed var(--border); border-radius:12px; display:flex; align-items:center; justify-content:center; cursor:pointer; overflow:hidden; position:relative; margin-bottom:20px; transition:all 0.3s">
                <img id="id-doc-preview" style="display:none; width:100%; height:100%; object-fit:contain">
                <div id="id-doc-placeholder" style="text-align:center; color:var(--text-muted)">
                    <div class="icon-inline" style="width:32px; height:32px; margin-bottom:8px; opacity:0.5">${ICO.user}</div>
                    <div style="font-size:12px; font-weight:600">Cliquez pour ajouter la photo</div>
                </div>
                <input type="file" id="id-doc-picker" style="display:none" accept="image/*"
                    onchange="window.handleFileSelect(event, b64 => {
                        document.getElementById('id-doc-preview').src=b64;
                        document.getElementById('id-doc-preview').style.display='block';
                        document.getElementById('id-doc-placeholder').style.display='none';
                        window._idDocBase64 = b64;
                    })">
            </div>
            <button class="btn btn-lime btn-full" style="height:48px; font-weight:800" onclick="submitIdDoc()">Envoyer pour vérification</button>
        </div>`;
    const modal = document.getElementById('generic-modal');
    if (modal) { document.getElementById('generic-modal-body').innerHTML = html; modal.classList.add('open'); }
};

window.submitIdDoc = async function () {
    if (!window._idDocBase64) { Toast.show('Sélectionnez une image', 'error'); return; }
    const res = await window.API.uploadIdDocument(window._idDocBase64);
    document.getElementById('generic-modal')?.classList.remove('open');
    window._idDocBase64 = null;
    Toast.show(res.success ? 'Document soumis — en cours de vérification ✓' : (res.error || 'Erreur'), res.success ? 'success' : 'error');
};

window.submitOtp = async function () {
    const code = document.getElementById('otp-input')?.value.trim();
    if (!code) { Toast.show('Entrez le code', 'error'); return; }
    const res = await window.API.verifyOtp(code);
    document.getElementById('generic-modal')?.classList.remove('open');
    if (res.success) { Toast.show('Compte vérifié ✔'); renderProfilePanel(); }
    else Toast.show('Code incorrect ou expiré', 'error');
};

// ── Feature 7: Remove favorite driver ───────────────────────────────────────
window.removeFavDriver = async function (driverId) {
    await window.API.removeFavoriteDriver(driverId);
    Toast.show('Retiré des favoris');
    renderProfilePanel();
};

// ── Trip History tab ─────────────────────────────────────────────────────────
function renderHistoryTab(history, currentUser) {
    if (!Array.isArray(history) || history.length === 0) {
        return `<div class="empty-state"><div class="empty-state-icon">${ICO.calendar}</div><h3>Aucun trajet terminé</h3><p>Vos trajets complétés apparaîtront ici</p></div>`;
    }

    // Sort newest first
    const sorted = [...history].sort((a, b) => new Date(b.date) - new Date(a.date));

    return sorted.map(entry => {
        const isDriver = entry.role === 'DRIVER';
        const dateStr  = entry.date ? new Date(entry.date).toLocaleDateString('fr-FR', { day:'2-digit', month:'short', year:'numeric' }) : '—';
        const people   = isDriver ? (entry.coPassengers || []) : [entry.driver, ...(entry.coPassengers || [])].filter(Boolean);

        return `
        <div class="booking-card" style="margin-bottom:12px">
            <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px">
                <div>
                    <div class="booking-route">${esc(entry.departure)} → ${esc(entry.arrival)}</div>
                    <div style="font-size:11px;color:var(--text-muted);margin-top:2px">${dateStr} · ${(entry.price||0).toFixed(2)} TND</div>
                </div>
                <span class="status status-completed" style="flex-shrink:0">${isDriver ? `${ICO.car} Chauffeur` : `${ICO.luggage} Passager`}</span>
            </div>

            <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;margin-bottom:8px">
                ${isDriver ? 'Passagers' : 'Compagnons de trajet'}
            </div>

            ${people.length === 0
                ? `<div style="font-size:12px;color:var(--text-muted);font-style:italic">Aucun compagnon enregistré</div>`
                : `<div style="display:flex;flex-direction:column;gap:8px">
                    ${people.map(p => personRowHTML(p, isDriver, currentUser)).join('')}
                   </div>`
            }
        </div>`;
    }).join('');
}

function personRowHTML(p, callerIsDriver, currentUser) {
    if (!p) return '';
    const avatarSrc = p.avatar && p.avatar.length > 10
        ? p.avatar
        : `https://ui-avatars.com/api/?name=${encodeURIComponent(p.name || 'User')}&background=random`;
    const roleLabel = `<span class="icon-inline">${p.role === 'DRIVER' ? ICO.car : ICO.luggage}</span>`;
    const verBadge  = p.isVerified ? `<span class="icon-inline" style="color:var(--lime);margin-left:4px">${ICO.check}</span>` : '';
    const idBadge   = p.idVerified  ? `<span class="icon-inline" style="color:var(--info);margin-left:4px">${ICO.idcard}</span>` : '';

    return `
        <div style="display:flex;align-items:center;gap:10px;padding:8px 10px;background:var(--bg);border:1px solid var(--border);border-radius:8px;cursor:pointer"
             onclick="openPersonModal(${JSON.stringify(p).replace(/"/g, '&quot;')})">
            <img src="${avatarSrc}" style="width:36px;height:36px;border-radius:50%;object-fit:cover;flex-shrink:0">
            <div style="flex:1;min-width:0">
                <div style="font-size:13px;font-weight:700;display:flex;align-items:center;gap:4px">
                    ${roleLabel} ${esc(p.name)} ${verBadge} ${idBadge}
                    ${p.isBlocked ? '<span style="color:var(--error);font-size:10px;font-weight:800">[bloqué]</span>' : ''}
                </div>
                <div style="font-size:11px;color:var(--text-muted)"><span style="display:inline-flex;width:11px;height:11px;vertical-align:middle">${ICO.star}</span> ${(p.rating||0).toFixed(1)}</div>
            </div>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" style="color:var(--text-muted);flex-shrink:0"><polyline points="9 18 15 12 9 6"/></svg>
        </div>`;
}

// ── Person detail modal ───────────────────────────────────────────────────────
window.openPersonModal = async function (p) {
    const modal = document.getElementById('generic-modal');
    if (!modal) return;

    // Default to DRIVER when role is missing (called from trip cards)
    if (!p.role) p.role = 'DRIVER';

    const avatarSrc = p.avatar && p.avatar.length > 10
        ? p.avatar
        : `https://ui-avatars.com/api/?name=${encodeURIComponent(p.name || 'User')}&background=random`;

    // Determine live block/favorite state
    const isFav = p.role === 'DRIVER' && await window.API.isDriverFavorite(p.id).catch(() => false);

    document.getElementById('generic-modal-body').innerHTML = `
        <div style="padding:24px">
            <div style="text-align:center;margin-bottom:20px">
                <img src="${avatarSrc}" style="width:72px;height:72px;border-radius:50%;object-fit:cover;border:2.5px solid var(--lime);margin-bottom:10px">
                <div style="font-size:18px;font-weight:800">${esc(p.name)}</div>
                <div style="font-size:12px;color:var(--text-muted);margin-top:2px;display:flex;align-items:center;justify-content:center;gap:4px">
                    <span class="icon-inline">${p.role === 'DRIVER' ? ICO.car : ICO.luggage}</span> ${p.role === 'DRIVER' ? 'Chauffeur' : 'Passager'}
                    ${p.isVerified ? ` · <span class="icon-inline" style="color:var(--lime)">${ICO.check}</span> <span style="color:var(--lime);font-weight:700">Vérifié</span>` : ''}
                    ${p.idVerified  ? ` · <span class="icon-inline" style="color:var(--info)">${ICO.idcard}</span> <span style="color:var(--info);font-weight:700">ID OK</span>` : ''}
                </div>
            </div>

            <div style="display:flex;justify-content:center;gap:24px;margin-bottom:20px">
                <div style="text-align:center">
                    <div style="font-size:22px;font-weight:900;color:var(--warning)">${ICO.star} ${(p.rating||0).toFixed(1)}</div>
                    <div style="font-size:10px;color:var(--text-muted)">Note</div>
                </div>
            </div>

            <div style="background:var(--bg);border:1px solid var(--border);border-radius:8px;padding:12px;margin-bottom:16px">
                ${p.phone ? `<div style="display:flex;align-items:center;justify-content:center;gap:10px;margin-bottom:12px;color:var(--lime)">
                    <span class="icon-inline" style="width:18px;height:18px">${ICO.msg}</span>
                    <a href="tel:${esc(p.phone)}" style="font-size:15px;font-weight:800;color:inherit;text-decoration:none;letter-spacing:0.5px">${esc(p.phone)}</a>
                </div>` : ''}
                ${p.bio ? `<div style="font-size:13px;color:var(--text-secondary);font-style:italic;line-height:1.5">"${esc(p.bio)}"</div>`
                        : `<div style="font-size:12px;color:var(--text-muted);font-style:italic">Aucune biographie</div>`}
            </div>

            <div style="display:flex;flex-direction:column;gap:8px">
                ${p.role === 'DRIVER' ? `
                <button id="fav-modal-btn" class="btn btn-full" style="height:42px;background:${isFav ? 'rgba(239,68,68,0.1)' : 'rgba(132,204,22,0.1)'};color:${isFav ? 'var(--error)' : 'var(--lime)'};border:1px solid ${isFav ? 'rgba(239,68,68,0.3)' : 'rgba(132,204,22,0.3)'};font-weight:700"
                    onclick="toggleFavFromModal(${p.id}, this)">
                    <span style="display:inline-flex;width:16px;height:16px;margin-right:6px">${isFav ? ICO.heartFilled : ICO.heart}</span>${isFav ? 'Retirer des favoris' : 'Ajouter aux favoris'}
                </button>` : ''}
                <button id="block-modal-btn" class="btn btn-full" style="height:42px;background:${p.isBlocked ? 'rgba(132,204,22,0.1)' : 'rgba(239,68,68,0.1)'};color:${p.isBlocked ? 'var(--lime)' : 'var(--error)'};border:1px solid ${p.isBlocked ? 'rgba(132,204,22,0.3)' : 'rgba(239,68,68,0.3)'};font-weight:700"
                    onclick="toggleBlockFromModal(${p.id}, ${p.isBlocked}, this)">
                    ${p.isBlocked ? 'Débloquer' : 'Bloquer'}
                </button>
                <button class="btn btn-outline btn-full" style="height:42px;color:var(--error);border-color:rgba(239,68,68,0.3);font-weight:700"
                    onclick="openReportModal(${p.id})">
                    <span class="icon-inline" style="margin-right:8px">${ICO.flag}</span> Signaler
                </button>
            </div>
        </div>`;

    modal.classList.add('open');
};

window.toggleFavFromModal = async function (driverId, btn) {
    const isCurrentlyFav = btn.textContent.includes('Retirer');
    if (isCurrentlyFav) {
        await window.API.removeFavoriteDriver(driverId);
        btn.innerHTML = `<span style="display:inline-flex;width:16px;height:16px;margin-right:6px">${ICO.heart}</span>Ajouter aux favoris`;
        btn.style.color = 'var(--lime)';
        Toast.show('Retiré des favoris');
    } else {
        await window.API.addFavoriteDriver(driverId);
        btn.innerHTML = `<span style="display:inline-flex;width:16px;height:16px;margin-right:6px">${ICO.heartFilled}</span>Retirer des favoris`;
        btn.style.color = 'var(--error)';
        Toast.show('Ajouté aux favoris');
    }
};

window.toggleBlockFromModal = async function (userId, currentlyBlocked, btn) {
    const res = await fetch(`${window.API.API_BASE}/api/users/block`, {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ blockedId: userId })
    }).then(r => r.json()).catch(() => ({}));

    if (res.blocked !== undefined) {
        btn.textContent  = res.blocked ? 'Débloquer' : 'Bloquer';
        btn.style.color  = res.blocked ? 'var(--lime)' : 'var(--error)';
        Toast.show(res.blocked ? 'Utilisateur bloqué' : 'Utilisateur débloqué');
    }
};

function renderSettingsTab(u) {
    return `
        <div class="section-title">${t('appearance')}</div>
        <div class="theme-switch" style="margin-top:0">
            <div style="display:flex; align-items:center; gap:10px">
                <span style="font-size:16px">${document.body.classList.contains('dark-theme') ? '🌙' : '☀️'}</span>
                <div>
                    <div style="font-size:13px; font-weight:700">${t('dark_mode')}</div>
                    <div style="font-size:11px; color:var(--text-muted)">${document.body.classList.contains('dark-theme') ? 'Mode sombre activé' : 'Mode clair activé'}</div>
                </div>
            </div>
            <div class="toggle-switch ${document.body.classList.contains('dark-theme') ? 'on' : ''}"
                 onclick="window.DB.toggleTheme(); renderProfilePanel('settings')"
                 style="cursor:pointer;flex-shrink:0"></div>
        </div>

        <div class="divider"></div>
        <div class="section-title">${t('notifications')}</div>
        <div style="display:flex; flex-direction:column; gap:8px">
            <div class="theme-switch" style="margin:0">
                <span style="font-size:13px; font-weight:600">${t('push')}</span>
                <div class="toggle-switch ${localStorage.getItem('cov_push') !== 'false' ? 'on' : ''}"
                     onclick="localStorage.setItem('cov_push', this.classList.toggle('on'))" style="cursor:pointer;flex-shrink:0"></div>
            </div>
            <div class="theme-switch" style="margin:0">
                <span style="font-size:13px; font-weight:600">${t('emails')}</span>
                <div class="toggle-switch ${localStorage.getItem('cov_email_notif') !== 'false' ? 'on' : ''}"
                     onclick="localStorage.setItem('cov_email_notif', this.classList.toggle('on'))" style="cursor:pointer;flex-shrink:0"></div>
            </div>
            <div class="theme-switch" style="margin:0">
                <span style="font-size:13px; font-weight:600">${t('sms')}</span>
                <div class="toggle-switch ${localStorage.getItem('cov_sms') === 'true' ? 'on' : ''}"
                     onclick="localStorage.setItem('cov_sms', this.classList.toggle('on'))" style="cursor:pointer;flex-shrink:0"></div>
            </div>
        </div>


        <div class="divider" style="margin-top:24px"></div>
        <div class="section-title">${t('account')}</div>
        <button class="btn btn-outline btn-full" style="justify-content:flex-start; height:44px; margin-bottom:8px" onclick="openEditProfileModal()">
            <span style="margin-right:10px">${ICO.shield}</span> ${t('change_pass')}
        </button>
        <button class="btn btn-outline btn-full" style="justify-content:flex-start; height:44px; color:var(--error); border-color:var(--error); opacity:1" onclick="window.confirmDeleteAccount()">
            <span style="margin-right:10px">${ICO.warning}</span> ${t('del_acc')}
        </button>
    `;
}

window.changeLang = function (l) {
    localStorage.setItem('cov_lang', l);
    Toast.show(t('lang_success'));
    location.reload(); // Hard refresh to apply dictionary everywhere
};

window.confirmDeleteAccount = function () {
    showConfirm({
        icon: ICO.warning,
        title: 'Supprimer votre compte ?',
        body: 'Cette action est irréversible. Toutes vos données seront définitivement supprimées.',
        okLabel: 'Supprimer définitivement',
        okClass: 'btn-danger',
        onOk: function () { window.API.deleteAccount(); }
    });
};

window.API.deleteBooking = async function (id) {
    try {
        const res = await fetch(`${window.API.API_BASE}/api/bookings/delete`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id }) });
        return await res.json();
    } catch (e) { return { error: "Erreur serveur" }; }
};
window.API.deleteCar = async function (id) {
    try {
        const res = await fetch(`${window.API.API_BASE}/api/cars/delete`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ id }) });
        return await res.json();
    } catch (e) { return { error: "Erreur serveur" }; }
};
window.API.updateCar = async function (carData) {
    try {
        const res = await fetch(`${window.API.API_BASE}/api/cars/update`, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(carData) });
        return await res.json();
    } catch (e) { return { error: "Erreur serveur" }; }
};

window.API.deleteAllArchivedBookings = async function () {
    try {
        const res = await fetch(`${window.API.API_BASE}/api/bookings/delete-archived`, { method: 'POST' });
        return await res.json();
    } catch (e) { return { error: "Erreur serveur" }; }
};

window.deleteAllArchivedBookingsBtn = function () {
    showConfirm({
        icon: ICO.trash,
        title: 'Effacer l\'historique ?',
        body: 'Toutes les réservations annulées et refusées seront supprimées de la liste.',
        okLabel: 'Effacer',
        okClass: 'btn-danger',
        onOk: async function () {
            const res = await window.API.deleteAllArchivedBookings();
            if (res.success) { Toast.show('Historique effacé !'); renderBookingsPanel('passager'); }
            else Toast.show(res.error || 'Erreur', 'error');
        }
    });
};

window.deleteBookingBtn = function (id) {
    showConfirm({
        icon: ICO.trash,
        title: 'Supprimer cette réservation ?',
        body: 'Elle sera retirée de votre historique.',
        okLabel: 'Supprimer',
        okClass: 'btn-danger',
        onOk: async function () {
            const res = await window.API.deleteBooking(id);
            if (res.success) { Toast.show('Réservation supprimée'); renderBookingsPanel('passager'); }
            else Toast.show(res.error || 'Erreur', 'error');
        }
    });
};

window.deleteCarReq = function (id) {
    showConfirm({
        icon: ICO.car,
        title: 'Supprimer ce véhicule ?',
        body: 'Le véhicule sera retiré de votre profil.',
        okLabel: 'Supprimer',
        okClass: 'btn-danger',
        onOk: async function () {
            const res = await window.API.deleteCar(id);
            if (res.success) { Toast.show('Véhicule supprimé'); renderProfilePanel(); }
            else Toast.show(res.error || 'Erreur', 'error');
        }
    });
};

window.openEditCarModal = function (car) {
    window._carState = { id: car.id, brand: car.brand, model: car.model, color: car.color, plate: car.plate, image: null };
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:20px">Modifier le véhicule</h2>
        <div class="form-group"><label class="form-label">Marque</label>
            <div class="brand-grid" id="car-brands">${CAR_BRANDS.map(b => `<div class="brand-item ${b.name === car.brand ? 'active' : ''}" onclick="selectCarBrand(this,'${b.name}')"><img src="${b.logo}" style="width:40px;height:40px;object-fit:contain;margin-bottom:8px">${b.name}</div>`).join('')}</div></div>
        <div class="form-group"><label class="form-label">Modèle</label><input class="form-input" id="car-model" value="${car.model}"></div>
        <div class="form-group"><label class="form-label">Immatriculation</label><input class="form-input" id="car-plate" value="${car.plate}"></div>
        <div class="form-group"><label class="form-label">Couleur</label>
            <div class="color-grid" id="car-colors" style="margin-bottom:12px">${CAR_COLORS.map(c => `<div class="color-swatch ${c.name === car.color ? 'active' : ''}" style="background:${c.hex}" title="${c.name}" onclick="selectCarColor(this,'${c.name}')"></div>`).join('')}</div></div>
        <div class="form-group"><label class="form-label">Nouvelle photo (optionnel)</label>
            <div class="image-upload-box" onclick="document.getElementById('car-img-picker').click()" style="height:120px; background:var(--bg); border:2px dashed var(--border); border-radius:var(--radius-sm); display:flex; align-items:center; justify-content:center; cursor:pointer; overflow:hidden; position:relative">
                <img id="car-preview" style="${car.image ? 'display:block' : 'display:none'}; width:100%; height:100%; object-fit:cover" src="${car.image || ''}">
                <div id="car-upload-placeholder" style="${car.image ? 'display:none' : 'text-align:center; color:var(--text-muted)'}">${ICO.plus}<div style="font-size:11px; margin-top:4px">Changer la photo</div></div>
                <input type="file" id="car-img-picker" style="display:none" accept="image/*" onchange="window.handleFileSelect(event, b64 => { document.getElementById('car-preview').src = b64; document.getElementById('car-preview').style.display='block'; document.getElementById('car-upload-placeholder').style.display='none'; window._carState.image = b64; })">
            </div>
        </div>
        <button class="btn btn-lime btn-full" onclick="confirmEditCar()" style="margin-top:16px">Enregistrer les modifications</button>`;
    modal.classList.add('open');
};

window.confirmEditCar = async function () {
    const brand = window._carState.brand;
    const model = document.getElementById('car-model').value.trim();
    const plate = document.getElementById('car-plate').value.trim();
    const color = window._carState.color;
    const image = window._carState.image; // can be null if not changed

    const res = await window.API.updateCar({ id: window._carState.id, brand, model, licensePlate: plate || 'Non spécifié', color, seats: 4, imageBase64: image });
    document.getElementById('book-modal').classList.remove('open');
    if (res.success) {
        Toast.show("Véhicule mis à jour !");
        renderProfilePanel();
    } else Toast.show(res.error || "Erreur", 'error');
};

window.openCarModal = function () {
    window._carState = { brand: '', model: '', color: 'Noir', plate: '' };
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:20px">Ajouter un véhicule</h2>
        <div class="form-group"><label class="form-label">Marque</label>
            <div class="brand-grid" id="car-brands">${CAR_BRANDS.map(b => `<div class="brand-item" onclick="selectCarBrand(this,'${b.name}')"><img src="${b.logo}" style="width:40px;height:40px;object-fit:contain;margin-bottom:8px">${b.name}</div>`).join('')}</div></div>
        <div class="form-group"><label class="form-label">Modèle</label><input class="form-input" id="car-model" placeholder="Ex: 3008, Clio, Corolla..."></div>
        <div class="form-group"><label class="form-label">Immatriculation</label><input class="form-input" id="car-plate" placeholder="205 TN 1234"></div>
        <div class="form-group"><label class="form-label">Couleur</label>
            <div class="color-grid" id="car-colors" style="margin-bottom:12px">${CAR_COLORS.map(c => `<div class="color-swatch ${c.name === 'Noir' ? 'active' : ''}" style="background:${c.hex}" title="${c.name}" onclick="selectCarColor(this,'${c.name}')"></div>`).join('')}</div></div>
        <div class="form-group"><label class="form-label">Photo du véhicule</label>
            <div class="image-upload-box" onclick="document.getElementById('car-img-picker').click()" style="height:120px; background:var(--bg); border:2px dashed var(--border); border-radius:var(--radius-sm); display:flex; align-items:center; justify-content:center; cursor:pointer; overflow:hidden; position:relative">
                <img id="car-preview" style="display:none; width:100%; height:100%; object-fit:cover">
                <div id="car-upload-placeholder" style="text-align:center; color:var(--text-muted)">${ICO.plus}<div style="font-size:11px; margin-top:4px">Ajouter une photo</div></div>
                <input type="file" id="car-img-picker" style="display:none" accept="image/*" onchange="window.handleFileSelect(event, b64 => { document.getElementById('car-preview').src = b64; document.getElementById('car-preview').style.display='block'; document.getElementById('car-upload-placeholder').style.display='none'; window._carState.image = b64; })">
            </div>
        </div>
        <button class="btn btn-lime btn-full" onclick="confirmAddCar()" style="margin-top:16px">Ajouter le véhicule</button>`;
    modal.classList.add('open');
};
window.selectCarBrand = function (el, name) {
    document.querySelectorAll('#car-brands .brand-item').forEach(b => b.classList.remove('active'));
    el.classList.add('active'); window._carState.brand = name;
};
window.selectCarColor = function (el, name) {
    document.querySelectorAll('#car-colors .color-swatch').forEach(c => c.classList.remove('active'));
    el.classList.add('active'); window._carState.color = name;
};
window.confirmAddCar = async function () {
    const brand = window._carState.brand;
    const model = document.getElementById('car-model').value.trim();
    const plate = document.getElementById('car-plate').value.trim();
    const color = window._carState.color;
    const image = window._carState.image;
    if (!brand) { Toast.show('Sélectionnez une marque', 'error'); return; }
    if (!model) { Toast.show('Entrez le modèle', 'error'); return; }
    const result = await window.API.addCar({ brand, model, plate: plate || 'Non spécifié', color, seats: 4, image });
    document.getElementById('book-modal').classList.remove('open');
    if (result.success) {
        Toast.show('Véhicule ajouté !');
        renderProfilePanel();
    } else {
        Toast.show(result.error || 'Erreur lors de l\'ajout', 'error');
    }
};

// --- EDIT PROFILE MODAL ---
window.openEditProfileModal = function () {
    const u = window.API.getCurrentUser();
    if (!u) return;
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:20px">Modifier mon profil</h2>
        <div style="display:flex; justify-content:center; margin-bottom:20px">
            <div style="position:relative; width:80px; height:80px; cursor:pointer" onclick="document.getElementById('ep-avatar-picker').click()">
                <img id="ep-avatar-preview" src="${u.avatar || 'https://ui-avatars.com/api/?name=' + u.name.replace(' ', '+')}" style="width:100%; height:100%; border-radius:50%; object-fit:cover; border:2px solid var(--lime)">
                <div style="position:absolute; bottom:0; right:0; background:var(--lime); color:var(--dark); width:24px; height:24px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:12px">${ICO.plus}</div>
                <input type="file" id="ep-avatar-picker" style="display:none" accept="image/*" onchange="window.handleFileSelect(event, b64 => { document.getElementById('ep-avatar-preview').src = b64; window._editAvatar = b64; })">
            </div>
        </div>
        <div class="form-group"><label class="form-label">Nom Complet</label><input type="text" class="form-input" id="ep-name" value="${u.name}"></div>
        <div class="form-group"><label class="form-label">Email</label><input type="email" class="form-input" id="ep-email" value="${u.email}"></div>
        <div class="form-group"><label class="form-label">Téléphone</label><input type="tel" class="form-input" id="ep-phone" value="${u.phone || ''}"></div>
        <div class="form-group"><label class="form-label">Bio</label><textarea class="form-input" id="ep-bio" rows="3" style="resize:none; padding-top:8px" placeholder="Parlez-nous de vous...">${u.bio || ''}</textarea></div>
        <div class="form-group"><label class="form-label">Nouveau Mot de passe</label><input type="password" class="form-input" id="ep-pass" placeholder="Laisser vide pour ne pas modifier"></div>
        <button class="btn btn-primary btn-full" onclick="confirmEditProfile()" style="margin-top:16px">Sauvegarder les modifications</button>`;
    modal.classList.add('open');
};
window.confirmEditProfile = async function () {
    const name = document.getElementById('ep-name').value.trim();
    const email = document.getElementById('ep-email').value.trim();
    const bio = document.getElementById('ep-bio').value.trim();
    const pass = document.getElementById('ep-pass').value.trim();
    const avatar = window._editAvatar;

    if (!name || !email) { Toast.show('Nom et email requis', 'error'); return; }

    try {
        const res = await window.API.updateProfile({ name, email, bio, password: pass || undefined, avatar });
        if (res.success) {
            await window.API.fetchCurrentUser(); // Refresh session
            document.getElementById('book-modal').classList.remove('open');
            Toast.show('Profil mis à jour');
            renderProfilePanel();
            setupSidebar();
        } else {
            Toast.show(res.error || 'Erreur lors de la mise à jour', 'error');
        }
    } catch (e) {
        // Fallback: update locally if server unavailable
        const u = window.API.getCurrentUser();
        if (u) {
            u.name = name; u.email = email;
            localStorage.setItem('cov_active_user', JSON.stringify(u));
        }
        document.getElementById('book-modal').classList.remove('open');
        Toast.show('Profil mis à jour (mode local)');
        renderProfilePanel();
        setupSidebar();
    }
};


// -------------------------------------------------------
// Badge display in profile
// -------------------------------------------------------
window.renderEcoBadges = async function(stats, rating) {
    var section = document.getElementById('eco-badges-section');
    if (!section) return;

    var badgeDefs = [
        { type: 'FIRST_TRIP', icon: ICO.car, name: 'Premier Voyage', desc: 'Votre 1er trajet effectué', check: function(s,r) { return s && s.totalTrips >= 1; } },
        { type: 'ECO_WARRIOR', icon: '🌱', name: 'Eco Warrior', desc: '500 kg CO2 économisés', check: function(s,r) { return s && s.co2Saved >= 500; } },
        { type: 'TOP_RATED', icon: '⭐', name: 'Top Noté', desc: 'Moyenne 4.8+ sur 5 trajets', check: function(s,r) { return r >= 4.8 && s && s.totalTrips >= 5; } },
        { type: 'TRIP_MASTER', icon: '🏆', name: 'Trip Master', desc: '100 trajets au total', check: function(s,r) { return s && s.totalTrips >= 100; } },
        { type: 'MONEY_SAVER', icon: '💰', name: 'Money Saver', desc: '200 TND économisés', check: function(s,r) { return s && s.moneySaved >= 200; } },
        { type: 'FREQUENT_TRAVELER', icon: '🧳', name: 'Grand Voyageur', desc: '10 trajets comme passager', check: function(s,r) { return s && s.totalTrips >= 10; } }
    ];

    var unlocked = [];
    var locked = [];
    badgeDefs.forEach(function(b) {
        if (b.check(stats, rating)) unlocked.push(b);
        else locked.push(b);
    });

    var html = '<div class="section-title" style="margin-bottom:12px">Mes Badges <span style="font-size:11px;color:var(--text-muted);font-weight:500">(' + unlocked.length + '/' + badgeDefs.length + ' obtenus)</span></div>';
    html += '<div class="badges-grid">';
    unlocked.forEach(function(b) {
        html += '<div class="badge-card"><div class="badge-icon">' + b.icon + '</div><div class="badge-name">' + b.name + '</div><div class="badge-desc">' + b.desc + '</div></div>';
    });
    locked.forEach(function(b) {
        html += '<div class="badge-card locked"><div class="badge-icon">' + b.icon + '</div><div class="badge-name">' + b.name + '</div><div class="badge-desc">' + b.desc + '</div></div>';
    });
    html += '</div>';
    section.innerHTML = html;
};

// -------------------------------------------------------
// Booking timeline helper
// -------------------------------------------------------
window.bookingTimelineHTML = function(status) {
    var steps = ['PENDING', 'ACCEPTED', 'COMPLETE'];
    var labels = ['Envoyée', 'Acceptée', 'Terminée'];
    var currentIdx = steps.indexOf(status);
    if (status === 'CANCELLED' || status === 'REJECTED') {
        return '<div style="font-size:10px;color:var(--error);font-weight:700;text-transform:uppercase;margin:8px 0">Annulée / Refusée</div>';
    }
    var html = '<div class="booking-timeline">';
    steps.forEach(function(s, i) {
        var dotClass = i < currentIdx ? 'done' : (i === currentIdx ? 'active' : '');
        var labelClass = i < currentIdx ? 'done' : (i === currentIdx ? 'active' : '');
        html += '<div class="timeline-step"><div class="timeline-dot ' + dotClass + '"></div><div class="timeline-label ' + labelClass + '">' + labels[i] + '</div></div>';
        if (i < steps.length - 1) {
            var lineClass = i < currentIdx ? 'done' : (i === currentIdx ? 'active' : '');
            html += '<div class="timeline-line ' + lineClass + '"></div>';
        }
    });
    html += '</div>';
    return html;
};

// -------------------------------------------------------
// Favorites (localStorage-based)
// -------------------------------------------------------
window.Favorites = {
    _KEY: 'cov_favorites',
    get: function() { try { return JSON.parse(localStorage.getItem(this._KEY)) || []; } catch(e) { return []; } },
    toggle: function(tripId) {
        var favs = this.get();
        var idx = favs.indexOf(tripId);
        if (idx >= 0) favs.splice(idx, 1); else favs.push(tripId);
        localStorage.setItem(this._KEY, JSON.stringify(favs));
        return idx < 0;
    },
    has: function(tripId) { return this.get().indexOf(tripId) >= 0; }
};

// -------------------------------------------------------
// Search autocomplete from recent searches
// -------------------------------------------------------
window.SearchHistory = {
    _KEY: 'cov_search_history',
    add: function(term) {
        if (!term || term.length < 2) return;
        var h = this.get();
        h = h.filter(function(x) { return x !== term; });
        h.unshift(term);
        if (h.length > 10) h = h.slice(0, 10);
        localStorage.setItem(this._KEY, JSON.stringify(h));
    },
    get: function() { try { return JSON.parse(localStorage.getItem(this._KEY)) || []; } catch(e) { return []; } }
};

// Lazy image observer for fade-in
if (window.IntersectionObserver) {
    var lazyObserver = new IntersectionObserver(function(entries) {
        entries.forEach(function(e) {
            if (e.isIntersecting) {
                e.target.classList.add('loaded');
                lazyObserver.unobserve(e.target);
            }
        });
    });
    document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('img[loading="lazy"]').forEach(function(img) {
            lazyObserver.observe(img);
        });
    });
}

window.Router.register("profile", renderProfilePanel);
