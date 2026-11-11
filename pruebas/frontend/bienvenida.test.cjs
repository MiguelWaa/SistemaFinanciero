// Pruebas del contrato del audio con un DOM y un reproductor controlados.
// No sustituyen la revisión de reproducción real en un navegador.
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { runInNewContext } = require('node:vm');
const source = readFileSync(resolve(__dirname, '../../web/sigsf-bienvenida.js'), 'utf8');
function entorno({ stored=false, denied=false, storageFails=false, iframe=false }={}) {
  const elements=[]; let value=stored?'1':null;
  class Element {
    constructor(tag) { this.tag=tag; this.children=[]; this.events={}; this.attrs={}; this.paused=true; this.ended=false; this.currentTime=0; this.plays=0; elements.push(this); }
    setAttribute(k,v) { this.attrs[k]=v; }
    appendChild(x) { this.children.push(x); return x; }
    addEventListener(k,fn) { (this.events[k] ||= []).push(fn); }
    emit(k) { (this.events[k]||[]).forEach(fn=>fn()); }
    remove() { this.removed=true; }
    pause() { this.paused=true; this.emit('pause'); }
    play() {
      this.plays++;
      if (this.denied) return Promise.reject(Object.assign(new Error('Gesture required'),{name:'NotAllowedError'}));
      this.paused=false; this.ended=false; this.emit('playing'); return Promise.resolve();
    }
  }
  const body=new Element('body');
  const document={readyState:'complete',body,createElement(tag){const x=new Element(tag); x.denied=denied; return x;},getElementById(id){return elements.find(e=>e.id===id&&!e.removed);}};
  const window={ addEventListener(){} }; window.self=window;window.top=iframe?{}:window;
  const sessionStorage={getItem(){if(storageFails)throw Error('Unavailable');return value;},setItem(k,v){if(storageFails)throw Error('Unavailable');value=v;}};
  const ctx={document,window,sessionStorage};
  runInNewContext(source,ctx);
  const panel=document.getElementById('sigsfBienvenida');
  return {panel,audio:elements.find(e=>e.tag==='audio'),button:panel?.children[1],state:panel?.children[2],close:panel?.children[3],stored:()=>value,again:()=>runInNewContext(source,ctx),elements};
}
const flush=()=>new Promise(resolve=>setImmediate(resolve));
test('bloqueo automático conserva botón y permite iniciar con un clic',async()=>{
 const e=entorno({denied:true});await flush();
 assert.equal(e.stored(),null);assert.equal(e.state.textContent,'Pulsa para escuchar');
 assert.equal(e.audio.src,'audio/bienvenida.mp3');
 e.audio.denied=false;e.button.emit('click');await flush();
 assert.equal(e.audio.paused,false);assert.equal(e.stored(),'1');assert.equal(e.button.children[1].textContent,'Pausar bienvenida');
});
test('pausa, reanudación y repetición explícita del MP3',async()=>{
 const e=entorno();await flush();e.audio.currentTime=3;e.button.emit('click');
 assert.equal(e.audio.paused,true);assert.equal(e.audio.currentTime,3);
 e.button.emit('click');await flush();assert.equal(e.audio.paused,false);
 e.audio.ended=true;e.audio.paused=true;e.audio.emit('ended');
 assert.equal(e.button.children[1].textContent,'Repetir bienvenida');
 e.button.emit('click');assert.equal(e.audio.currentTime,0);
});
test('una sesión escuchada no reinicia automáticamente; permite repetir',()=>{
 const e=entorno({stored:true});assert.equal(e.audio.plays,0);
 e.button.emit('click');assert.equal(e.audio.plays,1);
});
test('funciona si el almacenamiento del navegador está deshabilitado',async()=>{
 const e=entorno({storageFails:true});await flush();assert.equal(e.audio.paused,false);
 e.close.emit('click');assert.equal(e.panel.removed,true);assert.equal(e.audio.paused,true);
});
test('cerrar detiene el audio y evita el inicio automático en la sesión',()=>{
 const e=entorno({stored:true});e.button.emit('click');e.close.emit('click');
 assert.equal(e.audio.paused,true);assert.equal(e.panel.removed,true);assert.equal(e.stored(),'1');
});
test('archivo no disponible muestra aviso y mantiene el control independiente',()=>{
 const e=entorno({stored:true});e.audio.error={code:4};e.audio.emit('error');
 assert.equal(e.state.textContent,'Audio no disponible');assert.equal(e.panel.removed,undefined);
});
test('los módulos embebidos no crean otro reproductor',()=>{
 const e=entorno({iframe:true});assert.equal(e.audio,undefined);assert.equal(e.panel,undefined);
});
test('inicialización repetida no duplica controles ni reproducción',()=>{
 const e=entorno();e.again();assert.equal(e.elements.filter(x=>x.tag==='audio').length,1);assert.equal(e.audio.plays,1);
});
