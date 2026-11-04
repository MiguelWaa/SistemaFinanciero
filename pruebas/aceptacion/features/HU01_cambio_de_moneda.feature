# language: es
@aceptacion
Característica: HU-01 Cambio de moneda en caja
  Como cajero de la casa de cambio
  quiero registrar un cambio de moneda con el tipo de cambio vigente
  para entregar al cliente el importe correcto sin calcularlo a mano

  Criterios de aceptación (RF-OPE-01, RF-OPE-02):
  CA-01.1 El total a recibir = monto × tipo de cambio de venta, con 2 decimales.
  CA-01.2 El cálculo lo hace el servidor (no se puede manipular desde el navegador).

  Escenario: CA-01.1 El sistema calcula el total a recibir
    Dado que inicié sesión como "CAJERO"
    Y abrí el módulo "operaciones financieras.html"
    Cuando registro un cambio de "250" en "USD" para el primer cliente
    Entonces la operación aparece primera en la tabla con el total calculado

  Escenario: CA-01.2 El total enviado desde el navegador se ignora
    Dado que tengo un token de "CAJERO"
    Cuando envío a la API un cambio de 100 a 3.75 con total manipulado 999999
    Entonces el sistema guarda 375.00 como total a recibir
