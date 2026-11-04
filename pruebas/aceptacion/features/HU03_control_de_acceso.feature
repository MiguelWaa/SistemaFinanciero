# language: es
@aceptacion
Característica: HU-03 Control de acceso por rol
  Como administrador del sistema
  quiero que cada rol vea solo lo que le corresponde
  para proteger la información de clientes y usuarios

  Criterios de aceptación (RF-SEG-01, RF-SEG-03, RNF-SEG-02):
  CA-03.1 Un CAJERO no puede consultar la lista de usuarios.
  CA-03.2 Tras 5 intentos fallidos la cuenta se bloquea temporalmente.
  CA-03.3 Un cliente del portal no puede ver trámites de otro cliente.

  Escenario: CA-03.1 El cajero no ve los usuarios del sistema
    Dado que inicié sesión como "CAJERO"
    Cuando abro el módulo "usuarios.html"
    Entonces veo el aviso "no tiene permiso"
    Y no se muestra ningún correo de usuario

  Escenario: CA-03.2 Bloqueo por intentos fallidos
    Dado una cuenta de prueba nueva
    Cuando escribo una contraseña incorrecta 5 veces
    Entonces el sistema responde "bloqueada" aunque luego use la contraseña correcta

  Escenario: CA-03.3 Un cliente no ve trámites ajenos
    Dado que tengo un token del portal del documento "71000013"
    Cuando consulto los trámites del cliente con id 1
    Entonces la API responde 403
