document.addEventListener('DOMContentLoaded', function () {
  const pwd = document.getElementById('password');
  const btn = document.getElementById('togglePwd');
  if (!pwd || !btn) return;
  btn.addEventListener('click', function (e) {
    e.preventDefault();
    const show = pwd.type === 'password';
    pwd.type = show ? 'text' : 'password';
    this.setAttribute('aria-pressed', show);
    this.textContent = show ? 'Ẩn' : 'Hiện';
  });
});
