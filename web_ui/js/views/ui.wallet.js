async function renderWalletPanel() {
    const u = window.API.getCurrentUser();
    const balance = u ? (u.balance || 0) : 0;
    const reserved = u ? (u.reservedBalance || 0) : 0;
    const withdrawable = Math.max(0, balance - reserved);

    // Feature 10: driver earnings + Feature 6: cancellation rate
    let earnings = null;
    if (u && u.role === 'DRIVER') {
        try { earnings = await window.API.getDriverEarnings(); } catch (e) {}
    }
    const txs = (DB.get('cov_wallet') || []).filter(t => t.userId === u.id); // Payment history fallback
    const cardNum = '4*** **** **** ' + String(u.id * 1234 + 5678).slice(-4);
    
    // FETCH REAL PAYMENT METHODS FROM BACKEND
    const paymentMethods = await window.API.getPaymentMethods();
    
    document.getElementById('view-wallet').innerHTML = `
        <div class="panel-header"><h1 class="panel-title">Portefeuille</h1></div>
        <div class="panel-body" style="padding-top:8px">
            <div class="wallet-card">
                <div class="wallet-card-logo">CovoitDark Pay</div>
                <div class="wallet-card-number">${cardNum}</div>
                <div class="wallet-card-row">
                    <div><div class="wallet-card-label">Titulaire</div><div class="wallet-card-value">${u.name.toUpperCase()}</div></div>
                    <div><div class="wallet-card-label">Solde</div><div class="wallet-balance-big">${balance.toFixed(2)}<span style="font-size:16px;opacity:0.6;margin-left:4px">TND</span></div></div>
                </div>
                ${reserved > 0 ? `
                <div style="font-size:12px; margin-top:16px; padding-top:12px; border-top:1px solid rgba(255,255,255,0.1);">
                    <div style="display:flex; justify-content:space-between; margin-bottom:6px"><span class="wallet-card-label">Réservé (trajets actifs)</span> <b style="color:#fff">${reserved.toFixed(2)} TND</b></div>
                    <div style="display:flex; justify-content:space-between; font-weight:800"><span style="color:var(--lime)">Disponible au retrait</span> <b style="color:var(--lime)">${withdrawable.toFixed(2)} TND</b></div>
                </div>` : ''}
            </div>
            <div style="display:flex;gap:8px;margin-bottom:20px">
                <button class="btn btn-lime btn-full" onclick="openFundsModal()">+ Ajouter</button>
                <button class="btn btn-outline btn-full" style="border-color:var(--border)" onclick="openWithdrawModal()">Retirer</button>
            </div>

            ${earnings ? `
            <div style="background:var(--bg);border:1px solid var(--border);border-radius:var(--radius-sm);padding:16px;margin-bottom:20px">
                <div style="font-size:11px;font-weight:800;color:var(--text-muted);text-transform:uppercase;margin-bottom:12px">Tableau de bord Chauffeur</div>
                <div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">
                    <div style="text-align:center;padding:12px;background:var(--surface);border-radius:8px">
                        <div style="font-size:20px;font-weight:900;color:var(--lime)">${(earnings.moneyEarned||0).toFixed(2)}</div>
                        <div style="font-size:10px;color:var(--text-muted);margin-top:2px">TND gagnés</div>
                    </div>
                    <div style="text-align:center;padding:12px;background:var(--surface);border-radius:8px">
                        <div style="font-size:20px;font-weight:900;color:${(earnings.cancellationRate||0) >= 0.3 ? 'var(--error)' : 'var(--success)'}">${((earnings.cancellationRate||0)*100).toFixed(0)}%</div>
                        <div style="font-size:10px;color:var(--text-muted);margin-top:2px">Taux d'annulation</div>
                    </div>
                </div>
                ${(earnings.cancellationRate||0) >= 0.3 ? `
                    <div style="margin-top:10px;padding:8px 10px;background:rgba(239,68,68,0.08);border:1px solid rgba(239,68,68,0.2);border-radius:6px;font-size:12px;color:var(--error)">
                        ${ICO.warning} Votre taux d'annulation est élevé. Cela peut affecter votre visibilité.
                    </div>
                ` : ''}
            </div>
            ` : ''}
            
            <div class="section-title" style="margin-top:24px">Historique des transactions</div>
            <div style="display:flex;flex-direction:column;gap:8px">${txs.length === 0 ? '<div class="empty-state"><p>Aucun mouvement</p></div>' : txs.map(t => `<div class="car-card" style="padding:10px 14px"><div style="width:32px;height:32px;background:var(--bg);border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:16px">${t.type === 'CREDIT' ? ICO.check : ICO.x}</div><div class="car-info"><div class="car-model">${t.title}</div><div class="car-plate">${new Date(t.date).toLocaleDateString('fr-FR')}</div></div><div style="font-weight:800;color:${t.type === 'CREDIT' ? 'var(--success)' : 'var(--text)'}">${t.type === 'CREDIT' ? '+' : '-'}${t.amount.toFixed(1)}</div></div>`).join('')}</div>
            
            <div class="section-title" style="margin-top:24px; display:flex; justify-content:space-between">Mes Moyens de Paiement <button class="btn btn-ghost btn-xs" onclick="openSelectPaymentMethodModal()">+ Ajouter</button></div>
            <div style="display:flex;flex-direction:column;gap:8px">
                ${paymentMethods.length === 0 ? '<div class="empty-state"><p>Aucun moyen enregistré</p></div>' : paymentMethods.map(m => `
                    <div class="car-card" style="padding:10px 14px">
                        <div style="width:36px;height:24px;background:var(--dark);color:white;border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:10px;font-weight:900">${m.brand.toUpperCase()}</div>
                        <div class="car-info">
                            <div class="car-model">${m.brand} •••• ${m.last4}</div>
                            <div class="car-plate">${m.type === 'Card' ? 'Expire le ' + m.exp : 'Compte vérifié'}</div>
                        </div>
                        <button class="btn btn-ghost btn-xs" onclick="deletePaymentMethod(${m.id})">${ICO.trash}</button>
                    </div>
                `).join('')}
            </div>
        </div>`;
}

window.deletePaymentMethod = async function(id) {
    if (confirm("Supprimer ce moyen de paiement ?")) {
        const res = await window.API.deletePaymentMethod(id);
        if (res.success) {
            Toast.show("Moyen de paiement supprimé");
            renderWalletPanel();
        } else {
            Toast.show(res.error || "Erreur", "error");
        }
    }
};

window.Router.register("wallet", renderWalletPanel);
window.Sidebar.register({ view: 'wallet', icon: ICO.wallet, tip: 'Portefeuille', order: 5 });
