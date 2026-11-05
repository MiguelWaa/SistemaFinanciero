// Funciones de apoyo compartidas por las pruebas E2E del SIGSF.
const { expect } = require('@playwright/test');

const PERSONAL = {
  admin: { email: 'admin@sigsf.com', clave: 'admin123' },
  cajero: { email: 'lucia.ramos@sigsf.com', clave: 'lucia123' },
  analista: { email: 'jorge.torres@sigsf.com', clave: 'jorge123' },
};

/** Ingresa por la pantalla de login como lo haría una persona. */
async function ingresar(page, quien = 'admin') {
  const u = PERSONAL[quien];
  await page.goto('/login.html');
  await page.fill('#user', u.email);
  await page.fill('#pass', u.clave);
  await page.click('#btnEntrar');
  await page.waitForURL(/index\.html/);
  await expect(page.locator('[data-nav="home"]')).toBeVisible();
}

/** Token por API (para preparar datos sin pasar por la interfaz). */
async function tokenApi(request, quien = 'admin') {
  const u = PERSONAL[quien];
  const r = await request.post('/api/auth/login', { data: { email: u.email, password: u.clave } });
  expect(r.ok()).toBeTruthy();
  return (await r.json()).token;
}

module.exports = { PERSONAL, ingresar, tokenApi };
