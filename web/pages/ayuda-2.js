try { var u = sessionStorage.getItem('sigsf_user'); if (u) document.getElementById('navUser').textContent = u.split('@')[0]; } catch (e) {}
        var lb = document.getElementById('logoutBtn'); if (lb) lb.addEventListener('click', function(e){ if (window.API) { e.preventDefault(); API.logout(); } });

        var body = document.getElementById('chatBody');
        var input = document.getElementById('chatInput');
        // Delegación explícita: compatible con CSP sin scripts inline ni eval.
        input.addEventListener('keydown', function(event) { if (event.key === 'Enter') send(); });
        document.addEventListener('click', function(event) {
            var el = event.target.closest('[data-help-action]');
            if (!el) return;
            switch (el.dataset.helpAction) {
                case 'replay': replay(el); break;
                case 'quick': quick(el); break;
                case 'mic': toggleMic(); break;
                case 'send': send(); break;
                case 'faq': toggleFaq(el); break;
            }
        });

        /* ---------- Voz: texto a voz (TTS) ---------- */
        var voiceOn = true;
        var synth = window.speechSynthesis || null;
        var esVoice = null;
        function pickVoice(){ if (!synth) return; esVoice = window.sigsfPickVoice(synth); }
        if (synth){ pickVoice(); if (typeof synth.onvoiceschanged !== 'undefined') synth.onvoiceschanged = pickVoice; }

        function stripHtml(html){ var d = document.createElement('div'); d.innerHTML = html; return (d.textContent || d.innerText || '').replace(/\s+/g,' ').trim(); }

        function speak(text, btn){
            if (!synth || !voiceOn) return;
            try { synth.cancel(); } catch(e){}
            var u = new SpeechSynthesisUtterance(text);
            window.sigsfApplyVoice(u, esVoice);
            if (btn){
                btn.classList.add('speaking');
                u.onend = function(){ btn.classList.remove('speaking'); };
                u.onerror = function(){ btn.classList.remove('speaking'); };
            }
            synth.speak(u);
        }

        function replay(btn){
            var bubble = btn.closest('.bubble');
            var clone = bubble.cloneNode(true);
            var b = clone.querySelector('.replay'); if (b) b.remove();
            speak(stripHtml(clone.innerHTML), btn);
        }

        var voiceBtn = document.getElementById('voiceBtn');
        voiceBtn.addEventListener('click', function(){
            voiceOn = !voiceOn;
            voiceBtn.classList.toggle('off', !voiceOn);
            voiceBtn.innerHTML = voiceOn ? '<i class="fa-solid fa-volume-high"></i>' : '<i class="fa-solid fa-volume-xmark"></i>';
            voiceBtn.title = voiceOn ? 'Voz activada' : 'Voz desactivada';
            if (!voiceOn && synth){ try { synth.cancel(); } catch(e){} }
        });
        if (!synth){ voiceBtn.style.display = 'none'; }

        /* ---------- Base de conocimiento ---------- */
        var KB = [
            { k:['yape','plin','tunki','qr','verific','pago','pagos','cobr','billetera'], a:'Los pagos que el cliente hace con <b>Yape, Plin o Tunki</b> desde el portal llegan a <a href="pagos.html">Pagos y cobranzas</a> como <b>Por verificar</b>. Busca en la app de la empresa el abono con el mismo monto y número de operación y pulsa <b>Confirmar pago</b>: se descuenta el saldo y se emite el comprobante. Si no lo encuentras, pulsa <b>Rechazar</b> e indica el motivo.' },
            { k:['cotiza','cotización','cotizacion','proforma','cronograma'], a:'Las cotizaciones se generan en el portal del cliente o en <a href="pagos.html#cotizaciones">Pagos y cobranzas → Cotizaciones</a>. Usan las mismas tasas del sistema, incluyen cronograma y se imprimen o guardan en PDF.' },
            { k:['usuario','crear usuario','nuevo usuario','registrar','cuenta','alta'], a:'Para crear un usuario ve a <a href="usuarios.html">Usuarios</a>, completa nombre, correo, rol y estado, y pulsa <b>Guardar</b>. Si dejas la contraseña vacía se asigna una por defecto.' },
            { k:['rol','permiso','permisos','acceso'], a:'Los roles son Administrador, Supervisor, Analista y Cajero. Se asignan al crear o editar un usuario, y puedes revisar qué hace cada rol en <a href="seguridad.html">Seguridad</a>.' },
            { k:['reporte','reportes','informe','exportar','descargar','pdf','excel'], a:'En <a href="reportes.html">Reportes</a> generas el reporte de pagos imprimible (o en PDF desde el navegador) y descargas en Excel, CSV o JSON los pagos, cotizaciones, ventas, clientes, operaciones, préstamos, empeños y productos.' },
            { k:['2fa','doble factor','dos pasos','autenticaci'], a:'El sistema protege el acceso con contraseñas cifradas (PBKDF2), sesión que vence por inactividad y bloqueo tras 5 intentos fallidos. La verificación en dos pasos aún no está disponible.' },
            { k:['seguridad','bloqueo','intentos','sesion','sesión'], a:'En <a href="seguridad.html">Seguridad</a> ves los roles y la matriz de permisos que aplica el servidor. La bitácora de accesos e IPs está en <a href="respaldos.html">Respaldos y auditoría</a>.' },
            { k:['configur','ajuste','preferencia','tipo de cambio'], a:'En <a href="configuracion.html">Configuración</a> mantienes los tipos de cambio que usan Operaciones y el portal. Los números y QR de Yape, Plin y Tunki se configuran en <a href="pagos.html#billeteras">Pagos → Billeteras y QR</a>.' },
            { k:['respaldo','backup','copia','mantenimiento'], a:'En <a href="respaldos.html">Respaldos y auditoría</a> pulsa <b>Generar respaldo ahora</b>. El sistema además hace un respaldo automático cada 24 horas.' },
            { k:['contrase','clave','password','olvid'], a:'El administrador cambia contraseñas desde <a href="usuarios.html">Usuarios</a> (editar usuario). Los clientes del portal entran con su documento.' },
            { k:['modulo','módulo','modulos','menu','menú','secciones','que hay','qué hay'], a:'El sistema tiene módulos de Marketing y Ventas, Logística, Almacén, Producción, Operaciones, Dirección, Gerencia y Clientes y Servicios Financieros. Ábrelos desde el menú lateral izquierdo.' },
            { k:['marketing','campaña','campana'], a:'El módulo <b>Marketing y Ventas</b> gestiona campañas, clientes potenciales, ventas y servicios. Ábrelo desde el menú lateral.' },
            { k:['venta','vender'], a:'El módulo <b>Ventas</b> registra y da seguimiento a las ventas. Ábrelo desde el menú lateral.' },
            { k:['logistica','logística','inventario','stock','entrada','salida'], a:'El módulo <b>Logística</b> controla inventario, entradas y salidas. El módulo <b>Almacén</b> maneja stock y garantías. Ambos están en el menú lateral.' },
            { k:['almacen','almacén','garantia','garantía'], a:'El módulo <b>Almacén</b> controla el stock, el stock bajo y los bienes en garantía. Ábrelo desde el menú lateral.' },
            { k:['produccion','producción','lote','proceso','eficiencia'], a:'El módulo <b>Producción</b> gestiona lotes, procesos y eficiencia. Ábrelo desde el menú lateral.' },
            { k:['operacion','cambio','moneda','prestamo','préstamo','empeño','empeno','pago'], a:'El módulo <b>Operaciones Financieras</b> gestiona cambios de moneda, préstamos, empeños y pagos. Ábrelo desde el menú lateral.' },
            { k:['direccion','dirección','indicador','decision','decisión','estrateg'], a:'El módulo <b>Dirección</b> muestra indicadores y análisis para decisiones estratégicas. Ábrelo desde el menú lateral.' },
            { k:['gerencia','resultado','kpi','anual'], a:'El módulo <b>Gerencia General</b> presenta resultados globales y KPIs. Ábrelo desde el menú lateral.' },
            { k:['cliente','financiero','historial','servicio'], a:'El módulo <b>Clientes y Servicios Financieros</b> administra clientes, servicios y su historial. Ábrelo desde el menú lateral.' },
            { k:['cerrar sesion','cerrar sesión','salir','logout'], a:'Para salir usa <b>“Cerrar sesión”</b> al final del menú lateral. Volverás a la pantalla de inicio de sesión.' },
            { k:['inicio','dashboard','panel','kpi'], a:'El <b>Inicio</b> es tu panel principal: muestra los KPIs (ingresos, clientes, operaciones, rentabilidad) y el acceso a todos los módulos.' },
            { k:['voz','habla','hablar','escuchar','audio','sonido'], a:'¡Sí, puedo hablar! 🔊 Con el botón del altavoz (arriba a la derecha) activas o desactivas mi voz, y con “Escuchar” debajo de cada respuesta la repito. También puedes hablarme con el botón del micrófono.' },
            { k:['gracias','thanks','ok','perfecto','genial'], a:'¡Con gusto! 😊 Si necesitas algo más sobre el sistema, aquí estoy.' },
            { k:['hola','buenas','saludos','hey'], a:'¡Hola! ¿En qué parte del sistema te ayudo: usuarios, reportes, configuración, seguridad o los módulos?' }
        ];

        function answer(text){
            var t = text.toLowerCase();
            var best = null, bestScore = 0;
            KB.forEach(function(item){
                var score = 0;
                item.k.forEach(function(kw){ if (t.indexOf(kw) !== -1) score += kw.length; });
                if (score > bestScore){ bestScore = score; best = item; }
            });
            if (best) return best.a;
            return 'No estoy seguro de haber entendido. Puedo ayudarte con <b>usuarios</b>, <b>reportes</b>, <b>configuración</b>, <b>seguridad</b> o los <b>módulos</b> del sistema. También puedes escribir a <b>soporte@sigsf.com</b>. ¿Sobre cuál te ayudo?';
        }

        function addMsg(html, who){
            var d = document.createElement('div');
            d.className = 'msg ' + who;
            var icon = who === 'bot' ? '<i class="fa-solid fa-robot"></i>' : '<i class="fa-solid fa-user"></i>';
            d.innerHTML = '<div class="av">'+icon+'</div><div class="bubble">'+html+'</div>';
            body.appendChild(d);
            body.scrollTop = body.scrollHeight;
            return d;
        }

        function send(){
            var val = input.value.trim();
            if (!val) return;
            addMsg(val.replace(/</g,'&lt;'), 'user');
            input.value = '';
            var typing = addMsg('<span class="typing"><span></span><span></span><span></span></span>', 'bot');
            setTimeout(function(){
                var reply = answer(val);
                var bubble = typing.querySelector('.bubble');
                bubble.innerHTML = reply + '<br><button class="replay" data-help-action="replay"><i class="fa-solid fa-volume-high"></i> Escuchar</button>';
                body.scrollTop = body.scrollHeight;
                var rb = bubble.querySelector('.replay');
                speak(stripHtml(reply), rb);   // habla automáticamente si la voz está activa
            }, 650);
        }

        function quick(el){ input.value = el.textContent; send(); }
        function toggleFaq(el){ el.parentElement.classList.toggle('open'); }

        /* ---------- Voz: reconocimiento (micrófono) ---------- */
        var micBtn = document.getElementById('micBtn');
        var SR = window.SpeechRecognition || window.webkitSpeechRecognition || null;
        var recog = null, listening = false;
        if (SR){
            recog = new SR();
            recog.lang = 'es-ES'; recog.interimResults = false; recog.maxAlternatives = 1;
            recog.onresult = function(e){ input.value = e.results[0][0].transcript; send(); };
            recog.onend = function(){ listening = false; micBtn.classList.remove('listening'); };
            recog.onerror = function(){ listening = false; micBtn.classList.remove('listening'); };
        } else {
            micBtn.title = 'El reconocimiento de voz no está disponible en este navegador';
            micBtn.style.opacity = '.5';
        }
        function toggleMic(){
            if (!recog){ alert('El reconocimiento de voz por micrófono no está disponible en este navegador. Prueba con Google Chrome. Igual puedo hablar y responder por texto.'); return; }
            if (listening){ recog.stop(); return; }
            try { recog.start(); listening = true; micBtn.classList.add('listening'); } catch(e){}
        }
