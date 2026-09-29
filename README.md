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
cd rota/backend
