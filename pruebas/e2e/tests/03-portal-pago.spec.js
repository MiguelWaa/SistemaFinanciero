// E2E-08…10 · Portal del Cliente: ingreso, pago con QR y confirmación en caja.
const { test, expect } = require('@playwright/test');
const { ingresar } = require('./apoyo');

test.describe.serial('Portal del Cliente y pagos con QR (RF-PAG)', () => {
  let numero;

  test('E2E-08 · el cliente ingresa con su documento', async ({ page }) => {
    await page.goto('/portal.html');
    await page.fill('#liDoc', '70000002');
    await page.fill('#liPass', '70000002');
    await page.click('#btnLogin');
    await page.waitForURL(/portal-inicio\.html/);
    await expect(page.locator('#tabs')).toBeVisible();
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(800);
    await page.screenshot({ path: '../reportes/capturas/e2e08_portal_inicio.png' });
  });

  test('E2E-09 · paga una cuota con Yape y queda por verificar', async ({ page }) => {
    numero = String(Date.now()).slice(-8);
    await page.goto('/portal.html');
    await page.fill('#liDoc', '70000002');
    await page.fill('#liPass', '70000002');
    await page.click('#btnLogin');
    await page.waitForURL(/portal-inicio\.html/);
    await page.click('[data-tab="pagar"]');
    // Paso 1: trámite y billetera
    await page.locator('#listaPagables input[type="radio"], #listaPagables [role="radio"], #listaPagables label').first().click();
    await page.locator('#billeteras').getByText(/yape/i).first().click();
    await page.click('#btnPaso2');
    // Paso 2: QR de cobro
    await expect(page.locator('#ticket')).toBeVisible();
    await page.waitForTimeout(1200);
    await page.screenshot({ path: '../reportes/capturas/e2e09_portal_qr.png' });
    await page.click('#btnPaso3');
    // Paso 3: número de operación
    await page.fill('#pNumero', numero);
    await page.click('#btnRegistrar');
    await expect(page.locator('#pagoListo')).toBeVisible();
    await expect(page.locator('#pagoListo')).toContainText(/verific|registrad/i);
    await page.waitForTimeout(1200);
    await page.screenshot({ path: '../reportes/capturas/e2e09_pago_registrado.png' });
  });

  test('E2E-10 · caja ve el pago en la bandeja "Por verificar" y lo confirma', async ({ page }) => {
    await ingresar(page, 'cajero');
    await page.click('[data-nav="pagos.html"]');
    const frame = page.frameLocator('#moduleFrame');
    const tarjeta = frame.locator('article', { hasText: numero });
    await expect(tarjeta).toBeVisible();
    await page.screenshot({ path: '../reportes/capturas/e2e10_bandeja_por_verificar.png' });
    // Confirmar desde la interfaz: botón de la tarjeta y ventana de confirmación
    const respuesta = page.waitForResponse((r) => /\/api\/pagos\/\d+\/confirmar$/.test(r.url()));
    await tarjeta.getByRole('button', { name: /Confirmar pago/i }).click();
    await frame.getByRole('button', { name: /Sí, confirmar/i }).click();
    const r = await respuesta;
    expect(r.status()).toBe(200);
    expect((await r.json()).numeroComprobante).toMatch(/^RC-\d{6}$/);
    await expect(frame.locator('article', { hasText: numero })).toHaveCount(0);
    await page.screenshot({ path: '../reportes/capturas/e2e10_pago_confirmado.png' });
  });
});
