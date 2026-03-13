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


# 📘 1. Scraper Inmobiliario (`bot_inmobiliario.py`)

**Módulo:** Motor de Extracción de Datos (Web Scraping)

---

## 🏗️ 1.1 Propósito y Descripción General

El script `bot_inmobiliario.py` actúa como el agente de recolección de datos (Web Scraper) del sistema Evaluty. Su misión principal es simular la navegación humana en el portal inmobiliario Trovimap, realizar una búsqueda geolocalizada y extraer una lista de "testigos de mercado" (viviendas comparables) filtrando en tiempo real aquellas que no se ajusten a los criterios arquitectónicos de la vivienda a tasar.

---

## ⚙️ 1.2 Tecnologías y Configuración del Motor

* **Tecnología Base:** Python + Selenium WebDriver.
* **Modo de Ejecución:** `Headless Browsing` (Navegación fantasma). El driver de Chrome está configurado con `--headless=new` y `--disable-gpu` para ejecutarse de forma silenciosa y eficiente en servidores backend sin requerir interfaz gráfica.
* **Gestión de Tiempos:** Se utilizan esperas explícitas (`WebDriverWait` / `expected_conditions`) de hasta 15 segundos para interactuar dinámicamente con el DOM (Document Object Model) solo cuando los elementos están listos, evitando los frágiles `time.sleep()` estáticos siempre que es posible.

---

## 📥 1.3 Interfaz de Entrada (Inputs)

El script no está diseñado para ejecutarse en el vacío; es invocado por el orquestador (`main.py`) y requiere estrictamente 3 argumentos a través de la línea de comandos (`sys.argv`):

1.  `direccion_input` (String): Cadena de búsqueda optimizada (ej. "GLORIA SANTA CRUZ DE MUDELA").
2.  `usuario_m2` (Integer): Superficie en metros cuadrados de la vivienda a tasar.
3.  `usuario_hab` (Integer): Número de habitaciones.

---

## 🔄 1.4 Flujo de Ejecución (Pipeline Interno)

1.  **Inicialización y Sanitización:** Validación de argumentos y casteo de tipos (`String` a `Integer` para áreas y habitaciones). Creación automática de directorios de salida (`/json` y `/screenshots`).
2.  **Gestión de Barreras (Popups):** Interacción automatizada con modales de consentimiento (GDPR) y banners de cookies mediante selectores CSS y XPath.
3.  **Inyección de Búsqueda:** Localización del `input` principal, escritura de la dirección, estabilización de red y selección de la primera sugerencia del autocompletar (simulando pulsaciones de teclado `ARROW_DOWN` + `ENTER`).
4.  **Extracción de Nodos:** Lectura de las tarjetas de propiedades (`listing__container`) iterando sobre el DOM.
5.  **Exportación:** Volcado del array de diccionarios resultante a un archivo estructurado `resultados_scraping.json`.

---

## 🧠 1.5 Algoritmo de Filtrado y Calidad de Datos

Para garantizar que el cálculo de tasación posterior sea preciso, el bot no extrae todas las casas de la zona, sino que aplica un filtro paramétrico riguroso en tiempo de ejecución:

* **Limpieza de Datos (Sanitization):** Los precios crudos (ej. "875.000 €") se limpian eliminando puntos, símbolos y espacios, convirtiéndolos en enteros calculables (`875000`). Se descartan propiedades sin precio ("Consultar").
* **Tolerancia Superficial (±30%):** El algoritmo calcula un `min_m2` y un `max_m2` basándose en la superficie solicitada. Las viviendas fuera de este rango son descartadas automáticamente.
* **Tolerancia de Distribución (±1 Habitación):** Se descartan viviendas que difieran en más de una habitación respecto a la vivienda objetivo.
* **Límite de Muestra:** El bot se detiene (Break) al alcanzar `objetivo_casas = 10` testigos válidos, optimizando el tiempo de procesamiento y el uso de CPU.

---

## 🛡️ 1.6 Trazabilidad y Gestión de Errores

El sistema está diseñado para fallar de forma segura ("Fail-safe") y reportar su estado al orquestador:

* **Capturas de Pantalla de Auditoría:** Si la búsqueda tiene éxito, guarda una captura (`Captura_exito.png`). Si ocurre una excepción o el elemento no se encuentra, guarda el estado del DOM en `error_pantalla.png` antes de cerrarse, facilitando la depuración.
* **Timeout Handling:** Si Trovimap no devuelve resultados en 15 segundos, se atrapa la `TimeoutException`, se escribe un JSON de error (Código 408) y se aborta el subproceso con `sys.exit(1)`, deteniendo la cadena de cálculo del backend principal para evitar falsos positivos.


