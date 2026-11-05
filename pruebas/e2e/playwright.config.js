// ============================================================
//  SIGSF · Plan de pruebas v5.5 · Pruebas de SISTEMA en navegador (Playwright)
//  Requisito: el servidor SIGSF corriendo en http://localhost:8091
//  (NetBeans F6 con persistencia=memoria, o scripts del plan de pruebas).
//    npm install            (una vez)
//    npx playwright install chromium   (una vez, descarga el navegador)
//    npm run test:e2e
// ============================================================
const { defineConfig, devices } = require('@playwright/test');

module.exports = defineConfig({
  testDir: './tests',
  timeout: 45_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  workers: 1,
  retries: 0,
  outputDir: '../reportes/playwright-resultados',
  reporter: [
    ['list'],
    ['html', { outputFolder: '../reportes/playwright', open: 'never' }],
    ['junit', { outputFile: '../reportes/playwright/junit-e2e.xml' }],
  ],
  use: {
    baseURL: process.env.SIGSF_URL || 'http://localhost:8091',
    locale: 'es-PE',
    timezoneId: 'America/Lima',
    screenshot: 'on',
    trace: 'retain-on-failure',
    contextOptions: { reducedMotion: 'reduce' },
  },
  projects: [
    { name: 'escritorio-chromium', use: { ...devices['Desktop Chrome'], viewport: { width: 1366, height: 768 } }, testIgnore: /responsive\.spec\.js/ },
    { name: 'movil-chromium', use: { ...devices['Pixel 7'] }, testMatch: /responsive\.spec\.js/ },
  ],
});
