/* ============================================================
   SIGSF · Selección de voz
   Elige la voz FEMENINA en español más natural disponible en el
   navegador y define un tono/velocidad suave y juvenil (~20 años).
   Usado por el asistente por voz (index) y el chat (ayuda).
   ============================================================ */
(function(){
  // Nombres de voces femeninas en español frecuentes (Google / Microsoft / Apple / Android)
  var FEMALE = [
    'monica','mónica','paulina','sabina','helena','laura','dalia','elvira','ximena',
    'paloma','marisol','angelica','angélica','lucia','lucía','camila','sofia','sofía',
    'esperanza','isabela','valentina','catalina','renata','tania','google espanol',
    'google español','female','femenin','mujer'
  ];
  // Voces masculinas a evitar
  var MALE = ['pablo','raul','raúl','jorge','diego','carlos','miguel','juan','enrique',
              'arnau','male','masculin','hombre'];
  // Señales de voces más naturales / de alta calidad
  var NATURAL = ['natural','online','neural','google','wavenet','premium','enhanced'];

  function score(v){
    var n = (v.name || '').toLowerCase();
    var lang = (v.lang || '').toLowerCase();
    if (lang.indexOf('es') !== 0) return -1000;          // solo español
    var s = 0;
    if (/es[-_](es|mx|us|419)/.test(lang)) s += 8; else s += 3;  // acentos suaves comunes
    for (var i=0;i<FEMALE.length;i++)  if (n.indexOf(FEMALE[i])  !== -1){ s += 30; break; }
    for (var j=0;j<MALE.length;j++)    if (n.indexOf(MALE[j])    !== -1){ s -= 45; break; }
    for (var k=0;k<NATURAL.length;k++) if (n.indexOf(NATURAL[k]) !== -1){ s += 12; break; }
    if (v.localService === false) s += 6;                // voces "online" suelen sonar mejor
    return s;
  }

  // Devuelve la mejor voz femenina en español (o null si no hay español)
  window.sigsfPickVoice = function(synth){
    synth = synth || window.speechSynthesis;
    if (!synth) return null;
    var vs = synth.getVoices() || [];
    if (!vs.length) return null;
    var best = null, bestScore = -1e9;
    for (var i=0;i<vs.length;i++){
      var sc = score(vs[i]);
      if (sc > bestScore){ bestScore = sc; best = vs[i]; }
    }
    return (bestScore > -1000) ? best : null;
  };

  // Tono más agudo y ritmo un poco más lento = voz más suave y joven
  window.SIGSF_VOICE = { pitch: 1.28, rate: 0.93 };

  // Aplica voz + tono a una locución
  window.sigsfApplyVoice = function(utter, voice){
    if (voice){ utter.voice = voice; utter.lang = voice.lang || 'es-ES'; }
    else { utter.lang = 'es-ES'; }
    utter.pitch = window.SIGSF_VOICE.pitch;
    utter.rate  = window.SIGSF_VOICE.rate;
    return utter;
  };
})();
