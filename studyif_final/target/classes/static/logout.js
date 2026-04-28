document.getElementById('btn-logout').addEventListener('click', (e) => {
    e.preventDefault();
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';
    document.body.appendChild(form);
    form.submit();
});