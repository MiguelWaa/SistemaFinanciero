/*
 * SIGSF · Cliente de la API REST del backend (capa de acceso de la Vista).
 * Todas las páginas usan estas funciones para hablar con el servidor Java.
 *
 * Seguridad:
 *  - Guarda el token de sesión en sessionStorage (se borra al cerrar la pestaña).
 *  - Envía el token en cada petición:  Authorization: Bearer <token>.
 *  - Si el servidor responde 401 (sesión vencida o inválida) vuelve al login.
 *  - Protege las páginas internas: sin sesión, redirige al inicio de sesión.
 *  - Las descargas (Excel, respaldos) también viajan con el token.
 */
window.API = (function () {
  const BASE = "/api";

  // Ámbito: el Portal del Cliente usa una sesión distinta a la del personal.
  const pagina = (location.pathname.split("/").pop() || "").toLowerCase();
  const esPortal = pagina.indexOf("portal") === 0;
  const CLAVE_TOKEN = esPortal ? "sigsf_token_cliente" : "sigsf_token";
  const PAGINA_LOGIN = esPortal ? "portal.html" : "login.html";
  const PUBLICAS = ["login.html", "portal.html", "splash.html", "welcome.html", ""];

  function token() {
    try { return sessionStorage.getItem(CLAVE_TOKEN); } catch (e) { return null; }
  }
  function guardarToken(t) {
    try { sessionStorage.setItem(CLAVE_TOKEN, t); } catch (e) { /* sin almacenamiento */ }
  }
  function limpiarSesion() {
    try {
      sessionStorage.removeItem(CLAVE_TOKEN);
      if (esPortal) { sessionStorage.removeItem("sigsf_cliente"); }
      else { sessionStorage.removeItem("sigsf_user"); sessionStorage.removeItem("sigsf_usuario"); }
    } catch (e) { /* nada */ }
  }
  function irAlLogin(motivo) {
    limpiarSesion();
    const destino = PAGINA_LOGIN + (motivo ? "?motivo=" + encodeURIComponent(motivo) : "");
    try { (window.top || window).location.href = destino; } catch (e) { location.href = destino; }
  }

  // Protección de páginas internas: sin token no se muestra la página.
  if (PUBLICAS.indexOf(pagina) === -1 && !token()) {
    irAlLogin("sesion");
  }

  function cabeceras(extra) {
    const h = Object.assign({}, extra || {});
    const t = token();
    if (t) h["Authorization"] = "Bearer " + t;
    return h;
  }

  async function req(metodo, ruta, datos) {
    const opciones = { method: metodo, headers: cabeceras() };
    if (datos !== undefined) {
      opciones.headers["Content-Type"] = "application/json";
      opciones.body = JSON.stringify(datos);
    }
    let resp;
    try {
      resp = await fetch(BASE + ruta, opciones);
    } catch (e) {
      throw new Error("No se pudo conectar con el servidor. ¿Está iniciado el backend?");
    }
    const texto = await resp.text();
    let cuerpo = null;
    if (texto) { try { cuerpo = JSON.parse(texto); } catch (e) { cuerpo = texto; } }

    if (resp.status === 401 && ruta !== "/auth/login" && ruta !== "/portal/login") {
      irAlLogin("expirada");
      throw new Error("Su sesión expiró. Inicie sesión nuevamente.");
    }
    if (!resp.ok) {
      const msg = (cuerpo && cuerpo.error) ? cuerpo.error : ("Error " + resp.status);
      throw new Error(msg);
    }
    // Inicio de sesión / registro: el servidor entrega el token de la sesión.
    if (cuerpo && typeof cuerpo === "object" && cuerpo.token) {
      guardarToken(cuerpo.token);
    }
    return cuerpo;
  }

  /** Descarga un archivo protegido (Excel, respaldo) enviando el token. */
  async function descargar(ruta, nombreSugerido) {
    const resp = await fetch(BASE + ruta, { headers: cabeceras() });
    if (resp.status === 401) { irAlLogin("expirada"); return; }
    if (!resp.ok) {
      let msg = "Error " + resp.status;
      try { msg = (await resp.json()).error || msg; } catch (e) { /* nada */ }
      throw new Error(msg);
    }
    const blob = await resp.blob();
    let nombre = nombreSugerido || "descarga";
    const disp = resp.headers.get("Content-Disposition") || "";
    const m = disp.match(/filename="([^"]+)"/);
    if (m) nombre = m[1];
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url; a.download = nombre;
    document.body.appendChild(a); a.click(); a.remove();
    setTimeout(function () { URL.revokeObjectURL(url); }, 2000);
  }

  /** Cierra la sesión en el servidor (revoca el token) y vuelve al login. */
  async function logout() {
    try { await fetch(BASE + "/auth/logout", { method: "POST", headers: cabeceras() }); } catch (e) { /* nada */ }
    limpiarSesion();
    (window.top || window).location.href = PAGINA_LOGIN;
  }

  return {
    get:    (ruta)        => req("GET", ruta),
    post:   (ruta, datos) => req("POST", ruta, datos),
    put:    (ruta, datos) => req("PUT", ruta, datos),
    delete: (ruta)        => req("DELETE", ruta),
    login:  (email, password) => req("POST", "/auth/login", { email, password }),
    dashboard: () => req("GET", "/dashboard"),
    descargar: descargar,
    logout: logout,
    haySesion: () => !!token(),
  };
})();
