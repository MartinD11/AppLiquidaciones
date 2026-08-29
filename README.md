# RematePro - Sistema de Gestión y Liquidación Offline 🔨

**RematePro** es un sistema integral de gestión empresarial diseñado específicamente para digitalizar y automatizar la operatoria contable de casas de subastas y remates (desarrollado originalmente para Remates Avellaneda). 

El objetivo principal de esta aplicación es eliminar el error humano en el cálculo de comisiones post-evento, acelerar la administración de lotes y proporcionar una herramienta financiera robusta que funcione 100% offline.

## Funcionalidades Principales

*   **Liquidaciones Financieras Automatizadas:** Cruzamiento de datos post-remate para agrupar ventas por cliente, deducir automáticamente porcentajes de retención y generar estados de cuenta (`Pendiente` / `Pagada`).
*   **Gestión de Inventario y Lotes:** Seguimiento en tiempo real de productos, control de estados (`No Vendido`, `Vendido`) y asignación rápida de compradores y precios de bajada durante la subasta.
*   **CRM Integrado (Borrado Lógico):** Administración de compradores y vendedores. Implementa borrado lógico (`active = false`) para ocultar clientes inactivos en nuevas operaciones sin romper el historial contable de remates anteriores.
*   **Buscadores Dinámicos (AJAX):** Filtrado asíncrono en el frontend mediante `Fetch API` para buscar clientes históricos y productos sin necesidad de recargar el DOM.
*   **Generación de Catálogos Impresos:** Vistas limpias optimizadas con reglas CSS (`@media print`) para generar PDFs o imprimir directamente los catálogos en papel.

## Stack Tecnológico

*   **Backend:** Java 17, Spring Boot 3 (Spring Web, Spring Data JPA).
*   **Base de Datos:** H2 Database Engine (modo archivo persistente local).
*   **Frontend:** HTML5, CSS3, JavaScript Vanilla, Thymeleaf (Server-Side Rendering).
*   **Despliegue:** Maven, Launch4j (preparado para empaquetado como ejecutable `.exe` nativo de Windows).

## Arquitectura y Decisiones de Diseño

El proyecto está construido bajo una arquitectura monolítica clásica, priorizando la resiliencia, la velocidad y la independencia de internet.
*   **Offline-First:** Al utilizar una base de datos embebida, la aplicación no depende de servidores externos, garantizando que el negocio no se detenga por problemas de conectividad.
*   **Experiencia de Escritorio:** Aunque es una aplicación web, está diseñada para ser empaquetada y consumida localmente, simulando una experiencia de software de escritorio nativo para el usuario final.

## Instalación y Ejecución Local

Para clonar y probar este proyecto en un entorno de desarrollo, es necesario contar con Java 17 y Maven instalados.

```bash
# 1. Clonar el repositorio
git clone [https://github.com/tu-usuario/remate-pro.git](https://github.com/tu-usuario/remate-pro.git)

# 2. Navegar al directorio del proyecto
cd remate-pro

# 3. Compilar el proyecto
mvn clean install

# 4. Levantar el servidor
mvn spring-boot:run

👨‍💻 Autor
Martín Dentaro

[LinkedIn](https://www.linkedin.com/in/martin-dentaro-956034279/)
