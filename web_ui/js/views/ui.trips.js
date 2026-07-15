async function renderTripsPanel() {
    clearRoute();
    const u = window.API.getCurrentUser();
    // Show skeletons immediately
    document.getElementById('view-trips').innerHTML =
        '<div class="panel-header"><h1 class="panel-title">' + t('title_trips') + '</h1></div>' +
        '<div class="panel-body">' + skeletonCards(4) + '</div>';

    const trips = await window.API.getTrips();
    const activeTrips = trips.filter(trip => trip.status === 'ACTIVE' && (!u || trip.driverId !== u.id));

    const emptyHtml = '<div class="empty-state"><div class="empty-state-icon">&#128663;</div><h3>' + t('no_trips') + '</h3><p>Aucun trajet disponible pour le moment</p><button class="btn btn-lime" onclick="Router.navigate(\'publish\')">Publier un trajet</button></div>';

    document.getElementById('view-trips').innerHTML =
        '<div class="panel-header"><div style="display:flex;justify-content:space-between;align-items:center"><h1 class="panel-title">' + t('title_trips') + '</h1><span style="font-size:12px;color:var(--text-muted);font-weight:600">' + activeTrips.length + ' disponible(s)</span></div>' +
        '<div class="tabs"><button class="tab active" onclick="this.parentElement.querySelectorAll(\'.tab\').forEach(function(x){x.classList.remove(\'active\')});this.classList.add(\'active\');filterTrips(\'all\')">Tous</button>' +
        '<button class="tab" onclick="this.parentElement.querySelectorAll(\'.tab\').forEach(function(x){x.classList.remove(\'active\')});this.classList.add(\'active\');filterTrips(\'available\')">Disponibles</button></div></div>' +
        '<div class="panel-body" id="trips-list">' + (activeTrips.length ? activeTrips.map(trip => tripCardHTML(trip)).join('') : emptyHtml) + '</div>';
    syncFavoriteHearts();
}

