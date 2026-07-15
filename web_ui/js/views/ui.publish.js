async function renderPublishPanel() {
    const u = window.API.getCurrentUser();
    if (u.role === 'PASSENGER') {
        // ... (rest)
    }
    const cars = await window.API.getCars();
    const govOpts = GOV_NAMES.map(g => `<option value="${g}">${g}</option>`).join('');
    document.getElementById('view-publish').innerHTML = `
        <div class="panel-header"><h1 class="panel-title">${window.t('publish')}</h1><p class="panel-subtitle">Proposez votre prochain voyage</p></div>
            <div class="panel-body" style="padding-top:16px">
                ${cars.length === 0 ? '<div style="background:var(--lime-glow);padding:14px;border-radius:var(--radius-sm);margin-bottom:16px;font-size:13px;font-weight:600;border:1px solid rgba(195,248,50,0.2)">⚠️ Ajoutez d\'abord un véhicule dans votre profil.</div>' : ''}
                <div class="form-row"><div class="form-group"><label class="form-label">Gouvernorat départ</label><select class="form-input" id="p-gov-dep" onchange="updateCitySelect('p-gov-dep','p-city-dep')"><option value="">Choisir</option>${govOpts}</select></div>
                    <div class="form-group"><label class="form-label">Ville départ</label><select class="form-input" id="p-city-dep" onchange="window.triggerPublishRoute()"><option value=""></option></select></div></div>
                <div class="form-row"><div class="form-group"><label class="form-label">Gouvernorat arrivée</label><select class="form-input" id="p-gov-arr" onchange="updateCitySelect('p-gov-arr','p-city-arr')"><option value="">Choisir</option>${govOpts}</select></div>
                    <div class="form-group"><label class="form-label">Ville arrivée</label><select class="form-input" id="p-city-arr" onchange="window.triggerPublishRoute()"><option value=""></option></select></div></div>

                <div class="form-group">
                    <label class="form-label">Choisir votre véhicule</label>
                    <div class="car-select-grid" id="p-car-selector" style="margin-top:10px">
                        ${cars.map((c, idx) => {
        const brandLogo = `img/logos/${c.brand.toLowerCase()}.png`;
        return `
                        <div class="car-mini-card ${idx === 0 ? 'selected' : ''}" data-id="${c.id}" onclick="selectCarForPublish(this)">
                            <div class="car-mini-icon-frame">
                                <img src="${brandLogo}" class="car-mini-brand-img" onerror="this.src='https://ui-avatars.com/api/?name=${c.brand}&background=random'">
                            </div>
                            <div class="car-mini-info">
                                <div class="car-mini-name">${c.brand}</div>
                                <div class="car-mini-model">${c.model}</div>
                            </div>
                        </div>`;
    }).join('')}
                        <div class="car-mini-card add-new" onclick="Router.navigate('profile')" style="border: 1.5px dashed #cbd5e1; background: transparent; justify-content: center;">
                            <span style="font-weight:800; color:#64748b">+ Ajouter un véhicule</span>
                        </div>
                    </div>
                </div>
                <div class="form-row"><div class="form-group"><label class="form-label">Date de départ</label><input type="date" class="form-input" id="p-date" value="${new Date().toISOString().split('T')[0]}"></div>
                    <div class="form-group"><label class="form-label">Heure</label><input type="time" class="form-input" id="p-time" value="08:00"></div></div>

                <div class="toggle-wrap" style="margin-bottom:0">
                    <div><div class="toggle-label">Trajet récurrent</div><div class="toggle-desc">Ce trajet se répète plusieurs fois par semaine</div></div>
                    <div class="toggle-switch" id="p-recur-toggle" onclick="toggleRecurrence()"></div>
                </div>
                <div id="p-recur-panel" style="display:none; margin-top:12px; padding:14px; background:var(--lime-glow); border:1px solid rgba(195,248,50,0.2); border-radius:var(--radius-sm)">
                    <div class="form-group" style="margin-bottom:10px">
                        <label class="form-label">Jours de répétition</label>
                        <div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:6px">
                            ${['LUN','MAR','MER','JEU','VEN','SAM','DIM'].map((d,i) => {
                                const codes = ['MON','TUE','WED','THU','FRI','SAT','SUN'];
                                return `<button type="button" class="seat-chip" data-day="${codes[i]}" onclick="this.classList.toggle('active')">${d}</button>`;
                            }).join('')}
                        </div>
                    </div>
                    <div class="form-group" style="margin-bottom:0">
                        <label class="form-label">Date de fin</label>
                        <input type="date" class="form-input" id="p-end-date">
                    </div>
                </div>

                <div class="form-row" style="margin-top:16px"><div class="form-group"><label class="form-label">${t('price')} (TND)</label><input type="number" class="form-input" id="p-price" value="15" min="1"></div>
                    <div class="form-group"><label class="form-label">${t('seats')}</label><input type="number" class="form-input" id="p-seats" value="3" min="1" max="8"></div></div>
                <div class="form-group"><label class="form-label">Préférences</label>
                    <div class="pref-grid" id="p-prefs">
                        <div class="pref-item active" data-pref="ac" onclick="this.classList.toggle('active')">${ICO.ac} Climatisation</div>
                        <div class="pref-item" data-pref="music" onclick="this.classList.toggle('active')">${ICO.music} Musique</div>
                        <div class="pref-item" data-pref="smoking" onclick="this.classList.toggle('active')">${ICO.smoking} Fumeurs OK</div>
                        <div class="pref-item" data-pref="pets" onclick="this.classList.toggle('active')">${ICO.pets} Animaux OK</div>
                        <div class="pref-item" data-pref="women" onclick="this.classList.toggle('active')">${ICO.women} Femmes only</div>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label">${ICO.luggage} Politique bagages</label>
                    <select class="form-input" id="p-luggage-policy">
                        <option value="NONE">Aucun bagage</option>
                        <option value="SMALL">Petit bagage (sac à dos, cabine)</option>
                        <option value="LARGE">Grand bagage (valise, sac de sport)</option>
                        <option value="ANY">Peu importe</option>
                    </select>
                </div>
                <div class="toggle-wrap"><div><div class="toggle-label">Pickup passagers</div><div class="toggle-desc">Je peux récupérer les passagers à domicile</div></div><div class="toggle-switch" id="p-pickup" onclick="this.classList.toggle('on')"></div></div>
                <div class="form-group" style="margin-top:16px"><label class="form-label">Commentaire</label><textarea class="form-input" id="p-comment" placeholder="Infos supplémentaires" rows="2"></textarea></div>
                <button class="btn btn-lime btn-full" onclick="doPublish()" ${cars.length === 0 ? 'disabled' : ''}>Publier le trajet</button>
            </div>`;
}

