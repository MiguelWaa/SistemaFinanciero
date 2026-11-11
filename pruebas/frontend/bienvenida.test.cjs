// Pruebas del flujo de presentación y audio con DOM/reproductor controlados.
// Verifican transiciones y gestos; no afirman una escucha real en navegador.
const {test}=require('node:test');
const assert=require('node:assert/strict');
const {readFileSync}=require('node:fs');
const {resolve}=require('node:path');
const {runInNewContext}=require('node:vm');
const source=readFileSync(resolve(__dirname,'../../web/sigsf-bienvenida.js'),'utf8');
const presentation=readFileSync(resolve(__dirname,'../../web/pages/welcome-1.js'),'utf8');
function entorno({publicPage=false,denied=false,iframe=false,reduced=false,deferred=false,soundDenied=false,pending=false}={}) {
 const elements=[];let clock=0,next=1;const timers=new Map();
 class Element {
  constructor(tag){this.tag=tag;this.children=[];this.events={};this.attrs={};this.paused=true;this.ended=false;this.currentTime=0;this.plays=0;this.hidden=false;this.denied=denied;this.classes=new Set();this.classList={add:c=>this.classes.add(c),remove:c=>this.classes.delete(c),toggle:(c,v)=>v?this.classes.add(c):this.classes.delete(c),contains:c=>this.classes.has(c)};elements.push(this);}
  setAttribute(k,v){this.attrs[k]=v;}
  appendChild(x){this.children.push(x);return x;}
  addEventListener(k,fn){(this.events[k]||=[]).push(fn);}
  emit(k,e={}){(this.events[k]||[]).forEach(fn=>fn(e));}
  matches(s){return s.startsWith('#')?this.id===s.slice(1):s.startsWith('[')?Object.hasOwn(this.attrs,s.slice(1,-1)):this.tag===s;}
  querySelectorAll(s){const matches=[];const walk=e=>{for(const ch of e.children){if(s.split(',').some(q=>ch.matches(q.trim())))matches.push(ch);walk(ch);}};walk(this);return matches;}
  querySelector(s){return this.querySelectorAll(s)[0]||null;}
  remove(){this.removed=true;}
  focus(){this.focused=true;}
  pause(){this.paused=true;this.emit('pause');}
  play(){this.plays++;if(this.tag==='video'&&pending)return new Promise((ok,fail)=>{this.resolvePlay=ok;this.rejectPlay=fail;});if(this.denied||(this.tag==='video'&&soundDenied&&!this.muted))return Promise.reject(Object.assign(Error('Gesture required'),{name:'NotAllowedError'}));this.paused=false;this.ended=false;this.emit('playing');return Promise.resolve();}
 }
 const body=new Element('body');
 const add=(tag,id,parent=body)=>{const e=new Element(tag);e.id=id;parent.appendChild(e);return e;};
 if(publicPage){
  const intro=add('section','bankIntro');intro.hidden=true;add('video','sigsfVideoIntro',intro);
  const start=add('button','introStart',intro);const label=add('span','',start);label.setAttribute('data-audio-label','');label.textContent='Iniciar con sonido';add('i','',start);
  add('button','introSkip',intro);add('p','introStatus',intro);
  const motion=add('button','motion',intro);motion.setAttribute('data-bank-motion','');add('span','',motion);add('i','',motion);
  const landing=add('div','bankLanding');add('h1','landingTitle',landing);add('button','replayIntro',landing);add('small','bankAudioStatus',landing);
  const audio=add('audio','sigsfAudioBienvenida',landing);audio.src='audio/bienvenida.mp3';
 }
 const domEvents={};
 const document={readyState:deferred?'loading':'complete',addEventListener(k,fn){domEvents[k]=fn;},body,createElement:t=>new Element(t),getElementById:id=>elements.find(e=>e.id===id&&!e.removed),querySelectorAll:s=>body.querySelectorAll(s)};
 const windowEvents={};const window={addEventListener(k,fn){windowEvents[k]=fn;},emit(k){windowEvents[k]?.();},matchMedia:()=>({matches:reduced,addEventListener(){}})};window.self=window;window.top=iframe?{}:window;
 const ctx={document,window,setTimeout(fn,ms){const id=next++;timers.set(id,{fn,at:clock+ms});return id;},clearTimeout(id){timers.delete(id);}};
 runInNewContext(source,ctx);
 if(publicPage)runInNewContext(presentation,ctx);
 if(deferred && domEvents.DOMContentLoaded)domEvents.DOMContentLoaded();
 return {document,window,elements,get:id=>document.getElementById(id),audio:document.getElementById('sigsfAudioBienvenida'),buttons:document.querySelectorAll('[data-bienvenida-play]'),again:()=>runInNewContext(source,ctx),advance(ms){const end=clock+ms;while(true){const pending=[...timers].filter(([,t])=>t.at<=end).sort((a,b)=>a[1].at-b[1].at)[0];if(!pending)break;clock=pending[1].at;timers.delete(pending[0]);pending[1].fn();}clock=end;}};
}
const flush=()=>new Promise(r=>setImmediate(r));
test('las páginas internas no reproducen audio sin petición',()=>{const e=entorno();assert.equal(e.audio.plays,0);assert.equal(e.buttons.length,1);});
test('el clic inicia el MP3 sin mute y con volumen normal',()=>{const e=entorno();e.audio.muted=true;e.audio.volume=0;e.buttons[0].emit('click');assert.equal(e.audio.paused,false);assert.equal(e.audio.muted,false);assert.equal(e.audio.volume,1);assert.equal(e.audio.src,'audio/bienvenida.mp3');});
test('pausar y continuar conserva el punto del saludo',()=>{const e=entorno();e.buttons[0].emit('click');e.audio.currentTime=3;e.buttons[0].emit('click');assert.equal(e.audio.paused,true);e.buttons[0].emit('click');assert.equal(e.audio.currentTime,3);assert.equal(e.audio.paused,false);});
test('al finalizar se puede repetir desde cero',()=>{const e=entorno();e.audio.currentTime=8;e.audio.ended=true;e.audio.emit('ended');assert.equal(e.buttons[0].querySelector('span').textContent,'Repetir saludo');e.buttons[0].emit('click');assert.equal(e.audio.currentTime,0);});
test('un bloqueo de audio deja un aviso y un nuevo intento por clic',async()=>{const e=entorno({denied:true});e.buttons[0].emit('click');await flush();assert.match(e.get('sigsfAudioEstado').textContent,/Pulsa/);e.audio.denied=false;e.buttons[0].emit('click');assert.equal(e.audio.paused,false);});
test('los iframe no crean otro reproductor',()=>{const e=entorno({iframe:true});assert.equal(e.audio,undefined);});
test('inicialización duplicada reutiliza el reproductor',()=>{const e=entorno();e.again();assert.equal(e.elements.filter(x=>x.tag==='audio').length,1);});
test('cerrar el control detiene el audio',()=>{const e=entorno();e.buttons[0].emit('click');e.get('sigsfBienvenida').children[3].emit('click');assert.equal(e.audio.paused,true);assert.equal(e.get('sigsfBienvenida'),undefined);});
test('un error del archivo muestra una alternativa',()=>{const e=entorno();e.audio.error={code:4};e.audio.emit('error');assert.match(e.get('sigsfAudioEstado').textContent,/Abrir MP3/);});
test('la portada reutiliza su audio nativo, sin duplicarlo',()=>{const e=entorno({publicPage:true});assert.equal(e.elements.filter(x=>x.tag==='audio').length,1);assert.equal(e.get('sigsfBienvenida'),undefined);});
test('el inicio reproduce el video original y no el saludo MP3',()=>{const e=entorno({publicPage:true});assert.equal(e.get('sigsfVideoIntro').paused,false);assert.equal(e.audio.plays,0);assert.equal(e.get('bankLanding').inert,true);});
test('autoplay audible bloqueado intenta silencio y permite activar sonido desde cero',async()=>{const e=entorno({publicPage:true,soundDenied:true});await flush();const v=e.get('sigsfVideoIntro');assert.equal(v.muted,true);assert.equal(v.paused,false);assert.match(e.get('introStart').querySelector('span').textContent,/Activar sonido/);v.currentTime=4;v.play=function(){this.plays++;this.paused=false;this.emit('playing');return Promise.resolve();};e.get('introStart').emit('click');assert.equal(v.currentTime,0);assert.equal(v.muted,false);assert.equal(v.volume,1);assert.equal(e.audio.plays,0);});
test('bloqueo de toda reproducción mantiene el botón y permite omitir',async()=>{const e=entorno({publicPage:true,denied:true});await flush();assert.match(e.get('introStatus').textContent,/Reproducir con sonido/);e.get('introSkip').emit('click');e.advance(400);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('bankLanding').inert,false);});
test('el final del video revela la portada y devuelve el foco',()=>{const e=entorno({publicPage:true});e.get('sigsfVideoIntro').emit('ended');e.advance(400);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('bankLanding').inert,false);assert.equal(e.get('landingTitle').focused,true);});
test('saltar detiene el video y no arranca otro audio',()=>{const e=entorno({publicPage:true});e.get('introSkip').emit('click');e.advance(400);assert.equal(e.get('sigsfVideoIntro').paused,true);assert.equal(e.audio.plays,0);assert.equal(e.get('bankIntro').hidden,true);});
test('repetir pausa el MP3 y reinicia el video en el mismo documento',()=>{const e=entorno({publicPage:true});e.get('introSkip').emit('click');e.advance(400);e.audio.play();e.get('sigsfVideoIntro').currentTime=5;e.get('replayIntro').emit('click');assert.equal(e.audio.paused,true);assert.equal(e.get('sigsfVideoIntro').currentTime,0);assert.equal(e.get('sigsfVideoIntro').paused,false);assert.equal(e.elements.filter(x=>x.tag==='video').length,1);});
test('pausar espera al usuario y continuar mantiene el instante',()=>{const e=entorno({publicPage:true});const v=e.get('sigsfVideoIntro');v.currentTime=3;e.get('introStart').emit('click');e.advance(20000);assert.equal(v.paused,true);assert.equal(e.get('bankIntro').hidden,false);e.get('introStart').emit('click');assert.equal(v.currentTime,3);assert.equal(v.paused,false);});
test('movimiento reducido muestra el poster sin autoplay y permite clic explícito',()=>{const e=entorno({publicPage:true,reduced:true});const v=e.get('sigsfVideoIntro');assert.equal(v.plays,0);assert.equal(e.get('motion').disabled,true);e.advance(20000);assert.equal(e.get('bankIntro').hidden,false);e.get('introStart').emit('click');assert.equal(v.paused,false);v.emit('ended');e.advance(1);assert.equal(e.get('bankLanding').inert,false);});
test('Escape libera la portada y detiene el video',()=>{const e=entorno({publicPage:true});e.get('bankIntro').emit('keydown',{key:'Escape',preventDefault(){}});e.advance(400);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('sigsfVideoIntro').paused,true);});
test('error del video no impide entrar al sistema',()=>{const e=entorno({publicPage:true});e.get('sigsfVideoIntro').emit('error');assert.match(e.get('introStatus').textContent,/No se pudo/);e.advance(3000);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('bankLanding').inert,false);});
test('carga detenida tiene salida automática y detiene el video',()=>{const e=entorno({publicPage:true});e.get('sigsfVideoIntro').emit('stalled');e.advance(16000);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('sigsfVideoIntro').paused,true);});
test('una reproducción tardía después de omitir no vuelve a sonar',async()=>{const e=entorno({publicPage:true,pending:true});const v=e.get('sigsfVideoIntro');e.get('introSkip').emit('click');e.advance(400);v.paused=false;v.resolvePlay();await flush();assert.equal(v.paused,true);assert.equal(e.get('bankIntro').hidden,true);});
test('un rechazo tardío después de omitir no intenta autoplay silencioso',async()=>{const e=entorno({publicPage:true,pending:true});const v=e.get('sigsfVideoIntro');e.get('introSkip').emit('click');e.advance(400);v.rejectPlay(Object.assign(Error('blocked'),{name:'NotAllowedError'}));await flush();assert.equal(v.plays,1);assert.equal(e.get('bankIntro').hidden,true);});
test('no se superponen el video y el MP3',()=>{const e=entorno({publicPage:true});e.audio.play();assert.equal(e.audio.paused,true);assert.equal(e.get('sigsfVideoIntro').paused,false);});
test('salir de la página cancela la presentación y sus temporizadores',()=>{const e=entorno({publicPage:true});e.window.emit('pagehide');e.advance(20000);assert.equal(e.get('sigsfVideoIntro').paused,true);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.audio.paused,true);});
test('DOMContentLoaded no duplica audio ni cambia el estado del video',()=>{const e=entorno({publicPage:true,deferred:true});assert.equal(e.audio.paused,true);assert.equal(e.get('introStart').querySelector('span').textContent,'Pausar presentación');assert.equal(e.elements.filter(x=>x.tag==='audio').length,1);});

