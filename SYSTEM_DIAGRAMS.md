# Diagramas de Procesos y Roles — Lunaris Ansenuza

Este documento presenta los principales actores y flujos operativos de la
plataforma. Los diagramas utilizan sintaxis Mermaid compatible con renderizadores
Markdown modernos.

## 1. 👥 Diagrama de roles y casos de uso

> Mermaid no dispone de un tipo `usecase` estándar. Este modelo utiliza
> `flowchart LR` con actores y casos de uso agrupados por dominio.

```mermaid
flowchart LR
    passenger["👤 Pasajero"]
    driver["🚐 Chofer"]
    operator["🎧 Operador"]
    admin["🛡️ Administrador"]
    billing["🧾 Facturación"]

    subgraph passengerCases["Canales del pasajero — Web y WhatsApp"]
        quote(["Cotizar traslado"])
        reserve(["Crear reserva"])
        consult(["Consultar viajes"])
        cancel(["Cancelar reserva"])
        pay(["Enviar comprobante de pago"])
        promo(["Aplicar código promocional"])
    end

    subgraph driverCases["Atención del chofer — WhatsApp"]
        route(["Consultar Hoja de Ruta<br/>Ver Ruta / Mis Viajes"])
        location(["Notificar ubicación"])
        onboard(["Marcar abordaje<br/>Marcar a Bordo"])
    end

    subgraph operationCases["Panel operativo"]
        reservations(["Gestionar reservas"])
        agenda(["Gestionar agenda"])
        assign(["Asignar choferes"])
        unassign(["Desasignar o reemplazar rutas"])
        passengers(["Gestionar pasajeros"])
        chats(["Atender chats"])
    end

    subgraph adminCases["Administración"]
        drivers(["Gestionar choferes"])
        fares(["Administrar tarifas"])
        localities(["Administrar localidades"])
        users(["Gestionar usuarios y roles"])
        configuration(["Configurar bot y operación"])
    end

    subgraph billingCases["Gestión fiscal"]
        validate(["Validar pago"])
        invoice(["Emitir factura"])
        resend(["Enviar o reenviar PDF"])
    end

    passenger --> quote
    passenger --> reserve
    passenger --> consult
    passenger --> cancel
    passenger --> pay
    passenger --> promo

    driver --> route
    driver --> location
    driver --> onboard

    operator --> reservations
    operator --> agenda
    operator --> assign
    operator --> unassign
    operator --> passengers
    operator --> chats

    admin --> reservations
    admin --> agenda
    admin --> assign
    admin --> unassign
    admin --> passengers
    admin --> chats
    admin --> drivers
    admin --> fares
    admin --> localities
    admin --> users
    admin --> configuration
    admin --> validate
    admin --> invoice
    admin --> resend

    billing --> validate
    billing --> invoice
    billing --> resend

    classDef actor fill:#1e293b,color:#ffffff,stroke:#0f172a,stroke-width:2px;
    classDef passengerUse fill:#dbeafe,color:#1e3a8a,stroke:#3b82f6;
    classDef driverUse fill:#dcfce7,color:#14532d,stroke:#22c55e;
    classDef operationUse fill:#fef3c7,color:#78350f,stroke:#f59e0b;
    classDef adminUse fill:#ede9fe,color:#4c1d95,stroke:#8b5cf6;
    classDef billingUse fill:#fee2e2,color:#7f1d1d,stroke:#ef4444;

    class passenger,driver,operator,admin,billing actor;
    class quote,reserve,consult,cancel,pay,promo passengerUse;
    class route,location,onboard driverUse;
    class reservations,agenda,assign,unassign,passengers,chats operationUse;
    class drivers,fares,localities,users,configuration adminUse;
    class validate,invoice,resend billingUse;
```

### Matriz resumida de permisos

| Rol | Capacidades principales |
| --- | --- |
| `PASSENGER` | Perfil y reservas propias mediante web o WhatsApp |
| `CHOFER` | Hoja de ruta, ubicación y abordaje |
| `OPERADOR` | Reservas, agenda, pasajeros, asignaciones y chats |
| `ADMIN` | Acceso operativo y configuración integral |
| `FACTURACION` | Facturas y documentación fiscal |

---

## 2. 🤖 Flujo completo del bot y la reserva

