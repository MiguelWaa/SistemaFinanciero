# SIGSF 5.9.0 · Presentación de inicio en video

Actualización: 10/11/2026, hora de Lima. Se integra el archivo **121221.mp4** entregado por el usuario, sin recortar, regrabar ni reemplazar su sonido. La animación muestra el símbolo de la entidad y el nombre SIGSF. Dura 10,03 segundos, tiene resolución 1280 × 720, video H.264 y audio AAC estéreo.

## Abrir en Windows

1. Descomprimir **todo** el ZIP en una carpeta nueva.
2. Cerrar la consola de la demostración anterior que ocupa el puerto 8091.
3. Abrir `SistemaFinanciero/INICIAR_DEMO.bat`. El título de consola indica **SIGSF 5.9.0 - PRESENTACION EN VIDEO SIGSF**.
4. Entrar a `http://localhost:8091/`; usar **Ctrl+F5** si aún aparece la versión anterior.
5. Se muestra el video. Si inicia sin audio, pulsar **Activar sonido desde el inicio**. Si está pausado, pulsar **Reproducir con sonido**.
6. Al terminar aparece la portada bancaria clara, con los accesos de cliente y personal.

Se requiere Java 17+. El iniciador utiliza datos de demostración en memoria. Mantiene la cuenta demo `admin@sigsf.com` / `admin123` y no modifica la base PostgreSQL existente.

## Comportamiento

- El video conserva su proporción y los márgenes del archivo original. No se estira ni se corta el logotipo.
- La introducción usa el fondo azul oscuro del modelo recibido; después continúa la portada Banca Azul de 5.8.0.
- Se intenta iniciar con sonido. Si el navegador lo bloquea, se intenta reproducir en silencio y aparece el botón para activar el audio desde el comienzo. Si bloquea ambos intentos, quedan disponibles el botón de reproducción y el acceso directo.
- Los controles nativos permiten pausar, ajustar el volumen y avanzar o retroceder. El botón principal permite pausar y continuar.
- Al finalizar el clip, el sistema revela la portada sin recargar la página. **Entrar a la portada** o Escape lo omiten y detienen su reproducción.
- **Ver presentación** lo repite desde cero y pausa cualquier saludo MP3 que estuviera sonando.
- El MP3 original anterior sigue disponible manualmente en la portada y en los controles existentes. Nunca se inicia simultáneamente con el video.
- Con movimiento reducido activado, aparece una imagen del video y se espera un clic para reproducirlo.
- Un error del archivo lleva a la portada tras 2,5 segundos; una carga detenida tiene un límite de espera de 15 segundos. El botón de entrada siempre está disponible.
- Sin JavaScript, la portada permanece accesible y la introducción queda oculta.

El botón de audio respeta los permisos del navegador. Si no se oye después del clic, comprobar el volumen del reproductor, la pestaña y el dispositivo de salida. La señal del archivo fue comprobada; los altavoces y la reproducción en el equipo del usuario no fueron verificados aquí.

## Recursos y alcance técnico

| Recurso | Cambio |
|---|---|
| web/video/presentacion-sigsf.mp4 | Copia exacta de 121221.mp4, con sonido original |
| web/video/presentacion-sigsf.jpg | Imagen de espera extraída a los 3,5 segundos |
| web/welcome.html | Video, controles y transición hacia los accesos existentes |
| web/pages/welcome-1.js | Reproducción, fallback de sonido, pausa, salto, repetición y errores |
| web/sigsf-banco.css | Presentación adaptable que conserva el encuadre |
| web/sigsf-bienvenida.js | Estado del MP3 separado del estado del video |
| ApiServer.java | MIME video/mp4 y lectura por bloques con rangos HTTP simples |
| test/.../VideoEstaticoTest.java | 8 pruebas HTTP del video, rangos, HEAD y ETag |
| pruebas/frontend/bienvenida.test.cjs | 26 pruebas de bienvenida y transiciones con DOM simulado |

La entrega del video admite GET 200 completo, GET 206 por rango, HEAD sin cuerpo, ETag/304 e If-Range. Un rango fuera del archivo devuelve 416; rangos múltiples o no reconocidos se ignoran y se entrega el archivo completo. El archivo se lee por bloques de 16 KiB. No se incorporaron librerías ni servicios externos.

Respecto a 5.8.0, las 24 páginas funcionales y la página de compatibilidad splash permanecen iguales. La única modificación Java es la entrega estática del video. Las reglas financieras, autenticación, permisos, DAO, DTO, esquema, datos y configuración de PostgreSQL se conservan.

## Validación obtenida

Código probado: `97af192fea2977784d3ae4f08df62a4ac9416ecb`, sin cambios locales. Informes en `evidencias/v5.9.0/`.

| Verificación | Resultado |
|---|---|
| JUnit | 224/224 aprobadas, incluidas 8 nuevas de video |
| Audio/video y transiciones, con DOM simulado | 26/26 aprobadas |
| Newman | 26 peticiones, 63 aserciones, cero fallos |
| Comprobaciones adicionales de entradas | 24/24 aprobadas |
| JavaScript propio | Sintaxis aprobada |
| MP4 | Bytes originales, decodificación completa y señal de audio comprobados |
| Navegador real, escucha y Playwright | Pendientes por restricciones del entorno |

Las pruebas de frontend cubren bloqueo de autoplay, reproducción silenciosa, activación del sonido, final, pausa, repetición, Escape, errores, movimiento reducido y respuestas tardías que no deben reiniciar audio tras omitir.

Compilación local con Java 21 y `-source 17 -target 17`. El modo normal del validador mantiene `--release 17` y requiere JDK completo. PostgreSQL, ZAP, los E2E anteriores y los hallazgos npm conservan sus estados previos; esta actualización de presentación no se presenta como un cierre de esos pendientes.

Para repetir las comprobaciones desde la raíz del proyecto:

```sh
node --test pruebas/frontend/bienvenida.test.cjs
python scripts/validar_proyecto.py --omitir-e2e
```

La comprobación del JAR final y los recursos servidos se registra en `dist/evidencia_empaquetado.json`. El manifiesto y `dist/proveniencia_fuentes.json` permiten relacionar fuentes, pruebas y paquete.

## Versionamiento e inventario

La rama `feature/video-inicio-5.9` se integra en `release/5.9.0`, con etiqueta `v5.9.0`. Se conserva el historial previo; no se publica en GitHub automáticamente. Para volver al estado anterior existe `v5.8.0`; antes de cambiar de versión, guardar cualquier modificación local.

El índice general enlaza esta guía, el inventario de documentación y las evidencias actuales. Las guías 15 y 16 describen versiones históricas. El PPTX/PDF incluido sigue siendo la presentación académica de 5.6.0; la introducción audiovisual de 5.9.0 está documentada aquí.
