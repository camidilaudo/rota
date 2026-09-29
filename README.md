# 📦 Rota - Sistema de Gestión e Inteligencia de Vencimientos

![Java 17](https://img.shields.io/badge/Java-17-orange?style=flat&logo=openjdk)
![Spring Boot 3.3.9](https://img.shields.io/badge/Spring_Boot-3.3.9-brightgreen?style=flat&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue?style=flat&logo=postgresql)
![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green.svg)

**Rota** es una solución web diseñada para optimizar el control de inventario en comercios de retail y supermercados, enfocada en la **prevención de desperdicio de alimentos y pérdidas económicas** mediante el monitoreo dinámico del ciclo de vida de los productos.

---

## 🎯 Problema & Solución

* **El Problema:** La falta de visibilidad sobre las fechas de vencimiento genera mermas innecesarias, pérdidas de capital y tareas manuales ineficientes para el personal de reposición.
* **La Solución:** Rota prioriza automáticamente los lotes según su rotación y urgencia, generando una **Ruta Diaria de Control** con semáforos de riesgo (Crítico, Alerta, OK) para aplicar ofertas tempranas o registrar mermas a tiempo.

---

## 🚀 Características Principales

* 🔒 **Seguridad & Autenticación:** Control de acceso basado en roles (`ROLE_DUENO` y `ROLE_REPOSITOR`) implementado con Spring Security y JWT (JSON Web Tokens).
* 🏷️ **Categorización con Umbrales Dinámicos:** Gestión de categorías con reglas específicas de alertas por días y frecuencias de notificación (`DIARIO`, `CADA_2_DIAS`, `SEMANAL`).
* 📦 **Gestión Inteligente de Lotes:** Seguimiento individual por fecha de vencimiento, cantidad y ubicación física (`GONDOLA`, `DEPOSITO`).
* 📋 **Ruta Diaria Automatizada:** Algoritmo de priorización que arma el plan de trabajo diario para los repositores.
* 🏷️ **Estrategias de Descuento & Mermas:** Modificación de precios para liquidación rápida y trazabilidad de pérdidas con cálculo automático de impacto financiero.
* 🌱 **Semilla de Datos (DataInitializer):** Carga automática de usuarios, categorías, productos y lotes de prueba al iniciar la aplicación en entornos de desarrollo.

---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 17
* **Framework:** Spring Boot 3.3.9
  * Spring Data JPA (Hibernate 6)
  * Spring Security + JWT
  * Spring Validation
* **Base de Datos:** PostgreSQL
* **Herramientas de Construcción & Productividad:**
  * Apache Maven
  * Project Lombok
  * Docker / Docker Compose

---

## ⚙️ Requisitos Previos

Asegurate de tener instalado en tu sistema:

* **JDK 17** o superior
* **Maven 3.8+** (o usar `./mvnw` incluido)
* **Docker Desktop** (para la base de datos PostgreSQL)
* **Postman** o **cURL** (para probar la API)

---

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone [https://github.com/tu-usuario/rota.git](https://github.com/tu-usuario/rota.git)
cd rota/backendBash
git clone https://github.com/tu-usuario/rota.git
cd rota/backend
2. Levantar la Base de Datos con Docker Compose (Opcional)
Si contás con Docker instalado, podés iniciar PostgreSQL rápidamente:
Bash
docker-compose up -d
3. Compilar y Ejecutar la Aplicación
Bash
# Compilar el proyecto
./mvnw clean compile

# Ejecutar la aplicación
./mvnw spring-boot:run
La aplicación estará escuchando en http://localhost:8080.
🌱 Datos Semilla (Carga Inicial Automática)
Al iniciar la aplicación por primera vez, la clase DataInitializer verifica si existen registros en la base de datos. Si está vacía, genera automáticamente los siguientes datos de prueba:
Credenciales de Usuarios Preconfigurados:
Rol	Email	Contraseña
Dueño (ROLE_DUENO)	dueno@comercio.com	123456
Repositor (ROLE_REPOSITOR)	repositor@comercio.com	123456
Datos de Negocio Cargados:
Categorías: Lácteos (Umbral: 7 días), Panadería (Umbral: 3 días), Fiambres (Umbral: 10 días).
Productos: Leche Entera 1L, Yogur Frutilla 500g, Pan Lactal 500g, Queso Cremoso 1kg.
Lotes: Lotes asignados a Gondola/Depósito con diferentes fechas de vencimiento (estados Crítico, Alerta y Normal).
📡 Endpoints de la API REST
🔑 Autenticación
Método	Endpoint	Descripción	Requiere Auth
POST	/api/auth/login	Autenticarse y obtener Bearer JWT Token	No
📂 Categorías
Método	Endpoint	Descripción	Requiere Auth
GET	/api/categorias	Listar todas las categorías	Sí
POST	/api/categorias	Crear una nueva categoría con umbral	Sí
📦 Productos
Método	Endpoint	Descripción	Requiere Auth
GET	/api/productos	Listar todos los productos con categorías	Sí
POST	/api/productos	Registrar un producto en el sistema	Sí
🏷️ Lotes y Vencimientos
Método	Endpoint	Descripción	Requiere Auth
POST	/api/lotes	Registrar ingreso de un nuevo lote	Sí
PATCH	/api/lotes/{id}/descuento	Aplicar un porcentaje de descuento a un lote	Sí
📋 Operaciones Diarias
Método	Endpoint	Descripción	Requiere Auth
GET	/api/ruta-diaria	Consultar productos próximos a vencer según umbrales	Sí
POST	/api/mermas	Registrar pérdida o descarte de producto	Sí
🧪 Pruebas Rápidas con cURL
1. Iniciar sesión para obtener el Token:
Bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"dueno@comercio.com","password":"123456"}'
2. Consultar la Ruta Diaria usando el JWT obtenido:
Bash
curl -X GET http://localhost:8080/api/ruta-diaria \
  -H "Authorization: Bearer <TU_TOKEN_JWT>"
📂 Estructura del Proyecto
Plaintext
src/main/java/com/rota/
├── RotaApplication.java         # Clase Principal de Spring Boot
├── config/                      # Configuración de Seguridad y DataInitializer
│   ├── DataInitializer.java
│   └── SecurityConfig.java
├── controller/                  # Controladores REST API
│   ├── AuthController.java
│   ├── CategoriaController.java
│   ├── LoteController.java
│   ├── MermaController.java
│   ├── ProductoController.java
│   └── RutaDiariaController.java
├── entity/                      # Entidades JPA (Modelo de Datos)
│   ├── Categoria.java
│   ├── Lote.java
│   ├── Merma.java
│   ├── ModoNotificacion.java
│   ├── Producto.java
│   ├── Rol.java
│   ├── Ubicacion.java
│   └── Usuario.java
├── repository/                  # Interfaces Spring Data JPA
└── service/                     # Lógica de Negocio y Servicios
📄 Licencia
Este proyecto está distribuido bajo la licencia MIT. Consulta el archivo LICENSE para más detalles.
