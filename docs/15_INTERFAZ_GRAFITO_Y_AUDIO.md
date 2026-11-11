# SIGSF 5.7.0 · Interfaz grafito y bienvenida

Revisión visual solicitada sobre la entrega integrada 5.6.0. Fecha de validación: 11/11/2026 UTC.

## Iniciar en Windows

1. Descomprimir todo el ZIP en una carpeta nueva. No abrir el HTML con doble clic.
2. Cerrar la consola de una demostración anterior si ocupa el puerto 8091.
3. Abrir `INICIAR_DEMO.bat` dentro de `SistemaFinanciero` (requiere Java 17+).
4. Visitar `http://localhost:8091/`. Si se conserva el aspecto anterior, recargar con Ctrl+F5.
5. En la bienvenida, pulsar **Escuchar bienvenida** si el navegador no inicia el sonido.
6. Elegir **Soy personal de la entidad**. Cuenta de demostración: `admin@sigsf.com` / `admin123`.

El iniciador mantiene el modo de demostración en memoria. No modifica la base PostgreSQL existente. La ejecución habitual del proyecto mantiene su configuración y conexiones anteriores.

## Alcance implementado

- 26 páginas usan `web/sigsf-grafito.css`: inicio, acceso del personal y cliente, panel, módulos, Ayuda, portal y vistas de documentos.
- Fondos #080809, #141415 y #1b1d1f; superficies elevadas #26272d; bordes #34353c; texto secundario #acadae; acento #83c3ff.
- Menús compactos, tarjetas con radio de 8 px, controles de 4 px, estados legibles y foco de teclado visible. Formularios, tablas, modales y controles de archivos comparten el tema.
- Se retira de las páginas la hoja decorativa de gradientes, sombras y brillos. Se conservan iconos y confirmaciones; el tema desactiva las animaciones decorativas.
- Bootstrap 5.3.3 sigue como infraestructura local de rejilla, pestañas, diálogos y menús. Se reemplaza su **apariencia**, conservando los componentes funcionales.
- Tipografía del sistema con alternativas locales; las páginas ya no requieren Google Fonts. No se incorporan paquetes nuevos.
- Los comprobantes mantienen una hoja blanca y estilos de impresión; los QR conservan su fondo blanco.
- Sin cambios en reglas de negocio, endpoints de operaciones, DAO, DTO, cálculos, esquema o datos. En el backend solo se agrega el tipo MIME `audio/mpeg` al servidor de archivos estáticos.

## Audio original

`web/audio/bienvenida.mp3` es una copia byte a byte del MP3 entregado por el usuario, de aproximadamente 8,28 segundos. No se reemplazó por una voz sintética.

`web/sigsf-bienvenida.js` intenta reproducirlo al llegar a la bienvenida o a un acceso directo a login/panel/portal. El splash conserva su transición de 1,6 segundos, por lo que el audio se inicia en la página estable siguiente. Si la reproducción automática es rechazada, queda el botón **Escuchar bienvenida**. El botón permite pausar, reanudar y repetir; **×** cierra y detiene el audio. No intercepta el envío de formularios ni solicita acceso al micrófono.

Después de comenzar la reproducción, no vuelve a iniciarse automáticamente al cambiar de pantalla en la misma pestaña/sesión. Cambiar de página detiene el audio en curso; puede repetirse desde el control de la nueva página. Cerrar el control también evita nuevos inicios automáticos durante esa sesión. Los módulos embebidos no generan reproductores duplicados. Sin almacenamiento de sesión, los controles siguen funcionando, pero no puede conservarse la preferencia entre páginas.