# 📘 2. Motor de Cálculo (`calculo.py`)

**Módulo:** Algoritmo de Valoración y Lógica Fiscal

---

## 🏗️ 2.1 Propósito y Descripción General

El script `calculo.py` es el núcleo matemático del microservicio `data-service`. Su responsabilidad es procesar los "testigos de mercado" (viviendas comparables) extraídos previamente por el scraper, calcular la media del valor por metro cuadrado en la zona, y extrapolar ese precio a las dimensiones de la vivienda del usuario. Además, incorpora la lógica de negocio para la simulación de valores fiscales en el "Modo Pro".

---

## 📥 2.2 Interfaz de Entrada (Inputs)

El script recibe datos a través de dos vías simultáneas:

1.  **Argumentos del Sistema (`sys.argv`):**
    * `sys.argv[1]`: Dirección en formato texto.
    * `sys.argv[2]`: Superficie de la vivienda a tasar (se castea a `float` para permitir decimales).
2.  **Archivos Temporales JSON:**
    * Lee obligatoriamente `json/resultados_scraping.json`, el cual contiene la matriz de datos crudos (precios y tamaños) de las viviendas comparables.

---

## 🧠 2.3 Flujo de Procesamiento y Algoritmo

El algoritmo de tasación sigue un proceso iterativo de limpieza y cálculo:

1.  **Extracción de Unitarios:** Recorre la matriz de casas y divide el precio de venta (`precio_raw`) entre la superficie (`m2`) para obtener el precio unitario (`€/m2`) de cada testigo.
2.  **Filtro de Exclusión:** Implementa una capa de seguridad para descartar propiedades con ubicaciones inválidas (ej. filtrando cadenas específicas en la clave `ubicacion`) o datos corruptos (evitando divisiones por cero).
3.  **Cálculo de Media de Zona:** Suma todos los precios unitarios válidos y los divide entre el tamaño de la muestra, obteniendo una media aritmética rigurosa (`media_zona`).
4.  **Valoración Estimada:** Multiplica la `media_zona` por la superficie introducida por el usuario (`usuario_m2`), obteniendo el valor de mercado estimado (`precio_estimado`).

---

## 🏛️ 2.4 Lógica de Modos (Básico vs. Pro) y Fiscalidad

El script es "consciente del contexto" gracias a la detección de artefactos en el sistema de archivos:

* **Detección de Modo Pro:** El sistema comprueba si existe el archivo `json/datos_extraidos_catastro.json`.
* **Simulación Fiscal (Hacienda):** Si el archivo existe (indicando que el usuario usó una Referencia Catastral), el algoritmo aplica un descuento institucional, simulando el Valor Mínimo de Referencia de Hacienda como el **80% del valor de mercado estimado** (`precio_estimado * 0.80`). Este dato se añade dinámicamente al diccionario de resultados.

---

## 🧹 2.5 Gestión Ecológica de Archivos (Garbage Collection)

Para garantizar la estabilidad del servidor y evitar colisiones de estado ("Bugs Fantasma") en ejecuciones futuras, `calculo.py` implementa un protocolo de limpieza al final de su ciclo de vida:

* Elimina `datos_extraidos_catastro.json` inmediatamente después de comprobar el Modo Pro.
* Elimina `resultados_scraping.json` tras finalizar el empaquetado de datos.

---

## 📤 2.6 Salida de Datos (Outputs)

El script empaqueta todos los resultados, formatea los decimales con precisión de dos dígitos (`round(..., 2)`) y genera un archivo final estandarizado:

* **Archivo generado:** `json/precio_estimado.json`.
* Este archivo actúa como el "Payload" final que será consumido tanto por `generador_pdf.py` para la creación del informe visual, como por el backend de Java/Spring Boot para su almacenamiento en base de datos.

# 📘 3. Conector Gubernamental (`catastro.py`)

**Módulo:** Integración Externa (External Integration Module) o Cliente API

---

