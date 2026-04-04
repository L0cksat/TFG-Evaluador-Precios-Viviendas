# 🏡 Evaluador de Precios de Viviendas (Evaluty)

## 📖 Descripción del Proyecto
[cite_start]Este proyecto es una aplicación web enfocada en democratizar el acceso a las valoraciones de inmuebles de forma totalmente gratuita y sin pasarelas de pago[cite: 120]. 

[cite_start]El sistema permite a los usuarios obtener una estimación rápida y fiable del valor de una vivienda mediante la comparación de inmuebles cercanos (entre 3 y 10 propiedades)[cite: 121]. [cite_start]Además, ofrece la posibilidad de calcular el valor mínimo fiscal permitido por el gobierno a través de la referencia catastral, previniendo posibles sanciones de Hacienda[cite: 121, 125].

---

## ✨ Características Principales
* [cite_start]**Modo Básico:** Estimación de precio introduciendo una dirección, metros cuadrados y número de habitaciones[cite: 472].
* [cite_start]**Modo Pro:** Valoración fiscal introduciendo la Referencia Catastral[cite: 473].
* **Mapa Interactivo (Leaflet):** Selección visual de la ubicación con un mapa de OpenStreetMap. [cite_start]Incluye geocodificación inversa (*Reverse Geocoding*) para autocompletar formularios automáticamente[cite: 380, 384].
* [cite_start]**Generación de Informes:** Exportación de los resultados a un documento PDF descargable que incluye advertencias legales y fiscales[cite: 125].
* [cite_start]**Seguridad:** Autenticación robusta (Stateless) mediante tokens JWT y cifrado de contraseñas con BCrypt[cite: 440, 444].

---

## 🛠️ Stack Tecnológico
[cite_start]El proyecto sigue una arquitectura modular basada en microservicios[cite: 122], utilizando las siguientes tecnologías:

### 💻 Frontend
* [cite_start]**Angular:** Framework principal para la SPA (Single Page Application)[cite: 232].
* [cite_start]**Leaflet & OpenStreetMap:** Para la renderización de mapas interactivos y selección de coordenadas[cite: 216].

### ⚙️ Backend
* [cite_start]**Spring Boot (Java):** Orquestador central, API REST y lógica de negocio[cite: 225].
* [cite_start]**Spring Security & JWT:** Para la autorización y gestión de sesiones de los usuarios[cite: 441].

### 🐍 Microservicio de Datos (Scraping & Cálculo)
* [cite_start]**Python:** Para el procesamiento de datos y la lógica predictiva[cite: 234].
* [cite_start]**Selenium Webdriver:** Para automatizar la extracción de datos (web scraping) en portales inmobiliarios dinámicos[cite: 393].

### Base de Datos
* [cite_start]**MongoDB:** Base de datos NoSQL para almacenar perfiles de usuario, historial de valoraciones y datos semiestructurados provenientes del scraping[cite: 242, 243].

---

## ⚙️ Arquitectura del Sistema
1.  El **Frontend (Angular)** recoge los datos del usuario y se comunica con el Backend.
2.  El **Backend (Spring Boot)** valida la petición (JWT) y hace una llamada HTTP interna al microservicio.
3.  [cite_start]El **Microservicio (Python)** realiza el scraping, limpia los datos atípicos con un filtro matemático (descartando variaciones >30% en superficie/habitaciones) y devuelve un JSON estructurado[cite: 418, 430].
4.  [cite_start]La información se guarda en **MongoDB** y se devuelve al Frontend para su visualización[cite: 243].

---

## 👥 Autores
[cite_start]Proyecto Intermodular del ciclo superior DAW (Desarrollo de Aplicaciones Web) - UNIR FP[cite: 115, 116]:
* [cite_start]**Stephen Nicholas Jones De Giorgi** - *Microservicio Python, Web Scraping, Algoritmo y Generación PDF*[cite: 303].
* [cite_start]**Juan Montiel Fernández** - *Backend (Spring Boot), API REST y MongoDB*[cite: 303].
* [cite_start]**Kyle Lamm** - *Frontend (Angular), Diseño UI/UX e Integración de Mapas (Leaflet)*[cite: 303].