La reproducción automática depende de la política y permisos del navegador. Referencias: [Autoplay guide](https://developer.mozilla.org/en-US/docs/Web/Media/Guides/Autoplay) y [HTMLMediaElement.play](https://developer.mozilla.org/en-US/docs/Web/API/HTMLMediaElement/play). No se redujo la CSP ni se agregaron excepciones para forzar el sonido.

## Validación ejecutada

Código probado: `212792e28838b2015021d5bf1b0d573306c57349`. Evidencias: `evidencias/v5.7.0/`.

| Comprobación | Resultado | Evidencia |
|---|---|---|
| Compilación Java y pruebas | Aprobada, bytecode 17 | compilar_fuentes.txt, compilar_pruebas.txt |
| JUnit | 216/216, incluidos 2 casos HTTP de audio | junit.txt |
| Audio: bloqueo automático, clic, pausa, reanudación, repetición, cierre, fallo, sesión, iframe e inicialización | 8/8 casos con DOM y reproductor simulados | frontend_audio.txt |
| Newman | 26 solicitudes, 63 aserciones, 0 fallos | newman.xml, newman.txt |
| Entradas y controles de seguridad adicionales | 24/24 | entradas_seguridad.json |
| Campos, identificadores, formularios y enlaces previos | Conservados en 26 páginas | contratos_frontend.json |
| JavaScript propio | Sintaxis aprobada | frontend_sintaxis.txt |
| Audio servido | MIME correcto, bytes idénticos, HEAD y ETag | AudioEstaticoTest dentro de JUnit |
| Revisión visual y escucha real en navegador | PENDIENTE por bloqueo del entorno | revision_visual.json |

El entorno disponible usa Java 21 con `-source 17 -target 17`; no permite verificar las APIs de Java 17 con `--release 17`. La ejecución normal del validador sigue requiriendo JDK completo y usa `--release 17`.

## Reproducir y revisar en el equipo de exposición

Desde la raíz `SistemaFinanciero`, en Git Bash:

```sh
python scripts/validar_proyecto.py
```

Requisitos: JDK completo 17+, Python 3.10+, Node 20+ y las dependencias de pruebas preparadas como indica README.md. Solo los nuevos casos del control de audio, sin instalar paquetes:

```sh
node --test pruebas/frontend/bienvenida.test.cjs
```

Revisión de aceptación pendiente en el navegador del equipo:

| Vista | Acción y resultado esperado |
|---|---|
| Bienvenida | Audio al permitirse; botón reproducir/pausar/cerrar funcional; ambos accesos disponibles |
| Login | Campos legibles, foco visible, mostrar contraseña y acceso habitual |
| Panel | Menú, indicadores y navegación de módulos legibles; audio sin duplicarse |
| Clientes y operaciones | Buscar, abrir un formulario y cancelarlo; controles y botones visibles |
| Pagos | Revisar filtros, tabla y ventana de detalle sin confirmar transacciones reales |
| Ayuda | Abrir una pregunta, enviar una consulta y verificar respuestas y controles de voz |
| Portal | Acceder con datos demo, revisar solicitudes y QR sin cambiar la lógica de pago |
| Documentos | Vista blanca; previsualización de impresión legible |
| Móvil | Repetir a 390 px: menú accesible, formularios dentro del ancho, tablas desplazables y audio sin superponer al asistente |

No se afirma aprobación visual ni de los 14 E2E pendientes. PostgreSQL y ZAP siguen pendientes de evidencia de ejecución en el entorno correspondiente. La auditoría npm de 5.6.0 se conserva sin reinterpretarla ni corregir dependencias como parte de este cambio visual.

## Organización y retorno

- Tema: `web/sigsf-grafito.css`.
- Audio y controles: `web/audio/bienvenida.mp3`, `web/sigsf-bienvenida.js`.
- Pruebas nuevas: `test/.../web/AudioEstaticoTest.java`, `pruebas/frontend/bienvenida.test.cjs`.
- Ejecutor unificado: `scripts/validar_proyecto.py`, ahora también ejecuta las 8 pruebas de audio.
- Se conserva el historial Git y sus autores. Esta entrega agrega la rama `release/5.7.0` y etiqueta `v5.7.0`; no se ha subido a GitHub desde este entorno.
- La rama `release/5.6.0` permite consultar la entrega anterior. Para comparar sin modificar la copia actual: `git diff release/5.6.0 release/5.7.0 -- web`.
- La presentación existente conserva el alcance y las capturas históricas de 5.6.0; este documento y las evidencias de 5.7.0 describen la actualización visual y de audio.
