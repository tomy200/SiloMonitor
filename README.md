# SiloMonitor — Monitoreo de alimento en silos

Aplicación móvil Android (Kotlin + Jetpack Compose + Material 3) para que **operarios, supervisores y jefaturas** de una empresa avícola y ganadera consulten desde terreno el nivel de alimento de cada silo, identifiquen niveles críticos, reciban alertas, revisen históricos de consumo y registren movimientos e incidencias.

> Proyecto de la **Evaluación Parcial 2 — DSY1105 Desarrollo de Aplicaciones Móviles** (Duoc UC), basado en el **Caso 3: Monitoreo de alimento en silos**. Es la base del proyecto para la Evaluación Final Transversal (EFT).

## Integrantes

| Nombre | Rol en el equipo | Usuario GitHub |
|---|---|---|
| Tomás González Llanos | Capa de datos (Room, DataStore, API simulada), reglas de negocio, validaciones y recursos nativos (cámara, galería, notificaciones, vibración) | [@tomy200](https://github.com/tomy200) |
| Antonio Espinoza | Diseño visual Material 3, navegación, formularios y animaciones | [@antonioespinoza10](https://github.com/antonioespinoza10) |

**Sección:** DSY1105 Desarrollo de Aplicaciones Móviles (003D) · **Equipo:** N° _(por confirmar)_ · **Docente:** Fernando Sepúlveda Campos

---

## Problema que resuelve

Hoy el seguimiento de niveles, cargas, descargas y consumos no está centralizado en una app móvil, lo que retrasa las reposiciones y aumenta el riesgo de desabastecimiento. SiloMonitor permite:

- Consultar el nivel de alimento por **granja y silo** con su porcentaje de capacidad.
- Identificar de inmediato los silos en **estado normal, advertencia o crítico**.
- Recibir **notificaciones** cuando un silo requiere reposición.
- Revisar el **historial de niveles** y la **autonomía** (días restantes) de cada silo.
- Registrar **cargas, descargas y consumos** y reportar **incidencias con foto**.
- Trabajar con **conectividad limitada**: los datos quedan guardados en el teléfono.

## Funcionalidades implementadas

| Módulo | Detalle |
|---|---|
| Inicio de sesión | Usuarios de prueba o cuentas creadas en la app. Validación por campo, contraseña con hash SHA-256, opción "mantener sesión" (DataStore). |
| Registro de cuenta | Formulario con nombre, correo, contraseña (mín. 8 caracteres con letras y números), confirmación y **elección del cargo**: operario, supervisor o jefatura. No permite correos repetidos y deja la sesión iniciada. |
| Roles y permisos | **Operario**: consulta y registra movimientos/incidencias. **Supervisor**: además atiende alertas. **Jefatura**: solo consulta resúmenes e históricos. |
| Resumen general | Indicadores animados (silos totales, normales, en advertencia, críticos), alimento disponible global, prioridad de reposición y estado por granja. |
| Silos por granja | 3 granjas × 4 silos (12 silos simulados). Filtros por granja y estado, indicador gráfico del silo, deslizar para actualizar (API simulada). |
| Detalle del silo | Nivel animado, consumo diario estimado, días restantes, gráfico de historial de 14 días con umbrales, últimos movimientos e incidencias. |
| Registrar movimiento | Formulario validado de carga / descarga / consumo con vista previa del nivel resultante y aviso si generará una alerta. |
| Reportar incidencia | Tipo, prioridad, descripción y foto desde **cámara o galería** (obligatoria si la prioridad es alta). |
| Alertas | Activas / atendidas, badge en la barra de navegación, el supervisor las marca como atendidas y se cierran solas al reponer el silo. |
| Perfil | Foto de perfil (cámara/galería), permisos del rol, preferencias guardadas (tema oscuro, notificaciones, vibración), modo "sin conexión" de demostración y cierre de sesión. |

### Reglas de negocio

1. **Estado del silo** según su porcentaje de llenado: Normal **> 30 %**, Advertencia **20 % – 30 %**, Crítico **< 20 %**.
2. Una **carga** no puede superar la capacidad libre del silo; una **descarga o consumo** no puede superar el alimento disponible.
3. Toda **descarga** debe indicar su motivo.
4. Una incidencia de **prioridad alta** requiere foto como evidencia.
5. Cuando un silo **empeora de estado** se crea una alerta y se envía una notificación; cuando **vuelve a normal** tras una carga, sus alertas activas se cierran automáticamente.
6. **Días restantes** = alimento disponible ÷ consumo promedio diario de los últimos 7 días.
7. Solo **operarios y supervisores** registran movimientos e incidencias; solo **supervisores** atienden alertas.
8. Al **crear una cuenta** el usuario elige su cargo, y ese cargo define sus permisos. No puede haber dos cuentas con el mismo correo.

---

## Requisitos de la pauta y dónde se cumplen

| Requisito (Ev. Parcial 2) | Implementación | Archivos principales |
|---|---|---|
| Diseño visual con Material 3 | Esquema de colores propio claro/oscuro, colores semánticos por estado, tipografía con jerarquía, tarjetas, chips y barras de navegación M3. | `ui/theme/`, `ui/components/` |
| Diseño adaptable (Guía 9) | `WindowSizeClass`: barra inferior en celular y barra lateral (NavigationRail) en tablet; grilla de 1, 2 o 3 columnas; detalle en dos paneles en pantallas expandidas; previews Compact/Medium/Expanded. | `ui/utils/WindowSizeUtils.kt`, `navigation/AppNavigation.kt`, `ResumenScreen.kt` |
| Navegación funcional (Guía 10) | `sealed class Screen` con rutas tipadas y argumentos, `NavigationEvent` emitidos por `MainViewModel`, `NavHost` con transiciones animadas, TopAppBar con botón volver. | `navigation/` , `viewmodel/MainViewModel.kt` |
| Formularios validados con íconos y mensajes | Login, registro de cuenta, registrar movimiento y reportar incidencia. Cada campo muestra ícono de error/válido, mensaje en `supportingText`, contador de caracteres y validación en vivo después del primer envío. | `ui/components/CampoFormulario.kt`, `ui/screens/login`, `registro`, `movimiento`, `incidencia` |
| Validaciones desde la lógica (desacopladas) | Todas las reglas están en `Validadores` (funciones puras) y las usan los ViewModels; la UI solo muestra el resultado. Cubiertas con pruebas unitarias. | `domain/validation/Validadores.kt`, `app/src/test/` |
| Gestión de estado | `data class` de estado por pantalla, `MutableStateFlow` + `update`, `collectAsState`, `combine`/`stateIn`, `rememberSaveable`, `derivedStateOf`, eventos de una sola vez con `SharedFlow`. | `model/*UiState.kt`, `viewmodel/` |
| Almacenamiento local | **Room (SQLite)** para granjas, silos, movimientos, alertas, incidencias y usuarios. **DataStore Preferences** para sesión y preferencias (Guía 12). Funciona sin conexión. | `data/local/`, `data/preferences/` |
| Recursos nativos (mín. 2) | **Cámara** (TakePicture + FileProvider + permiso en tiempo de ejecución), **Galería** (GetContent), **Notificaciones** (canal + permiso POST_NOTIFICATIONS en Android 13+) y **Vibración** (feedback háptico). | `ui/components/SelectorImagen.kt`, `util/NotificacionHelper.kt`, `util/VibracionHelper.kt`, `AndroidManifest.xml` |
| Animaciones | Splash con rebote, transiciones entre pantallas, nivel del silo que sube/baja, colores animados por estado, ícono crítico que pulsa, KPIs que cuentan, gráfico que se dibuja, sacudida del formulario con errores, `AnimatedVisibility` en mensajes, `AnimatedContent` en botones, `animateItem` en listas. | `ui/components/`, `ui/screens/` |
| Estructura modular MVVM | Carpetas por capa: `model`, `viewmodel`, `repository`, `data`, `domain`, `navigation`, `ui`, `util`. Inyección manual de dependencias en `ContenedorApp`. | ver árbol abajo |
| API (caso) | `SiloApiSimulada` imita el sistema de monitoreo (demora de red y falla controlada). Solo el repositorio la conoce, así que puede reemplazarse por Retrofit en la EFT. | `data/remote/SiloApiSimulada.kt` |

---

## Arquitectura (MVVM)

```
UI (Compose)  ──observa──▶  ViewModel (StateFlow)  ──usa──▶  Repository  ──▶  Room · DataStore · API simulada
     ▲                              │
     └──────── eventos ◀────────────┘
```

```
app/src/main/java/com/example/silomonitor/
├── MainActivity.kt              # Única Activity (Compose + tema + tamaño de ventana)
├── SiloMonitorApp.kt            # Application: crea dependencias y canal de notificaciones
├── ContenedorApp.kt             # Inyección manual de dependencias
├── data/
│   ├── local/                   # Room: SiloDatabase, entity/, dao/
│   ├── preferences/             # DataStore: sesión y preferencias
│   ├── remote/                  # API simulada del sistema de monitoreo
│   └── seed/                    # Datos ficticios (3 granjas, 12 silos, 14 días de historial)
├── domain/
│   ├── CalculosSilo.kt          # Reglas: porcentaje, estado, consumo, días restantes
│   └── validation/Validadores.kt
├── model/                       # Enums, modelos de presentación y UiState de cada pantalla
├── navigation/                  # Screen (rutas), NavigationEvent, AppNavigation (NavHost)
├── repository/                  # SiloRepository, UsuarioRepository
├── viewmodel/                   # Un ViewModel por pantalla + MainViewModel
├── ui/
│   ├── components/              # CampoFormulario, SiloIndicador, GraficoHistorial, ImagenInteligente…
│   ├── screens/                 # splash, login, resumen, silos, detalle, movimiento, incidencia, alertas, perfil
│   ├── theme/                   # Colores, tipografía, colores de estado
│   └── utils/WindowSizeUtils.kt
└── util/                        # Notificaciones, vibración, imágenes, formatos, seguridad
```

## Tecnologías

Kotlin 2.0 · Jetpack Compose (BOM 2024.12) · Material 3 · Navigation Compose · ViewModel + StateFlow · Coroutines · Room 2.6 (KSP) · DataStore Preferences · Coil · Material 3 Window Size Classes · JUnit 4.

---

## Cómo ejecutar

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/tomy200/SiloMonitor.git
   ```
2. Abrir la carpeta en **Android Studio** (Ladybug o más reciente) y esperar a que Gradle sincronice (descarga las dependencias la primera vez).
3. Crear un emulador en **Device Manager** (recomendado: Pixel con Android 13/14, API 33+) o conectar un teléfono con depuración USB.
4. Presionar **Run ▶ 'app'**.
5. Ingresar con uno de los usuarios de prueba (también aparecen en la pantalla de login; tocar uno completa el formulario):

| Rol | Correo | Contraseña |
|---|---|---|
| Operario | `operario@silomonitor.test` | `Operario123` |
| Supervisor | `supervisor@silomonitor.test` | `Super1234` |
| Jefatura | `jefatura@silomonitor.test` | `Jefatura123` |

6. Aceptar el permiso de notificaciones. Para probar la cámara, usar un dispositivo físico o la cámara virtual del emulador.

**Pruebas unitarias:** clic derecho en `app/src/test/java` → *Run Tests* (o `./gradlew test`).

### Guion sugerido para la demostración

1. Login como **Supervisor** → Resumen (indicadores animados y prioridad de reposición).
2. Silos → filtrar por "Crítico" → abrir **Silo A-01** (gráfico de historial, autonomía de ~1 día).
3. Abrir **Silo P-03** (54 %) → Registrar movimiento: intentar una **descarga** sin motivo o con más kilos de los disponibles (ver validaciones y sacudida) y luego registrar un **consumo de 9000 kg** → el silo queda en ~18 % (crítico): alerta, vibración y notificación.
4. Alertas → **Registrar carga** de 12000 kg sobre P-03 → vuelve a ~66 % y la alerta se cierra sola.
5. Reportar una **incidencia de prioridad alta** con foto de la cámara.
6. Perfil → activar **Simular sin conexión** y deslizar para actualizar en Silos (los datos siguen disponibles).
7. Cerrar sesión e ingresar como **Jefatura** (solo consulta: no ve botones de registro).

---

## Trabajo colaborativo

- **Repositorio:** https://github.com/tomy200/SiloMonitor
- **Tablero Trello:** https://trello.com/b/Wjt5M7IE/silomonitor (listas: Por hacer · En curso · Finalizado)
- Ramas por funcionalidad (`feature/...`) integradas a `main` mediante Pull Requests.
- El checklist de entrega está en [`docs/CHECKLIST_ENTREGA.md`](docs/CHECKLIST_ENTREGA.md).
- El detalle de tareas, ramas y commits está en [`docs/PLAN_TRABAJO.md`](docs/PLAN_TRABAJO.md) y el informe del proyecto en [`docs/INFORME_PROYECTO.md`](docs/INFORME_PROYECTO.md).

## Próximos pasos (EFT)

- Reemplazar la API simulada por una API REST real (Retrofit) manteniendo Room como caché offline.
- Sincronización periódica en segundo plano (WorkManager) para generar alertas sin abrir la app.
- Pruebas de interfaz con Compose UI Test.
