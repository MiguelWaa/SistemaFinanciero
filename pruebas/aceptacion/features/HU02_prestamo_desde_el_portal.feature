# language: es
@aceptacion
Característica: HU-02 Solicitud de préstamo desde el Portal del Cliente
  Como cliente
  quiero solicitar un préstamo y ver mi cuota antes de ir a la agencia
  para decidir si me conviene

  Criterios de aceptación (RF-OPE-05, RF-POR-03):
  CA-02.1 La cuota = monto × (1 + 15 % × meses) ÷ meses, con 2 decimales.
  CA-02.2 Montos fuera de S/ 100 a S/ 1 000 000 se rechazan con un mensaje claro.
  CA-02.3 Una solicitud rechazada no aparece en mi historial.

  Esquema del escenario: Cuota según monto y plazo
    Dado que ingresé al portal con el documento "<documento>"
    Cuando solicito un préstamo de "<monto>" a "<plazo>" meses
    Entonces veo el mensaje "<mensaje>"
    Y mi historial <efecto>

    Ejemplos:
      | documento | monto  | plazo | mensaje                | efecto                 |
      | 71000008  | 1200   | 6     | 380.00                 | muestra el préstamo    |
      | 71000009  | 10000  | 24    | 1,916.67               | muestra el préstamo    |
      | 71000010  | 99.99  | 12    | mínimo es S/ 100.00    | no cambia              |
      | 71000011  | 2000000 | 12   | máximo es S/ 1000000.00 | no cambia             |