```mermaid
flowchart TD
    start([👤 Pasajero escribe Hola o Menú])
    session{¿Existe sesión?}
    createSession["Crear ConversationSession<br/>Asignar operador con menor carga"]
    menu["Mostrar menú principal"]
    choice{Opción elegida}

    quote["2 — Ver precios y cotizar"]
    booking["1 — Reservar viaje"]
    support["3 — Hablar con operador"]
    trips["4 — Consultar reservas"]
    cancellation["5 — Cancelar viaje"]

    locality["Seleccionar localidad de origen"]
    schedule["Seleccionar horario disponible"]
    knownPassenger{¿Pasajero registrado?}
    identity["Solicitar nombre y datos"]
    companions["Indicar acompañantes"]
    knownAddress{¿Domicilio habitual<br/>en la localidad?}
    confirmAddress["Confirmar domicilio existente"]
    newAddress["Ingresar o compartir nueva dirección"]
    destination["Seleccionar destino"]
    tripType["Elegir solo ida o ida y vuelta"]
    outboundDate["Ingresar fecha de ida"]
    returnChoice{¿Ida y vuelta?}
    returnDate["Elegir fecha de vuelta<br/>o vuelta abierta"]
    invoiceData["Indicar necesidad de factura<br/>y datos fiscales"]
    promotion["Ingresar código de 4 dígitos<br/>o SIN PROMO"]
    summary["Mostrar resumen y precio"]
    confirm{¿Confirmar reserva?}
    persist["Guardar Passenger y Reservation"]
    freePromo{¿Promoción del 100 %?}
    paymentPending["Estado PENDING_PAYMENT<br/>Enviar datos bancarios"]
    sendReceipt["Pasajero envía comprobante"]
    receiptReceived["Estado PAYMENT_RECEIVED"]
    validatePayment["Operador valida el pago"]
    confirmed["Estado CONFIRMED"]
    end([✅ Reserva confirmada])

    start --> session
    session -- No --> createSession --> menu
    session -- Sí --> menu
    menu --> choice

    choice -- "1" --> booking --> locality
    choice -- "2" --> quote --> locality
    choice -- "3" --> support
    choice -- "4" --> trips
    choice -- "5" --> cancellation

    locality --> schedule --> knownPassenger
    knownPassenger -- No --> identity --> companions
    knownPassenger -- Sí --> companions
    companions --> knownAddress
    knownAddress -- Sí --> confirmAddress --> destination
    knownAddress -- No --> newAddress --> destination
    destination --> tripType --> outboundDate --> returnChoice
    returnChoice -- Sí --> returnDate --> invoiceData
    returnChoice -- No --> invoiceData
    invoiceData --> promotion --> summary --> confirm
    confirm -- No --> menu
    confirm -- Sí --> persist --> freePromo
    freePromo -- Sí --> confirmed
    freePromo -- No --> paymentPending --> sendReceipt
    sendReceipt --> receiptReceived --> validatePayment --> confirmed --> end

    classDef startEnd fill:#1e293b,color:#ffffff,stroke:#0f172a,stroke-width:2px;
    classDef interaction fill:#dbeafe,color:#1e3a8a,stroke:#3b82f6;
    classDef decision fill:#fef3c7,color:#78350f,stroke:#f59e0b;
    classDef persistence fill:#ede9fe,color:#4c1d95,stroke:#8b5cf6;
    classDef success fill:#dcfce7,color:#14532d,stroke:#22c55e,stroke-width:2px;
    classDef exception fill:#fee2e2,color:#7f1d1d,stroke:#ef4444;

    class start,end startEnd;
    class menu,quote,booking,support,trips,cancellation,locality,schedule,identity,companions,confirmAddress,newAddress,destination,tripType,outboundDate,returnDate,invoiceData,promotion,summary,sendReceipt interaction;
    class session,choice,knownPassenger,knownAddress,returnChoice,confirm,freePromo decision;
    class createSession,persist,paymentPending,receiptReceived,validatePayment persistence;
    class confirmed success;
```

### Secuencia WhatsApp–backend

```mermaid
sequenceDiagram
    autonumber
    actor P as 👤 Pasajero
    participant WA as WhatsApp / Meta
    participant O as ConversationOrchestrator
    participant S as ConversationSession
    participant UC as Casos de uso
    participant DB as PostgreSQL
    participant OP as 🎧 Operador

    P->>WA: Hola
    WA->>O: Webhook con mensaje entrante
    O->>DB: Buscar sesión por teléfono

    alt Primera interacción
        O->>UC: Obtener operador con menor carga
        UC-->>O: Operador asignado
        O->>DB: Crear ConversationSession
    else Sesión existente
        DB-->>O: Sesión y currentStep
    end

    O->>WA: Mostrar menú
    P->>WA: Seleccionar Reservar o Cotizar

    loop Datos requeridos
        WA->>O: Respuesta o botón
        O->>S: Validar currentStep
        S->>DB: Persistir avance y datos parciales
        O->>WA: Solicitar siguiente dato
    end

    O->>UC: Calcular precio y validar promoción
    UC->>DB: Consultar Fare, parámetros y promoción
    UC-->>O: Total y descuento
    O->>WA: Mostrar resumen
    P->>WA: Confirmar
    O->>UC: Crear reserva
    UC->>DB: Guardar Passenger y Reservation

    alt Promoción 100 %
        UC->>DB: Estado CONFIRMED
        O->>WA: Reserva confirmada sin pago
    else Pago requerido
        UC->>DB: Estado PENDING_PAYMENT
        O->>WA: Enviar instrucciones de transferencia
        P->>WA: Foto del comprobante
        O->>DB: Guardar comprobante y PAYMENT_RECEIVED
        OP->>UC: Validar pago
        UC->>DB: Actualizar a CONFIRMED
        UC->>WA: Notificar confirmación
    end
```

