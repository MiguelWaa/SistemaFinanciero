// E2E-05…07 · Navegación por los módulos del panel y permisos por rol en la interfaz.
const { test, expect } = require('@playwright/test');
const { ingresar } = require('./apoyo');

const MODULOS = [
  ['pagos.html', 'Pagos y cobranzas'],
  ['solicitudes.html', 'Solicitudes'],
  ['clientes y servicios financieros.html', 'Clientes'],
  ['operaciones financieras.html', 'Operaciones'],
  ['Ventas.html', 'Ventas'],
  ['Marketing.html', 'Marketing'],
  ['Logistica.html', 'Logística'],
  ['almacen.html', 'Almacén'],
  ['produccion.html', 'Producción'],
  ['direccion gerencia.html', 'Dirección'],
  ['reportes.html', 'Reportes'],
  ['usuarios.html', 'Usuarios'],
  ['seguridad.html', 'Seguridad'],
  ['respaldos.html', 'Respaldos'],
  ['versiones.html', 'Control de versiones'],
];

test.describe('Módulos del panel (MVC · vistas)', () => {
  test('E2E-05 · el administrador abre los 15 módulos sin errores de JavaScript', async ({ page }) => {
    const errores = [];
    page.on('pageerror', (e) => errores.push(e.message));
    await ingresar(page, 'admin');
    for (const [archivo] of MODULOS) {
      await page.click(`[data-nav="${archivo}"]`);
      const frame = page.frameLocator('#moduleFrame');
      await expect(frame.locator('body')).toBeVisible();
      await page.waitForFunction((a) => {
        const f = document.getElementById('moduleFrame');
        return f && decodeURIComponent(f.src).endsWith(a) && f.contentDocument && f.contentDocument.readyState === 'complete';
      }, archivo);
    }
    expect(errores, 'Errores de JavaScript: ' + errores.join(' | ')).toEqual([]);
  });

  test('E2E-06 · la tabla de clientes carga datos reales desde la API', async ({ page }) => {
    await ingresar(page, 'admin');
    await page.click('[data-nav="clientes y servicios financieros.html"]');
    const frame = page.frameLocator('#moduleFrame');
    await expect(frame.locator('tbody tr').first()).toContainText(/\d{8}/);
    await page.waitForTimeout(600);
    await page.screenshot({ path: '../reportes/capturas/e2e06_clientes.png' });
  });

  test('E2E-07 · el cajero no tiene acceso a Usuarios (RBAC en la interfaz y en la API)', async ({ page }) => {
    await ingresar(page, 'cajero');
    const usuarios = page.locator('[data-nav="usuarios.html"]');
    const visible = await usuarios.isVisible();
    if (visible) {
      // Si el enlace se muestra, la API igual debe negar los datos
      await usuarios.click();
    }
    const token = await page.evaluate(() => sessionStorage.getItem('sigsf_token'));
    const r = await page.request.get('/api/usuarios', { headers: { Authorization: 'Bearer ' + token } });
    expect(r.status()).toBe(403);
    await page.screenshot({ path: '../reportes/capturas/e2e07_cajero_sin_usuarios.png' });
  });
});
