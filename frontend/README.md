# 💻 Frontend: Angular SPA (Evaluty)

Este módulo contiene la Interfaz de Usuario (UI) de la aplicación, desarrollada por **Kyle Lamm**. Es una *Single Page Application* (SPA) diseñada con un enfoque minimalista y accesible [cite: 320], optimizada para ofrecer la mejor Experiencia de Usuario (UX) a particulares y agentes inmobiliarios [cite: 366-378].

## 🛠️ Tecnologías Utilizadas
* **Framework:** Angular / TypeScript [cite: 229, 344]
* **Mapas:** Leaflet + OpenStreetMap [cite: 380]
* **Estilos:** CSS3 / HTML5 (Diseño en Figma) [cite: 364]

## 🚀 Funcionalidades Principales
* **Mapa Interactivo:** Integración de Leaflet que permite a los usuarios seleccionar una ubicación en el mapa. Utiliza geocodificación inversa (*Reverse Geocoding*) mediante Nominatim para autocompletar la dirección postal en el formulario [cite: 382-384, 539].
* **Gestión de Estado y Sesión:** Manejo e inyección del token JWT en las cabeceras HTTP (`Authorization: Bearer`) para interactuar de forma segura con los endpoints protegidos del backend[cite: 442].
* **Accesibilidad:** Puntuación de 89 en Lighthouse, asegurando contrastes adecuados y navegación intuitiva[cite: 356, 371].
* **Consumo de API:** Recepción de resultados matemáticos y visualización de descargas de informes PDF [cite: 433-436].

## 📦 Instalación y Ejecución
1. Asegúrate de tener **Node.js** y **Angular CLI** instalados.
2. Clona el repositorio y navega a este directorio.
3. Instala las dependencias:
   ```bash
   npm install

4.Levanta el servidor de desarrollo:
    ```bash
    ng serve

5.Navega a http://localhost:4200 en tu navegador.

---