---

## 3. 📅 Asignación de reservas a un chofer

```mermaid
sequenceDiagram
    autonumber
    actor OP as 🎧 Operador / Admin
    participant PANEL as Panel Agenda
    participant ROUTE as DriverRouteService
    participant DB as PostgreSQL
    participant WA as WhatsApp / Meta
    actor D as 🚐 Chofer
    actor P as 👤 Pasajero

    OP->>PANEL: Abrir Agenda y seleccionar fecha
    PANEL->>DB: Consultar reservas activas
    DB-->>PANEL: Pasajeros, horarios y estados
    OP->>PANEL: Seleccionar chofer y reservas
    PANEL->>ROUTE: Reemplazar hoja de ruta
    ROUTE->>DB: Bloquear y validar reservas

    alt Selección válida
        ROUTE->>DB: Asignar driver_id y route_sequence
        ROUTE->>DB: Desasignar reservas omitidas<br/>de la ruta reemplazada
        DB-->>ROUTE: Ruta persistida
        ROUTE-->>PANEL: Asignación correcta
        PANEL->>WA: Notificar chofer asignado
        WA-->>P: Mensaje con nombre del chofer
        PANEL->>WA: Enviar plantilla de hoja de ruta
        WA-->>D: Aviso para consultar Ver Ruta
    else Fecha, chofer o reservas inválidas
        ROUTE-->>PANEL: Rechazar operación
        PANEL-->>OP: Mostrar error sin asignar
    end
```

```mermaid
flowchart LR
    agenda["📅 Agenda del día"] --> selectDriver["Seleccionar chofer activo"]
    selectDriver --> selectPassengers["Marcar pasajeros"]
    selectPassengers --> replace["Reemplazar hoja de ruta"]
    replace --> assigned["Asignar driver_id"]
    replace --> sequence["Calcular route_sequence"]
    replace --> removed["Desasignar pasajeros omitidos"]
    assigned --> notifyPassenger["Notificar al pasajero"]
    sequence --> notifyDriver["Avisar al chofer por WhatsApp"]
    removed --> available["Reservas disponibles para reasignación"]

    classDef operation fill:#fef3c7,color:#78350f,stroke:#f59e0b;
    classDef persistence fill:#ede9fe,color:#4c1d95,stroke:#8b5cf6;
    classDef notification fill:#dbeafe,color:#1e3a8a,stroke:#3b82f6;
    classDef available fill:#dcfce7,color:#14532d,stroke:#22c55e;

    class agenda,selectDriver,selectPassengers operation;
    class replace,assigned,sequence,removed persistence;
    class notifyPassenger,notifyDriver notification;
    class available available;
```

---

## 4. 🚐 Abordaje y ciclo del viaje

```mermaid
sequenceDiagram
    autonumber
    actor D as 🚐 Chofer
    participant WA as WhatsApp / Meta
    participant O as ConversationOrchestrator
    participant UC as OnboardPassengerUseCase
    participant DB as PostgreSQL
    actor NEXT as 👤 Próximo pasajero
    actor OP as 🎧 Operador

    D->>WA: Ver Ruta / Mis Viajes
    WA->>O: Comando del chofer
    O->>DB: Buscar chofer y reservas asignadas
    DB-->>O: Hoja ordenada
    O->>WA: Ruta, mapa y lista de pasajeros
    WA-->>D: Mostrar Ver Pasajeros

    D->>WA: Marcar a Bordo
    WA->>O: ONBOARD_{reservationId}
    O->>DB: Consultar estado actual

    alt Reserva abordable
        O->>UC: Ejecutar abordaje
        UC->>DB: Bloqueo pesimista de la reserva
        UC->>DB: Estado ONBOARDED
        UC->>DB: Buscar próximo pasajero
        opt Existe próximo pasajero
            UC->>WA: Enviar plantilla Próximo en camino
            WA-->>NEXT: Aviso y ubicación disponible
        end
        UC-->>O: Reserva actualizada
        O->>WA: Confirmar pasajero a bordo
        WA-->>D: ✓ Pasajero marcado a bordo
    else Ya abordada o finalizada
        O->>WA: Informar operación ya cerrada
        WA-->>D: Sin cambios duplicados
    else Reserva inexistente o no autorizada
        O->>WA: Informar error
        WA-->>D: Solicitar revisión con operador
    end

    OP->>DB: Actualizar cierre operativo
    DB-->>OP: Estado COMPLETED / REALIZED
```

