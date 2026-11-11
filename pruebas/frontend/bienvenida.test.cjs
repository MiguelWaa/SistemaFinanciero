// Pruebas del flujo de presentación y audio con DOM/reproductor controlados.
// Verifican transiciones y gestos; no afirman una escucha real en navegador.
const {test}=require('node:test');
const assert=require('node:assert/strict');
const {readFileSync}=require('node:fs');
const {resolve}=require('node:path');
const {runInNewContext}=require('node:vm');
const source=readFileSync(resolve(__dirname,'../../web/sigsf-bienvenida.js'),'utf8');
const presentation=readFileSync(resolve(__dirname,'../../web/pages/welcome-1.js'),'utf8');
function entorno({publicPage=false,denied=false,iframe=false,reduced=false,deferred=false}={}) {
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
  play(){this.plays++;if(this.denied)return Promise.reject(Object.assign(Error('Gesture required'),{name:'NotAllowedError'}));this.paused=false;this.ended=false;this.emit('playing');return Promise.resolve();}
 }
 const body=new Element('body');
 const add=(tag,id,parent=body)=>{const e=new Element(tag);e.id=id;parent.appendChild(e);return e;};
 if(publicPage){
  const intro=add('section','bankIntro');intro.hidden=true;
  const start=add('button','introStart',intro);start.setAttribute('data-bienvenida-play','');const label=add('span','',start);label.setAttribute('data-audio-label','');label.textContent='Iniciar con sonido';add('i','',start);
  add('button','introSkip',intro);add('p','introStatus',intro);
  const motion=add('button','motion',intro);motion.setAttribute('data-bank-motion','');add('span','',motion);add('i','',motion);
  const landing=add('div','bankLanding');add('h1','landingTitle',landing);add('button','replayIntro',landing);add('small','bankAudioStatus',landing);
  const audio=add('audio','sigsfAudioBienvenida',landing);audio.src='audio/bienvenida.mp3';
 }
 const domEvents={};
 const document={readyState:deferred?'loading':'complete',addEventListener(k,fn){domEvents[k]=fn;},body,createElement:t=>new Element(t),getElementById:id=>elements.find(e=>e.id===id&&!e.removed),querySelectorAll:s=>body.querySelectorAll(s)};
 const window={addEventListener(){},matchMedia:()=>({matches:reduced,addEventListener(){}})};window.self=window;window.top=iframe?{}:window;
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
test('autoplay bloqueado avanza a portada y libera sus accesos',async()=>{const e=entorno({publicPage:true,denied:true});await flush();assert.equal(e.get('bankLanding').inert,true);assert.match(e.get('introStatus').textContent,/Iniciar con sonido/);e.advance(9400);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('bankLanding').inert,false);});
test('el audio activo no se corta al cumplirse el tiempo visual; ended muestra portada',()=>{const e=entorno({publicPage:true});e.advance(9500);assert.equal(e.get('bankIntro').hidden,false);assert.equal(e.audio.paused,false);e.audio.ended=true;e.audio.paused=true;e.audio.emit('ended');e.advance(400);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('bankLanding').inert,false);});
test('saltar la presentación detiene sonido y permite entrar',()=>{const e=entorno({publicPage:true});e.get('introSkip').emit('click');e.advance(400);assert.equal(e.audio.paused,true);assert.equal(e.get('bankIntro').hidden,true);});
test('repetir presentación usa el mismo audio y recupera el inicio',()=>{const e=entorno({publicPage:true});e.get('introSkip').emit('click');e.advance(400);e.audio.currentTime=5;e.get('replayIntro').emit('click');assert.equal(e.get('bankIntro').hidden,false);assert.equal(e.audio.currentTime,0);assert.equal(e.audio.paused,false);assert.equal(e.elements.filter(x=>x.tag==='audio').length,1);});
test('se puede pausar la animación sin detener el saludo',()=>{const e=entorno({publicPage:true});e.get('motion').emit('click');assert.equal(e.document.body.classList.contains('bank-paused'),true);assert.equal(e.audio.paused,false);});
test('movimiento reducido desactiva el control de animación',()=>{const e=entorno({publicPage:true,reduced:true});assert.equal(e.get('motion').disabled,true);assert.equal(e.document.body.classList.contains('bank-paused'),true);});
test('Escape abre la portada y detiene el saludo',()=>{const e=entorno({publicPage:true});e.get('bankIntro').emit('keydown',{key:'Escape',preventDefault(){}});e.advance(400);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.audio.paused,true);});
test('fallo de audio en presentación no impide llegar a la portada',()=>{const e=entorno({publicPage:true});e.audio.error={code:4};e.audio.emit('error');e.advance(4000);assert.equal(e.get('bankIntro').hidden,true);assert.equal(e.get('bankLanding').inert,false);});

test('el control refleja audio iniciado antes de DOMContentLoaded',()=>{const e=entorno({publicPage:true,deferred:true});assert.equal(e.audio.paused,false);assert.equal(e.get('introStart').querySelector('span').textContent,'Pausar saludo');assert.equal(e.elements.filter(x=>x.tag==='audio').length,1);});
