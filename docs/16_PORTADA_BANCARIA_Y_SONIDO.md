# SIGSF 5.8.0 · Portada bancaria animada

Actualización del 10/11/2026 (Lima). Sustituye la apariencia grafito de 5.7.0 por **Banca Azul**. Mantiene los flujos financieros y la configuración del sistema.

## Abrir la versión nueva en Windows

1. Descomprimir **todo** el ZIP en una carpeta nueva.
2. Cerrar la consola de la demostración anterior que usa el puerto 8091.
3. Abrir `SistemaFinanciero/INICIAR_DEMO.bat`. Debe mostrar **SIGSF 5.8.0 - PORTADA BANCARIA ANIMADA**.
4. Entrar a `http://localhost:8091/`. Si conserva una página anterior, pulsar **Ctrl+F5**.
5. En la presentación, pulsar **Iniciar con sonido** para activar el saludo.
6. Al terminar aparece la portada. Elegir **Entrar como cliente** o **Acceso del personal**. La cuenta demo sigue siendo `admin@sigsf.com` / `admin123`.

Java 17+ sigue siendo necesario. El iniciador usa datos de demostración en memoria; la base PostgreSQL existente no se edita. Para el arranque habitual del proyecto se conserva su configuración.

## Cambios visibles

- Una introducción a pantalla completa presenta la ilustración de la entidad SIGSF en grande, con billetes simbólicos cayendo, movimiento suave y un mensaje breve.
- La portada deja el fondo negro: usa blanco, azul bancario, celeste y pequeños detalles dorados. Incluye ilustración, servicios y dos accesos diferenciados.
- El tema se aplica a las 26 páginas: menú lateral claro, encabezado del panel azul, indicadores en tarjetas separadas, botones, tablas, formularios, modales, Ayuda y portal.
- Bootstrap 5.3.3 permanece como infraestructura local; se cambia su estilo y las páginas públicas, manteniendo el funcionamiento de los componentes.
- Los billetes son elementos gráficos simbólicos. La ilustración no identifica a una entidad bancaria real ni acredita una licencia financiera.
- Las animaciones se pueden pausar. Se respeta la preferencia de movimiento reducido del sistema.

## Audio y transición

El MP3 original se conserva sin alterar sus bytes. La medición local verifica 8,28 segundos de audio y una señal no silenciosa. No es una voz sintetizada.

La presentación y la portada ahora comparten **el mismo documento y el mismo reproductor**. Se evita la redirección que podía cortar el saludo. Al cargar la presentación se intenta reproducir; si el navegador lo bloquea, el botón **Iniciar con sonido** llama directamente a `play()` durante el clic. No se cambian permisos, CSP ni ajustes del navegador.

- Con audio iniciado, la introducción termina al finalizar el saludo; el paso a la portada no cambia de página.
- Sin audio, la presentación pasa a la portada tras unos 9 segundos (3,5 con movimiento reducido).
- **Entrar a la portada** y la tecla Escape permiten saltar la introducción y detener el audio.
- **Ver presentación** repite la introducción desde cero.
- La portada incluye reproductor nativo con volumen y **Abrir MP3**. Las páginas internas mantienen un botón manual de bienvenida, sin reproducción inesperada al navegar.
- El control ya no depende de una marca persistida de “ya reproducido”. Una sesión anterior no impide volver a escuchar.
- Si hay un error de sonido, los accesos siguen disponibles. Una carga detenida tiene un límite de espera para revelar la portada sin cortar el audio.

Si no se oye tras pulsar el botón, probar el reproductor y **Abrir MP3**; comprobar el volumen y que la pestaña no esté silenciada. La prueba del archivo no permite verificar el dispositivo de salida del usuario.

## Cambios técnicos acotados

| Recurso | Responsabilidad |
|---|---|
| web/welcome.html | Introducción y portada bancaria con accesos existentes |
| web/sigsf-banco.css | Tema, animaciones, estados, adaptación responsive y reducción de movimiento |
| web/img/banco-sigsf.svg | Ilustración vectorial propia de la entidad SIGSF |
| web/pages/welcome-1.js | Transición por final del audio, tiempo, botón, Escape y repetición |
| web/sigsf-bienvenida.js | Un solo reproductor y controles accesibles |
| web/splash.html y pages/splash-1.js | Compatibilidad con enlaces antiguos hacia welcome.html |
| ApiServer.java | La ruta pública `/` entrega directamente welcome.html |
| pruebas/frontend/bienvenida.test.cjs | 19 regresiones del audio y la presentación |

La única diferencia nueva de backend respecto a 5.7.0 es el archivo público que responde a `/`. No cambian cálculos, DAO, DTO, reglas, autenticación, permisos, endpoints de operaciones, esquema ni datos. Los formularios, identificadores y enlaces de las 24 páginas funcionales se compararon contra 5.7.0; las dos páginas públicas se rediseñaron deliberadamente.

## Evidencias obtenidas

Código probado: `6a4d58360a50ce84d4ddc8aad758ff7cc7798f71`. Directorio: `evidencias/v5.8.0/`.

| Verificación | Resultado |
|---|---|
| JUnit | 216/216 aprobadas |
| Audio y presentación con DOM/reproductor simulados | 19/19 aprobadas |
| Newman | 26 solicitudes, 63 aserciones, 0 fallos |
| Comprobaciones adicionales de entradas y seguridad | 24/24 aprobadas |
| JavaScript propio | Sintaxis aprobada |
| Contratos de las 24 páginas funcionales | Conservados |
| MP3 | Bytes originales, decodificación y señal comprobados |
| Ilustración SVG | XML válido, render estático inspeccionado |
| Revisión de la interfaz y escucha en navegador real | Pendiente por restricción del entorno |

Compilación local con Java 21 y `-source 17 -target 17`. El entorno carece del soporte necesario para comprobar APIs con `--release 17`; el validador normal mantiene `--release 17` con JDK completo. Las 14 pruebas E2E, PostgreSQL, ZAP y hallazgos npm anteriores conservan sus estados documentados; no se presentan como aprobados por este cambio visual.

## Reproducir las pruebas

Desde la raíz de SistemaFinanciero, con las dependencias indicadas en README.md:

```sh
python scripts/validar_proyecto.py
```

Solo audio/presentación, con Node.js 20+ y sin instalar paquetes:

```sh
node --test pruebas/frontend/bienvenida.test.cjs
```

Validación visual pendiente en el equipo de exposición: probar el inicio con sonido y sin sonido; saltar/repetir/pausar; comprobar los accesos de cliente y personal; abrir un módulo, un formulario y Ayuda; revisar a 390 px; previsualizar la impresión de un comprobante.

## Historial y entrega

La rama `release/5.8.0` y la etiqueta `v5.8.0` incorporan esta revisión. Se conservan las ramas y autores previos; no se publica en GitHub automáticamente. Los documentos 15 y anteriores describen sus versiones históricas. La presentación PPTX/PDF ya incluida conserva su alcance de 5.6.0; este documento y los informes 5.8.0 describen la nueva portada.
