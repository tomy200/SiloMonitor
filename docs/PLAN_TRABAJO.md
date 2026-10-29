# Plan de trabajo colaborativo (GitHub + Trello)

La pauta evalúa (20 %) que **ambos integrantes** tengan commits reales, distribuidos y con mensajes técnicos, y que la planificación esté visible en Trello. Este plan reparte el proyecto en tareas, ramas y commits para que cada integrante suba **su parte desde su propia cuenta**.

- **Integrante A — Tomás González Llanos (@tomy200):** capa de datos, persistencia, lógica y recursos nativos.
- **Integrante B — Antonio Espinoza (@antonioespinoza10):** diseño visual, navegación, formularios y animaciones.

## 1. Preparación (Guía 7)

1. ✅ Tomás creó el repositorio público https://github.com/tomy200/SiloMonitor. Falta agregar como colaboradores a Antonio (@antonioespinoza10) y al docente Fernando Sepúlveda Campos (Settings → Collaborators).
2. ✅ Tablero creado: https://trello.com/b/Wjt5M7IE/silomonitor (listas **Por hacer · En curso · Finalizado**). Falta invitar a Antonio y al docente (botón Compartir).
3. Cada tarjeta lleva responsable, fecha, checklist y el enlace al commit o Pull Request cuando se termina.
4. Proteger `main`: cada funcionalidad se trabaja en su rama `feature/...` y se integra con Pull Request revisado por el otro integrante.

## 2. Tarjetas de Trello, ramas y commits

| # | Tarjeta Trello | Resp. | Rama | Archivos | Mensaje de commit sugerido |
|---|---|---|---|---|---|
| 1 | Estructura base MVVM | A | `main` | `settings.gradle.kts`, `build.gradle.kts`, `gradle/`, `app/build.gradle.kts`, `AndroidManifest.xml`, carpetas vacías | `Inicio de proyecto + estructura base MVVM` |
| 2 | Tema Material 3 y recursos | B | `feature/tema-material3` | `ui/theme/`, `res/drawable/`, `res/values/`, `res/mipmap-anydpi-v26/` | `feat(ui): tema Material 3 claro/oscuro, colores de estado e íconos` |
| 3 | Modelos y reglas de negocio | A | `feature/modelo-dominio` | `model/`, `domain/`, `util/Formatos.kt`, `util/Seguridad.kt` | `feat(domain): modelos, cálculo de estado por umbrales y días restantes` |
| 4 | Validaciones + pruebas unitarias | A | `feature/modelo-dominio` | `domain/validation/`, `app/src/test/` | `test(domain): validadores de formularios y pruebas unitarias` |
| 5 | Persistencia Room | A | `feature/persistencia-room` | `data/local/`, `data/seed/` | `feat(data): base de datos Room con entidades, DAOs y datos simulados` |
| 6 | DataStore y API simulada | A | `feature/persistencia-room` | `data/preferences/`, `data/remote/`, `repository/`, `ContenedorApp.kt`, `SiloMonitorApp.kt` | `feat(data): sesión en DataStore, API simulada y repositorios` |
| 7 | Componentes reutilizables | B | `feature/componentes-ui` | `ui/components/` (excepto ImagenInteligente y SelectorImagen) | `feat(ui): componentes SiloIndicador, gráfico, KPI y campo de formulario` |
| 8 | Navegación y estructura visual | B | `feature/navegacion` | `navigation/`, `viewmodel/MainViewModel.kt`, `MainActivity.kt`, `ui/utils/` | `Navegación y menú generado` |
| 9 | Splash, login y registro | B | `feature/registro-formulario` | `ui/screens/splash/`, `ui/screens/login/`, `ui/screens/registro/`, `viewmodel/SplashViewModel.kt`, `viewmodel/LoginViewModel.kt`, `viewmodel/RegistroViewModel.kt`, `model/RegistroUiState.kt` | `Formulario y resumen funcional` (login validado + splash animado) |
| 10 | Resumen y lista de silos | B | `feature/home-screen` | `ui/screens/resumen/`, `ui/screens/silos/`, `ui/preview/`, `viewmodel/ResumenViewModel.kt`, `viewmodel/SilosViewModel.kt` | `feat(ui): resumen adaptable y lista de silos con filtros` |
| 11 | Detalle y movimientos | A | `feature/movimientos` | `ui/screens/detalle/`, `ui/screens/movimiento/`, `viewmodel/DetalleSiloViewModel.kt`, `viewmodel/MovimientoViewModel.kt` | `feat(movimientos): detalle del silo y registro validado de movimientos` |
| 12 | Cámara y galería | A | `feature/imagen-inteligente` | `ui/components/ImagenInteligente.kt`, `ui/components/SelectorImagen.kt`, `util/ImagenHelper.kt`, `res/xml/file_paths.xml`, `ui/screens/incidencia/`, `viewmodel/IncidenciaViewModel.kt` | `Funcionalidad cámara y galería integrada` |
| 13 | Notificaciones y vibración | A | `feature/alertas-nativas` | `util/NotificacionHelper.kt`, `util/VibracionHelper.kt`, `util/Retroalimentacion.kt`, `ui/screens/alertas/`, `viewmodel/AlertasViewModel.kt` | `feat(alertas): notificaciones locales, vibración y gestión de alertas` |
| 14 | Perfil, estados y animaciones | B | `feature/estado-animaciones` | `ui/screens/perfil/`, `viewmodel/PerfilViewModel.kt` | `Estado y animaciones` |
| 15 | Documentación | A y B | `docs/readme` | `README.md`, `docs/` | `docs: README con funcionalidades, arquitectura y pasos de ejecución` |

> Los mensajes que coinciden con las guías (`Navegación y menú generado`, `Formulario y resumen funcional`, `Estado y animaciones`, `Funcionalidad cámara y galería integrada`) facilitan que el docente relacione cada commit con su actividad.

**Importante:** el proyecto compila completo solo cuando están todas las piezas. Subir en el orden de la tabla y probar con **Run** después de cada Pull Request. Si una rama depende de otra (por ejemplo, las pantallas dependen de los componentes), crearla desde `main` actualizado.

## 3. Checklist por tarjeta (ejemplo: Cámara y galería — Guía 13)

- [ ] ViewModel con Uri en SavedStateHandle
- [ ] Componente ImagenInteligente (circular y rectangular)
- [ ] Galería con GetContent
- [ ] Cámara con TakePicture + FileProvider + permiso CAMERA
- [ ] Prueba en dispositivo físico (incluye cancelar sin que se caiga)
- [ ] Commit y Pull Request en GitHub
- [ ] Capturas de evidencia adjuntas en la tarjeta

## 4. Buenas prácticas para los commits

- Un commit por cambio lógico, con mensaje en imperativo y técnico: `feat(...)`, `fix(...)`, `test(...)`, `docs(...)`, `refactor(...)`.
- Evitar mensajes como "cambios", "avance" o "listo".
- Usar Issues de GitHub para errores encontrados y cerrarlos desde el commit: `fix(login): corrige validación de correo vacío (closes #3)`.
- **No modificar el repositorio después de la fecha de entrega** (la pauta asigna nota 1.0 si hay cambios posteriores).