test('inicio audible autorizado no requiere clic y oculta controles',()=>{const e=entorno({publicPage:true});const v=e.get('sigsfVideoIntro');assert.equal(v.plays,1);assert.equal(v.muted,false);assert.equal(v.volume,1);assert.equal(e.get('bankIntro').classList.contains('needs-action'),false);assert.equal(e.get('bankIntro').classList.contains('show-controls'),false);});
test('mover el mouse muestra controles temporalmente sin interrumpir video',()=>{const e=entorno({publicPage:true});const i=e.get('bankIntro');i.emit('pointermove');assert.equal(i.classList.contains('show-controls'),true);e.advance(2300);assert.equal(i.classList.contains('show-controls'),false);assert.equal(e.get('sigsfVideoIntro').paused,false);});
test('teclado y toque permiten descubrir controles',()=>{const e=entorno({publicPage:true});const i=e.get('bankIntro');i.emit('keydown',{key:'Tab'});assert.equal(i.classList.contains('show-controls'),true);e.advance(2300);i.emit('pointerdown');assert.equal(i.classList.contains('show-controls'),true);});
test('pausa o silencio conservan controles disponibles',async()=>{const e=entorno({publicPage:true});e.get('introStart').emit('click');e.advance(3000);assert.equal(e.get('bankIntro').classList.contains('needs-action'),true);const blocked=entorno({publicPage:true,soundDenied:true});await flush();assert.equal(blocked.get('bankIntro').classList.contains('needs-action'),true);});
test('omitir limpia la visibilidad temporal de controles',()=>{const e=entorno({publicPage:true});const i=e.get('bankIntro');i.emit('pointermove');e.get('introSkip').emit('click');e.advance(3000);assert.equal(i.hidden,true);assert.equal(i.classList.contains('show-controls'),false);});