## 🏗️ 3.1 Propósito y Reto Tecnológico
El script `catastro.py` funciona como un cliente HTTP diseñado para interactuar con la Sede Electrónica del Catastro de España. El principal reto técnico es que el Gobierno no utiliza una API REST moderna con JSON, sino un servicio web legado (SOAP/XML) estructurado con Espacios de Nombres (Namespaces) estrictos, lo que requiere un parseo avanzado del Árbol Documental.

---

## 🧠 3.2 Soluciones Técnicas y Tolerancia a Fallos

### 3.2.1. Resiliencia de Red (Timeouts y Exceptions)
La conexión a servidores gubernamentales es propensa a cuelgues o latencias altas. Se ha configurado un `timeout=10` explícito en la librería `requests`. 
Si el servidor no responde, se capturan las excepciones `Timeout` y `RequestException`, generando un *Payload* de error estandarizado (códigos HTTP 408 y 500) para que el frontend de Angular pueda mostrar un mensaje amigable al usuario en lugar de colapsar la aplicación.

### 3.2.2 Navegación XML y Espacios de Nombres (Namespaces)
Para procesar la respuesta, se utiliza `xml.etree.ElementTree`. Debido a la arquitectura del Catastro, las etiquetas XML no pueden buscarse por su nombre simple (ej. `<ldt>`). Es imperativo inyectar el Namespace oficial (`{http://www.catastro.meh.es/}`) en cada búsqueda XPath (`raiz.find()`) para localizar los nodos correctamente.

### 3.2.3. Data Cleansing y la Estrategia "Goldilocks"
El XML devuelve información altamente técnica. Se implementa una extracción híbrida para satisfacer a dos clientes internos distintos:
1.  **Extracción Legal (Para el PDF):** Extrae el nodo `<ldt>` ("CL GLORIA 51 13730...") que mantiene la rigurosidad legal.
2.  **Extracción Semántica (Para el Scraper):** Localiza los nodos `<nv>` (Vía) y `<nm>` (Municipio) y los concatena. Esto actúa como un "Limpiador de Datos" nativo, eliminando códigos postales y tipos de vía complejos para entregar a Trovimap una cadena de búsqueda ideal ("GLORIA SANTA CRUZ DE MUDELA"), reduciendo la tasa de fallos de autocompletado del buscador a casi cero.

---

## 📦 3.3 Estructura del Payload Resultante
El script actúa como un adaptador (Adapter Pattern), traduciendo el complejo XML del gobierno a un formato JSON moderno y ligero que el resto del sistema puede consumir fácilmente. 

Ejemplo del formato de salida (`datos_extraidos_catastro.json`):
```json
{
    "status": "success",
    "direccion_oficial": "CL GLORIA 51 13730 SANTA CRUZ DE MUDELA (CIUDAD REAL)",
    "direccion_busqueda": "GLORIA SANTA CRUZ DE MUDELA",
    "metros_totales": "308"
}

```
# 📘 4. Motor de Reportes (`generador_pdf.py`)

**Módulo:** Reportes (Reporting Module) o Módulo de Presentación

---

## 🏗️ 4.1 Propósito y Patrón Arquitectónico
Este script constituye la capa de presentación del backend. Para su implementación, se ha seguido el patrón **MVC (Modelo-Vista-Controlador)** en su aproximación a la generación de documentos. En lugar de usar librerías que "dibujan" el PDF línea a línea desde código Python (altamente ineficiente y difícil de mantener), se separa la lógica de los datos (El Modelo/Python) del diseño visual (La Vista/HTML+CSS) usando un motor de plantillas.

---

## ⚙️ 4.2 Lógica de Inyección y Seguridad

### 4.2.1 Inyección de Dependencias Bidireccional
El motor no acopla la dirección del inmueble a los resultados matemáticos. Evita buscar la dirección en los JSON de cálculo, ya que estos carecen del contexto legal del Catastro. En su lugar, la `direccion_oficial` es inyectada directamente por el orquestador (`main.py`) a través de la CLI (`sys.argv[1]`). Esto asegura que el "Single Source of Truth" (Única Fuente de la Verdad) de la dirección provenga siempre del controlador superior.

### 4.2.2 Programación Defensiva (Safe Defaults)
Al leer los cálculos financieros de `precio_estimado.json`, el diccionario de `contexto` se construye utilizando el método `.get(clave, valor_por_defecto)` nativo de Python. 
* **Justificación:** Si por algún motivo el motor de cálculo falla parcialmente y omite un dato (por ejemplo, si no se ejecutó en Modo Pro y falta la clave `valor_minimo_hacienda`), el uso de `.get()` evita que el script lance un error fatal (`KeyError`). En su lugar, inyecta `None` o `0`, permitiendo que el motor de plantillas oculte el bloque HTML correspondiente dinámicamente.

