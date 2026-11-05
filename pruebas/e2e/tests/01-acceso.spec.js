// E2E-01…04 · Ingreso del personal, errores y protección de pantallas.
const { test, expect } = require('@playwright/test');
const { ingresar } = require('./apoyo');

test.describe('Acceso al sistema (RF-SEG-01)', () => {
  test('E2E-01 · el administrador ingresa y ve el panel con indicadores', async ({ page }) => {
    await ingresar(page, 'admin');
    await expect(page).toHaveTitle(/SIGSF/i);
    await expect(page.locator('[data-nav="usuarios.html"]')).toBeVisible();
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: '../reportes/capturas/e2e01_panel_admin.png' });
  });

  test('E2E-02 · una clave incorrecta muestra un aviso y no entra', async ({ page }) => {
    await page.goto('/login.html');
    await page.fill('#user', 'admin@sigsf.com');
    await page.fill('#pass', 'clave-equivocada');
    await page.click('#btnEntrar');
    await expect(page.locator('#aviso')).toBeVisible();
    await expect(page.locator('#aviso')).toContainText(/incorrect/i);
    await expect(page).toHaveURL(/login\.html/);
    await page.screenshot({ path: '../reportes/capturas/e2e02_login_fallido.png' });
  });

  test('E2E-03 · sin sesión, el panel redirige al login', async ({ page }) => {
    await page.goto('/index.html');
    await page.waitForURL(/login\.html/);
    await expect(page.locator('#loginForm')).toBeVisible();
  });

  test('E2E-04 · cerrar sesión devuelve al login y el token deja de servir', async ({ page }) => {
    await ingresar(page, 'cajero');
    const token = await page.evaluate(() => sessionStorage.getItem('sigsf_token'));
    expect(token).toBeTruthy();
    await page.evaluate(() => document.getElementById('logoutBtn').click());
    await page.waitForURL(/login\.html/);
    const r = await page.request.get('/api/clientes', { headers: { Authorization: 'Bearer ' + token } });
    expect(r.status()).toBe(401);
  });
});
