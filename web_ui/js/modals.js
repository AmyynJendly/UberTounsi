// ─── BOOKING MODAL ───
window.openBookModal = async function (tripId) {
    const allTrips = await window.API.getTrips();
    const t = allTrips.find(x => x.id === tripId);
    if (!t) return;
    window._bqty = 1;
    window._bPrice = t.price || 0;
    window._bDiscount = 0;
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:20px">Réserver · ${t.departure} → ${t.arrival}</h2>
        <div style="background:var(--bg);border-radius:var(--radius-sm);padding:14px;margin-bottom:16px">
            <div style="display:flex;justify-content:space-between;margin-bottom:8px"><span style="color:var(--text-secondary);font-size:13px">Prix unitaire</span><span style="font-weight:700;font-size:14px">${(t.price || 0).toFixed(1)} TND</span></div>
            <div style="display:flex;justify-content:space-between;align-items:center"><span style="color:var(--text-secondary);font-size:13px">Places</span>
                <div class="stepper"><button class="step-btn" onclick="updateBookQty(-1,${t.availableSeats},${t.price})">−</button><span class="step-val" id="bk-qty">1</span><button class="step-btn" onclick="updateBookQty(1,${t.availableSeats},${t.price})">+</button></div></div>
        </div>
        <div style="display:flex;gap:8px;margin-bottom:6px">
            <input type="text" id="promo-input" class="input" placeholder="Code promo" style="flex:1;height:38px;font-size:13px;text-transform:uppercase" oninput="this.value=this.value.toUpperCase()">
            <button class="btn btn-outline" style="height:38px;padding:0 14px;font-size:13px;white-space:nowrap" onclick="applyPromoCode()">Appliquer</button>
        </div>
        <div id="promo-msg" style="font-size:12px;min-height:16px;margin-bottom:10px"></div>
        <div style="display:flex;justify-content:space-between;font-size:18px;font-weight:800;margin-bottom:20px"><span>Total</span><span id="bk-total">${(t.price || 0).toFixed(1)} TND</span></div>
        <div id="book-error-msg" style="color:var(--error);font-size:13px;text-align:center;min-height:18px;margin-bottom:8px"></div>
        <button class="btn btn-lime btn-full" onclick="confirmBook(${t.id})">Confirmer la réservation</button>`;
    modal.classList.add('open');
};
window.updateBookQty = function (delta, max, price) {
    window._bqty = Math.max(1, Math.min(max, (window._bqty || 1) + delta));
    document.getElementById('bk-qty').textContent = window._bqty;
    const base = price * window._bqty;
    const total = window._bDiscount > 0 ? Math.max(0, base - window._bDiscount * window._bqty) : base;
    document.getElementById('bk-total').textContent = total.toFixed(1) + ' TND';
};
window.applyPromoCode = async function () {
    const code = (document.getElementById('promo-input').value || '').trim();
    const msgEl = document.getElementById('promo-msg');
    if (!code) { msgEl.style.color = 'var(--error)'; msgEl.textContent = 'Entrez un code promo.'; return; }
    const base = window._bPrice * (window._bqty || 1);
    const res = await window.API.validatePromoCode(code, base);
    if (res && res.valid) {
        const saved = base - res.discountedPrice;
        window._bDiscount = saved / (window._bqty || 1);
        document.getElementById('bk-total').textContent = res.discountedPrice.toFixed(1) + ' TND';
        msgEl.style.color = 'var(--lime)';
        msgEl.textContent = 'Code applique - economie de ' + saved.toFixed(1) + ' TND';
    } else {
        window._bDiscount = 0;
        msgEl.style.color = 'var(--error)';
        msgEl.textContent = 'Code invalide ou expire.';
    }
};
window.confirmBook = async function (tripId) {
    const btn = document.querySelector('#book-modal .btn-lime');
    if (btn) { btn.disabled = true; btn.textContent = 'Traitement...'; }
    const result = await window.API.bookTrip(tripId, window._bqty || 1);
    if (result.success) {
        document.getElementById('book-modal').classList.remove('open');
        const db = document.getElementById('detail-bar');
        if (db) db.classList.remove('visible');
        if (window.clearRoute) clearRoute();
        Toast.show('Réservation confirmée !');
        updateMoneyPill();
        Router.navigate('trips');
    } else {
        if (btn) { btn.disabled = false; btn.textContent = 'Confirmer la réservation'; }
        const errEl = document.getElementById('book-error-msg');
        if (errEl) errEl.textContent = result.error || 'Erreur';
    }
};

// ─── FUNDS MODAL ───
window.openFundsModal = async function () {
    const u = window.API.getCurrentUser();
    if (!u) {
        Toast.show("Veuillez vous connecter", 'error');
        return;
    }

    const cards = await window.API.getPaymentMethods();
    if (cards.length === 0) {
        Toast.show("Veuillez d'abord ajouter un moyen de paiement", 'error');
        window.openSelectPaymentMethodModal();
        return;
    }

    const modal = document.getElementById('funds-modal');
    document.getElementById('funds-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:20px">Ajouter des fonds</h2>
        <div class="form-group"><label class="form-label">Montant (TND)</label><input type="number" class="form-input" id="fund-amount" value="50" min="1"></div>
        <div style="display:flex;gap:8px;margin-bottom:20px"><button class="btn btn-outline btn-xs" onclick="document.getElementById('fund-amount').value=20">20</button><button class="btn btn-outline btn-xs" onclick="document.getElementById('fund-amount').value=50">50</button><button class="btn btn-outline btn-xs" onclick="document.getElementById('fund-amount').value=100">100</button><button class="btn btn-outline btn-xs" onclick="document.getElementById('fund-amount').value=200">200</button></div>
        <button class="btn btn-lime btn-full" onclick="confirmFunds()">Confirmer le dépôt</button>`;
    modal.classList.add('open');
};
window.confirmFunds = async function () {
    const amount = parseFloat(document.getElementById('fund-amount').value);
    if (!amount || amount <= 0) { Toast.show('Montant invalide', 'error'); return; }
    const result = await window.API.addFunds(amount);
    document.getElementById('funds-modal').classList.remove('open');
    if (result.success) { Toast.show(`${amount.toFixed(2)} TND ajoutés !`); updateMoneyPill(); renderWalletPanel(); }
    else Toast.show(result.error || 'Erreur', 'error');
};

