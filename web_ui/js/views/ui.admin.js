(function () {

    // ─── State ───────────────────────────────────────────────────────────────
    window._adminTab = window._adminTab || 'overview';
    window._adminUserFilter = window._adminUserFilter || { q: '', role: '', status: '' };
    window._adminRideFilter = window._adminRideFilter || { q: '', status: '' };
    window._adminPromoShowInactive = false;

    const ROLE_LABEL = { ADMIN: 'admin', DRIVER: 'driver', PASSENGER: 'passenger' };

    function safeEsc(s) { return (typeof esc === 'function') ? esc(s == null ? '' : String(s)) : String(s == null ? '' : s); }

    // ─── Header ──────────────────────────────────────────────────────────────
    function renderHeader(stats, badges) {
        const tabs = [
            { id: 'overview',  label: "Vue d'ensemble" },
            { id: 'users',     label: 'Utilisateurs' },
            { id: 'rides',     label: 'Trajets' },
            { id: 'reports',   label: 'Modération', badge: badges.moderation },
            { id: 'kyc',       label: 'Vérif. ID',  badge: badges.kyc },
            { id: 'broadcast', label: 'Diffusion' },
            { id: 'promos',    label: 'Promos' },
            { id: 'analytics', label: 'Analytique' },
            { id: 'audit',     label: 'Journal' },
        ];
        const u = window.API.getCurrentUser();
        return `
            <div class="admin-hero">
                <div>
                    <h1 class="admin-hero-title">Administration</h1>
                    <div class="admin-hero-sub">Tableau de bord — gestion complète de la plateforme</div>
                </div>
                <div class="admin-hero-meta">Connecté · ${safeEsc(u?.name || 'admin')}</div>
            </div>
            <div class="admin-tabs">
                ${tabs.map(t => `
                    <button class="admin-tab ${window._adminTab === t.id ? 'active' : ''}" onclick="renderAdminPanel('${t.id}')">
                        <span>${t.label}</span>
                        ${t.badge ? `<span class="admin-tab-badge">${t.badge}</span>` : ''}
                    </button>`).join('')}
            </div>`;
    }

    // ─── Overview ────────────────────────────────────────────────────────────
    function renderOverview(stats, users, reports, disputes, kyc, trips, sparkline) {
        const blocked      = stats.blockedUsers   ?? users.filter(u => u.isBlocked).length;
        const drivers      = stats.drivers         ?? users.filter(u => u.role === 'DRIVER').length;
        const activeTrips  = stats.activeTrips     ?? trips.filter(t => t.status === 'ACTIVE').length;
        const completedT   = stats.completedTrips  ?? trips.filter(t => t.status === 'COMPLETE').length;
        const revenue      = parseFloat(stats.totalRevenue) || 0;

        // Sparkline SVG for volume card (7 days)
        function miniSparkline(data) {
            if (!data || data.length === 0) return '<span style="font-size:11px;color:var(--text-muted)">Pas de données cette semaine</span>';
            const counts = data.map(d => d.count || 0);
            const maxV = Math.max(...counts, 1);
            const w = 120, h = 32, pts = counts.length;
            const xs = counts.map((_, i) => (i / Math.max(pts - 1, 1)) * w);
            const ys = counts.map(v => h - (v / maxV) * h);
            const path = xs.map((x, i) => (i === 0 ? `M${x},${ys[i]}` : `L${x},${ys[i]}`)).join(' ');
            return `<svg width="${w}" height="${h}" viewBox="0 0 ${w} ${h}" style="display:block;margin-top:6px">
                <path d="${path}" fill="none" stroke="var(--lime)" stroke-width="2" stroke-linejoin="round"/>
                ${counts.map((v, i) => v > 0 ? `<circle cx="${xs[i]}" cy="${ys[i]}" r="2.5" fill="var(--lime)"/>` : '').join('')}
            </svg>`;
        }

        // Color encoding: green=healthy, yellow=needs attention, red=needs action
        const tiles = [
            { label: 'Utilisateurs',    val: stats.totalUsers || users.length,
              sub: `${drivers} chauffeurs · ${blocked} bloqués`,
              cls: blocked > 5 ? 'warn' : 'ok' },
            { label: 'Trajets actifs',  val: activeTrips,
              sub: `${completedT} terminés · ${trips.filter(t=>t.status==='CANCELLED').length} annulés`,
              cls: 'ok' },
            { label: 'Volume (TND)',
              val: revenue.toFixed(0),
              sub: miniSparkline(sparkline),
              cls: revenue > 0 ? 'lime' : 'warn',
              rawSub: true },
            { label: 'Signalements',    val: reports.length,
              sub: reports.length ? `${reports.length} action${reports.length>1?'s':''} requise${reports.length>1?'s':''}` : 'Tout est clair ✓',
              cls: reports.length > 0 ? 'danger' : 'ok' },
            { label: 'Litiges ouverts', val: disputes.length,
              sub: disputes.length ? 'En attente de traitement' : 'Aucun litige',
              cls: disputes.length > 0 ? 'danger' : 'ok' },
            { label: 'Vérif. ID',       val: kyc.length,
              sub: kyc.length ? `${kyc.length} document${kyc.length>1?'s':''} à examiner` : 'File vide ✓',
              cls: kyc.length > 0 ? 'warn' : 'ok' },
        ];

        function fmtDate(d) {
            if (!d) return '—';
            try { return new Date(d).toLocaleString('fr-FR', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' }); }
            catch { return String(d); }
        }

        const recentReports = reports.slice(0, 5).map(r => {
            const reporter = users.find(u => u.id === r.reporterId);
            const reported = users.find(u => u.id === r.reportedId);
            return { kind: 'report', date: r.createdAt || '',
                icon: ICO.flag,
                label: `${reporter?.name || '?'} a signalé ${reported?.name || '?'}`,
                text: `${reporter?.name || '?'} a signalé ${reported?.name || '?'}`,
                sub: r.reason || '' };
        });
        const recentDisputes = disputes.slice(0, 5).map(d => ({
            kind: 'dispute', date: d.createdAt || '',
            icon: ICO.scale,
            label: `Litige #${d.id} — Trajet #${d.tripId}`,
            text: `Litige #${d.id} — Trajet #${d.tripId}`,
            sub: `${d.complainantName || 'Utilisateur'} : ${d.reason || ''}`
        }));
        const feed = [...recentReports, ...recentDisputes]
            .sort((a, b) => (b.date || '').localeCompare(a.date || ''))
            .slice(0, 8);

        return `
            <div class="admin-grid">
                ${tiles.map(t => `
                    <div class="admin-tile ${t.cls}">
                        <div class="admin-tile-label">${t.label}</div>
                        <div class="admin-tile-val">${t.val}</div>
                        <div class="admin-tile-sub">${t.rawSub ? t.sub : safeEsc(t.sub)}</div>
                    </div>`).join('')}
            </div>

            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Activité récente</h2>
                    <button class="btn btn-outline btn-xs" onclick="renderAdminPanel('reports')">Voir tout</button>
                </div>
                ${feed.length === 0 ? `
                    <div class="admin-empty">
                        <div class="admin-empty-icon">${ICO.check}</div>
                        <div>Aucune activité de modération récente</div>
                    </div>` : `
                    <div style="display:flex;flex-direction:column;gap:10px">
                        ${feed.map(f => `
                            <div style="display:flex;justify-content:space-between;align-items:center;padding:10px 12px;border:1px solid var(--border);border-radius:8px">
                                <div style="min-width:0;flex:1">
                                    <div style="font-size:13px;font-weight:700;color:var(--text);display:flex;align-items:center;gap:6px"><span style="display:inline-flex;width:14px;height:14px;flex-shrink:0">${f.icon || ''}</span>${safeEsc(f.label || f.text)}</div>
                                    <div style="font-size:12px;color:var(--text-muted);overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${safeEsc(f.sub)}</div>
                                </div>
                                <div style="font-size:11px;color:var(--text-muted);margin-left:12px;white-space:nowrap">${fmtDate(f.date)}</div>
                            </div>`).join('')}
                    </div>`}
            </div>`;
    }

    // ─── Users ───────────────────────────────────────────────────────────────
    function renderUsers(users) {
        const f = window._adminUserFilter;
        const filtered = users.filter(u => {
            if (f.q) {
                const q = f.q.toLowerCase();
                if (!(u.name || '').toLowerCase().includes(q) && !(u.email || '').toLowerCase().includes(q)) return false;
            }
            if (f.role && u.role !== f.role) return false;
            if (f.status === 'BLOCKED' && !u.isBlocked) return false;
            if (f.status === 'ACTIVE' && u.isBlocked) return false;
            return true;
        });

        return `
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Utilisateurs · ${filtered.length}/${users.length}</h2>
                    <button class="btn btn-outline btn-xs" onclick="adminExportUsersCsv()">${ICO.download} Export CSV</button>
                </div>
                <div class="admin-toolbar">
                    <input type="text" placeholder="Rechercher par nom ou email…" value="${safeEsc(f.q)}" oninput="adminSetUserFilter('q', this.value)">
                    <select onchange="adminSetUserFilter('role', this.value)">
                        <option value="" ${f.role===''?'selected':''}>Tous les rôles</option>
                        <option value="ADMIN" ${f.role==='ADMIN'?'selected':''}>Admin</option>
                        <option value="DRIVER" ${f.role==='DRIVER'?'selected':''}>Chauffeur</option>
                        <option value="PASSENGER" ${f.role==='PASSENGER'?'selected':''}>Passager</option>
                    </select>
                    <select onchange="adminSetUserFilter('status', this.value)">
                        <option value="" ${f.status===''?'selected':''}>Tous</option>
                        <option value="ACTIVE" ${f.status==='ACTIVE'?'selected':''}>Actifs</option>
                        <option value="BLOCKED" ${f.status==='BLOCKED'?'selected':''}>Bloqués</option>
                    </select>
                </div>
                ${filtered.length === 0 ? `
                    <div class="admin-empty"><div class="admin-empty-icon">${ICO.inbox}</div>Aucun utilisateur ne correspond aux filtres.</div>` : `
                    <table class="admin-table">
                        <thead><tr><th>Utilisateur</th><th>Email</th><th>Rôle</th><th>Statut</th><th class="row-actions">Actions</th></tr></thead>
                        <tbody>
                            ${filtered.map(u => {
                                const roleColor = u.role === 'DRIVER' ? '#16a34a' : u.role === 'ADMIN' ? '#000' : '#2563eb';
                                const roleBg    = u.role === 'DRIVER' ? 'rgba(22,163,74,0.12)' : u.role === 'ADMIN' ? 'rgba(0,0,0,0.08)' : 'rgba(37,99,235,0.12)';
                                const avatarUrl = u.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(u.name||'U')}&background=${u.role==='DRIVER'?'16a34a':u.role==='ADMIN'?'111':'2563eb'}&color=fff`;
                                return `
                                <tr onclick="adminOpenUserDetail(${u.id})">
                                    <td>
                                        <div style="display:flex;align-items:center;gap:10px">
                                            <img src="${avatarUrl}" style="width:32px;height:32px;border-radius:50%;border:2px solid ${roleBg}">
                                            <span style="font-weight:700">${safeEsc(u.name)}</span>
                                        </div>
                                    </td>
                                    <td style="color:var(--text-muted)">${safeEsc(u.email)}</td>
                                    <td><span class="role-chip ${ROLE_LABEL[u.role]||''}" style="color:${roleColor};background:${roleBg}">${safeEsc(u.role)}</span></td>
                                    <td>
                                        ${u.isBlocked ? '<span class="status-chip blocked">Bloqué</span>' : '<span class="status-chip active">Actif</span>'}
                                        ${u.idVerified ? ' <span class="status-chip verified">ID ✓</span>' : ''}
                                    </td>
                                    <td class="row-actions" onclick="event.stopPropagation()">
                                        <div style="display:flex;gap:6px;justify-content:flex-end">
                                            <button class="btn btn-outline btn-xs" onclick="adminOpenUserDetail(${u.id})">Voir</button>
                                            <button class="btn ${u.isBlocked ? 'btn-lime' : 'btn-danger'} btn-xs" onclick="adminToggleBlock(${u.id}, ${!u.isBlocked})">
                                                ${u.isBlocked ? 'Débloquer' : 'Bloquer'}
                                            </button>
                                            ${u.role !== 'ADMIN' ? `<button class="btn btn-danger btn-xs" style="background:rgba(239,68,68,0.15)" title="Supprimer" onclick="adminConfirmDeleteUser(${u.id}, '${safeEsc(u.name?.replace(/'/g,"\\'")||'')}')">✕</button>` : ''}
                                        </div>
                                    </td>
                                </tr>`;
                            }).join('')}
                        </tbody>
                    </table>`}
            </div>`;
    }

    // ─── Rides ───────────────────────────────────────────────────────────────
    function renderRides(trips, users) {
        const f = window._adminRideFilter;
        const filtered = trips.filter(t => {
            if (f.q) {
                const q = f.q.toLowerCase();
                if (!`${t.departure} ${t.arrival}`.toLowerCase().includes(q)) return false;
            }
            if (f.status && t.status !== f.status) return false;
            return true;
        });

        const driverName = id => (users.find(u => u.id === id)?.name) || `#${id}`;

        return `
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Trajets · ${filtered.length}/${trips.length}</h2>
                </div>
                <div class="admin-toolbar">
                    <input type="text" placeholder="Rechercher par ville…" value="${safeEsc(f.q)}" oninput="adminSetRideFilter('q', this.value)">
                    <select onchange="adminSetRideFilter('status', this.value)">
                        <option value="" ${f.status===''?'selected':''}>Tous statuts</option>
                        <option value="ACTIVE"    ${f.status==='ACTIVE'   ?'selected':''}>Actifs</option>
                        <option value="COMPLETE"  ${f.status==='COMPLETE' ?'selected':''}>Terminés</option>
                        <option value="CANCELLED" ${f.status==='CANCELLED'?'selected':''}>Annulés</option>
                    </select>
                </div>
                ${filtered.length === 0 ? `
                    <div class="admin-empty"><div class="admin-empty-icon">${ICO.inbox}</div>Aucun trajet.</div>` : `
                    <table class="admin-table">
                        <thead><tr><th>Itinéraire</th><th>Chauffeur</th><th>Date &amp; Heure</th><th>Places</th><th>Prix</th><th>Statut</th></tr></thead>
                        <tbody>
                            ${filtered.map(t => {
                                const dateStr = t.startDate ? new Date(t.startDate).toLocaleDateString('fr-FR') : '—';
                                const timeStr = t.departureTime || t.time || '';
                                const seatsAvail = t.availableSeats ?? t.seats ?? 0;
                                const seatsTotal = t.totalSeats && t.totalSeats > 0 ? t.totalSeats : null;
                                const seatsDisplay = seatsTotal ? `${seatsAvail}/${seatsTotal}` : `${seatsAvail} dispo`;
                                return `
                                <tr onclick="adminOpenRideDetail(${t.id})">
                                    <td><b>${safeEsc(t.departure)}</b> <span style="color:var(--text-muted)">→</span> <b>${safeEsc(t.arrival)}</b></td>
                                    <td style="color:var(--text-muted)">${safeEsc(driverName(t.driverId))}</td>
                                    <td style="color:var(--text-muted)">
                                        ${dateStr}
                                        ${timeStr ? `<span style="display:block;font-size:11px;font-weight:700">${String(timeStr).slice(0,5)}</span>` : ''}
                                    </td>
                                    <td>${seatsDisplay}</td>
                                    <td style="font-weight:700">${(t.price || 0).toFixed(1)} TND</td>
                                    <td><span class="status-chip ${(t.status || '').toLowerCase()}">${safeEsc(t.status)}</span></td>
                                </tr>`;
                            }).join('')}
                        </tbody>
                    </table>`}
            </div>`;
    }

    // ─── Moderation (reports + disputes) ─────────────────────────────────────
    function renderModeration(reports, disputes, users) {
        // Group reports by reported user to avoid duplicate "Bloquer X" buttons
        const grouped = {};
        reports.forEach(r => {
            if (!grouped[r.reportedId]) grouped[r.reportedId] = [];
            grouped[r.reportedId].push(r);
        });

        function fmtDate(d) {
            if (!d) return '';
            try { return new Date(d).toLocaleString('fr-FR', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' }); }
            catch { return ''; }
        }

        const reportsHtml = Object.keys(grouped).length === 0
            ? `<div class="admin-empty"><div class="admin-empty-icon">${ICO.check}</div>Aucun signalement.</div>`
            : Object.values(grouped).map(group => {
                const reported = users.find(u => u.id === group[0].reportedId);
                const reportedName = reported?.name || `#${group[0].reportedId}`;
                const isBlocked = reported?.isBlocked;
                return `
                    <div class="booking-card" style="border-left:4px solid var(--error);margin-bottom:12px">
                        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
                            <div style="font-size:12px;font-weight:700;color:var(--error);display:flex;align-items:center;gap:5px"><span style="display:inline-flex;width:13px;height:13px;flex-shrink:0">${ICO.flag}</span>${group.length} signalement${group.length>1?'s':''} contre <b>${safeEsc(reportedName)}</b></div>
                            ${isBlocked ? '<span class="status-chip blocked" style="font-size:10px">Déjà bloqué</span>' : ''}
                        </div>
                        ${group.map(r => {
                            const reporter = users.find(u => u.id === r.reporterId);
                            const sev = r.severity || 'mineur';
                            const sevColor = sev === 'critique' ? 'var(--error)' : sev === 'grave' ? 'var(--warning)' : 'var(--text-muted)';
                            return `<div style="font-size:12px;padding:6px 8px;background:var(--bg);border-radius:6px;margin-bottom:6px">
                                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:4px">
                                    <span style="font-weight:600">${safeEsc(reporter?.name||'?')}</span>
                                    <div style="display:flex;align-items:center;gap:8px">
                                        <span style="color:var(--text-muted);font-size:10px">${fmtDate(r.createdAt)}</span>
                                        <span style="font-size:10px;font-weight:700;color:${sevColor};text-transform:uppercase">${safeEsc(sev)}</span>
                                    </div>
                                </div>
                                <div style="color:var(--text-secondary);font-style:italic">"${safeEsc(r.reason)}"</div>
                            </div>`;
                        }).join('')}
                        <div style="display:flex;gap:8px;margin-top:8px">
                            ${!isBlocked ? `<button class="btn btn-danger btn-xs" onclick="adminToggleBlock(${group[0].reportedId}, true)">Bloquer ${safeEsc(reportedName.split(' ')[0])}</button>` : ''}
                            <button class="btn btn-outline btn-xs" onclick="dismissAllReports([${group.map(r=>r.id).join(',')}])">Tout ignorer</button>
                        </div>
                    </div>`;
            }).join('');

        const disputesHtml = disputes.length === 0
            ? `<div class="admin-empty"><div class="admin-empty-icon">${ICO.check}</div>Aucun litige ouvert.</div>`
            : disputes.map(d => {
                const trip = (window._adminTrips||[]).find(t => t.id === d.tripId);
                const tripLabel = trip ? `${trip.departure} → ${trip.arrival}` : `Trajet #${d.tripId}`;
                const dateStr  = d.createdAt ? new Date(d.createdAt).toLocaleString('fr-FR', {day:'2-digit',month:'short',hour:'2-digit',minute:'2-digit'}) : '';
                return `
                <div class="booking-card" style="border-left:4px solid var(--warning);margin-bottom:10px">
                    <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px">
                        <div>
                            <div style="font-size:12px;font-weight:700;color:var(--warning);display:flex;align-items:center;gap:5px"><span style="display:inline-flex;width:13px;height:13px;flex-shrink:0">${ICO.scale}</span>LITIGE #${d.id}</div>
                            <div style="font-size:13px;font-weight:700;margin-top:2px">${safeEsc(tripLabel)}</div>
                        </div>
                        <div style="font-size:11px;color:var(--text-muted);text-align:right">${safeEsc(dateStr)}</div>
                    </div>
                    <div style="font-size:13px;margin-bottom:8px">
                        <b>${safeEsc(d.complainantName || 'Utilisateur')}</b>
                        <span style="color:var(--text-secondary)"> · "${safeEsc(d.reason)}"</span>
                    </div>
                    <div style="display:flex;gap:8px;flex-wrap:wrap">
                        <button class="btn btn-lime btn-xs" onclick="openResolveDisputeModal(${d.id})">${ICO.check} Résoudre</button>
                        <button class="btn btn-outline btn-xs" style="color:var(--text-muted)" onclick="adminDismissDispute(${d.id})">Clôturer sans action</button>
                    </div>
                </div>`;}).join('');

        return `
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Signalements · ${reports.length}</h2>
                </div>
                ${reportsHtml}
            </div>
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Litiges · ${disputes.length}</h2>
                </div>
                ${disputesHtml}
            </div>`;
    }

    // ─── KYC ─────────────────────────────────────────────────────────────────
    function renderKyc(pendingIds) {
        return `
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Vérifications d'identité · ${pendingIds.length}</h2>
                </div>
                ${pendingIds.length === 0 ? `
                    <div class="admin-empty"><div class="admin-empty-icon">${ICO.check}</div>Aucune vérification en attente.</div>` :
                    pendingIds.map(u => `
                        <div class="booking-card" style="margin-bottom:12px">
                            <div style="display:flex;align-items:center;gap:10px;margin-bottom:12px">
                                <img src="${u.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(u.fullName || u.name || 'Driver')}" style="width:40px;height:40px;border-radius:50%">
                                <div>
                                    <div style="font-size:14px;font-weight:700">${safeEsc(u.fullName || u.name)}</div>
                                    <div style="font-size:12px;color:var(--text-muted)">${safeEsc(u.email)} · Chauffeur</div>
                                </div>
                            </div>

                            <div id="kyc-doc-${u.id}" style="margin-bottom:12px;min-height:40px">
                                <button class="btn btn-outline btn-xs btn-full" onclick="kycLoadDoc(${u.id})">${ICO.idcard} Charger la pièce d'identité</button>
                            </div>

                            <div style="display:flex;gap:8px;flex-wrap:wrap">
                                <button class="btn btn-lime btn-xs" style="flex:1" onclick="adminApproveId(${u.id})">${ICO.check} Approuver</button>
                                <button class="btn btn-danger btn-xs" style="flex:1" onclick="adminRejectId(${u.id})">${ICO.x} Rejeter</button>
                                <button class="btn btn-outline btn-xs" style="flex:1" onclick="adminRequestIdResubmit(${u.id})">${ICO.refresh} Demander nouvelle photo</button>
                            </div>
                        </div>`).join('')}
            </div>`;
    }

    // ─── Broadcast ───────────────────────────────────────────────────────────
    function renderBroadcast() {
        const last = localStorage.getItem('cov_admin_last_broadcast');
        const lastInfo = last ? JSON.parse(last) : null;
        return `
            <div class="admin-section broadcast-card">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Diffuser une notification</h2>
                </div>
                <div style="font-size:13px;color:var(--text-muted);margin-bottom:4px">
                    Le message sera envoyé à <b>tous</b> les utilisateurs connectés. Action irréversible.
                </div>
                <textarea id="broadcast-input" placeholder="Ex : Maintenance programmée ce soir à 22h…"></textarea>
                <div class="broadcast-meta">
                    <span>${lastInfo ? `Dernière diffusion : ${new Date(lastInfo.at).toLocaleString('fr-FR')} — « ${safeEsc(lastInfo.preview)} »` : 'Aucune diffusion enregistrée.'}</span>
                    <button class="btn btn-lime" onclick="adminSubmitBroadcast()">📢 Envoyer à tous</button>
                </div>
            </div>`;
    }

    // ─── Promos ──────────────────────────────────────────────────────────────
    // Backend format: "id | CODE | -X% / -Y TND | used/max utilisations [ACTIF|EXPIRÉ]"
    function parsePromo(raw) {
        const parts = String(raw).split('|').map(s => s.trim());
        const id   = parseInt(parts[0]) || 0;
        const code = parts[1] || '?';
        const disc = parts[2] || '';           // "-10% / -0.00 TND"
        const uses = parts[3] || '';           // "3/100 utilisations"
        const last = parts[4] || '';           // "[ACTIF]" or "[EXPIRÉ]"
        const active = last.includes('ACTIF');
        return { id, code, disc, uses, active, raw };
    }

    function renderPromos(promos) {
        const lines = (promos || []).map(parsePromo);
        return `
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Codes promo · ${lines.length}</h2>
                    <button class="btn btn-lime btn-xs" onclick="openCreatePromoModal()">+ Nouveau code</button>
                </div>
                ${lines.length === 0 ? `
                    <div class="admin-empty"><div class="admin-empty-icon">🎁</div>Aucun code promo configuré.</div>` : `
                    <table class="admin-table">
                        <thead><tr><th>Code</th><th>Remise</th><th>Utilisations</th><th>Statut</th><th class="row-actions">Action</th></tr></thead>
                        <tbody>
                            ${lines.map(l => `
                                <tr onclick="adminOpenPromoDetail('${safeEsc(l.code)}','${safeEsc(l.disc)}','${safeEsc(l.uses)}',${l.active},${l.id})">
                                    <td style="font-family:monospace;letter-spacing:1.5px;font-weight:800">${safeEsc(l.code)}</td>
                                    <td style="color:var(--text-muted)">${safeEsc(l.disc)}</td>
                                    <td style="color:var(--text-muted)">${safeEsc(l.uses)}</td>
                                    <td><span class="status-chip ${l.active ? 'active' : 'cancelled'}">${l.active ? 'Actif' : 'Expiré'}</span></td>
                                    <td class="row-actions" onclick="event.stopPropagation()">
                                        ${l.active ? `<button class="btn btn-danger btn-xs" onclick="adminDeactivatePromo(${l.id})">Désactiver</button>` : '<span style="font-size:11px;color:var(--text-muted)">—</span>'}
                                    </td>
                                </tr>`).join('')}
                        </tbody>
                    </table>`}
            </div>`;
    }

    // ─── Audit Log ───────────────────────────────────────────────────────────
    function renderAudit(entries) {
        function fmtDate(d) {
            if (!d) return '—';
            try { return new Date(d).toLocaleString('fr-FR', { day:'2-digit', month:'short', hour:'2-digit', minute:'2-digit' }); }
            catch { return String(d); }
        }
        const ACTION_ICON = {
            BLOCK_USER: '🚫', UNBLOCK_USER: '✅', DELETE_USER: '🗑️',
            APPROVE_ID: '✔', REJECT_ID: '✖', REQUEST_ID_RESUBMIT: '🔄',
        };
        return `
            <div class="admin-section">
                <div class="admin-section-head">
                    <h2 class="admin-section-title">Journal d'audit · ${entries.length} entrées</h2>
                    <button class="btn btn-outline btn-xs" onclick="adminReloadAudit()">↺ Actualiser</button>
                </div>
                ${entries.length === 0 ? `<div class="admin-empty"><div class="admin-empty-icon">📋</div>Aucune entrée.</div>` : `
                <table class="admin-table">
                    <thead><tr><th>Date</th><th>Admin</th><th>Action</th><th>Cible</th><th>Détail</th></tr></thead>
                    <tbody>
                        ${entries.map(e => `
                            <tr>
                                <td style="color:var(--text-muted);white-space:nowrap">${fmtDate(e.createdAt)}</td>
                                <td style="font-weight:700">${safeEsc(e.actorName || '#'+e.actorId)}</td>
                                <td><span style="font-size:12px">${ACTION_ICON[e.action]||'⚙️'} ${safeEsc(e.action)}</span></td>
                                <td style="color:var(--text-muted)">${safeEsc(e.targetType||'')} #${e.targetId}</td>
                                <td style="color:var(--text-muted);font-size:12px">${safeEsc(e.detail||'—')}</td>
                            </tr>`).join('')}
                    </tbody>
                </table>`}
            </div>`;
    }

    // ─── Main render ─────────────────────────────────────────────────────────
    async function renderAdminPanel(tab) {
        if (tab) window._adminTab = tab;
        const t = window._adminTab;

        const [stats, users, reports, disputes, kyc, trips] = await Promise.all([
            window.API.getAdminStats(),
            window.API.adminGetUsers(),
            window.API.adminGetReports(),
            window.API.adminGetDisputes ? window.API.adminGetDisputes() : Promise.resolve([]),
            window.API.adminGetPendingIdVerifications ? window.API.adminGetPendingIdVerifications() : Promise.resolve([]),
            window.API.fetchTrips(),
        ]);
        const safe = (x, fallback = []) => Array.isArray(x) ? x : fallback;
        const U = safe(users), R = safe(reports), D = safe(disputes), K = safe(kyc), T = safe(trips);

        // Store trips globally so dispute renderer can look up routes
        window._adminTrips = T;

        let body = '';
        switch (t) {
            case 'users':     body = renderUsers(U); break;
            case 'rides':     body = renderRides(T, U); break;
            case 'reports':   body = renderModeration(R, D, U); break;
            case 'kyc':       body = renderKyc(K); break;
            case 'broadcast': body = renderBroadcast(); break;
            case 'promos': {
                const promos = await window.API.adminGetPromoCodes();
                body = renderPromos(safe(promos));
                break;
            }
            case 'analytics':
                body = renderAnalytics(stats || {}, U, T, safe(await window.API.adminGetDisputes()));
                break;
            case 'audit': {
                const entries = await window.API.getAuditLog(100);
                body = renderAudit(safe(entries));
                break;
            }
            default: {
                const sparkline = await window.API.getSparkline().catch(() => []);
                body = renderOverview(stats || {}, U, R, D, K, T, sparkline);
            }
        }

        const badges = { moderation: R.length + D.length, kyc: K.length };
        document.getElementById('view-admin').innerHTML = `
            <div class="admin-shell">
                ${renderHeader(stats || {}, badges)}
                <div class="admin-content">${body}</div>
            </div>`;
    }


    // ─── Analytics ───────────────────────────────────────────────────────────
    function renderAnalytics(stats, users, trips, disputes) {
        const drivers    = users.filter(u => u.role === 'DRIVER').length;
        const passengers = users.filter(u => u.role === 'PASSENGER').length;
        const blocked    = users.filter(u => u.isBlocked).length;
        const verified   = users.filter(u => u.idVerified).length;

        const active    = trips.filter(t => t.status === 'ACTIVE').length;
        const completed = trips.filter(t => t.status === 'COMPLETE').length;
        const cancelled = trips.filter(t => t.status === 'CANCELLED').length;
        const total     = trips.length || 1;

        const revenue = parseFloat(stats.totalRevenue) || 0;
        const avgPrice = trips.length ? (trips.reduce((s,t) => s + (t.price||0), 0) / trips.length).toFixed(1) : '—';

        // Top routes (departure → arrival counts)
        const routeMap = {};
        trips.forEach(t => {
            const key = `${t.departure} → ${t.arrival}`;
            routeMap[key] = (routeMap[key] || 0) + 1;
        });
        const topRoutes = Object.entries(routeMap).sort((a,b) => b[1]-a[1]).slice(0, 5);

        function bar(pct, color) {
            return `<div style="height:8px;border-radius:4px;background:var(--border);overflow:hidden;margin-top:4px">
                <div style="height:100%;width:${Math.min(100,pct)}%;background:${color};border-radius:4px;transition:width 0.4s"></div>
            </div>`;
        }

        return `
            <div class="admin-grid" style="grid-template-columns:repeat(auto-fit,minmax(180px,1fr))">
                <div class="admin-tile lime"><div class="admin-tile-label">Chauffeurs</div><div class="admin-tile-val">${drivers}</div><div class="admin-tile-sub">${passengers} passagers</div></div>
                <div class="admin-tile ${blocked?'danger':'ok'}"><div class="admin-tile-label">Comptes bloqués</div><div class="admin-tile-val">${blocked}</div><div class="admin-tile-sub">${((blocked/(users.length||1))*100).toFixed(0)}% des utilisateurs</div></div>
                <div class="admin-tile ok"><div class="admin-tile-label">Identités vérifiées</div><div class="admin-tile-val">${verified}</div><div class="admin-tile-sub">${((verified/(users.length||1))*100).toFixed(0)}% des utilisateurs</div></div>
                <div class="admin-tile lime"><div class="admin-tile-label">Volume TND</div><div class="admin-tile-val">${revenue.toFixed(0)}</div><div class="admin-tile-sub">Prix moyen ${avgPrice} TND/trajet</div></div>
            </div>

            <div class="admin-section">
                <div class="admin-section-head"><h2 class="admin-section-title">Répartition des trajets</h2></div>
                <div style="display:flex;flex-direction:column;gap:14px">
                    <div>
                        <div style="display:flex;justify-content:space-between;font-size:13px"><span>Actifs</span><b>${active} (${((active/total)*100).toFixed(0)}%)</b></div>
                        ${bar((active/total)*100, 'var(--lime)')}
                    </div>
                    <div>
                        <div style="display:flex;justify-content:space-between;font-size:13px"><span>Terminés</span><b>${completed} (${((completed/total)*100).toFixed(0)}%)</b></div>
                        ${bar((completed/total)*100, 'var(--success)')}
                    </div>
                    <div>
                        <div style="display:flex;justify-content:space-between;font-size:13px"><span>Annulés</span><b>${cancelled} (${((cancelled/total)*100).toFixed(0)}%)</b></div>
                        ${bar((cancelled/total)*100, 'var(--error)')}
                    </div>
                </div>
            </div>

            <div class="admin-section">
                <div class="admin-section-head"><h2 class="admin-section-title">Routes les plus populaires</h2></div>
                ${topRoutes.length === 0 ? '<div class="admin-empty"><div class="admin-empty-icon">${ICO.inbox}</div>Aucune donnée.</div>' :
                    topRoutes.map(([route, count], i) => `
                        <div style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;${i>0?'border-top:1px solid var(--border-light)':''}">
                            <div style="font-size:13px;font-weight:700">${safeEsc(route)}</div>
                            <div style="font-size:13px;color:var(--lime);font-weight:800;font-variant-numeric:tabular-nums">${count} trajet${count>1?'s':''}</div>
                        </div>`).join('')}
            </div>

            <div class="admin-section">
                <div class="admin-section-head"><h2 class="admin-section-title">Litiges résolus vs ouverts</h2></div>
                ${disputes.length === 0 ? '<div class="admin-empty"><div class="admin-empty-icon">${ICO.check}</div>Aucun litige.</div>' : (() => {
                    const open   = disputes.filter(d => d.status === 'OPEN'   || !d.status).length;
                    const closed = disputes.filter(d => d.status === 'CLOSED' || d.status === 'RESOLVED').length;
                    return `<div style="display:flex;gap:24px;flex-wrap:wrap">
                        <div><div style="font-size:28px;font-weight:900;color:var(--warning)">${open}</div><div style="font-size:12px;color:var(--text-muted)">Ouverts</div></div>
                        <div><div style="font-size:28px;font-weight:900;color:var(--success)">${closed}</div><div style="font-size:12px;color:var(--text-muted)">Résolus</div></div>
                    </div>`;
                })()}
            </div>`;
    }

    window.renderAdminPanel = renderAdminPanel;

    // ─── Filters ─────────────────────────────────────────────────────────────
    let _userFilterTimer = null;
    window.adminSetUserFilter = function (key, val) {
        window._adminUserFilter[key] = val;
        if (key === 'q') {
            clearTimeout(_userFilterTimer);
            _userFilterTimer = setTimeout(() => renderAdminPanel('users'), 200);
        } else {
            renderAdminPanel('users');
        }
    };
    let _rideFilterTimer = null;
    window.adminSetRideFilter = function (key, val) {
        window._adminRideFilter[key] = val;
        if (key === 'q') {
            clearTimeout(_rideFilterTimer);
            _rideFilterTimer = setTimeout(() => renderAdminPanel('rides'), 200);
        } else {
            renderAdminPanel('rides');
        }
    };

    // ─── User actions ────────────────────────────────────────────────────────
    window.adminToggleBlock = async function (id, block) {
        const res = await window.API.adminBlockAccount(id, block);
        if (res.success) Toast.show(block ? 'Compte bloqué' : 'Compte débloqué');
        else Toast.show(res.error || 'Erreur', 'error');
        renderAdminPanel(window._adminTab);
    };

    window.adminOpenUserDetail = async function (id) {
        const users = await window.API.adminGetUsers();
        const u = (users || []).find(x => x.id === id);
        if (!u) { Toast.show('Utilisateur introuvable', 'error'); return; }
        const modal = document.getElementById('generic-modal');
        if (!modal) { Toast.show('Modal non disponible', 'error'); return; }
        document.getElementById('generic-modal-body').innerHTML = `
            <div style="padding:24px">
                <div style="display:flex;align-items:center;gap:14px;margin-bottom:18px">
                    <img src="${u.avatar || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(u.name || 'U')}" style="width:56px;height:56px;border-radius:50%">
                    <div>
                        <div style="font-size:18px;font-weight:800">${safeEsc(u.name)}</div>
                        <div style="font-size:12px;color:var(--text-muted)">${safeEsc(u.email)}</div>
                    </div>
                </div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px;margin-bottom:18px">
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Rôle</div><div>${safeEsc(u.role)}</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Téléphone</div><div>${safeEsc(u.phone || '—')}</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Statut</div><div>${u.isBlocked ? '<span class="status-chip blocked">Bloqué</span>' : '<span class="status-chip active">Actif</span>'}</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Identité</div><div>${u.idVerified ? '<span class="status-chip verified">Vérifiée</span>' : '<span class="status-chip">Non vérifiée</span>'}</div></div>
                </div>
                <div style="display:flex;gap:8px;flex-direction:column">
                    <button class="btn ${u.isBlocked ? 'btn-lime' : 'btn-danger'} btn-full" onclick="adminToggleBlock(${u.id}, ${!u.isBlocked});document.getElementById('generic-modal').classList.remove('open')">
                        ${u.isBlocked ? 'Débloquer' : 'Bloquer'} le compte
                    </button>
                    ${u.role !== 'ADMIN' ? `
                    <button class="btn btn-outline btn-full" style="color:var(--error);border-color:rgba(239,68,68,0.4)"
                        onclick="document.getElementById('generic-modal').classList.remove('open');adminConfirmDeleteUser(${u.id}, '${safeEsc(u.name?.replace(/'/g,"\\'")||'')}')">
                        🗑️ Supprimer définitivement
                    </button>` : ''}
                </div>
            </div>`;
        modal.classList.add('open');
    };

    window.adminExportUsersCsv = async function () {
        const users = await window.API.adminGetUsers();
        if (!Array.isArray(users) || !users.length) { Toast.show('Aucune donnée', 'error'); return; }
        const head = ['id','name','email','role','phone','isBlocked','idVerified'];
        const escCsv = v => `"${String(v == null ? '' : v).replace(/"/g, '""')}"`;
        const rows = [head.join(',')].concat(users.map(u => head.map(k => escCsv(u[k])).join(',')));
        const blob = new Blob([rows.join('\n')], { type: 'text/csv;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `users-${new Date().toISOString().slice(0,10)}.csv`;
        document.body.appendChild(a); a.click(); document.body.removeChild(a);
        URL.revokeObjectURL(url);
        Toast.show('Export CSV téléchargé');
    };

    // ─── Ride detail ─────────────────────────────────────────────────────────
    window.adminOpenRideDetail = async function (id) {
        const trips = await window.API.fetchTrips();
        const users = await window.API.adminGetUsers();
        const t = (trips || []).find(x => x.id === id);
        if (!t) { Toast.show('Trajet introuvable', 'error'); return; }
        const driver = (users || []).find(u => u.id === t.driverId);
        const modal = document.getElementById('generic-modal');
        if (!modal) { Toast.show('Modal non disponible', 'error'); return; }
        document.getElementById('generic-modal-body').innerHTML = `
            <div style="padding:24px">
                <div style="font-size:11px;color:var(--text-muted);font-weight:800;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px">Trajet #${t.id}</div>
                <div style="font-size:20px;font-weight:800;margin-bottom:14px">${safeEsc(t.departure)} → ${safeEsc(t.arrival)}</div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:18px">
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Chauffeur</div><div>${safeEsc(driver?.name || '#'+t.driverId)}</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Date</div><div>${t.startDate ? new Date(t.startDate).toLocaleString('fr-FR') : '—'}</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Places</div><div>${t.availableSeats ?? 0} / ${t.totalSeats ?? '?'}</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Prix</div><div>${(t.price || 0).toFixed(1)} TND</div></div>
                    <div><div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase">Statut</div><div><span class="status-chip ${(t.status || '').toLowerCase()}">${safeEsc(t.status)}</span></div></div>
                </div>
                <button class="btn btn-outline btn-full" onclick="document.getElementById('generic-modal').classList.remove('open')">Fermer</button>
            </div>`;
        modal.classList.add('open');
    };

    // ─── Broadcast action ────────────────────────────────────────────────────
    window.adminSubmitBroadcast = async function () {
        const ta = document.getElementById('broadcast-input');
        const msg = (ta?.value || '').trim();
        if (!msg) { Toast.show('Message vide', 'error'); return; }
        if (!confirm(`Envoyer ce message à TOUS les utilisateurs ?\n\n"${msg.slice(0, 200)}"`)) return;
        const res = await window.API.broadcastMessage(msg);
        if (res.error) { Toast.show(res.error, 'error'); return; }
        localStorage.setItem('cov_admin_last_broadcast', JSON.stringify({ at: Date.now(), preview: msg.slice(0, 80) }));
        Toast.show('Diffusion envoyée');
        renderAdminPanel('broadcast');
    };

    // ─── New actions ─────────────────────────────────────────────────────────

    window.adminConfirmDeleteUser = function (id, name) {
        showConfirm({
            icon: '🗑️',
            title: `Supprimer ${name} ?`,
            body: 'Cette action est irréversible. Tous les trajets et données associés seront supprimés.',
            okLabel: 'Supprimer définitivement',
            okClass: 'btn-danger',
            onOk: async function () {
                const res = await window.API.adminDeleteUser(id);
                Toast.show(res.success ? `${name} supprimé` : (res.error || 'Erreur'), res.success ? 'success' : 'error');
                if (res.success) renderAdminPanel('users');
            }
        });
    };

    window.kycLoadDoc = async function (userId) {
        const el = document.getElementById(`kyc-doc-${userId}`);
        if (!el) return;
        el.innerHTML = '<div style="font-size:12px;color:var(--text-muted)">Chargement…</div>';
        const res = await window.API.adminGetIdDocument(userId);
        if (res.document) {
            el.innerHTML = `
                <div style="font-size:11px;color:var(--text-muted);margin-bottom:6px;font-weight:700;text-transform:uppercase">Pièce d'identité soumise</div>
                <img src="${res.document}" style="width:100%;max-height:220px;object-fit:contain;border-radius:8px;border:1px solid var(--border);background:var(--bg)">`;
        } else {
            el.innerHTML = '<div style="font-size:12px;color:var(--error);font-style:italic">Aucun document soumis.</div>';
        }
    };

    window.adminRequestIdResubmit = async function (driverId) {
        const res = await window.API.adminRequestIdResubmit(driverId);
        Toast.show(res.success ? 'Demande envoyée au chauffeur' : (res.error || 'Erreur'), res.success ? 'success' : 'error');
        if (res.success) renderAdminPanel('kyc');
    };

    window.adminReloadAudit = function () { renderAdminPanel('audit'); };

    window.dismissAllReports = async function (ids) {
        await Promise.all(ids.map(id => window.API.adminDismissReport(id)));
        Toast.show('Signalements ignorés');
        renderAdminPanel('reports');
    };

    // ─── Existing wrappers (preserved) ───────────────────────────────────────
    window.dismissReport = async function (id) {
        await window.API.adminDismissReport(id);
        renderAdminPanel(window._adminTab);
    };
    window.adminApproveId = async function (driverId) {
        const res = await window.API.adminApproveId(driverId);
        Toast.show(res.success ? 'Identité approuvée ✔' : (res.error || 'Erreur'), res.success ? 'success' : 'error');
        renderAdminPanel('kyc');
    };
    window.adminRejectId = async function (driverId) {
        const res = await window.API.adminRejectId(driverId);
        Toast.show(res.success ? 'Rejeté' : (res.error || 'Erreur'), res.success ? 'success' : 'error');
        renderAdminPanel('kyc');
    };

    window.openResolveDisputeModal = function (disputeId) {
        const html = `
            <div style="padding:24px">
                <h2 style="font-size:18px;font-weight:800;margin-bottom:12px">⚖️ Résoudre le litige #${disputeId}</h2>
                <input type="number" id="dispute-refund-input" class="input" placeholder="Remboursement (TND, 0 = aucun)" style="margin-bottom:12px;width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:8px" min="0" step="0.5">
                <textarea id="dispute-note-input" class="input" rows="3" placeholder="Note admin…" style="resize:vertical;margin-bottom:16px;width:100%;padding:10px;border:1px solid var(--border);border-radius:8px;font-family:inherit"></textarea>
                <button class="btn btn-lime btn-full" onclick="submitResolveDispute(${disputeId})">Confirmer la résolution</button>
            </div>`;
        const modal = document.getElementById('generic-modal');
        if (modal) { document.getElementById('generic-modal-body').innerHTML = html; modal.classList.add('open'); }
    };
    window.submitResolveDispute = async function (disputeId) {
        const refund = parseFloat(document.getElementById('dispute-refund-input')?.value) || 0;
        const note   = document.getElementById('dispute-note-input')?.value?.trim() || '';
        const res = await window.API.adminResolveDispute(disputeId, note, refund);
        document.getElementById('generic-modal')?.classList.remove('open');
        Toast.show(res.message || res.error || 'Traité', res.error ? 'error' : 'success');
        renderAdminPanel('reports');
    };
    window.adminDismissDispute = async function (disputeId) {
        const res = await window.API.adminDismissDispute(disputeId, 'Clôturé sans action');
        Toast.show(res.message || res.error || 'Clôturé', res.error ? 'error' : 'success');
        renderAdminPanel('reports');
    };

    window.openCreatePromoModal = function () {
        const html = `
            <div style="padding:24px">
                <h2 style="font-size:18px;font-weight:800;margin-bottom:12px">🎁 Nouveau code promo</h2>
                <input type="text"   id="promo-code-input"    placeholder="Code (ex: ETE2025)"                 style="display:block;width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:8px;margin-bottom:10px;text-transform:uppercase">
                <input type="number" id="promo-pct-input"     placeholder="Remise % (0–1, ex: 0.1 = 10%)"      style="display:block;width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:8px;margin-bottom:10px" step="0.05" min="0" max="1">
                <input type="number" id="promo-fixed-input"   placeholder="Remise fixe TND (ex: 5)"            style="display:block;width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:8px;margin-bottom:10px" step="0.5" min="0">
                <input type="number" id="promo-maxuses-input" placeholder="Utilisations max (ex: 100)"         style="display:block;width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:8px;margin-bottom:16px" min="1">
                <button class="btn btn-lime btn-full" onclick="submitCreatePromo()">Créer le code</button>
            </div>`;
        const modal = document.getElementById('generic-modal');
        if (modal) { document.getElementById('generic-modal-body').innerHTML = html; modal.classList.add('open'); }
    };
    window.submitCreatePromo = async function () {
        const code     = document.getElementById('promo-code-input')?.value?.trim().toUpperCase();
        const pct      = parseFloat(document.getElementById('promo-pct-input')?.value)     || 0;
        const fixed    = parseFloat(document.getElementById('promo-fixed-input')?.value)   || 0;
        const maxUses  = parseInt(document.getElementById('promo-maxuses-input')?.value)   || 100;
        if (!code) { Toast.show('Code requis', 'error'); return; }
        const res = await window.API.adminCreatePromoCode(code, pct, fixed, maxUses);
        document.getElementById('generic-modal')?.classList.remove('open');
        Toast.show(res.message || res.error || 'Créé', res.error ? 'error' : 'success');
        renderAdminPanel('promos');
    };
    window.adminOpenPromoDetail = function (code, disc, uses, active, id) {
        const modal = document.getElementById('generic-modal');
        if (!modal) { Toast.show('Modal non disponible', 'error'); return; }
        document.getElementById('generic-modal-body').innerHTML = `
            <div style="padding:24px">
                <div style="font-size:11px;color:var(--text-muted);font-weight:800;text-transform:uppercase;letter-spacing:0.5px;margin-bottom:6px">Code promo</div>
                <div style="font-size:28px;font-weight:900;font-family:monospace;letter-spacing:4px;margin-bottom:18px">${safeEsc(code)}</div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-bottom:20px">
                    <div>
                        <div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase;margin-bottom:4px">Remise</div>
                        <div style="font-size:15px;font-weight:700">${safeEsc(disc)}</div>
                    </div>
                    <div>
                        <div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase;margin-bottom:4px">Utilisations</div>
                        <div style="font-size:15px;font-weight:700">${safeEsc(uses)}</div>
                    </div>
                    <div>
                        <div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase;margin-bottom:4px">Statut</div>
                        <div><span class="status-chip ${active ? 'active' : 'cancelled'}">${active ? 'Actif' : 'Expiré / Désactivé'}</span></div>
                    </div>
                    <div>
                        <div style="font-size:11px;color:var(--text-muted);font-weight:700;text-transform:uppercase;margin-bottom:4px">ID interne</div>
                        <div style="font-size:13px;color:var(--text-muted)">#${id}</div>
                    </div>
                </div>
                <div style="display:flex;gap:8px">
                    ${active ? `<button class="btn btn-danger btn-full" onclick="adminDeactivatePromo(${id});document.getElementById('generic-modal').classList.remove('open')">Désactiver ce code</button>` : ''}
                    <button class="btn btn-outline btn-full" onclick="document.getElementById('generic-modal').classList.remove('open')">Fermer</button>
                </div>
            </div>`;
        modal.classList.add('open');
    };

    window.adminDeactivatePromo = async function (id) {
        if (!id || isNaN(id)) { Toast.show('ID invalide', 'error'); return; }
        const res = await window.API.adminDeactivatePromoCode(Number(id));
        Toast.show(res.success ? 'Code désactivé' : (res.error || 'Erreur'), res.success ? 'success' : 'error');
        renderAdminPanel('promos');
    };

    // ─── Register ────────────────────────────────────────────────────────────
    window.Router.register('admin', renderAdminPanel);
    window.Sidebar.register({ view: 'admin', icon: ICO.shield, tip: 'Administration', role: 'ADMIN', order: 90 });
})();