function tripCardHTML(trip) {
    const dist = window.API.getDistance(trip.departure, trip.arrival);
    const carLabel = trip.carInfo && trip.carInfo !== 'Véhicule' ? trip.carInfo : '';
    const carColorDot = trip.carColor ? `<span style="display:inline-block;width:8px;height:8px;border-radius:50%;background:${getColorHex(trip.carColor)};border:1px solid rgba(0,0,0,0.15);vertical-align:middle;margin-right:3px"></span>` : '';
    const safeName = window.esc ? window.esc((trip.driverName || '').split(' ')[0]) : (trip.driverName || '').split(' ')[0];
    const safeFullName = window.esc ? window.esc(trip.driverName || 'User') : (trip.driverName || 'User');
    const safeDep = window.esc ? window.esc(trip.departure) : trip.departure;
    const safeArr = window.esc ? window.esc(trip.arrival) : trip.arrival;
    return `<div class="trip-card ${selectedTripId === trip.id ? 'selected' : ''} ${trip.availableSeats === 0 ? 'sold-out' : ''}" onclick="selectTrip(${trip.id})" id="tc-${trip.id}">
        <div style="display:flex;justify-content:space-between;align-items:flex-start"><div><div class="trip-route"><span>${safeDep}</span><span class="arrow">→</span><span>${safeArr}</span></div><div class="trip-id">Trajet #${trip.id}${carLabel ? ' · ' + carColorDot + carLabel : ''}</div></div><span class="status ${trip.availableSeats > 0 ? 'status-available' : 'status-completed'}">${trip.availableSeats > 0 ? trip.availableSeats + ' ' + window.t('seats') : 'Complet'}</span></div>
        <div class="trip-progress"><div class="trip-progress-fill" style="width:${Math.min(90, 30 + Math.random() * 55)}%"></div></div>
        <div class="trip-meta"><div class="trip-meta-item"><span class="trip-meta-label">${window.t('driver')}</span><span class="trip-meta-value">${safeName}</span></div><div class="trip-meta-item"><span class="trip-meta-label">${window.t('from')}</span><span class="trip-meta-value">${trip.departureTime || '— '}</span></div><div class="trip-meta-item"><span class="trip-meta-label">Distance</span><span class="trip-meta-value">${dist} km</span></div><div class="trip-meta-item"><span class="trip-meta-label">${window.t('price')}</span><span class="trip-meta-value">${(trip.price || 0).toFixed(1)} TND</span></div></div>
        <div class="trip-driver"><div class="trip-driver-info"><img class="trip-driver-avatar" src="${(trip.avatar && trip.avatar !== 'no-avatar' && trip.avatar.length > 10) ? trip.avatar : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(trip.driverName || 'User') + '&background=random'}"><div><div class="trip-driver-label">${window.t('driver')}</div><div class="trip-driver-name">${safeFullName} <span style="color:var(--warning);font-weight:800;margin-left:4px;cursor:pointer;text-decoration:underline dotted" onclick="event.stopPropagation();openReviewsModal(${trip.driverId},'${encodeURIComponent(trip.driverName||'')}',${Number(trip.rating||5).toFixed(1)})">★ ${Number(trip.rating || 5.0).toFixed(1)}</span></div></div></div><div class="trip-actions"><button class="trip-action-btn" title="Signaler" onclick="event.stopPropagation();openReportModal(${trip.driverId})">${ICO.shield}</button><button class="trip-action-btn" title="Message" onclick="event.stopPropagation();Router.navigate('messages')">${ICO.msg}</button><button class="trip-action-btn trip-fav-btn" id="fav-btn-${trip.driverId}" title="Ajouter aux favoris" onclick="event.stopPropagation();toggleFavoriteDriver(${trip.driverId}, this)">${ICO.heart}</button><button class="trip-action-btn" title="${window.t('book')}" onclick="event.stopPropagation();openBookModal(${trip.id})">${ICO.book}</button></div></div></div>`;
}

// ── Feature 7: Favorite driver toggle ────────────────────────────────────────
window.toggleFavoriteDriver = async function (driverId, btn) {
    const u = window.API.getCurrentUser();
    if (!u || u.role !== 'PASSENGER') { Toast.show('Réservé aux passagers', 'error'); return; }

    const isFav = btn.dataset.fav === '1';
    if (isFav) {
        await window.API.removeFavoriteDriver(driverId);
        btn.dataset.fav = '0'; btn.innerHTML = ICO.heart;
        btn.title = 'Ajouter aux favoris';
        Toast.show('Retiré des favoris');
    } else {
        await window.API.addFavoriteDriver(driverId);
        btn.dataset.fav = '1'; btn.innerHTML = ICO.heartFilled;
        btn.title = 'Retirer des favoris';
        Toast.show('Ajouté aux favoris');
    }
};

/** After rendering trip cards, mark drivers already in favorites with a filled heart. */
async function syncFavoriteHearts() {
    const u = window.API.getCurrentUser();
    if (!u || u.role !== 'PASSENGER') return;
    const favs = await window.API.getMyFavoriteDrivers();
    const favIds = new Set((Array.isArray(favs) ? favs : []).map(f => f.driverId));
    document.querySelectorAll('.trip-fav-btn').forEach(btn => {
        const id = parseInt(btn.id.replace('fav-btn-', ''));
        if (favIds.has(id)) { btn.dataset.fav = '1'; btn.innerHTML = ICO.heartFilled; btn.title = 'Retirer des favoris'; }
    });
}

window.filterTrips = async function (type) {
    const u = window.API.getCurrentUser();
    const allTrips = await window.API.getTrips();
    const trips = allTrips.filter(trip => trip.status === 'ACTIVE' && (!u || trip.driverId !== u.id) && (type === 'all' || trip.availableSeats > 0));
    document.getElementById('trips-list').innerHTML = trips.length ? trips.map(trip => tripCardHTML(trip)).join('') : '<div class="empty-state"><h3>' + window.t('no_trips') + '</h3></div>';
};

