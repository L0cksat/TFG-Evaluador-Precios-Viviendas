# 🏡 Evaluty – Real Estate Price Evaluator

<p align="center">
  Plataforma web para estimar el valor de viviendas de forma rápida, gratuita y fiable.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/status-Complete-yellow" />
  <img src="https://img.shields.io/badge/frontend-Angular-red" />
  <img src="https://img.shields.io/badge/backend-SpringBoot-green" />
  <img src="https://img.shields.io/badge/microservice-Python-blue" />
  <img src="https://img.shields.io/badge/database-MongoDB-brightgreen" />
</p>

---

## 📌 About The Project

**Evaluty** es una aplicación web diseñada para **democratizar el acceso a la valoración inmobiliaria**, permitiendo a cualquier usuario obtener una estimación del precio de una vivienda sin costes ni intermediarios.

El sistema combina:
- Comparación de propiedades cercanas
- Datos obtenidos mediante scraping
- Algoritmos de filtrado y estimación

Además, incluye herramientas para calcular el **valor fiscal mínimo**, ayudando a evitar posibles sanciones tributarias.

---

## ✨ Key Features

- 🏠 **Estimación de precios en tiempo real**
- 📍 **Mapa interactivo (Leaflet + OpenStreetMap)**
- 🔎 **Geocodificación inversa automática**
- 📊 **Comparación de inmuebles similares (3–10 referencias)**
- 📄 **Generación de informes PDF**
- 🔐 **Autenticación segura con JWT**
- ⚖️ **Cálculo del valor fiscal (Referencia Catastral)**

---

## 🧱 Tech Stack

### 💻 Frontend
- Angular (SPA)
- Leaflet + OpenStreetMap

### ⚙️ Backend
- Spring Boot (API REST)
- Spring Security + JWT

### 🐍 Data Microservice
- Python
- Selenium WebDriver (Web Scraping)

### 🗄️ Database
- MongoDB

---

## 🏗️ Architecture

El sistema sigue una arquitectura basada en microservicios:

```text
Frontend (Angular)
        ↓
Backend API (Spring Boot)
        ↓
Python Microservice (Scraping & Processing)
        ↓
MongoDB
```


### 🔄 Workflow

1. El usuario introduce datos desde el frontend.
2. El backend valida la solicitud (JWT).
3. Se envía una petición al microservicio Python.
4. El microservicio:
   - Extrae datos mediante scraping
   - Filtra valores atípicos (>30%)
   - Calcula estimaciones
5. Se devuelve un JSON con los resultados.
6. Los datos se almacenan y se muestran al usuario.

---

## 🔐 Security

- Autenticación basada en JWT (stateless)
- Contraseñas cifradas con BCrypt
- Control de acceso seguro

---

## 🚀 Getting Started

### Prerequisites

- Node.js / Angular CLI
- Java 17+
- Python 3.x
- MongoDB

### Installation

```bash
git clone <repo>
cd evaluty

```
---

### Run Frontend
```bash
cd frontend
npm install
ng serve

```
---

### Run Backend
```bash
cd backend
mvn spring-boot:run

```
---

### Run Microservice
```bash
cd microservice
python app.py

```

---

## 📂 Project Structure

```text
.
├── frontend/        # Angular SPA
├── backend/         # Spring Boot API
├── microservice/    # Python scraping & processing
├── database/        # MongoDB configuration
└── docs/            # Documentation
```

---

## 📈 Roadmap

- [ ] Mejorar precisión del algoritmo
- [ ] Integración con APIs inmobiliarias
- [ ] Dashboard de análisis avanzado
- [ ] Sistema de recomendaciones
- [ ] Deploy en cloud

---

## 👥 Team

Proyecto Intermodular DAW - UNIR FP

- **Stephen Nicholas Jones De Giorgi**  
  Microservicio Python, scraping, algoritmo y PDF  

- **Juan Montiel Fernández**  
  Backend (Spring Boot), API REST y base de datos  

- **Kyle Lamm**  
  Frontend (Angular), UI/UX y mapas  

---
## 📄 License
Proyecto académico - UNIR (DAW)