// E2E-13 · Compatibilidad móvil del Portal del Cliente (proyecto "movil-chromium").
const { test, expect } = require('@playwright/test');

test('E2E-13 · el portal se usa en celular sin desplazamiento horizontal', async ({ page }) => {
  await page.goto('/portal.html');
  await page.fill('#liDoc', '70000001');
  await page.fill('#liPass', '70000001');
  await page.click('#btnLogin');
  await page.waitForURL(/portal-inicio\.html/);
  await page.waitForLoadState('networkidle');
  const desborde = await page.evaluate(() => document.documentElement.scrollWidth - window.innerWidth);
  expect(desborde).toBeLessThanOrEqual(1);
  await page.screenshot({ path: '../reportes/capturas/e2e13_portal_movil.png' });
});
