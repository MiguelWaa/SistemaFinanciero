# ============================================================
#  SIGSF 5.11.0 · Imagen de contenedor (despliegue)
#
#  Construir (con Internet):
#     docker build -t sigsf:5.11.0 .
#  Construir sin Internet (laboratorio, imagen base local):
#     docker build --build-arg BASE_IMAGE=sigsf-base-local:jre -t sigsf:5.11.0 .
#
#  Requisito: dist/SIGSF.jar ya generado por
#     python scripts/validar_proyecto.py --solo-junit && python scripts/empaquetar.py
#  (en CI/CD lo hace el workflow .github/workflows/cd.yml antes del build).
# ============================================================
ARG BASE_IMAGE=eclipse-temurin:17-jre-jammy
FROM ${BASE_IMAGE}

ARG VERSION=5.11.0
ARG COMMIT=desconocido
LABEL org.opencontainers.image.title="SIGSF" \
      org.opencontainers.image.description="Sistema Integral de Gestión de Servicios Financieros - UTP" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${COMMIT}" \
      org.opencontainers.image.source="https://github.com/MiguelWaa/SistemaFinanciero" \
      org.opencontainers.image.licenses="UNLICENSED"

WORKDIR /app

# Solo lo necesario para ejecutar: JAR, librerías, frontend, plantilla de configuración y SQL.
COPY --chown=10001:10001 dist/SIGSF.jar          /app/dist/SIGSF.jar
COPY --chown=10001:10001 lib/*.jar               /app/lib/
COPY --chown=10001:10001 web/                    /app/web/
COPY --chown=10001:10001 db/                     /app/db/
COPY --chown=10001:10001 db.properties.ejemplo   /app/db.properties.ejemplo
COPY --chown=10001:10001 VERSION                 /app/VERSION

# Configuración por variables de entorno (Configuracion.get lee SIGSF_<CLAVE>).
# La contraseña NO va en la imagen: se inyecta en tiempo de ejecución.
ENV SIGSF_PERSISTENCIA=postgresql \
    SIGSF_SERVER_PORT=8091 \
    SIGSF_WEB_DIR=/app/web \
    SIGSF_BACKUP_DIR=/app/respaldos \
    SIGSF_BACKUP_AUTOMATICO_HORAS=0 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8 -Dsigsf.log.dir=/app/logs"

# Usuario sin privilegios (no root) y carpetas de escritura.
USER 10001:10001
VOLUME ["/app/logs", "/app/respaldos"]
EXPOSE 8091

# Sonda propia en Java: no requiere curl ni wget dentro de la imagen.
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=3 \
  CMD ["java", "-cp", "/app/dist/SIGSF.jar", "com.utp.sistemafinanciero.despliegue.SondaSalud"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/dist/SIGSF.jar"]
