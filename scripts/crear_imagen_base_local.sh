#!/bin/sh
# ============================================================
#  SIGSF 5.11.0 · Imagen base SIN Internet (solo laboratorio)
#
#  Cuando Docker Hub no es accesible, crea "sigsf-base-local:jre" con una JRE
#  mínima generada por jlink desde el JDK instalado y las bibliotecas del
#  sistema que necesita. Así el MISMO Dockerfile se construye con:
#     docker build --build-arg BASE_IMAGE=sigsf-base-local:jre -t sigsf:5.11.0 .
#  En producción y en CI se usa eclipse-temurin:17-jre-jammy.
# ============================================================
set -eu
TAG="${1:-sigsf-base-local:jre}"
TMP="$(mktemp -d)"; ROOTFS="$TMP/rootfs"
trap 'rm -rf "$TMP"' EXIT
MODULOS=java.base,java.desktop,java.management,java.naming,java.security.jgss,java.security.sasl,java.sql,jdk.httpserver,jdk.crypto.ec,jdk.zipfs,java.logging,java.xml
jlink --add-modules "$MODULOS" --strip-debug --no-header-files --no-man-pages --compress=zip-6 \
      --output "$ROOTFS/opt/java"
mkdir -p "$ROOTFS/etc" "$ROOTFS/tmp" "$ROOTFS/app" "$ROOTFS/bin" "$ROOTFS/usr/bin"
chmod 1777 "$ROOTFS/tmp"
# Bibliotecas del sistema que requieren la JRE y /bin/sh
for b in "$ROOTFS"/opt/java/bin/java "$ROOTFS"/opt/java/lib/*.so "$ROOTFS"/opt/java/lib/server/*.so /bin/sh; do
  ldd "$b" 2>/dev/null | awk '/=>/ {print $3} /^\t\/lib/ {print $1}' ; done | sort -u | while read -r lib; do
  case "$lib" in "$ROOTFS"*) continue;; esac
  if [ -f "$lib" ]; then mkdir -p "$ROOTFS$(dirname "$lib")"; cp -L "$lib" "$ROOTFS$lib"; fi; done
for lib in /lib/x86_64-linux-gnu/libnss_files.so.2 /lib/x86_64-linux-gnu/libnss_dns.so.2 /lib/x86_64-linux-gnu/libresolv.so.2; do
  if [ -f "$lib" ]; then cp -L "$lib" "$ROOTFS$lib"; fi; done
mkdir -p "$ROOTFS/lib64"; cp -L /lib64/ld-linux-x86-64.so.2 "$ROOTFS/lib64/"
cp -L /bin/sh "$ROOTFS/bin/sh"
ln -s /opt/java/bin/java "$ROOTFS/usr/bin/java"
printf 'root:x:0:0:root:/root:/bin/sh\nsigsf:x:10001:10001:sigsf:/app:/bin/sh\n' > "$ROOTFS/etc/passwd"
printf 'root:x:0:\nsigsf:x:10001:\n' > "$ROOTFS/etc/group"
printf 'hosts: files dns\n' > "$ROOTFS/etc/nsswitch.conf"
if [ -d /etc/ssl/certs/java ]; then mkdir -p "$ROOTFS/etc/ssl/certs/java"; cp -L /etc/ssl/certs/java/cacerts "$ROOTFS/etc/ssl/certs/java/"; fi
tar -C "$ROOTFS" -c . | docker import \
  -c 'ENV PATH=/opt/java/bin:/usr/bin:/bin JAVA_HOME=/opt/java LANG=C.UTF-8 LD_LIBRARY_PATH=/usr/local/lib:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu' \
  -c 'CMD ["java","-version"]' - "$TAG"
docker run --rm "$TAG" java -version
echo "Imagen base creada: $TAG"
