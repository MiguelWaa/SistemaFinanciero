# language: es
@aceptacion
Característica: HU-04 Bandeja de solicitudes
  Como supervisor
  quiero aprobar las solicitudes que llegan del portal
  para que el préstamo quede activo y el cliente lo vea en su historial

  Criterios de aceptación (RF-SOL-01, RF-SOL-02):
  CA-04.1 Al aprobar, el préstamo pasa a ACTIVO y la operación a COMPLETADA.

  Escenario: CA-04.1 Aprobar una solicitud del portal
    Dado que el cliente "71000015" solicitó un préstamo de "4000" a "12" meses
    Y que inicié sesión como "ADMIN"
    Cuando apruebo su solicitud en la bandeja
    Entonces la operación del cliente queda "COMPLETADA" en su historial
