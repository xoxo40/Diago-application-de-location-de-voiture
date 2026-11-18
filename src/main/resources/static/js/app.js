document.addEventListener('DOMContentLoaded', () => {
  const fmt = n => Math.round(n).toLocaleString('fr-FR').replace(/ /g, ' ') + ' Ar';

  // Galerie de la fiche voiture
  const main = document.getElementById('galleryMain');
  document.querySelectorAll('.thumbs img').forEach(t => t.addEventListener('click', () => {
    main.src = t.dataset.full;
    document.querySelectorAll('.thumbs img').forEach(x => x.classList.remove('on'));
    t.classList.add('on');
  }));

  // Mode "avec / sans chauffeur" : affiche le bloc d'information correspondant
  const modeRadios = document.querySelectorAll('input[name="mode"]');
  const syncMode = () => {
    const sel = document.querySelector('input[name="mode"]:checked');
    document.querySelectorAll('.mode-info').forEach(b => b.classList.toggle('show', sel && b.dataset.mode === sel.value));
  };
  modeRadios.forEach(r => r.addEventListener('change', syncMode));
  syncMode();

  // Options : total mis à jour en direct
  const form = document.getElementById('optionsForm');
  if (form) {
    const rental = +form.dataset.rental, days = +form.dataset.days, driver = +form.dataset.driver;
    const out = document.querySelectorAll('.live-total');
    const compute = () => {
      let total = rental;
      const m = form.querySelector('input[name="mode"]:checked');
      if (m && m.value === 'WITH_DRIVER') total += driver * days;
      form.querySelectorAll('.switch input:checked').forEach(c => {
        total += (+c.dataset.price) * (c.dataset.perday === 'true' ? days : 1);
      });
      out.forEach(o => o.textContent = fmt(total));
    };
    form.addEventListener('change', compute);
    compute();
  }

  // Paiement : affiche les champs de la méthode choisie
  const pay = document.querySelectorAll('input[name="method"]');
  const syncPay = () => {
    const sel = document.querySelector('input[name="method"]:checked');
    document.querySelectorAll('.pay-detail').forEach(d => d.classList.toggle('show', sel && d.dataset.method === sel.value));
  };
  pay.forEach(r => r.addEventListener('change', syncPay));
  syncPay();

  // Filtres : envoi automatique
  document.querySelectorAll('form.auto-submit select').forEach(s => s.addEventListener('change', () => s.form.submit()));

  // Confirmations
  document.querySelectorAll('form[data-confirm]').forEach(f => f.addEventListener('submit', e => {
    if (!confirm(f.dataset.confirm)) e.preventDefault();
  }));
});
