async function renderBookingsPanel(tab = window._bookingTab) {
    window._bookingTab = tab;
    const u = window.API.getCurrentUser();

    // Fetch data from real API
    let bks = [];
    let myTrips = [];
    let trips = [];
    let users = [];
    try {
        const resB = await fetch(`${window.API.API_BASE}/api/bookings/my`);
        const jsonB = await resB.json();
        bks = Array.isArray(jsonB) ? jsonB : [];

        const resT = await fetch(`${window.API.API_BASE}/api/trips/my`);
        const jsonT = await resT.json();
        myTrips = Array.isArray(jsonT) ? jsonT : [];

        trips = await window.API.fetchTrips() || [];
        const uRes = await window.API.getUsersList();
        users = Array.isArray(uRes) ? uRes : [];
    } catch (e) { console.error("API Error", e); }

    const sm = { PENDING: 'status-pending', ACCEPTED: 'status-accepted', REFUSED: 'status-refused', COMPLETE: 'status-completed', CANCELLED: 'status-cancelled', NEGOTIATING: 'status-pending' };

    const myBookings = bks; // backend only returns my bookings
    const allRequests = await window.API.getDriverRequests() || [];
    const requests = allRequests.filter(r => r.status === 'PENDING' || r.status === 'NEGOTIATING');

    const tabsHtml = u.role !== 'PASSENGER' ? `
        <div class="tabs" style="margin-top:10px; padding-bottom:8px">
            <button class="tab ${tab === 'passager' ? 'active' : ''}" onclick="renderBookingsPanel('passager')">Passager</button>
            <button class="tab ${tab === 'conducteur' ? 'active' : ''}" onclick="renderBookingsPanel('conducteur')">Conducteur</button>
            <button class="tab ${tab === 'demandes' ? 'active' : ''}" onclick="renderBookingsPanel('demandes')">Demandes ${requests.length > 0 ? '<span class="sidebar-badge" style="position:relative;display:inline-block;top:auto;right:auto;margin-left:4px"></span>' : ''}</button>
        </div>` : '';

    let bodyHtml = '';
    if (tab === 'passager') {
        const hasArchived = myBookings.some(booking => booking.status === 'CANCELLED' || booking.status === 'REJECTED');
        const deleteAllBtn = hasArchived ? `<button class="btn btn-outline btn-xs btn-full" style="margin-bottom:12px; color:var(--text-muted); border-color:var(--border)" onclick="deleteAllArchivedBookingsBtn()">${ICO.trash} Tout effacer l'historique (Annulés/Refusés)</button>` : '';

        bodyHtml = myBookings.length === 0 ? '<div class="empty-state"><h3>Aucune réservation</h3><p>Réservez un trajet pour commencer</p></div>'
            : deleteAllBtn + myBookings.map(booking => {
                const trip = trips.find(x => x.id === booking.tripId) || {};
                const isRated = (DB.get('cov_rated_users') || []).includes(`${booking.tripId}_${u.id}_${trip.driverId || 'unknown'}`);
                return `<div class="booking-card">
                    <div style="display:flex;justify-content:space-between;align-items:center">
                        <span class="booking-route">${booking.departure || trip.departure || '?'} → ${booking.arrival || trip.arrival || '?'}</span>
                        <span class="status ${sm[booking.status] || ''}">${booking.status}</span>
                    </div>
                    ${booking.driverName ? `
                        <div style="display:flex;align-items:center;gap:8px;margin-top:8px">
                            <img src="${booking.driverAvatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(booking.driverName)}" style="width:22px;height:22px;border-radius:50%;object-fit:cover">
                            <span style="font-size:12px;color:var(--text-muted)">Chauffeur : <b style="color:var(--text)">${esc(booking.driverName)}</b></span>
                        </div>
                    ` : ''}
                    ${window.bookingTimelineHTML ? window.bookingTimelineHTML(booking.status) : ''}
                    <div class="booking-detail">${booking.qty || 1} ${window.t('seats')} · ${(booking.totalCost || 0).toFixed(1)} TND · ${new Date(booking.date || Date.now()).toLocaleDateString('fr-FR')}</div>
                    ${booking.status === 'ACCEPTED' ? `
                        <div style="margin-top:10px; padding:10px; text-align:center; border:1px dashed rgba(132, 204, 22, 0.4); border-radius:8px; background:rgba(132, 204, 22, 0.05)">
                            <div style="font-size:11px; text-transform:uppercase; color:var(--lime); letter-spacing:0.5px; margin-bottom:4px">Code Voyageur</div>
                            <div style="font-size:28px; font-weight:900; letter-spacing:6px; font-family:monospace">${booking.secretCode || '------'}</div>
                            <div style="font-size:10px; opacity:0.7; margin-top:4px">À donner au conducteur à la montée</div>
                        </div>
                    ` : ''}
                    ${booking.status === 'NEGOTIATING' ? `
                        <div style="margin-top:10px; padding:10px; border:1px solid rgba(245,158,11,0.4); border-radius:8px; background:rgba(245,158,11,0.05)">
                            <div style="font-size:11px; font-weight:700; color:var(--warning); margin-bottom:6px"><span class="icon-inline">${ICO.chat}</span> Contre-offre envoyée : ${(booking.counterPrice||0).toFixed(2)} TND</div>
                            <div style="font-size:12px; color:var(--text-muted)">En attente de réponse du chauffeur…</div>
                        </div>
                    ` : ''}
                    ${booking.status === 'PENDING' ? `
                        <button class="btn btn-outline btn-xs btn-full" style="margin-top:8px; border-color:rgba(245,158,11,0.4); color:var(--warning)" onclick="openCounterOfferModal(${booking.id}, ${booking.totalCost || 0})"><span class="icon-inline">${ICO.chat}</span> Proposer un autre prix</button>
                    ` : ''}
                    ${booking.status === 'COMPLETE' && !isRated ? `
                        <button class="btn btn-lime btn-xs btn-full" style="margin-top:10px; height:34px" onclick="openRatingModal(${booking.tripId}, ${trip.driverId}, 'conducteur')"> Laisser une note au conducteur</button>
                    ` : ''}
                    ${booking.status === 'ACCEPTED' ? `
                        <button class="btn btn-outline btn-xs btn-full" style="margin-top:10px" onclick="openTripChatModal(${booking.tripId}, '${(booking.departure||'').replace(/'/g,"\\'")} → ${(booking.arrival||'').replace(/'/g,"\\'")}')">${ICO.chat} Chat du trajet</button>
                        <button class="btn btn-outline btn-xs btn-full" style="margin-top:6px; color:var(--error); border-color:rgba(239,68,68,0.2)" onclick="cancelBookingReq(${booking.id})">${ICO.x} Annuler ma place</button>
                    ` : ''}
                    ${(booking.status === 'CANCELLED' || booking.status === 'REJECTED') ? `<button class="btn btn-outline btn-xs btn-full" style="margin-top:10px; color:var(--text-muted); border-color:var(--border)" onclick="deleteBookingBtn(${booking.id})">${ICO.trash} Supprimer l'historique</button>` : ''}
                </div>`;
            }).join('');
    } else if (tab === 'conducteur') {
        bodyHtml = myTrips.length === 0 ? '<div class="empty-state"><h3>Aucun trajet</h3><p>Publiez un trajet pour le voir ici</p></div>'
            : myTrips.map(trip => {
                const tripBks = allRequests.filter(booking => booking.tripId === trip.id && (booking.status === 'ACCEPTED' || booking.status === 'COMPLETE' || (trip.status === 'COMPLETE' && booking.status === 'PENDING')));
                return `<div class="booking-card">
                    <div style="display:flex;justify-content:space-between;align-items:center">
                        <span class="booking-route">${trip.departure} → ${trip.arrival}</span>
                        <span class="status ${trip.status === 'ACTIVE' ? 'status-available' : trip.status === 'COMPLETE' ? 'status-completed' : 'status-cancelled'}">${trip.status}</span>
                    </div>
                    <div class="booking-detail">${trip.price.toFixed(1)} TND/${window.t('seats').toLowerCase()} · ${new Date(trip.startDate).toLocaleDateString('fr-FR')}</div>
                    
                    ${tripBks.length > 0 ? `
                        <div style="margin-top:12px; border-top:1px solid var(--border-light); padding-top:10px">
                            <div style="font-size:11px; font-weight:700; color:var(--text-muted); text-transform:uppercase; margin-bottom:8px">Passagers</div>
                            ${tripBks.map(booking => {
                    const pass = users.find(x => x.id === booking.userId);
                    const isRated = (DB.get('cov_rated_users') || []).includes(`${trip.id}_${u.id}_${booking.userId}`);
                    return `<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:6px">
                                    <div style="display:flex; align-items:center; gap:8px">
                                        <img src="${pass?.avatar || 'https://ui-avatars.com/api/?name=' + (pass?.name || 'User').replace(' ', '+')}" style="width:24px; height:24px; border-radius:50%">
                                        <span style="font-size:13px; font-weight:600">${pass?.name}</span>
                                    </div>
                                    <div style="display:flex; gap:4px">
                                        ${booking.status === 'ACCEPTED' ? `
                                            <button class="btn btn-lime btn-xs" style="height:26px; padding:0 8px" onclick="openVerifyCodeModal(${booking.id}, '${pass?.name?.replace(/'/g, "\\'")}')">Saisir le Code</button>
                                        ` : booking.status === 'COMPLETE' ? `
                                            <span style="font-size:11px; color:var(--lime); font-weight:800; border:1px solid var(--lime); padding:2px 6px; border-radius:4px; display:flex; align-items:center">${ICO.check} ARRIVÉ</span>
                                        ` : ''}
                                        ${(trip.status === 'COMPLETE' || booking.status === 'COMPLETE') && !isRated ? `
                                            <button class="btn btn-outline btn-xs" style="height:26px; padding:0 8px; border-color:var(--border)" onclick="openRatingModal(${trip.id}, ${booking.userId}, 'passager')">${ICO.star} Noter</button>
                                        ` : isRated ? '<span style="font-size:11px; color:var(--success); align-self:center">Déjà noté</span>' : ''}
                                    </div>
                                </div>`;
                }).join('')}
                        </div>
                    ` : ''}

                    <div style="display:flex;gap:8px;margin-top:10px;align-items:center">
                        ${trip.status === 'ACTIVE' ? `<button class="btn btn-lime btn-xs" style="flex:1" onclick="completeTripReq(${trip.id})">${ICO.check} ${window.t('complete')}</button>` : ''}
                        ${trip.status === "COMPLETE" ? `<div style="font-size:11px; font-weight:800; color:var(--success); flex:1; text-transform:uppercase; letter-spacing:0.5px">${ICO.check} Trajet Terminé</div>` : ""}
                        <button class="btn btn-outline btn-xs" style="padding:0 10px" title="Chat du trajet" onclick="openTripChatModal(${trip.id}, '${(trip.departure||'').replace(/'/g,"\\'")} → ${(trip.arrival||'').replace(/'/g,"\\'")}')">${ICO.chat}</button>
                        ${trip.status === 'ACTIVE' ? `<button class="btn btn-danger btn-xs" style="width:40px;padding:0;background:rgba(239,68,68,0.1);color:var(--error)" onclick="cancelTripReq(${trip.id})">${ICO.trash}</button>` : ''}
                    </div>
                </div>`;
            }).join('');
    } else if (tab === 'demandes') {
        bodyHtml = requests.length === 0 ? '<div class="empty-state"><h3>Aucune demande en attente</h3></div>'
            : requests.map(b => {
                const trip = trips.find(x => x.id === b.tripId) || {};
                const pass = users.find(x => x.id === b.userId);
                const isNegotiating = b.status === 'NEGOTIATING';
                return `<div class="booking-card" style="border-color:${isNegotiating ? 'var(--warning)' : 'var(--lime)'}">
                    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
                        <span class="booking-route">${trip.departure} → ${trip.arrival}</span>
                        <span class="status ${isNegotiating ? 'status-pending' : 'status-pending'}">${isNegotiating ? 'Négo.' : 'Nouveau'}</span>
                    </div>
                    <div style="display:flex;align-items:center;gap:10px;margin-bottom:12px">
                        <img src="https://ui-avatars.com/api/?name=${(b.userName || 'User').replace(' ', '+')}" style="width:30px;height:30px;border-radius:50%">
                        <div style="font-size:13px;font-weight:600">${b.userName || 'Passager'}
                            <span style="font-size:11px;color:var(--text-muted);font-weight:500;margin-left:4px">demande ${b.qty} place(s)</span>
                            ${b.luggageSize && b.luggageSize !== 'NONE' ? `<span style="font-size:10px;color:var(--info);margin-left:6px;font-weight:700">${ICO.luggage} ${b.luggageSize}</span>` : ''}
                        </div>
                    </div>
                    ${isNegotiating ? `
                        <div style="padding:8px 10px; background:rgba(245,158,11,0.08); border:1px solid rgba(245,158,11,0.3); border-radius:8px; margin-bottom:10px; font-size:13px">
                            Contre-offre : <b style="color:var(--warning)">${(b.counterPrice||0).toFixed(2)} TND</b>
                            <span style="font-size:11px;color:var(--text-muted);margin-left:6px">(prix affiché : ${(b.totalCost||0).toFixed(2)} TND)</span>
                        </div>
                        <div style="display:flex;gap:8px">
                            <button class="btn btn-lime btn-xs" style="flex:1" onclick="acceptOfferReq(${b.id})"><span class="icon-inline">${ICO.check}</span> Accepter l'offre</button>
                            <button class="btn btn-danger btn-xs" style="flex:1;background:rgba(239,68,68,0.1);color:var(--error)" onclick="rejectOfferReq(${b.id})"><span class="icon-inline">${ICO.x}</span> Refuser</button>
                        </div>
                    ` : `
                        <div style="display:flex;gap:8px">
                            <button class="btn btn-lime btn-xs" style="flex:1" onclick="acceptBookingReq(${b.id})">${window.t('accept')}</button>
                            <button class="btn btn-danger btn-xs" style="flex:1;background:rgba(239,68,68,0.1);color:var(--error)" onclick="refuseBookingReq(${b.id})">${window.t('refuse')}</button>
                        </div>
                    `}
                </div>`;
            }).join('');
    }

    document.getElementById('view-bookings').innerHTML = `
        <div class="panel-header">
            <h1 class="panel-title">${tab === 'demandes' ? 'Demandes en attente' : tab === 'conducteur' ? 'Mes Trajets' : t('nav_bks')}</h1>
            ${tabsHtml}
        </div>
        <div class="panel-body" style="padding-top:16px">${bodyHtml}</div>`;
}

window.acceptBookingReq = async function (id) {
    const res = await window.API.acceptBooking(id);
    if (res.success) { Toast.show('Demande acceptée'); renderBookingsPanel('demandes'); }
};
window.refuseBookingReq = async function (id) {
    const res = await window.API.refuseBooking(id);
    if (res.success) { Toast.show('Demande refusée'); renderBookingsPanel('demandes'); }
};
window.completeTripReq = function (id) {
    showConfirm({
        icon: ICO.check,
        title: 'Terminer ce trajet ?',
        body: 'Confirmez que le trajet est bien terminé. Vous pourrez ensuite noter vos passagers.',
        okLabel: 'Oui, terminer',
        okClass: 'btn-lime',
        onOk: async function () {
            const res = await window.API.completeTrip(id);
            if (res.success) {
                Toast.show('Trajet terminé !');
                renderBookingsPanel('conducteur');
                const passengers = await window.API.getAcceptedPassengers(id);
                if (passengers && passengers.length > 0) {
                    openRatingSeriesModal(id, passengers);
                }
            } else {
                Toast.show(res.error || 'Erreur lors de la clôture', 'error');
            }
        }
    });
};

window.openVerifyCodeModal = function(requestId, passName) {
    const modal = document.getElementById('verify-modal');
    document.getElementById('verify-modal-body').innerHTML = `
        <h2 style="font-size:20px; font-weight:800; margin-bottom:12px; text-align:center">Vérification de ${passName}</h2>
        <p style="font-size:14px; color:var(--text-muted); text-align:center; margin-bottom:24px">Demandez au passager le code PIN à 4 caractères affiché sur son application lors de la montée du véhicule.</p>
        
        <div style="display:flex; justify-content:center; gap:8px; margin-bottom:24px">
            <input type="text" id="verify-code-input" maxlength="4" style="text-align:center; font-size:32px; font-weight:900; letter-spacing:8px; width:100%; text-transform:uppercase" class="input" placeholder="W9X2" autocomplete="off">
        </div>
        
        <button id="verify-submit-btn" class="btn btn-lime btn-full" style="height:50px; font-size:16px" onclick="submitVerifyCode(${requestId}, '${passName}')">Valider et récupérer les fonds</button>
    `;
    modal.classList.add('open');
    setTimeout(() => document.getElementById('verify-code-input').focus(), 100);
};

window.submitVerifyCode = async function(requestId, passName) {
    const codeInput = document.getElementById('verify-code-input');
    const code = codeInput.value.trim().toUpperCase();
    
    if (code.length !== 4) {
        Toast.show("Le code doit faire exactement 4 caractères.", "error");
        return;
    }
    
    document.getElementById('verify-submit-btn').innerHTML = 'Vérification...';
    document.getElementById('verify-submit-btn').disabled = true;
    
    const res = await window.API.verifyPassengerCode(requestId, code);
    
    if (res.success) {
        document.getElementById('verify-modal').classList.remove('open');
        Toast.show(`✓ Code correct ! Les fonds de ${passName} sont débloqués.`);
        renderBookingsPanel('conducteur');
        updateMoneyPill();
    } else {
        Toast.show(res.error || "Code incorrect.", "error");
        document.getElementById('verify-submit-btn').innerHTML = 'Valider et récupérer les fonds';
        document.getElementById('verify-submit-btn').disabled = false;
        codeInput.value = '';
        codeInput.focus();
    }
};

window.cancelTripReq = function (id) {
    showConfirm({
        icon: ICO.ban,
        title: 'Annuler ce trajet ?',
        body: 'Tous les passagers acceptés seront remboursés intégralement et notifiés.',
        okLabel: 'Oui, annuler le trajet',
        okClass: 'btn-danger',
        onOk: async function () {
            const res = await window.API.cancelTrip(id);
            Toast.show(res.message || 'Trajet annulé');
            renderBookingsPanel('conducteur');
        }
    });
};

window.cancelBookingReq = function (id) {
    showConfirm({
        icon: ICO.x,
        title: 'Annuler votre réservation ?',
        body: 'Un remboursement partiel (50%) s\'applique si moins de 24h avant le départ.',
        okLabel: 'Confirmer l\'annulation',
        okClass: 'btn-danger',
        onOk: async function () {
            const res = await window.API.cancelBooking(id);
            if (res.success) {
                Toast.show(res.message || 'Réservation annulée');
                renderBookingsPanel('passager');
                updateMoneyPill();
            } else Toast.show(res.error, 'error');
        }
    });
};

// --- WALLET (with card) ---

// ── Feature 8: Negotiation ───────────────────────────────────────────────────
window.openCounterOfferModal = function (requestId, currentPrice) {
    const html = `
        <div style="padding:24px">
            <h2 style="font-size:18px;font-weight:800;margin-bottom:8px"><span class="icon-inline">${ICO.chat}</span> Proposer un prix</h2>
            <p style="font-size:13px;color:var(--text-muted);margin-bottom:16px">Prix affiché : <b>${Number(currentPrice).toFixed(2)} TND</b>. Votre offre doit être inférieure.</p>
            <input type="number" id="counter-price-input" class="input" style="margin-bottom:16px" placeholder="Votre prix (TND)" step="0.5" min="0.5">
            <button class="btn btn-lime btn-full" onclick="submitCounterOffer(${requestId})">Envoyer l'offre</button>
        </div>`;
    const modal = document.getElementById('generic-modal');
    if (modal) { document.getElementById('generic-modal-body').innerHTML = html; modal.classList.add('open'); }
    else { Toast.show('Modal non disponible', 'error'); }
};

window.submitCounterOffer = async function (requestId) {
    const price = parseFloat(document.getElementById('counter-price-input').value);
    if (!price || price <= 0) { Toast.show('Prix invalide', 'error'); return; }
    const res = await window.API.proposeCounterOffer(requestId, price);
    document.getElementById('generic-modal')?.classList.remove('open');
    Toast.show(res.message || res.error || 'Envoyé', res.error ? 'error' : 'success');
    renderBookingsPanel('passager');
};

window.acceptOfferReq = async function (requestId) {
    const res = await window.API.acceptCounterOffer(requestId);
    Toast.show(res.message || res.error || 'Traité', res.error ? 'error' : 'success');
    renderBookingsPanel('demandes');
    updateMoneyPill && updateMoneyPill();
};

window.rejectOfferReq = async function (requestId) {
    const res = await window.API.rejectCounterOffer(requestId);
    Toast.show(res.message || res.error || 'Traité', res.error ? 'error' : 'success');
    renderBookingsPanel('demandes');
};

// ── Feature 13: Dispute filing ───────────────────────────────────────────────
window.openDisputeModal = function (tripId) {
    const html = `
        <div style="padding:24px">
            <h2 style="font-size:18px;font-weight:800;margin-bottom:8px">${ICO.warning} Signaler un problème</h2>
            <p style="font-size:13px;color:var(--text-muted);margin-bottom:16px">Décrivez le problème rencontré. L'équipe traitera votre réclamation sous 48h.</p>
            <textarea id="dispute-reason-input" class="input" rows="4" style="resize:vertical;margin-bottom:16px" placeholder="Ex: paiement capturé mais trajet non effectué…"></textarea>
            <button class="btn btn-lime btn-full" onclick="submitDispute(${tripId})">Envoyer la réclamation</button>
        </div>`;
    const modal = document.getElementById('generic-modal');
    if (modal) { document.getElementById('generic-modal-body').innerHTML = html; modal.classList.add('open'); }
};

window.submitDispute = async function (tripId) {
    const reason = document.getElementById('dispute-reason-input').value.trim();
    if (!reason) { Toast.show('Motif requis', 'error'); return; }
    const res = await window.API.fileDispute(tripId, reason);
    document.getElementById('generic-modal')?.classList.remove('open');
    Toast.show(res.message || res.error || 'Réclamation envoyée', res.error ? 'error' : 'success');
};

window.Router.register("bookings", renderBookingsPanel);
window.Sidebar.register({ view: 'bookings', icon: ICO.calendar, tip: () => window.t('nav_bks'), badge: true, order: 4 });
