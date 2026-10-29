# Informe del proyecto semestral — SiloMonitor

**Asignatura:** DSY1105 Desarrollo de Aplicaciones Móviles · **Sección:** 003D · **Equipo:** N° _(por confirmar)_ · **Docente:** Fernando Sepúlveda Campos
**Integrantes:** Tomás González Llanos (@tomy200) · Antonio Espinoza (@antonioespinoza10)

## 1. Nombre del proyecto

**SiloMonitor** — aplicación móvil para el monitoreo de alimento en silos (repositorio `SiloMonitor`).

## 2. Contexto

Empresa de gran tamaño del sector agroalimentario chileno (producción y comercialización de pollo, pavo, cecinas, congelados y huevos), con granjas avícolas y ganaderas. Actualmente los operarios siguen manualmente los niveles de alimento de los silos y los movimientos de carga, descarga y consumo; los supervisores controlan el abastecimiento y las jefaturas planifican las reposiciones. La información no está centralizada en una herramienta móvil que se pueda consultar desde terreno.

## 3. Problema

La falta de una herramienta móvil dificulta consultar rápidamente existencias, consumos y alertas, lo que puede retrasar las reposiciones y aumentar el riesgo de desabastecimiento de alimento en las granjas.

## 4. Objetivo

Disponer de una aplicación Android que permita consultar el nivel de alimento por granja y silo, visualizar su porcentaje de capacidad, identificar niveles críticos, recibir alertas, revisar históricos de consumo y apoyar la planificación de reposiciones, funcionando también con conectividad limitada.

## 5. Alcance del MVP (Evaluación Parcial 2)

**Incluye**

- 3 granjas con 4 silos cada una (12 silos) con datos ficticios.
- Inicio de sesión y registro de cuentas nuevas, eligiendo uno de tres perfiles: operario, supervisor o jefatura.
- Resumen general, listado de silos por granja con filtros y detalle de cada silo.
- Historial de niveles de los últimos 14 días, consumo diario estimado y días restantes.
- Registro de cargas, descargas y consumos.
- Reporte de incidencias con foto (cámara o galería).
- Alertas de nivel (advertencia y crítico) con notificaciones locales.
- Persistencia local con Room y DataStore; API simulada del sistema de monitoreo.

**No incluye (futuras versiones)**

- Conexión con el sistema productivo real de monitoreo (se usa una API simulada).
- Administración de granjas, silos y usuarios desde la app.
- Mapa con la ubicación de las granjas.
- Sincronización automática en segundo plano.

## 6. Usuarios y responsabilidades

| Perfil | Responsabilidades | Permisos en la app |
|---|---|---|
| Operario | Consultar niveles y registrar movimientos | Ver todo · registrar movimientos · reportar incidencias |
| Supervisor | Revisar alertas, controlar existencias y coordinar reposiciones | Todo lo anterior · marcar alertas como atendidas |
| Jefatura | Consultar información resumida e históricos para planificar | Solo consulta |

## 7. Requerimientos funcionales

| Código | Requerimiento |
|---|---|
| RF01 | El usuario inicia sesión con correo y contraseña, o crea una cuenta eligiendo su cargo (operario, supervisor o jefatura). |
| RF02 | La app muestra un resumen con la cantidad de silos por estado y el alimento disponible total. |
| RF03 | La app lista los silos por granja, con porcentaje, estado y días restantes, y permite filtrar. |
| RF04 | La app muestra el detalle de un silo con su historial de niveles, movimientos e incidencias. |
| RF05 | El operario o supervisor registra cargas, descargas y consumos. |
| RF06 | El operario o supervisor reporta incidencias con foto desde la cámara o la galería. |
| RF07 | La app genera alertas y notificaciones cuando un silo baja a advertencia o crítico. |
| RF08 | El supervisor marca alertas como atendidas. |
| RF09 | La app consulta la API de monitoreo y guarda los datos localmente para usarlos sin conexión. |
| RF10 | El usuario configura sus preferencias (tema, notificaciones, vibración) y su foto de perfil. |

## 8. Requerimientos no funcionales

- Android 8.0 (API 26) o superior; interfaz adaptable a celulares y tablets.
- Funcionamiento con conectividad limitada (datos recientes almacenados en el dispositivo).
- Contraseñas almacenadas como hash; permisos solicitados solo cuando se necesitan.
- Interfaz clara con indicadores gráficos y colores por estado acompañados de íconos (accesibilidad).

## 9. Reglas de negocio

1. Normal > 30 %, Advertencia entre 20 % y 30 %, Crítico < 20 % de la capacidad.
2. Una carga no puede superar la capacidad libre; una descarga o consumo no puede superar lo disponible.
3. Toda descarga debe indicar su motivo.
4. Las incidencias de prioridad alta requieren foto.
5. Al empeorar el estado de un silo se genera una alerta y una notificación; al volver a normal se cierran sus alertas.
6. Días restantes = disponible ÷ consumo promedio de los últimos 7 días.
7. Cada usuario crea su cuenta eligiendo su cargo, que define sus permisos; no se permiten correos repetidos. Además existen tres usuarios de prueba para la demostración.