window.openWithdrawModal = async function () {
    const u = window.API.getCurrentUser();
    const balance = u ? (u.balance || 0) : 0;
    const reserved = u ? (u.reservedBalance || 0) : 0;
    const withdrawable = Math.max(0, balance - reserved);

    const methods = await window.API.getPaymentMethods();
    if (methods.length === 0) {
        Toast.show("Ajoutez un moyen de retrait d'abord", 'error');
        window.openSelectPaymentMethodModal();
        return;
    }

    const modal = document.getElementById('funds-modal');
    document.getElementById('funds-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:8px">Retirer des fonds</h2>
        <p style="font-size:12px; color:var(--text-muted); margin-bottom:20px">Disponible pour retrait : <b>${withdrawable.toFixed(2)} TND</b></p>
        
        <div class="form-group">
            <label class="form-label">Montant à retirer (TND)</label>
            <input type="number" class="form-input" id="withdraw-amount" value="${withdrawable.toFixed(0)}" min="1" max="${withdrawable}">
        </div>
        
        <div style="background:var(--bg); padding:12px; border-radius:var(--radius-sm); border:1px solid var(--border); margin-bottom:20px">
            <div style="font-size:11px; font-weight:800; color:var(--text-muted); text-transform:uppercase; margin-bottom:4px">Virement vers</div>
            <div style="font-size:13px; font-weight:700; color:var(--dark)">${methods[0].brand} •••• ${methods[0].last4}</div>
        </div>

        <button class="btn btn-primary btn-full" onclick="confirmWithdraw()">Confirmer le retrait</button>
    `;
    modal.classList.add('open');
};

window.confirmWithdraw = async function () {
    const amount = parseFloat(document.getElementById('withdraw-amount').value);
    if (!amount || amount <= 0) { Toast.show('Montant invalide', 'error'); return; }
    
    const result = await window.API.withdrawFunds(amount);
    document.getElementById('funds-modal').classList.remove('open');
    
    if (result.success) { 
        Toast.show(`${amount.toFixed(2)} TND retirés vers votre compte !`); 
        updateMoneyPill(); 
        renderWalletPanel(); 
    } else {
        Toast.show(result.error || 'Erreur lors du retrait', 'error');
    }
};

// ─── RATINGS MODAL ───
window.openRatingModal = async function (tripId, targetId, roleLabel = 'utilisateur') {
    const modal = document.getElementById('book-modal');
    window.currentRating = { tripId, ratedId: targetId, score: 5 };

    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:12px">Noter le ${roleLabel}</h2>
        <div style="display:flex; justify-content:center; gap:10px; margin:20px 0" id="star-rating">
            ${[1, 2, 3, 4, 5].map(i => `<span class="star" data-val="${i}" onclick="setRatingScore(${i})" style="font-size:32px; cursor:pointer; color:var(--lime)">★</span>`).join('')}
        </div>
        <div class="form-group">
            <label class="form-label">Commentaire (optionnel)</label>
            <textarea class="form-input" id="rating-comment" placeholder="Comment s'est passé le trajet ?"></textarea>
        </div>
        <button class="btn btn-primary btn-full" onclick="submitSingleRating()">Envoyer la note</button>
    `;
    modal.classList.add('open');
    setRatingScore(5);
};

window.setRatingScore = function (val) {
    window.currentRating = window.currentRating || {};
    window.currentRating.score = val;
    const stars = document.querySelectorAll('#star-rating .star');
    stars.forEach((s, i) => {
        s.style.opacity = (i < val) ? '1' : '0.2';
    });
};

window.submitSingleRating = async function () {
    const comment = document.getElementById('rating-comment').value;
    const res = await window.API.addRating({ ...window.currentRating, comment });
    if (res.success) {
        Toast.show('Merci pour votre avis !');
        document.getElementById('book-modal').classList.remove('open');
        renderBookingsPanel();
    } else Toast.show(res.error, 'error');
};

// ── REPORT MODAL ──
window.openReportModal = function (reportedId) {
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:8px">Signaler un utilisateur</h2>
        <p style="font-size:13px; color:var(--text-muted); margin-bottom:24px">L'administration examinera votre demande sous 24h.</p>
        <div class="form-group">
            <label class="form-label" style="color:#111">Raison du signalement</label>
            <textarea class="form-input" id="rep-reason" placeholder="Comportement inapproprié, retard important, etc..." style="height:100px;padding:12px"></textarea>
        </div>
        <button class="btn btn-danger btn-full" onclick="confirmReport(${reportedId})">Envoyer le signalement</button>`;
    modal.classList.add('open');
};
window.confirmReport = async function (id) {
    const reason = document.getElementById('rep-reason').value.trim();
    if (!reason) return;
    await window.API.submitReport(id, reason);
    document.getElementById('book-modal').classList.remove('open');
    Toast.show('Signalement envoyé');
};

// ── PAYMENT SELECTION MODAL ──
window.openSelectPaymentMethodModal = function() {
    const modal = document.getElementById('book-modal');
    const iconColor = document.body.classList.contains('dark-theme') ? '#ffffff' : '#334155';
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:16px">Moyen de Paiement</h2>
        <div class="payment-options-grid">
            <div class="payment-opt-card" onclick="openAddCardModal()">
                <div class="pay-icon">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="${iconColor}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"/><line x1="1" y1="10" x2="23" y2="10"/></svg>
                </div>
                <div class="pay-label">Carte Bancaire</div>
            </div>
            <div class="payment-opt-card" onclick="openAddD17Modal()">
                <div class="pay-icon">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="${iconColor}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="5" y="2" width="14" height="20" rx="2"/><line x1="12" y1="18" x2="12.01" y2="18"/></svg>
                </div>
                <div class="pay-label">D17</div>
            </div>
            <div class="payment-opt-card" onclick="openAddCardModal('VISA')">
                <div class="pay-icon" style="background:#1a1f71;border-color:#1a1f71">
                    <svg width="40" height="40" viewBox="0 0 60 40" fill="none" xmlns="http://www.w3.org/2000/svg"><text x="30" y="26" text-anchor="middle" font-family="Arial,sans-serif" font-size="18" font-weight="900" font-style="italic" fill="#ffffff" letter-spacing="-0.5">VISA</text></svg>
                </div>
                <div class="pay-label">VISA</div>
            </div>
            <div class="payment-opt-card" onclick="Toast.show('PayPal bientôt disponible !')">
                <div class="pay-icon">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="${iconColor}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><path d="M7.5 21H4L6.5 3h6.5c2.5 0 4.5 1.5 4.5 4 0 4-3 5.5-6 5.5H9L7.5 21z"/><path d="M10 12.5h2c2.5 0 5-1.5 5-5 0-2-1.5-3.5-4-3.5"/></svg>
                </div>
                <div class="pay-label">PayPal</div>
            </div>
            <div class="payment-opt-card" onclick="Toast.show('Virement bientôt disponible !')">
                <div class="pay-icon">
                    <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="${iconColor}" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="7" width="18" height="14" rx="2"/><path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><line x1="12" y1="12" x2="12" y2="16"/><line x1="10" y1="14" x2="14" y2="14"/></svg>
                </div>
                <div class="pay-label">Bank Transfer</div>
            </div>
        </div>
    `;
    modal.classList.add('open');
};

window.openAddD17Modal = function() {
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:8px">Associer D17</h2>
        <p style="font-size:13px; color:var(--text-muted); margin-bottom:20px">Entrez le numéro associé à votre compte D17.</p>
        <div class="form-group">
            <label class="form-label">Numéro de téléphone</label>
            <input type="tel" class="form-input" id="d17-phone" placeholder="22 333 444">
        </div>
        <button class="btn btn-lime btn-full" onclick="confirmAddD17()">Vérifier et Associer</button>
    `;
};

window.confirmAddD17 = async function() {
    const phone = document.getElementById('d17-phone').value.trim();
    if (phone.length < 8) return Toast.show('Numéro de téléphone invalide', 'error');
    
    const res = await window.API.addPaymentMethod({
        type: 'D17',
        last4: phone.slice(-4),
        brand: 'D17',
        exp: 'N/A'
    });
    
    if (res.success) {
        document.getElementById('book-modal').classList.remove('open');
        Toast.show('Compte D17 associé !');
        renderWalletPanel();
    } else {
        Toast.show(res.error || 'Erreur lors de l\'ajout', 'error');
    }
};

window.openAddCardModal = function(brandHint = 'Card') {
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:8px">Ajouter une ${brandHint}</h2>
        <p style="font-size:13px; color:var(--text-muted); margin-bottom:20px">Paiement sécurisé via CovoitDark SSL.</p>
        <div class="form-group">
            <label class="form-label">Numéro de carte</label>
            <input type="text" class="form-input" id="card-number" placeholder="4000 0000 0000 0000">
        </div>
        <div style="display:flex; gap:12px">
            <div class="form-group" style="flex:1"><label class="form-label">Expiration</label><input type="text" class="form-input" id="card-exp" placeholder="MM/YY"></div>
            <div class="form-group" style="flex:1"><label class="form-label">CVC</label><input type="text" class="form-input" id="card-cvc" placeholder="•••"></div>
        </div>
        <button class="btn btn-lime btn-full" onclick="confirmAddCard()">Valider la carte</button>
    `;
};

window.confirmAddCard = async function () {
    const rawNum = (document.getElementById('card-number').value || '').replace(/\s/g, '');
    const exp = document.getElementById('card-exp').value;
    const cvc = document.getElementById('card-cvc').value;

    const isLuhnValid = (num) => {
        let sum = 0; let alt = false;
        for (let i = num.length - 1; i >= 0; i--) {
            let n = parseInt(num[i]);
            if (alt) { n *= 2; if (n > 9) n = (n % 10) + 1; }
            sum += n; alt = !alt;
        }
        return (sum % 10 === 0);
    };

    if (rawNum.length < 13 || !isLuhnValid(rawNum)) {
        Toast.show('Numéro de carte invalide (Luhn check failed)', 'error');
        return;
    }
    if (!/^\d{2}\/\d{2}$/.test(exp)) {
        Toast.show('Format expiration invalide (MM/YY)', 'error');
        return;
    }
    if (cvc.length < 3) {
        Toast.show('CVC invalide', 'error');
        return;
    }

    const brand = rawNum.startsWith('4') ? 'Visa' : rawNum.startsWith('5') ? 'Mastercard' : 'Card';

    const res = await window.API.addPaymentMethod({
        type: 'Card',
        last4: rawNum.slice(-4),
        brand: brand,
        exp: exp
    });

    if (res.success) {
        document.getElementById('book-modal').classList.remove('open');
        Toast.show('Carte validée et ajoutée !');
        renderWalletPanel();
    } else {
        Toast.show(res.error || 'Erreur', 'error');
    }
};

// ── SAVED SEARCH / ALERTS ──
window.openAlertModal = function () {
    const dep = document.getElementById('s-city-dep').value || document.getElementById('s-gov-dep').value;
    const arr = document.getElementById('s-city-arr').value || document.getElementById('s-gov-arr').value;
    if (!dep || !arr) { Toast.show("Remplissez l'itinéraire d'abord", 'error'); return; }
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:8px">Créer une alerte route</h2>
        <p style="font-size:13px; color:var(--text-muted); margin-bottom:20px">Vous recevrez une notification dès qu'un trajet pour <b>${dep} → ${arr}</b> est publié.</p>
        <button class="btn btn-lime btn-full" onclick="confirmAlert('${dep}', '${arr}')">Activer l'alerte</button>`;
    modal.classList.add('open');
};
window.confirmAlert = async function (dep, arr) {
    await window.API.saveSearch({ dep, arr });
    document.getElementById('book-modal').classList.remove('open');
    Toast.show('Alerte créée !');
    renderSearchAlerts();
};
window.deleteAlert = async function (id) {
    if (confirm("Supprimer cette alerte ?")) {
        await window.API.deleteSavedSearch(id);
        renderSearchAlerts();
    }
};

// --- PROFILE MODAL ---
window.openEditProfileModal = function () {
    const u = window.API.getCurrentUser();
    window._editAvatar = u.avatar;
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <h2 style="font-size:18px;font-weight:800;margin-bottom:20px">${t('edit_profile')}</h2>
        <div style="display:flex; justify-content:center; margin-bottom:24px; position:relative">
            <div style="width:100px; height:100px; border-radius:50%; overflow:hidden; border:3px solid var(--border); background:var(--bg)">
                <img id="edit-avatar-preview" src="${u.avatar || 'https://ui-avatars.com/api/?name=' + u.name.replace(' ', '+')}" style="width:100%; height:100%; object-fit:cover">
            </div>
            <button class="btn btn-icon" onclick="document.getElementById('edit-avatar-picker').click()" style="position:absolute; bottom:0; right:calc(50% - 50px); width:32px; height:32px; border-radius:50%; background:var(--white); color:var(--dark); border:1px solid var(--border); box-shadow:var(--shadow-sm)">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z"/><circle cx="12" cy="13" r="4"/></svg>
            </button>
            <input type="file" id="edit-avatar-picker" hidden accept="image/*" onchange="window.handleFileSelect(event, b64 => { document.getElementById('edit-avatar-preview').src = b64; window._editAvatar = b64; })">
        </div>
        <div class="form-group"><label class="form-label">${t('name')}</label><input type="text" class="form-input" id="edit-name" value="${u.name}"></div>
        <div class="form-group"><label class="form-label">${t('email') || 'Email'}</label><input type="email" class="form-input" id="edit-email" value="${u.email}"></div>
        <div class="form-group"><label class="form-label">${t('phone')}</label><input type="tel" class="form-input" id="edit-phone" value="${u.phone || ''}"></div>
        <div class="form-group"><label class="form-label">${t('bio')}</label><textarea class="form-input" id="edit-bio" style="height:80px; padding:10px">${u.bio || ''}</textarea></div>
        <div class="form-group"><label class="form-label">${t('change_pass')} (Optionnel)</label><input type="password" class="form-input" id="edit-password" placeholder="••••••••"></div>
        <button class="btn btn-primary btn-full" onclick="confirmProfileUpdate()">${t('save')}</button>`;
    modal.classList.add('open');
};

window.confirmProfileUpdate = async function () {
    const name = document.getElementById('edit-name').value.trim();
    const email = document.getElementById('edit-email').value.trim();
    const phone = document.getElementById('edit-phone').value.trim();
    const password = document.getElementById('edit-password').value;
    const bio = document.getElementById('edit-bio').value.trim();
    const avatar = window._editAvatar;

    if (!name || !email) return Toast.show('Nom et email requis', 'error');

    const res = await window.API.updateProfile({ name, email, phone, bio, password: password || undefined, avatar });
    if (res.success) {
        Toast.show('Profil mis à jour !');
        document.getElementById('book-modal').classList.remove('open');
        await window.API.fetchCurrentUser();
        location.reload(); 
    } else Toast.show(res.error, 'error');
};

// ── REVIEWS MODAL ────────────────────────────────────────────────────────────
window.openReviewsModal = async function (driverId, driverName, avgRating) {
    const modal = document.getElementById('book-modal');
    document.getElementById('book-modal-body').innerHTML = `
        <div style="text-align:center;margin-bottom:20px">
            <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(driverName)}&background=1a1a19&color=c3f832&size=80" style="width:64px;height:64px;border-radius:50%;margin-bottom:10px">
            <h2 style="font-size:18px;font-weight:800">${driverName}</h2>
            <div style="color:var(--warning);font-size:22px;font-weight:900;margin:4px 0">&#9733; ${Number(avgRating||5).toFixed(1)}</div>
            <div style="font-size:12px;color:var(--text-muted)">Note moyenne</div>
        </div>
        <div id="reviews-list" style="max-height:340px;overflow-y:auto"><div style="text-align:center;padding:24px;color:var(--text-muted)">Chargement...</div></div>`;
    modal.classList.add('open');
    const ratings = await window.API.getRatings(driverId);
    const list = document.getElementById('reviews-list');
    if (!list) return;
    if (!Array.isArray(ratings) || ratings.length === 0) {
        list.innerHTML = `<div style="text-align:center;padding:32px;color:var(--text-muted)"><div style="font-size:32px;margin-bottom:8px">&#128172;</div><p>Aucun avis pour l'instant.</p></div>`;
        return;
    }
    list.innerHTML = ratings.map(r => {
        const starsHtml = '&#9733;'.repeat(r.score) + '&#9734;'.repeat(5 - r.score);
        const date = r.date ? new Date(r.date).toLocaleDateString('fr-FR',{day:'numeric',month:'short',year:'numeric'}) : '';
        return `<div style="background:var(--bg);border:1px solid var(--border);border-radius:var(--radius-sm);padding:14px;margin-bottom:10px">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:6px">
                <div style="display:flex;align-items:center;gap:8px">
                    <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(r.raterName||'A')}&background=random&size=32" style="width:28px;height:28px;border-radius:50%">
                    <span style="font-size:13px;font-weight:700">${r.raterName || 'Anonyme'}</span>
                </div>
                <div style="display:flex;align-items:center;gap:6px">
                    <span style="color:var(--warning);font-size:13px;font-weight:800">${starsHtml}</span>
                    <span style="font-size:11px;color:var(--text-muted)">${date}</span>
                </div>
            </div>
            ${r.comment ? `<p style="font-size:12px;color:var(--text-secondary);line-height:1.5;font-style:italic">"${r.comment}"</p>` : ''}
        </div>`;
    }).join('');
};

