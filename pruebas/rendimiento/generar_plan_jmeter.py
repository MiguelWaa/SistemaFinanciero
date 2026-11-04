"""Genera SIGSF_carga.jmx: plan de carga de Apache JMeter (compatible 2.13 → 5.6).

Escenario PR-01 "hora punta en caja": 50 usuarios virtuales con las 43 cuentas de cajero (cajeros.csv: correo,clave sin cabecera) inician sesión
una vez y repiten durante 60 s: panel → clientes → operaciones → registrar
cambio → bandeja de solicitudes, con 0.3–0.8 s de pausa entre pasos.
Ejecutar (sin interfaz gráfica):
    jmeter -n -t SIGSF_carga.jmx -l resultados.jtl -Jhost=localhost -Jport=8092 -Jusuarios=50 -Jduracion=60
"""
from xml.sax.saxutils import escape

def http(nombre, metodo, ruta, cuerpo=None, extraer_token=False):
    body = ""
    if cuerpo is not None:
        body = f'''
          <boolProp name="HTTPSampler.postBodyRaw">true</boolProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{escape(cuerpo)}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>'''
    else:
        body = '''
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments"/>
          </elementProp>'''
    hijos = '''
          <ResponseAssertion guiclass="AssertionGui" testclass="ResponseAssertion" testname="HTTP 200" enabled="true">
            <collectionProp name="Asserion.test_strings"><stringProp name="49586">200</stringProp></collectionProp>
            <stringProp name="Assertion.test_field">Assertion.response_code</stringProp>
            <boolProp name="Assertion.assume_success">false</boolProp>
            <intProp name="Assertion.test_type">8</intProp>
          </ResponseAssertion>
          <hashTree/>'''
    if extraer_token:
        hijos += '''
          <RegexExtractor guiclass="RegexExtractorGui" testclass="RegexExtractor" testname="token" enabled="true">
            <stringProp name="RegexExtractor.useHeaders">false</stringProp>
            <stringProp name="RegexExtractor.refname">token</stringProp>
            <stringProp name="RegexExtractor.regex">"token":"([^"]+)"</stringProp>
            <stringProp name="RegexExtractor.template">$1$</stringProp>
            <stringProp name="RegexExtractor.default">SIN_TOKEN</stringProp>
            <stringProp name="RegexExtractor.match_number">1</stringProp>
          </RegexExtractor>
          <hashTree/>'''
    return f'''
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="{escape(nombre)}" enabled="true">{body}
          <stringProp name="HTTPSampler.domain">${{__P(host,localhost)}}</stringProp>
          <stringProp name="HTTPSampler.port">${{__P(port,8092)}}</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>
          <stringProp name="HTTPSampler.path">{escape(ruta)}</stringProp>
          <stringProp name="HTTPSampler.method">{metodo}</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
        </HTTPSamplerProxy>
        <hashTree>{hijos}
        </hashTree>'''

CAMBIO = '{"idCliente":${__Random(1,100)},"idTipoCambio":${__Random(1,3)},"monto":${__Random(50,2000)}.50}'

plan = f'''<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="2.8" jmeter="2.13">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="SIGSF · PR-01 Hora punta en caja (Avance 4)" enabled="true">
      <stringProp name="TestPlan.comments">Plan de Pruebas SIGSF v6.0.0 · prueba de carga. Criterios: p90 &lt; 1000 ms, errores &lt; 1 %, rendimiento &gt;= 20 peticiones/s.</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.user_defined_variables" elementType="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <stringProp name="TestPlan.user_define_classpath"></stringProp>
    </TestPlan>
    <hashTree>
      <CSVDataSet guiclass="TestBeanGUI" testclass="CSVDataSet" testname="Cajeros (cajeros.csv)" enabled="true">
        <stringProp name="filename">${{__P(csv,cajeros.csv)}}</stringProp>
        <stringProp name="fileEncoding">UTF-8</stringProp>
        <stringProp name="variableNames">email,clave</stringProp>
        <stringProp name="delimiter">,</stringProp>
        <boolProp name="ignoreFirstLine">false</boolProp>
        <boolProp name="quotedData">false</boolProp>
        <boolProp name="recycle">true</boolProp>
        <boolProp name="stopThread">false</boolProp>
        <stringProp name="shareMode">shareMode.all</stringProp>
      </CSVDataSet>
      <hashTree/>
      <HeaderManager guiclass="HeaderPanel" testclass="HeaderManager" testname="Cabeceras JSON + Bearer" enabled="true">
        <collectionProp name="HeaderManager.headers">
          <elementProp name="" elementType="Header">
            <stringProp name="Header.name">Content-Type</stringProp>
            <stringProp name="Header.value">application/json</stringProp>
          </elementProp>
          <elementProp name="" elementType="Header">
            <stringProp name="Header.name">Authorization</stringProp>
            <stringProp name="Header.value">Bearer ${{token}}</stringProp>
          </elementProp>
        </collectionProp>
      </HeaderManager>
      <hashTree/>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="Cajeros concurrentes" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlPanel" testclass="LoopController" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <intProp name="LoopController.loops">-1</intProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">${{__P(usuarios,50)}}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">${{__P(rampa,15)}}</stringProp>
        <boolProp name="ThreadGroup.scheduler">true</boolProp>
        <stringProp name="ThreadGroup.duration">${{__P(duracion,60)}}</stringProp>
        <stringProp name="ThreadGroup.delay">0</stringProp>
      </ThreadGroup>
      <hashTree>
        <UniformRandomTimer guiclass="UniformRandomTimerGui" testclass="UniformRandomTimer" testname="Pausa del usuario 0.3–0.8 s" enabled="true">
          <stringProp name="ConstantTimer.delay">300</stringProp>
          <stringProp name="RandomTimer.range">500</stringProp>
        </UniformRandomTimer>
        <hashTree/>
        <OnceOnlyController guiclass="OnceOnlyControllerGui" testclass="OnceOnlyController" testname="Inicio de sesión (una vez)" enabled="true"/>
        <hashTree>{http("01 Login cajero", "POST", "/api/auth/login", '{"email":"${email}","password":"${clave}"}', extraer_token=True)}
        </hashTree>{http("02 Panel (indicadores)", "GET", "/api/dashboard")}{http("03 Listar clientes", "GET", "/api/clientes")}{http("04 Listar operaciones", "GET", "/api/operaciones")}{http("05 Registrar cambio de moneda", "POST", "/api/operaciones", CAMBIO)}{http("06 Bandeja de solicitudes", "GET", "/api/solicitudes/pendientes")}
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
'''
with open("SIGSF_carga.jmx", "w", encoding="utf-8") as f:
    f.write(plan)
print("SIGSF_carga.jmx generado")