---

## 🎨 4.3 Proceso de Transpilación (Jinja2 + XHTML2PDF)

El flujo de generación consta de 3 fases técnicas:
1.  **Carga del Entorno:** `FileSystemLoader` monta el directorio `/templates` en memoria.
2.  **Renderizado (Binding):** Jinja2 toma el diccionario de Python y lo interpola dentro de `informe.html`, sustituyendo las variables (ej. `{{ precio_estimado }}`) por los cálculos reales y evaluando condicionales lógicos (`{% if valor_hacienda %}`).
3.  **Conversión Binaria:** La librería `xhtml2pdf` emula un motor de renderizado web, transformando el HTML/CSS resultante en un lienzo PDF vectorial. La escritura final utiliza la directiva `"w+b"` (Write + Binary) para garantizar la codificación correcta de los bytes del documento final.

# 📘 5. Orquestador Principal (`main.py`)

**Módulo:** Orquestador (Orchestrator Module) o Módulo Core

---

## 🏗️ 1. Propósito y Patrón Arquitectónico
El script `main.py` actúa como el "cerebro" del microservicio. Implementa un patrón de diseño **Orquestador (Orchestrator)**. En lugar de tener un único script monolítico gigante, este módulo delega responsabilidades a sub-scripts especializados, coordinando el flujo de ejecución, inyectando dependencias (variables) y gestionando el ciclo de vida de los datos temporales.

---

## ⚙️ 2. Decisiones Críticas de Arquitectura

### 2.1. Aislamiento de Procesos (`subprocess` vs `import`)
Una decisión técnica clave fue ejecutar los módulos mediante `subprocess.run([sys.executable, ...])` en lugar de importarlos directamente en Python (ej. `import bot_inmobiliario`). 
* **Justificación:** El web scraping con Selenium (Chrome) consume mucha memoria RAM y puede dejar procesos zombis si falla. Al usar subprocesos aislados, el Sistema Operativo libera el 100% de la memoria de Selenium en el momento en que el script termina o crashea, garantizando que el servidor backend nunca se sature por fugas de memoria (Memory Leaks).

### 2.2. Protocolo de Limpieza (Garbage Collection y Estado Limpio)
Al iniciar (`ejecutar_bronca()`), el sistema implementa una rutina de sanitización del entorno. Recorre un array (`archivos_basura`) y utiliza `os.remove()` para eliminar archivos `.json` residuales. 
* **Justificación:** Previene la "Corrupción de Estado". Si un usuario ejecutó el Modo Pro previamente, el archivo `datos_extraidos_catastro.json` se quedaría en el disco. Una ejecución posterior en Modo Básico leería erróneamente ese archivo e inyectaría la simulación de Hacienda de otro usuario. La escoba asegura un entorno "estéril" antes de cada tasación.

### 2.3. Principio "Fail-Fast" (Cortafuegos)
Se aplica el principio de diseño *Fail-Fast* (Falla Rápido). Después de cada subproceso crítico, se verifica su código de salida (`resultado_bot.returncode != 0`). 
* **Justificación:** Si el Scraper falla por un cambio en el HTML de Trovimap, no tiene sentido ejecutar el motor de cálculo ni generar un PDF vacío o erróneo. El orquestador intercepta el fallo y aborta inmediatamente con `sys.exit(1)`, ahorrando recursos de CPU.

---

## 🔀 3. Enrutamiento Inteligente de Variables (Multimodal)
El sistema deduce dinámicamente la intención del usuario evaluando la longitud de los argumentos del sistema (`len(sys.argv)`):

* **Modo Pro (Catastro - 3 Argumentos):** Se asume una Referencia Catastral. Invoca a `catastro.py`, lee su salida JSON y realiza un "Bifurcado de Variables": almacena la `direccion_oficial` (larga, para el aspecto legal del PDF) y la `direccion_busqueda` (corta, para maximizar el éxito del Scraper).
* **Modo Básico (Manual - 4 Argumentos):** Se asigna la entrada del usuario simultáneamente a `direccion_oficial` y `direccion_busqueda`, ya que no hay intervención gubernamental para limpiar el dato.

---
*Documentación generada para la versión de Producción v1.0.*