// ── ECO BADGES ───────────────────────────────────────────────────────────────
window.renderEcoBadges = function(stats, rating) {
    const el = document.getElementById('eco-badges-section');
    if (!el) return;
    const trips = stats?.totalTrips || 0;
    const co2 = stats?.co2Saved || 0;
    const money = stats?.moneySaved || 0;
    const ALL_BADGES = [
        { icon: '&#128664;', label: 'Premier Trajet',        desc: 'Votre 1er trajet partag\u00e9',  earned: trips >= 1 },
        { icon: '&#127807;', label: '\u00c9co-Voyageur',          desc: '5+ trajets partag\u00e9s',        earned: trips >= 5 },
        { icon: '&#127795;', label: 'Gardien de la Plan\u00e8te', desc: '10+ trajets partag\u00e9s',       earned: trips >= 10 },
        { icon: '&#128168;', label: 'Sauveur de CO\u2082',        desc: '10 kg de CO\u2082 \u00e9conomis\u00e9s', earned: co2 >= 10 },
        { icon: '&#127758;', label: 'H\u00e9ros Climatique',      desc: '50 kg de CO\u2082 \u00e9conomis\u00e9s', earned: co2 >= 50 },
        { icon: '&#11088;',  label: '\u00c9toile d\'Or',          desc: 'Note moyenne \u2265 4.5',         earned: rating >= 4.5 },
        { icon: '&#127942;', label: 'Parfait',                desc: 'Note parfaite de 5.0',       earned: rating >= 5.0 },
        { icon: '&#128176;', label: '\u00c9conomiseur',           desc: '100 TND \u00e9conomis\u00e9s',       earned: money >= 100 },
    ];
    const earned  = ALL_BADGES.filter(b => b.earned);
    const pending = ALL_BADGES.filter(b => !b.earned).slice(0, 3);
    el.innerHTML = `
        <div class="section-title" style="margin-bottom:12px">&#127881; Mes Badges ${earned.length > 0 ? `<span style="background:var(--lime);color:var(--dark);font-size:10px;font-weight:800;padding:2px 8px;border-radius:20px;margin-left:6px">${earned.length}</span>` : ''}</div>
        ${earned.length === 0
            ? `<div style="text-align:center;padding:16px;color:var(--text-muted);font-size:13px;background:var(--bg);border-radius:var(--radius-sm);border:1px dashed var(--border)">Effectuez votre premier trajet pour d\u00e9bloquer vos badges&nbsp;!</div>`
            : `<div style="display:grid;grid-template-columns:repeat(2,1fr);gap:8px;margin-bottom:12px">
                ${earned.map(b => `<div style="background:linear-gradient(135deg,var(--dark),#2d2d2b);border:1px solid var(--lime);border-radius:var(--radius-sm);padding:10px;display:flex;align-items:center;gap:10px">
                    <span style="font-size:22px">${b.icon}</span>
                    <div><div style="font-size:11px;font-weight:800;color:var(--lime)">${b.label}</div><div style="font-size:10px;color:#999">${b.desc}</div></div>
                </div>`).join('')}
               </div>`}
        ${pending.length > 0 ? `<div style="font-size:10px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:.05em;margin-bottom:8px">\u00c0 d\u00e9bloquer</div>
        <div style="display:flex;flex-direction:column;gap:6px">
            ${pending.map(b => `<div style="background:var(--bg);border:1px dashed var(--border);border-radius:var(--radius-sm);padding:8px 12px;display:flex;align-items:center;gap:10px;opacity:0.55">
                <span style="font-size:18px;filter:grayscale(1)">${b.icon}</span>
                <div><div style="font-size:11px;font-weight:700">${b.label}</div><div style="font-size:10px;color:var(--text-muted)">${b.desc}</div></div>
                <span style="margin-left:auto;font-size:10px">&#128274;</span>
            </div>`).join('')}
        </div>` : ''}`;
}

// ── COLOR HEX HELPER ─────────────────────────────────────────────────────────
window.getColorHex = function(n) {
    return {'Noir':'#1a1a1a','Blanc':'#f5f5f5','Gris':'#9ca3af','Gris M\u00e9tallique':'#6b7280',
        'Blanc Nacr\u00e9':'#f0f0e8','Rouge':'#ef4444','Rouge Passion':'#dc2626',
        'Bleu':'#3b82f6','Bleu Saphir':'#1d4ed8','Vert':'#22c55e','Jaune':'#eab308','Orange':'#f97316'}[n] || '#9ca3af';
};