### Máquina de estados operativa simplificada

```mermaid
stateDiagram-v2
    [*] --> PENDING: Reserva creada
    PENDING --> ONBOARDED: Chofer marca abordaje
    PENDING --> CANCELED: Cancelación
    PENDING --> NO_SHOW: Pasajero ausente
    ONBOARDED --> COMPLETED: Operador finaliza viaje
    COMPLETED --> REALIZED: Cierre operativo
    CANCELED --> [*]
    NO_SHOW --> [*]
    REALIZED --> [*]

    note right of ONBOARDED
      La acción desde WhatsApp es idempotente:
      repetirla no duplica el abordaje.
    end note
```

> En el modelo persistido, `ONBOARD`, `BOARDED` y `ONBOARDED` son estados
> reconocidos por compatibilidad. El flujo conversacional actual escribe
> `ONBOARDED`. El cierre puede representarse mediante el estado comercial
> `COMPLETED` y el estado operativo `REALIZED`.

---

## 5. 🧾 Pago, confirmación y facturación

```mermaid
flowchart TD
    reservation["Reserva PENDING_PAYMENT"]
    receipt["Pasajero envía comprobante"]
    received["Reserva PAYMENT_RECEIVED"]
    review{¿Pago válido?}
    reject["Solicitar revisión o nuevo comprobante"]
    confirm["Reserva CONFIRMED"]
    invoiceRequired{¿Requiere factura?}
    eligible["Disponible en módulo Facturación"]
    upload["Operador sube PDF fiscal"]
    invoice["Crear o actualizar Invoice<br/>reservation_id → reservations.id"]
    send["Enviar factura por WhatsApp"]
    sent["Registrar sent_via_whatsapp y sent_at"]
    complete([✅ Proceso completado])

    reservation --> receipt --> received --> review
    review -- No --> reject --> receipt
    review -- Sí --> confirm --> invoiceRequired
    invoiceRequired -- No --> complete
    invoiceRequired -- Sí --> eligible --> upload --> invoice --> send --> sent --> complete

    classDef pending fill:#fef3c7,color:#78350f,stroke:#f59e0b;
    classDef review fill:#dbeafe,color:#1e3a8a,stroke:#3b82f6;
    classDef rejected fill:#fee2e2,color:#7f1d1d,stroke:#ef4444;
    classDef persistence fill:#ede9fe,color:#4c1d95,stroke:#8b5cf6;
    classDef success fill:#dcfce7,color:#14532d,stroke:#22c55e,stroke-width:2px;

    class reservation,receipt,received pending;
    class review,invoiceRequired review;
    class reject rejected;
    class eligible,upload,invoice,send,sent persistence;
    class confirm,complete success;
```

```mermaid
sequenceDiagram
    autonumber
    actor P as 👤 Pasajero
    participant WA as WhatsApp / Meta
    participant PAYMENT as ConfirmPaymentUseCase
    participant DB as PostgreSQL
    actor B as 🧾 Admin / Facturación
    participant INVOICE as IssueInvoiceUseCase
    participant STORAGE as Almacenamiento PDF

    P->>WA: Enviar comprobante
    WA->>DB: Guardar URL y PAYMENT_RECEIVED
    B->>PAYMENT: Validar pago
    PAYMENT->>DB: Bloquear reserva o grupo
    PAYMENT->>DB: Actualizar paymentVerified y CONFIRMED
    PAYMENT->>WA: Notificar pago confirmado
    WA-->>P: Reserva confirmada

    alt Pasajero requiere factura
        B->>INVOICE: Emitir factura con PDF
        INVOICE->>DB: Validar reserva confirmada
        INVOICE->>STORAGE: Subir PDF
        STORAGE-->>INVOICE: URL segura
        INVOICE->>DB: Guardar Invoice asociado
        INVOICE->>WA: Enviar documento
        WA-->>P: Factura en PDF
        INVOICE->>DB: Registrar fecha de envío
    else No requiere factura
        DB-->>B: Sin acción fiscal pendiente
    end
```

---

## 6. 🎨 Leyenda visual

- 🔵 **Azul:** interacción con usuarios o canales.
- 🟡 **Amarillo:** decisión o actividad operativa.
- 🟣 **Violeta:** persistencia y procesos internos.
- 🟢 **Verde:** resultado exitoso o estado confirmado.
- 🔴 **Rojo:** rechazo, cancelación o error.
- ⚫ **Oscuro:** actor o punto de inicio/fin.

