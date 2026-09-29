# 📦 Rota - Sistema de Gestión e Inteligencia de Vencimientos

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat&logo=openjdk)
![Spring Boot 3.3.9](https://img.shields.io/badge/Spring_Boot-3.3.9-brightgreen?style=flat&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue?style=flat&logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**Rota** es una solución web para la gestión inteligente de inventario, enfocada en la **prevención de desperdicios, vencimientos y pérdidas económicas** en pequeños comercios.

El sistema permite registrar productos y lotes, controlar fechas de vencimiento, generar alertas, priorizar tareas mediante una ruta diaria y registrar acciones comerciales o mermas.

---

## 📑 Índice

- [🎯 Problema](#-problema)
- [💡 Solución](#-solución)
- [🎯 Objetivos](#-objetivos)
- [🚀 Características principales](#-características-principales)
- [🛠️ Stack tecnológico](#️-stack-tecnológico)
- [🏗️ Arquitectura](#️-arquitectura)
- [📦 Modelo funcional](#-modelo-funcional)
- [🚦 Sistema de alertas](#-sistema-de-alertas)
- [📋 Ruta diaria](#-ruta-diaria)
- [💰 Acciones comerciales](#-acciones-comerciales)
- [📉 Gestión de mermas](#-gestión-de-mermas)
- [🔐 Seguridad](#-seguridad)
- [📁 Estructura del proyecto](#-estructura-del-proyecto)
- [⚙️ Requisitos previos](#️-requisitos-previos)
- [🚀 Instalación](#-instalación)
- [🐳 Docker](#-docker)
- [🔑 Credenciales de prueba](#-credenciales-de-prueba)
- [📌 Endpoints](#-endpoints)
- [🧪 Ejemplos de uso](#-ejemplos-de-uso)
- [🌱 Datos iniciales](#-datos-iniciales)
- [📊 Flujo principal](#-flujo-principal)
- [🧪 Pruebas](#-pruebas)
- [📈 Indicadores](#-indicadores)
- [🔮 Próximas mejoras](#-próximas-mejoras)
- [📌 Estado del proyecto](#-estado-del-proyecto)
- [👥 Equipo](#-equipo)
- [📄 Licencia](#-licencia)

---

## 🎯 Problema

En pequeños comercios, el control de productos próximos a vencer suele realizarse de manera manual.

La información sobre los lotes, cantidades, ubicaciones y fechas de vencimiento puede encontrarse dispersa entre etiquetas, documentos de proveedores o controles manuales.

Esto puede generar:

- Productos que llegan a su fecha de vencimiento sin ser detectados.
- Pérdidas económicas por productos que deben descartarse.
- Falta de trazabilidad sobre las mermas.
- Descuentos aplicados demasiado tarde.
- Tiempo perdido realizando controles manuales.
- Dificultad para conocer qué productos requieren atención prioritaria.

---

## 💡 Solución

**Rota** centraliza la información de productos y lotes y utiliza las fechas de vencimiento para ayudar a priorizar las tareas de control.

El sistema permite:

1. Registrar productos y categorías.
2. Registrar lotes con sus fechas de vencimiento.
3. Asociar cantidades y ubicaciones.
4. Configurar umbrales de alerta según la categoría.
5. Identificar productos próximos a vencer.
6. Generar una ruta diaria de control.
7. Aplicar acciones comerciales.
8. Registrar productos descartados.
9. Calcular el impacto económico de las mermas.

La idea de Rota es **anticiparse al vencimiento para reducir pérdidas y mejorar la gestión del inventario**.

---

## 🎯 Objetivos

### Objetivo general

Desarrollar una herramienta que facilite la gestión de productos con vencimiento y permita a los comercios tomar acciones antes de que los productos se conviertan en una pérdida económica.

### Objetivos específicos

- Centralizar la información de inventario.
- Mejorar la visibilidad de las fechas de vencimiento.
- Priorizar productos según su nivel de riesgo.
- Facilitar el control diario del inventario.
- Permitir la aplicación de descuentos.
- Registrar y analizar las mermas.
- Obtener información sobre el impacto económico de los productos vencidos.
- Reducir tareas manuales relacionadas con el control de vencimientos.

---

## 🚀 Características principales

### 🔐 Seguridad y autenticación

Sistema de autenticación basado en **Spring Security + JWT**.

Incluye diferentes roles de usuario:

- `ROLE_DUENO`
- `ROLE_REPOSITOR`

Cada rol posee permisos diferentes según las funcionalidades que necesita utilizar.

### 🏷️ Gestión de categorías

Permite crear y administrar categorías de productos.

Cada categoría puede contar con parámetros específicos para determinar cuándo un producto debe comenzar a generar alertas.

También permite configurar la frecuencia de notificación:

- `DIARIO`
- `CADA_2_DIAS`
- `SEMANAL`

### 📦 Gestión de productos

Permite registrar y administrar productos del comercio.

Cada producto puede estar asociado a:

- Categoría.
- Proveedor.
- Precio.
- Margen.
- Lotes.
- Información de stock.

### 📦 Gestión de lotes

Los productos pueden dividirse en diferentes lotes.

Cada lote permite realizar seguimiento de:

- Fecha de vencimiento.
- Cantidad.
- Ubicación.
- Precio.
- Estado.
- Producto asociado.

Esto permite trabajar con una lógica basada en lotes y no únicamente en el stock total del producto.

### 📍 Gestión de ubicaciones

Los lotes pueden encontrarse en diferentes ubicaciones físicas.

Ejemplos:

- `GONDOLA`
- `DEPOSITO`

Esto permite conocer dónde se encuentra físicamente un producto que necesita ser controlado.

### 📋 Ruta diaria de control

Rota genera una lista priorizada de productos que requieren atención.

La ruta utiliza información como:

- Fecha de vencimiento.
- Días restantes.
- Nivel de riesgo.
- Categoría.
- Ubicación.
- Estado del lote.

El objetivo es que el repositor pueda saber **qué productos revisar primero**.

### 🏷️ Acciones comerciales

Cuando un producto se encuentra próximo a vencer, el sistema permite registrar acciones destinadas a acelerar su rotación.

Entre ellas:

- Aplicación de descuentos.
- Modificación del precio.
- Seguimiento del precio original.
- Seguimiento del precio final.
- Control del margen mínimo.

### 📉 Gestión de mermas

Cuando un producto no puede ser comercializado, se puede registrar como merma.

La información registrada puede incluir:

- Producto.
- Lote.
- Cantidad.
- Motivo.
- Fecha.
- Valor económico.

Esto permite obtener trazabilidad de las pérdidas y analizar su impacto económico.

---

## 🛠️ Stack tecnológico

| Tecnología | Uso |
| --- | --- |
| Java 17 | Lenguaje de programación |
| Spring Boot 3.3.9 | Framework principal |
| Spring Data JPA | Persistencia |
| Hibernate 6 | ORM |
| Spring Security | Seguridad |
| JWT | Autenticación |
| PostgreSQL | Base de datos |
| Maven | Gestión y construcción |
| Lombok | Reducción de código repetitivo |
| Docker | Contenedores |
| Postman | Pruebas de API |
| cURL | Pruebas de endpoints |

---

## 🏗️ Arquitectura

Rota utiliza una arquitectura basada en capas.

```text
┌─────────────────────────┐
│       Controller        │
│       REST API          │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│        Service          │
│     Lógica de negocio   │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│       Repository        │
│     Acceso a datos      │
└────────────┬────────────┘
             │
             ▼
┌─────────────────────────┐
│       PostgreSQL        │
│        Database         │
└─────────────────────────┘
```

### Controller

Recibe las solicitudes HTTP y expone los endpoints REST de la aplicación.

### Service

Contiene la lógica de negocio y las reglas principales del sistema.

### Repository

Gestiona el acceso y persistencia de los datos utilizando Spring Data JPA.

### Database

PostgreSQL almacena la información persistente de la aplicación.

---

## 📦 Modelo funcional

Las principales entidades del sistema son:

```text
Usuario
│
├── Rol
└── Permisos

Categoría
│
└── Producto
    │
    └── Lote
        │
        ├── Vencimiento
        ├── Cantidad
        └── Ubicación
             │
             ├── Alertas
             ├── Acciones comerciales
             └── Mermas
```

### Principales entidades

- `Usuario`
- `Rol`
- `Categoría`
- `Producto`
- `Proveedor`
- `Lote`
- `Ubicación`
- `MovimientoStock`
- `Alerta`
- `AcciónComercial`
- `Merma`

---

## 🚦 Sistema de alertas

Rota utiliza un sistema de semáforo para representar el estado de los productos según su proximidad a la fecha de vencimiento.

| Estado | Descripción |
| --- | --- |
| 🟢 **OK** | El producto se encuentra dentro de un período normal de comercialización. |
| 🟡 **ALERTA** | El producto se encuentra próximo a vencer y requiere seguimiento. |
| 🔴 **CRÍTICO** | El producto requiere atención prioritaria. |

Los umbrales pueden configurarse según la categoría.

Esto permite que diferentes tipos de productos tengan reglas de alerta diferentes.

---

## 📋 Ruta diaria

La **Ruta Diaria de Control** organiza los productos que deben ser revisados durante la jornada.

El sistema analiza los lotes y determina cuáles requieren mayor atención.

### Información utilizada

- Fecha de vencimiento.
- Días restantes.
- Categoría.
- Ubicación.
- Cantidad.
- Estado del lote.
- Nivel de riesgo.

### Objetivo

Facilitar el trabajo del repositor y evitar que los productos próximos a vencer sean detectados demasiado tarde.

---

## 💰 Acciones comerciales

Rota permite registrar acciones comerciales para productos próximos a vencer.

Una acción comercial puede contemplar:

- Producto.
- Lote.
- Precio original.
- Porcentaje de descuento.
- Precio final.
- Margen mínimo.
- Fecha de aplicación.
- Estado de la acción.

El objetivo es favorecer la rotación del producto antes de que se produzca una merma.

---

## 📉 Gestión de mermas

Cuando un producto debe ser descartado, Rota permite registrar la merma.

### Información registrada

- Producto.
- Lote.
- Cantidad.
- Motivo.
- Fecha.
- Valor unitario.
- Pérdida económica.

### Impacto económico

El sistema permite calcular el valor económico asociado a la merma.

De esta manera, el comercio puede obtener información sobre cuánto dinero representa el producto que tuvo que ser descartado.

---

## 🔐 Seguridad

La aplicación utiliza:

- **Spring Security**
- **JWT (JSON Web Tokens)**
- Control de acceso basado en roles.

### Roles

#### `ROLE_DUENO`

Usuario encargado de las funciones administrativas y de gestión del comercio.

#### `ROLE_REPOSITOR`

Usuario encargado principalmente de las tareas operativas relacionadas con el inventario y el control de productos.

Las solicitudes protegidas utilizan el siguiente header:

```text
Authorization: Bearer <TU_TOKEN_JWT>
```

---

## 📁 Estructura del proyecto

```text
rota/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## ⚙️ Requisitos previos

Antes de ejecutar el proyecto es necesario contar con:

- **JDK 17** o superior.
- **Maven 3.8+** o utilizar el Maven Wrapper incluido.
- **Docker Desktop**.
- **Postman** o **cURL** para realizar pruebas.

---

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/tu-usuario/rota.git
cd rota
```

### 2. Levantar PostgreSQL

Se puede utilizar Docker para levantar una instancia de PostgreSQL.

```bash
docker run --name rota-postgres \
  -e POSTGRES_DB=rotabd \
  -e POSTGRES_USER=rota_user \
  -e POSTGRES_PASSWORD=rota_password \
  -p 5432:5432 \
  -d postgres:15
```

### 3. Configurar la aplicación

La configuración principal se encuentra en:

```text
src/main/resources/application.properties
```

Ejemplo:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rotabd
spring.datasource.username=rota_user
spring.datasource.password=rota_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
```

### 4. Compilar el proyecto

```bash
./mvnw clean compile
```

### 5. Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

La aplicación estará disponible en:

```text
http://localhost:8080
```

---

## 🐳 Docker

### Crear el contenedor PostgreSQL

```bash
docker run --name rota-postgres \
  -e POSTGRES_DB=rotabd \
  -e POSTGRES_USER=rota_user \
  -e POSTGRES_PASSWORD=rota_password \
  -p 5432:5432 \
  -d postgres:15
```

### Verificar el contenedor

```bash
docker ps
```

### Detener el contenedor

```bash
docker stop rota-postgres
```

### Volver a iniciar el contenedor

```bash
docker start rota-postgres
```

---

## 🔑 Credenciales de prueba

Al iniciar la aplicación con una base de datos vacía, `DataInitializer` puede cargar usuarios de prueba.

| Rol | Email | Contraseña |
| --- | --- | --- |
| **Dueño / Administrador** | `dueno@comercio.com` | `123456` |
| **Repositor** | `repositor@comercio.com` | `123456` |

> Estas credenciales son únicamente para ambientes de desarrollo y pruebas.

---

## 📌 Endpoints

### 🔐 Autenticación

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/auth/login` | Iniciar sesión y obtener JWT. |

### 🏷️ Categorías

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `GET` | `/api/categorias` | Obtener todas las categorías. |
| `POST` | `/api/categorias` | Crear una nueva categoría. |

### 📦 Productos

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/productos` | Registrar un nuevo producto. |

### 📦 Lotes

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/lotes` | Registrar un nuevo lote. |
| `PATCH` | `/api/lotes/{id}/descuento` | Aplicar un descuento a un lote. |

### 📋 Ruta diaria

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `GET` | `/api/ruta-diaria` | Obtener la ruta diaria priorizada. |

### 📉 Mermas

| Método | Endpoint | Descripción |
| --- | --- | --- |
| `POST` | `/api/mermas` | Registrar una merma y calcular su impacto económico. |

---

## 🧪 Ejemplos de uso

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dueno@comercio.com","password":"123456"}'
```

La respuesta devolverá un token JWT que deberá utilizarse para acceder a los endpoints protegidos.

### 2. Consultar la ruta diaria

```bash
curl -X GET http://localhost:8080/api/ruta-diaria \
  -H "Authorization: Bearer <TU_TOKEN_JWT>"
```

### 3. Consultar categorías

```bash
curl -X GET http://localhost:8080/api/categorias \
  -H "Authorization: Bearer <TU_TOKEN_JWT>"
```

---

## 🌱 Datos iniciales

El proyecto cuenta con un `DataInitializer` para facilitar el desarrollo y las pruebas.

Cuando corresponde, puede cargar automáticamente información inicial como:

- Usuarios.
- Roles.
- Categorías.
- Productos.
- Proveedores.
- Lotes.
- Ubicaciones.

Esto permite comenzar a probar el sistema sin necesidad de cargar todos los datos manualmente.

---

## 📊 Flujo principal

```text
┌──────────────────────────┐
│   Registrar Producto     │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│      Registrar Lote      │
│                          │
│ - Fecha de vencimiento   │
│ - Cantidad               │
│ - Ubicación              │
└────────────┬─────────────┘
             │
             ▼
┌──────────────────────────┐
│ Analizar vencimiento     │
└────────────┬─────────────┘
             │
             ▼
       ┌─────┴─────┐
       │           │
       ▼           ▼
   🟢 OK      🟡 / 🔴 Riesgo
                   │
                   ▼
          ┌────────────────┐
          │ Ruta diaria    │
          │ / Acción       │
          │ comercial      │
          └───────┬────────┘
                  │
                  ▼
          ┌──────────────────┐
          │ ¿Se comercializa?│
          └───────┬──────────┘
                  │
             ┌────┴────┐
             │         │
             ▼         ▼
            Sí         No
             │         │
             ▼         ▼
       Venta /      Registrar
       descuento      merma
                         │
                         ▼
                  Pérdida económica
```

---

## 🔄 Flujo de control de vencimientos

```text
Producto
   │
   ▼
Lote
   │
   ▼
Fecha de vencimiento
   │
   ▼
Cálculo de días restantes
   │
   ▼
Comparación con umbral
   │
   ├───────────────┬────────────────┐
   ▼               ▼                ▼
  OK            ALERTA           CRÍTICO
   │               │                │
   │               ▼                ▼
   │          Seguimiento      Acción inmediata
   │               │                │
   └───────────────┴────────────────┘
                   │
                   ▼
                Resultado
                   │
          ┌────────┴────────┐
          ▼                 ▼
       Rotación           Merma
          │                 │
          ▼                 ▼
        Venta           Pérdida $
```

---

## 🧪 Pruebas

La API puede probarse utilizando:

- Postman.
- cURL.
- Insomnia.

Para los endpoints protegidos se debe incluir el JWT obtenido mediante el endpoint de login.

```text
Authorization: Bearer <TU_TOKEN_JWT>
```

---

## 📈 Indicadores

La información registrada por Rota puede utilizarse para generar indicadores como:

- Cantidad de productos próximos a vencer.
- Cantidad de lotes en estado crítico.
- Cantidad de mermas.
- Valor económico de las mermas.
- Productos con mayor cantidad de alertas.
- Categorías con mayor cantidad de vencimientos.
- Descuentos aplicados.
- Productos recuperados mediante acciones comerciales.

---

## 🔮 Próximas mejoras

Entre las funcionalidades que pueden incorporarse al proyecto se encuentran:

- Dashboard de indicadores.
- Mejoras en la ruta diaria.
- Mayor personalización de alertas.
- Análisis histórico de mermas.
- Reportes de pérdidas económicas.
- Mejoras en la experiencia de usuario.
- Integración con código de barras.
- Notificaciones automáticas.
- Mayor cantidad de acciones comerciales.
- Mejoras en los permisos y roles.

---

## 📌 Estado del proyecto

El proyecto se encuentra en desarrollo como parte del proyecto académico:

**Seminario de Integración Profesional - UADE**

El desarrollo se realiza mediante historias de usuario y entregas incrementales.

### Funcionalidades

- [x] Configuración inicial del proyecto.
- [x] Configuración de Spring Boot.
- [x] Configuración de PostgreSQL.
- [x] Gestión de categorías.
- [x] Gestión de productos.
- [x] Gestión de lotes.
- [x] Control de fechas de vencimiento.
- [x] Umbrales de alerta.
- [x] Sistema de roles.
- [x] Autenticación.
- [x] Gestión de inventario.
- [x] Ruta diaria.
- [x] Acciones comerciales.
- [x] Registro de mermas.
- [ ] Dashboard de indicadores.
- [ ] Reportes avanzados.
- [ ] Mejoras de notificaciones.

---

## 👥 Equipo

Proyecto desarrollado para:

**Seminario de Integración Profesional - UADE**

### Integrantes

| Integrante | Rol |
| --- | --- |
| **Santiago Weinbinder** | Project Manager |
| **Joaquín Fernandes** | Product Owner / Analista de Negocios |
| **Mateo Galluzzo** | Scrum Master / Documentación |
| **Matías Fernández Arana** | UX/UI |
| **Paolo Maffei** | Usuario |
| **Matías Lubiato** | QA / Testing |
| **Julián De Vasconcelos** | Front-end / Arquitecto Cloud |
| **Camila Di Laudo** | Back-end / Arquitecta de Datos |

---

## 📚 Conceptos principales

Rota se basa en conceptos relacionados con:

- Gestión de inventario.
- Control de vencimientos.
- Gestión de lotes.
- FEFO (First Expired, First Out).
- Alertas de vencimiento.
- Acciones comerciales.
- Gestión de mermas.
- Trazabilidad.
- Análisis de pérdidas económicas.
- Automatización de tareas.
- Control de stock.

---

## 🔧 Comandos útiles

### Compilar

```bash
./mvnw clean compile
```

### Ejecutar tests

```bash
./mvnw test
```

### Ejecutar la aplicación

```bash
./mvnw spring-boot:run
```

### Limpiar el proyecto

```bash
./mvnw clean
```

### Construir el proyecto

```bash
./mvnw clean package
```

---

## 🌐 URL de la aplicación

Una vez iniciada la aplicación:

```text
http://localhost:8080
```

---

## 📄 Licencia

Este proyecto está bajo la **Licencia MIT**.

Para más información, consultar el archivo:

```text
LICENSE
```

---

## 📬 Contacto

Para consultas relacionadas con el proyecto, contactar al equipo de desarrollo.

---

# 📦 Rota

**Sistema de Gestión e Inteligencia de Vencimientos**

> Una solución orientada a ayudar a los comercios a detectar productos próximos a vencer, priorizar acciones y reducir pérdidas económicas asociadas a las mermas.
