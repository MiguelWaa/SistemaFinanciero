// E2E-11…12 · Seguridad vista desde el navegador: XSS y cabeceras.
const { test, expect } = require('@playwright/test');
const { ingresar, tokenApi } = require('./apoyo');

test('E2E-11 · un nombre con código HTML se muestra como texto (sin XSS)', async ({ page, request }) => {
  const token = await tokenApi(request, 'admin');
  const doc = '6' + String(Date.now()).slice(-7);
  const carga = `<img src=x onerror="window.__xss=1">XSS-${doc}`;
  const r = await request.post('/api/clientes', { headers: { Authorization: 'Bearer ' + token }, data: { nombres: carga, documento: doc } });
  expect(r.ok()).toBeTruthy();

  let dialogo = false;
  page.on('dialog', async (d) => { dialogo = true; await d.dismiss(); });
  await ingresar(page, 'admin');
  await page.click('[data-nav="clientes y servicios financieros.html"]');
  const frame = page.frameLocator('#moduleFrame');
  await frame.getByPlaceholder(/Buscar/i).first().fill(doc);
  const celda = frame.getByText(`XSS-${doc}`).first();
  await expect(celda).toBeVisible();
  // El texto se ve literal: la etiqueta <img> NO se convirtió en un elemento
  await expect(celda).toContainText('<img');
  await celda.scrollIntoViewIfNeeded();
  const modulo = page.frames().find((f) => decodeURIComponent(f.url()).includes('clientes y servicios'));
  expect(await modulo.evaluate(() => window.__xss)).toBeUndefined();
  expect(dialogo).toBe(false);
  await page.screenshot({ path: '../reportes/capturas/e2e11_xss_como_texto.png' });
});

test('E2E-12 · las páginas llegan con CSP y anti-clickjacking', async ({ page }) => {
  const resp = await page.goto('/login.html');
  const h = resp.headers();
  expect(h['content-security-policy']).toContain("default-src 'self'");
  expect(h['x-frame-options']).toBe('SAMEORIGIN');
  expect(h['x-content-type-options']).toBe('nosniff');
});