window.toggleRecurrence = function () {
    const toggle = document.getElementById('p-recur-toggle');
    const panel = document.getElementById('p-recur-panel');
    toggle.classList.toggle('on');
    panel.style.display = toggle.classList.contains('on') ? 'block' : 'none';
};

window.selectCarForPublish = function (el) {
    document.querySelectorAll('#p-car-selector .car-mini-card').forEach(c => c.classList.remove('selected'));
    el.classList.add('selected');
};

window.doPublish = async function () {
    const dep = document.getElementById('p-city-dep').value || document.getElementById('p-gov-dep').value;
    const arr = document.getElementById('p-city-arr').value || document.getElementById('p-gov-arr').value;

    const selectedCar = document.querySelector('#p-car-selector .car-mini-card.selected');
    const carId = selectedCar ? selectedCar.dataset.id : null;

    if (!dep || !arr || !carId) {
        Toast.show('Veuillez remplir le trajet et choisir un véhicule', 'error');
        return;
    }

    const prefs = {};
    document.querySelectorAll('#p-prefs .pref-item.active').forEach(p => prefs[p.dataset.pref] = true);

    const luggagePolicy = document.getElementById('p-luggage-policy')?.value || 'NONE';

    const isRecurring = document.getElementById('p-recur-toggle').classList.contains('on');
    const repeatDays = isRecurring
        ? [...document.querySelectorAll('#p-recur-panel .seat-chip.active')].map(b => b.dataset.day).join(',')
        : '';
    const endDate = isRecurring ? (document.getElementById('p-end-date').value || '') : '';

    if (isRecurring && !repeatDays) {
        Toast.show('Choisissez au moins un jour de répétition', 'error');
        return;
    }
    if (isRecurring && !endDate) {
        Toast.show('Choisissez une date de fin', 'error');
        return;
    }

    const result = await window.API.publishTrip({
        carId,
        dep,
        arr,
        price: parseFloat(document.getElementById('p-price').value),
        seats: parseInt(document.getElementById('p-seats').value),
        date: document.getElementById('p-date').value,
        depTime: document.getElementById('p-time').value,
        comment: document.getElementById('p-comment').value,
        prefs,
        luggagePolicy,
        pickup: document.getElementById('p-pickup').classList.contains('on'),
        repeatDays,
        endDate
    });

    if (result.success) {
        Toast.show('Trajet publié !');
        if (window.TripCache) window.TripCache.clear();
        Router.navigate('trips');
    } else {
        Toast.show(result.error || 'Erreur', 'error');
    }
};

// --- BOOKINGS & DRIVER DASHBOARD ---
window._bookingTab = 'passager';

window.Router.register("publish", renderPublishPanel);
window.Sidebar.register({ view: 'publish', icon: ICO.plus, tip: 'Publier', order: 3, role: 'DRIVER' });