// ━ ━ ━ SELECT TRIP ━ ━ ━
window.selectTrip = async function (id) {
    selectedTripId = id;
    const allTrips = await window.API.getTrips();
    const trip = allTrips.find(x => x.id === id);
    if (!trip) return;
    highlightRoute(trip.departure, trip.arrival);
    const dist = window.API.getDistance(trip.departure, trip.arrival);
    const bar = document.getElementById('detail-bar');

    const initials = trip.driverName ? trip.driverName.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : '??';

    // Use car data from the trip (joined in backend)
    const car = {
        brand: trip.carInfo ? trip.carInfo.split(' ')[0] : 'Véhicule',
        model: trip.carInfo ? trip.carInfo.split(' ').slice(1).join(' ') : 'Inconnu',
        color: trip.carColor || 'Gris',
        plate: trip.carPlate || '— ',
        image: (trip.carImage && trip.carImage !== 'no-image') ? trip.carImage : null
    };
    const carLogoEntry = CAR_BRANDS.find(b => b.name === car.brand);
    const carLogo = carLogoEntry?.logo || '';
    const logoHtml = (carLogo && carLogo.includes('img/')) ? `<img src="${carLogo}" class="car-logo-mini">` : '';

    // Badges Row
    const p = trip.prefs || {};
    const luggageLabels = { NONE: 'Sans bagage', SMALL: 'Petit bagage', LARGE: 'Grand bagage', ANY: 'Tout bagage' };
    let badgesHtml = '';
    if (p.ac) badgesHtml += `<div class="detail-pill">${ICO.ac} A/C</div>`;
    if (p.music) badgesHtml += `<div class="detail-pill">${ICO.music} Musique</div>`;
    if (p.smoking) badgesHtml += `<div class="detail-pill">${ICO.smoking} Fumeurs</div>`;
    if (p.pets) badgesHtml += `<div class="detail-pill">${ICO.pets} Animaux</div>`;
    const lugPolicy = (typeof p.luggage === 'string') ? p.luggage : (p.luggage ? 'SMALL' : 'NONE');
    badgesHtml += `<div class="detail-pill">${ICO.luggage} ${luggageLabels[lugPolicy] || lugPolicy}</div>`;
    if (p.women) badgesHtml += `<div class="detail-pill" style="color:#db2777;border-color:rgba(219,39,119,0.3)">${ICO.women} Femmes Only</div>`;

    const currentUser = window.API.getCurrentUser();
    const isMale = currentUser && currentUser.gender === 'MALE';
    const isRestricted = p.women && isMale;

    const pickupAvail = trip.flexiblePickup || p.flex;

    // Car thumbnail: show uploaded photo if available, else placeholder; logo always as badge
    const carPhotoHtml = car.image
        ? `<img class="detail-car-photo" src="${car.image}" onerror="this.style.display='none';this.insertAdjacentHTML('afterend','<div class=\'detail-car-nophoto\'>${(logoHtml || ICO.car).replace(/'/g,'\\'')}\</div>')"`>
        : `<div class="detail-car-nophoto">${logoHtml || ICO.car}</div>`;
    const logoBadgeHtml = (carLogo && carLogo.includes('img/'))
        ? `<div class="detail-car-logo-badge"><img src="${carLogo}" onerror="this.parentElement.style.display='none'"></div>`
        : '';

    bar.innerHTML = `
        <div class="detail-bar-inner">

            <!-- LEFT: hero -->
            <div class="detail-hero">
                <div class="detail-avatar-circle">
                    <img src="${(trip.avatar && trip.avatar !== 'no-avatar') ? trip.avatar : 'https://ui-avatars.com/api/?name=' + encodeURIComponent(initials) + '&background=3b82f6&color=fff'}" style="width:100%;height:100%;object-fit:cover">
                </div>
                <div class="detail-hero-info">
                    <h2 class="detail-route-title">${trip.departure} <span class="arrow">→</span> ${trip.arrival}</h2>
                    <div class="detail-subtitle">${trip.driverName || '—'} <span style="color:#fbbf24;font-weight:800;cursor:pointer;text-decoration:underline dotted;margin-left:4px" onclick="openReviewsModal(${trip.driverId},'${(trip.driverName||'').replace(/'/g,'')}',${Number(trip.rating||5).toFixed(1)})">★ ${Number(trip.rating||5).toFixed(1)}</span> · #${trip.id}</div>
                    <div class="detail-badges-row">
                        ${badgesHtml}
                        ${trip.pickup ? '<div class="detail-recommended">${ICO.pin} Prise en charge</div>' : ''}
                    </div>
                </div>
                <button class="detail-hero-close" onclick="document.getElementById('detail-bar').classList.remove('visible');clearRoute()">${ICO.x}</button>
            </div>

            <!-- MIDDLE: stats + pickup -->
            <div class="detail-middle-col">
                <div class="detail-stats-strip">
                    <div class="detail-stat">
                        <div class="detail-stat-val">${trip.departureTime || '—'}</div>
                        <div class="detail-stat-unit">Départ</div>
                    </div>
                    <div class="detail-stat">
                        <div class="detail-stat-val">${dist}</div>
                        <div class="detail-stat-unit">km</div>
                    </div>
                    <div class="detail-stat">
                        <div class="detail-stat-val price">${(trip.price||0).toFixed(1)}</div>
                        <div class="detail-stat-unit">TND</div>
                    </div>
                </div>
                <div class="detail-policy">
                    <span>${ICO.pin}</span>
                    <span>Pickup : <span class="policy-val">${pickupAvail ? 'Disponible' : 'Non disponible'}</span></span>
                </div>
            </div>

            <!-- RIGHT: car + book -->
            <div class="detail-right-col">
                <div class="detail-car-row">
                    <div class="detail-car-thumb">
                        ${carPhotoHtml}
                        ${logoBadgeHtml}
                    </div>
                    <div style="flex:1;min-width:0">
                        <div class="detail-car-name">${car.brand} ${car.model}</div>
                        <div class="detail-car-meta">${car.color} · ${car.plate}</div>
                    </div>
                    <div class="detail-car-verified">
                        <span style="width:7px;height:7px;background:var(--lime);border-radius:50%;box-shadow:0 0 4px var(--lime);display:block"></span>
                        Vérifié
                    </div>
                </div>
                <div class="detail-footer">
                    ${isRestricted ? `
                    <button class="book-btn-split disabled" disabled>
                        <div class="book-btn-text" style="font-size:12px">Femmes uniquement</div>
                        <div class="book-btn-icon" onclick="event.stopPropagation();openReportModal(${trip.driverId})">${ICO.shield}</div>
                    </button>
                    ` : `
                    <button class="book-btn-split" onclick="openBookModal(${trip.id})">
                        <div class="book-btn-text">${window.t('book')}</div>
                        <div class="book-btn-icon" title="${window.t('report')}" onclick="event.stopPropagation();openReportModal(${trip.driverId})">${ICO.shield}</div>
                    </button>
                    `}
                </div>
            </div>

        </div>`;
    bar.classList.add('visible');
    document.querySelectorAll('.trip-card').forEach(c => c.classList.remove('selected'));
    document.getElementById('tc-' + id)?.classList.add('selected');
};

// â”€â”€ SEARCH (governorate → city) Ã¢â€â‚¬Ã¢â€â‚¬

window.Router.register("trips", renderTripsPanel);
window.Sidebar.register({ view: 'trips', icon: ICO.home, tip: () => window.t('nav_home'), order: 1 });
