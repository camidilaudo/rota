📦 Rota - Sistema de Gestión de Vencimientos y Mermas
Rota es una plataforma web integral diseñada para la gestión inteligente de stock, control de vencimientos de productos perecederos y optimización de mermas en comercios minoristas y supermercados.

🛠️️ Tecnologías Utilizadas
Backend: Java 17, Spring Boot 3.3.x, Spring Data JPA, Spring Security + JWT, Lombok.
Base de Datos: PostgreSQL 15+.
Herramientas de Construcción & Contenedores: Maven, Docker, Docker Compose.
Documentación & Pruebas API: Postman, cURL.
🏗️ Arquitectura e Infraestructura
Plaintext
       ┌────────────────────────┐
       │     Cliente Web /      │
       │    Postman / Mobile    │
       └───────────┬────────────┘
                   │  HTTP / REST + Bearer JWT
                   ▼
       ┌────────────────────────┐
       │   Spring Boot Backend  │
       │     (Port 8080)        │
       └───────────┬────────────┘
                   │  JDBC / JPA
                   ▼
       ┌────────────────────────┐
       │  PostgreSQL Database   │
       │     (Port 5432)        │
       └────────────────────────┘
🚀 Requisitos Previos
Asegurate de contar con el siguiente software instalado en tu entorno local:
JDK 17 o superior.
Apache Maven 3.8+ (o el ejecutable ./mvnw incluido).
Docker y Docker Compose (opcional para ejecución en contenedores).
Git.
⚙️ Configuración del Entorno (application.properties)
El archivo se encuentra ubicado en src/main/resources/application.properties:
Properties
# Servidor
server.port=8080

# Conexión a PostgreSQL (Docker / Local)
spring.datasource.url=jdbc:postgresql://localhost:5432/${DB_NAME:rotabd}
spring.datasource.username=${DB_USER:rota_user}
spring.datasource.password=${DB_PASSWORD:rota_password}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false
🚦 Guía de Instalación y Ejecución
1. Clonar el repositorio
Bash
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
