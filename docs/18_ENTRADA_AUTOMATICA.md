# SIGSF 5.10.0 · Entrada automática a pantalla completa

Actualización del 10/11/2026 (Lima). Ajuste solicitado: presentar el video como entrada del sistema, ocupando toda la pantalla y con sonido automático, sin pulsar dentro de la página.

## Iniciar en Windows

1. Descomprimir todo el ZIP en una carpeta nueva.
2. Cerrar la consola de la versión anterior que ocupa el puerto 8091.
3. Hacer doble clic en **INICIAR_DEMO.bat**. Requiere Java 17+, Google Chrome y Windows PowerShell 5.1 (incluido en Windows 10).
4. El iniciador espera a que responda el servidor y abre una ventana de Chrome con pantalla completa y reproducción automática con sonido habilitada para esa sesión.
5. Al terminar el video, aparece la portada bancaria. Se mantienen los accesos de cliente y personal.

El doble clic inicia la aplicación; no se requiere un segundo clic dentro del video cuando Chrome admite los argumentos y la salida de audio está habilitada. El modo de movimiento reducido conserva la opción de reproducción manual.

**F11** sale de pantalla completa. **Alt+F4** cierra la ventana. Al terminar, cerrar esa ventana y pulsar Enter en la consola para detener la demostración. Los datos de demostración continúan en memoria; no se modifica la base PostgreSQL.

## Qué cambió

- El video ocupa la ventana completa, sin tarjeta, encabezado ni controles nativos permanentes.
- En escritorio se amplía el encuadre central para ocultar las franjas negras incorporadas al clip. Puede recortar fondo periférico; los bytes del MP4 original permanecen intactos. En orientación vertical se mantiene el encuadre completo.
- Al reproducirse con sonido, los controles se ocultan. Mover el mouse, tocar o usar el teclado permite mostrarlos; pausa y bloqueo de sonido los mantienen visibles.
- La transición, Escape para omitir, repetición y prevención de audio simultáneo se conservan.
- No cambian fuentes Java, reglas financieras, autenticación, DAO, datos ni esquema respecto a 5.9.0.

## Sonido automático y navegador

Una página normal no puede garantizar reproducción audible sin interacción en todos los navegadores. Por eso el iniciador local abre Chrome con:

```text
--autoplay-policy=no-user-gesture-required
--start-fullscreen
--user-data-dir=<perfil temporal propio de esta demostración>
```

La opción de autoplay está documentada por Chrome para pruebas locales. Se utiliza un perfil nuevo por sesión para evitar que un proceso anterior ignore los argumentos. No se editan preferencias del perfil habitual, registro, políticas de equipo ni configuración global del navegador. No se deshabilitan comprobaciones de certificados, sandbox o seguridad de origen.

Si se abre la dirección manualmente en una ventana habitual, siguen vigentes sus permisos de autoplay y podría aparecer el botón de sonido. Para la entrada automática solicitada se debe usar **INICIAR_DEMO.bat**. Una política administrada, el volumen del sistema, un dispositivo sin audio o la preferencia de movimiento reducido pueden impedir el comportamiento esperado.

El archivo BAT ejecuta el script local con `-ExecutionPolicy Bypass` solo para ese proceso de PowerShell, sin cambiar políticas persistentes. El perfil temporal se elimina si Chrome ya está cerrado al detener el servidor; si sigue abierto, se conserva en la carpeta temporal del usuario hasta su limpieza habitual. La sesión de demostración no utiliza las contraseñas ni sesiones de Chrome del usuario.

Referencias consultadas:

- [Chrome: política de autoplay y opción para pruebas locales](https://developer.chrome.com/blog/autoplay/).
- [Chromium: perfiles y directorio de datos independiente](https://www.chromium.org/developers/creating-and-using-profiles/).

## Archivos

| Archivo | Responsabilidad |
|---|---|
| INICIAR_DEMO.bat | Punto único de inicio para Windows |
| scripts/iniciar_demo_windows.ps1 | Localizar Java/Chrome, comprobar puerto, iniciar servidor y abrir la presentación |
| web/welcome.html | Entrada sin recuadro ni encabezado |
| web/sigsf-banco.css | Video a toda la ventana y controles superpuestos |
| web/pages/welcome-1.js | Inicio, sonido, transición y controles temporales |
| pruebas/frontend/bienvenida.test.cjs | 31 casos de audio, video y controles |

El iniciador evita detener procesos ajenos. Si 8091 está ocupado, informa el problema y se detiene. El script de Linux/macOS conserva su arranque habitual y no configura automáticamente el navegador.

## Evidencia de esta revisión

Código probado: `f5026259f4d0dfd6698a60124e88bea5b846a35a`.

| Verificación | Resultado |
|---|---|
| Flujo de audio/video con DOM simulado | 31/31 aprobadas |
| JUnit selectivo: CSP, audio y video HTTP | 11/11 aprobadas |
| Sintaxis de scripts de bienvenida | Aprobada |
| Backend respecto a v5.9.0 | Sin cambios; clases previamente compiladas |
| Inicio con PowerShell y Chrome en Windows | Pendiente de ejecución en Windows |
| Revisión visual y escucha en navegador real | Pendiente por restricciones del entorno |

Los resultados completos de 5.9.0 (224 JUnit, 63 aserciones API, 24 comprobaciones adicionales) se conservan como antecedente; no se presentan como reejecutados en 5.10.0. Los estados previos de E2E, JDBC, ZAP y SCA permanecen documentados.

Los informes actuales están en `evidencias/v5.10.0/`. La comprobación HTTP del ejecutable final se registra en `dist/evidencia_empaquetado.json`.

## Historial

Rama de trabajo `feature/inicio-automatico-5.10`, integración `release/5.10.0`, etiqueta `v5.10.0`. Se conserva `v5.9.0` para volver a la entrada anterior. No se publican cambios en GitHub automáticamente.
