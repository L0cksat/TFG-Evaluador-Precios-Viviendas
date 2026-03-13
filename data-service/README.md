# 📘 Documentación Técnica: Microservicio de Tasación (data-service)

**Proyecto:** EvalutyBot - TFG Intermodular (DAW UNIR-2026)
**Autores:** Stephen Nicholas Jones De Giorgi, Kylie Lamm, Juan Montiel Fernández
**Módulo:** Backend de Python (`data-service`)

---

## 🏗️ 1. Arquitectura y Propósito

El microservicio `data-service` es el motor de cálculo e inteligencia del proyecto Evaluty. Se encarga de recibir peticiones del backend principal (Java/Spring Boot), extraer datos oficiales del Gobierno, realizar web scraping en el mercado inmobiliario real, aplicar algoritmos de tasación y generar un informe final en formato PDF.

El sistema está orquestado a través de subprocesos (`subprocess`) y se comunica internamente mediante la lectura y escritura de archivos `.json` temporales, garantizando una ejecución secuencial y controlada.

---

## 🔄 2. Flujo de Ejecución (Pipeline)

El microservicio admite dos flujos de trabajo principales, gestionados por el orquestador `main.py`:

### A. Modo Básico (Entrada Manual)
Diseñado para cuando el usuario no dispone de la Referencia Catastral.
1. **Entrada:** `python main.py <Dirección> <Metros> <Habitaciones>`
2. **Orquestación (`main.py`):** Aplica el Protocolo de Limpieza y lanza directamente el scraper. Ambas variables internas (`direccion_oficial` y `direccion_busqueda`) adoptan el texto introducido por el usuario.
3. **Scraping (`bot_inmobiliario.py`):** Extrae testigos de mercado de Trovimap.
4. **Cálculo (`calculo.py`):** Calcula el precio medio sin aplicar simulaciones fiscales.
5. **Generación PDF (`generador_pdf.py`):** Crea el informe sin la caja de advertencia de Hacienda.

### B. Modo Pro (Catastro)
Diseñado para alta precisión técnica y fiscal.
1. **Entrada:** `python main.py <Referencia_Catastral> <Habitaciones>`
2. **Extracción (`catastro.py`):** Conecta con la API SOAP del Catastro Español. Extrae el XML, aislando la dirección oficial larga para el PDF y construyendo una dirección optimizada (Municipio + Calle) para el buscador.
3. **Scraping (`bot_inmobiliario.py`):** Utiliza la "dirección optimizada" para maximizar la tasa de éxito en el buscador de Trovimap.
4. **Cálculo (`calculo.py`):** Detecta la presencia de datos catastrales y aplica el algoritmo de simulación del Valor Mínimo de Hacienda (80% del valor de mercado).
5. **Generación PDF (`generador_pdf.py`):** Renderiza el informe oficial inyectando la "dirección oficial" y mostrando las advertencias legales fiscales.

---

## 🛠️ 3. Componentes y Soluciones Técnicas Implementadas

Durante el desarrollo de la versión final, se han resuelto los siguientes retos arquitectónicos:

### 3.1. Data Cleansing ("Estrategia Goldilocks") en `catastro.py`
**Problema:** El XML del Catastro devuelve direcciones altamente técnicas (ej. `CL GLORIA 51 13730...`) que provocan fallos en el autocompletado del buscador de propiedades.
**Solución:** Se implementó una lógica de extracción XML híbrida usando XPath. Se extrae el nodo `<ldt>` para el informe legal, y se combinan los nodos `<nv>` (Nombre Vía) y `<nm>` (Nombre Municipio) para inyectar al scraper una cadena de búsqueda con el equilibrio perfecto de precisión (ej. `GLORIA SANTA CRUZ DE MUDELA`).

### 3.2. Enrutamiento de Variables y Cortafuegos en `main.py`
**Problema:** Riesgo de fallos en cascada si un componente falla, y pérdida de la dirección oficial al optimizar la búsqueda.
**Solución:** * Desacoplamiento de variables: `main.py` gestiona instancias separadas para `direccion_busqueda` (Scraper) y `direccion_oficial` (PDF).
* Cortafuegos: Implementación de comprobaciones de estado (`resultado_bot.returncode != 0`) tras cada subproceso. Si Selenium falla o el Catastro da Timeout, la ejecución se aborta limpiamente (`sys.exit(1)`) antes de corromper el siguiente paso.

### 3.3. Manejo de Estados y Protocolo de Limpieza ("Garbage Collection")
**Problema:** Bugs de estado donde ejecuciones del Modo Básico leían archivos residuales (`datos_extraidos_catastro.json`) de pruebas anteriores del Modo Pro, inyectando simulaciones de Hacienda erróneas.
**Solución:** Se diseñó un Protocolo de Limpieza en `main.py` que actúa como "escoba" al inicio de cada ejecución, purgando la carpeta `json/` de ejecuciones anteriores para asegurar un entorno de ejecución estéril y determinista.

### 3.4. Inyección de Dependencias en `generador_pdf.py`
**Problema:** El PDF no era capaz de diferenciar entre la dirección oficial larga y la de búsqueda si leía la dirección directamente del JSON de cálculo.
**Solución:** Se refactorizó `generador_pdf.py` para dejar de depender de variables globales y del JSON de cálculo para la dirección. Ahora recibe la `direccion_oficial` inyectada directamente por el orquestador a través de los argumentos del sistema (`sys.argv[1]`), asegurando una fuente de la verdad única.

---

## 🚀 4. Requisitos y Ejecución

**Dependencias principales:**
* `requests` (Llamadas HTTP al Catastro)
* `selenium` (Web Scraping)
* `jinja2` & `xhtml2pdf` (Renderizado de plantillas HTML a PDF)

**Comandos de prueba locales:**
* Modo Básico: `python main.py "Calle Sibelius 145, 03184 Torrevieja" 120 3`
* Modo Pro: `python main.py "9872023VH5797S0001WX" 2`

**Salida esperada:**
Los componentes depositarán los resultados finales en la carpeta `json/`:
1. `precio_estimado.json`: Payload preparado para ser consumido por el backend Java.
2. `informe_tasacion.pdf`: Documento final listo para el cliente.

---
*Documentación generada para la versión de Producción v1.0.*