import sys
import os
import json
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

print("-" * 43)
print("---INICIANDO EL ROBOT (MODO INTEGRACIÓN)---")
print("-" * 43)
print()
print("¡Hola! ¡Soy Evaluty Bot!")
print()
print("¿¡Buscas bronca chaval!?")
print()

# VALIDACIÓN DE ARGUMENTOS
# Java nos debe enviarnos 3 valores: Dirección, m2 y habitaciones. En este caso estamos usando el terminal para poder
# realizar pruebas de funcionamiento.
# Si contamos también el nombre del script en sí, entonces son 4 valores en total.

if len(sys.argv) < 4:
    print("ERROR: ¡Faltan argumentos!")
    print("USO CORRECTO: python bot_inmobiliario.py 'Dirección' m2 habitaciones")
    print("Ejemplo: python bot_inmobiliario.py 'Calle Sibelius 148, Torrevieja', 90 3")
    sys.exit(1) #Ceramos el programa por el error.
else:
# Captura de variable desde la terminal
    try:
        #Ignoramos el sys.argv[0] ya que es el nombre del script.

        direccion_input = sys.argv[1] # "Texto de la dirreción"

     #IMPORTANTE todo lo que viene de la terminal es texto (String)
        # Hay que castear las variables para que sean enteros para que el script los acepte.
        usuario_m2 = int(sys.argv[2])
        usuario_hab = int(sys.argv[3])

        print(f"Datos recibidos: {direccion_input} | {usuario_m2}m² | {usuario_hab} habitaciones")
        print()
    except ValueError:
        print("ERROR DE TIPO: los m2 y habitaciones deben ser números enteros.")
        print()
        sys.exit(1)

# Preparación de las carpetas para screenshots y archivos json
carpeta_json = "json"
carpeta_img = "screenshots"

# El os.makedirs crea las carpetas necesarias, exist_ok=True, si existe no me des el error, sigue en adelante.
os.makedirs(carpeta_json, exist_ok=True)
os.makedirs(carpeta_img, exist_ok=True)

print(f"Carpetas '{carpeta_img}' y '{carpeta_json}' verificadas")
print()


# 1. Configuración del Driver
driver = webdriver.Chrome()
driver.maximize_window() # Esto es para maximizar la ventana del navegador, y así se evita elementos ocultos
#Definimos una espera de 15 segundos
wait = WebDriverWait(driver, 15)

try:
    driver.get("https://www.trovimap.com")

    
    # 1. ---POPUP DE CONSENTIMIENTO---
    try:
        print("Verificando si hay algún popup de 'Consent'...")
        print()

        boton_consent = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button.fc-cta-consent")))

        boton_consent.click()
        print("Popup de 'Consent cerrado (click en botón padre).")
        print()
        time.sleep(1)
    except:
        print("No apareció el popup de 'Consent', avanzamos")
        print()


    # 2. ---GESTIÓN DE COOKIES---
    try:

        print("Esperando botón de cookies...")
        print()
        #XPATH traducción: Busca cualquier elemento <button> que contenga el texto "Aceptar"
        boton_cookies = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(), 'Aceptar')]")))

        boton_cookies.click()
        print("Cookies aceptadas. Barrera superada.")
        print()

    except:
        print("No se encontró el aviso de cookies o ya no está.")
        print()

    # 3. ---BÚSQUEDA---
    print("Estabilizando la página (2s)..")
    print()
    time.sleep(2)

    print("Buscando la barra de búsqueda...")
    print()

    print(f"Escribiendo dirección: {direccion_input}")
    print()

    # Enfocamos en el atributo 'placeholder' que es más seguro que buscar por clases génericas.

    selector_css = "input[placeholder='Escriba una ciudad, comunidad, CP']"

    # Usamos element_to_be_clickable para asegurar que no solo está ahí, sino que está listo para escribir.

    caja_busqueda = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, selector_css)))

    # Hacemos click primero para despertar el input
    caja_busqueda.click()
    # Hacemos limpieza por si acaso
    caja_busqueda.clear()

    #Ahora enviamos la dirección recogida del formulario frontend
    caja_busqueda.send_keys(direccion_input)

    # un pausa para que pueda ver que escribe en el navegador.
    print("Estabilizando la página (2s)...")
    print()
    time.sleep(2)

    # A veces hay que pulsar ENTER, a veces hay que hacer clic en la sugerencia

    caja_busqueda.send_keys(Keys.RETURN)
    print("ENTER pulsado. Búsqueda enviada...")
    print()

    # Aquí usamos este print para confirmar que estamos viendo casas en la cercanía de la 
    # driección de la vivienda introducida.
    print(f"URL de  resultado: {driver.current_url}")
    print()

    #Necesitamos pulsar un boton más para poder ver todas las propiedades en lista con el mapa.
    boton_ver_todos = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[contains(text(), 'Ver todas')]")))

    boton_ver_todos.click()
    print("Botón Ver todas pulsada...")
    print()
    print(f"URL de  resultado: {driver.current_url}")
    print()

    # 4. ---VERIFICACIÓN DE ÉXITO---
    print("Esperando carga de resultados..")
    print()
    #Esperamos 5s para obtener los resultados
    time.sleep(5)

    # Añadimos os.path.join para unir carpeta con archivo de forma segura
    ruta_captura = os.path.join(carpeta_img, "Captura_exito.png")
    driver.save_screenshot(ruta_captura)
    print(f"Captura guardada en {ruta_captura}")
    print()

    # 5. ---EXTRACCIÓN DE DATOS---
    
    # Hay que añadir márgenes de tolerancia
    margen_m2 = 0.30 # Se aceptan casas un 30% más grandes o pequeños
    margen_hab = 1 # Se aceptan casas con 1 habitación por arriba o abajo

    # ---

    print("Esperando que se carguen las tarjetas de propiedades...")
    print()
    wait.until(EC.presence_of_element_located((By.CLASS_NAME, "listing__container")))

    listas_casas = driver.find_elements(By.CLASS_NAME, "listing__container")
    print(f"He detectado {len(listas_casas)} casas en total. Seleccionando las más relevantes...")
    print()

    datos_comparables = [] # Aquí se guardaran los datos limpios.
    objetivo_casas = 10 # El limite de casa a un máximo de 10

    for indice, casa in enumerate(listas_casas):
        # Se va a parar el bucle en cuanto tengamos las propiedades que necesitamos.
        if len(datos_comparables) >= objetivo_casas:
            print("Objetivo cumplido. Paramos la búsqueda ^_^")
            print()
            break

        print(f"---Analizando Casa #{indice + 1}---")

        try:
            item = {} #Se crea un Diccionario temporal

            # 5.1 Extracción del precio de la vivienda
            try:
                precio_texto = casa.find_element(By.CSS_SELECTOR, ".price h4").text
                item['precio_raw'] = precio_texto.replace(".", "").replace("€", "").strip() # Limpiamos el dato para luego realizar la futura calculación. Se convierte 875.000 € -> 875000
            except:
                item['precio_raw'] = 0 # En este caso si no tiene precio, no nos sirve para el calculo.
            
            # Si el precio de la casa es 0 o "Consultar", se salta directamente.
            if not item["precio_raw"].isdigit():
                print("Casa descartada: Sin Precio Válido")
                continue

            # 5.2 Extracción de los metros cuadrados y habitaciones usando una extracción flexible.

            # Sacamos todos los detalles de la tarjeta.
            detalles = casa.find_elements(By.CSS_SELECTOR, ".card__details span")

            # Valores por defecto, si no se encuentran
            item['m2'] = 0
            item['habitaciones'] = 0

            for detalle in detalles:
                texto = detalle.text.strip()
                
                if "m²" in texto:
                    try:
                        m2_limpio = texto.replace("m²", "").strip() # Limpieza del dato, convertimos "167 m²" -> "167"
                        item['m2'] = int(m2_limpio) # Aquí casteamos para convertir por fuerza el número a enmtero
                    except:
                        item['m2'] = 0
                elif texto.isdigit() and len(texto) < 3:
                    try:
                        if item['habitaciones'] == 0:
                            item['habitaciones'] = int(texto)
                    except:
                        item['habitaciones'] = 0 

            # Adición de filtros: Filtro 1 -- Comparación de m2 --:
            # Se calcula el rango de metros
            if item['m2'] > 0:
                min_m2 = usuario_m2 * (1 - margen_m2)
                max_m2 = usuario_m2 * (1 + margen_m2)

                # Añadimos este if not por si la casa es muy grande o muy pequeña, se ignora y no se mete en lista.
                if not (min_m2 <= item['m2'] <= max_m2):
                    print(f"Descartada por tamaño {item['m2']}m²")
                    continue
            else:
                print(" Descartada: Sin datos de metros.")

                # Filtro 2 -- Comparación de habitaciones --:
                if not (usuario_hab - margen_hab <=item['habitaciones'] <= usuario_hab + margen_hab):
                    continue
            
            # 5.4 Extracción de la ubicación
            try:
                #Buscamos dentro de listing__description suele tener el municipio o barrio
                ubi_texto = casa.find_element(By.CSS_SELECTOR, ".listing__description h3").text
                item['ubicacion'] = ubi_texto
            except:
                item['ubicacion'] = "Desconocida"

            # 5.5 Extracción del titulo de la tarjeta
            try:
                # Buscamos el titulo tipo h2
                item['titulo'] = casa.find_element(By.TAG_NAME, "h2").text
            except:
                item['titulo'] = "Sin titulo"

            # Guardamos el item limpio
            datos_comparables.append(item)
            print(f"Guardada: {item['precio_raw']}€ | {item['m2']}m² | {item['habitaciones']}hab | {item['ubicacion']}")
        except Exception as e:
            print(f" Error leyendo tarjeta: {e}")

    # 6. Resultado final
    print("-" * 50)
    print(f" Scraping completado! ^_^")
    print("-" * 50)
    print(f"Se han extraído {len(datos_comparables)} propiedades para el algoritmo.")
    print("-" * 50)
    print(datos_comparables) # Esto es lo que luego exportaremos al Excel o a otra parte de la app para el calcúlo.
    print("-" * 50)

    # 7. Exportación de los datos al formato JSON

    nombre_archivo = "resultados_scraping.json"

    ruta_json = os.path.join(carpeta_json, nombre_archivo)

    with open(ruta_json, 'w', encoding='utf-8') as f:
        # dump volca la lista al archivo
        # ensure_acii=False permite que se lean las tildes
        # indent=4 lo deja bonito y legible
        json.dump(datos_comparables, f, ensure_ascii=False, indent=4)

    print(f"Datos guardados exitosamente en {ruta_json}")
    print()

except Exception as e:
    print("Hubo un error en la búsqueda:", e)
    print()
    # Con este bloque nuestro robot hará una captura de pantalla en el momento que falla:
    ruta_captura = os.path.join(carpeta_img, "error_pantalla.png")
    driver.save_screenshot(ruta_captura)
    print(f"Captura de pantalla del error guardada en: {ruta_captura}")
    print()

finally:
    # 4 Cierre del navegador.
    driver.quit()
    print("-" * 50)
    print("---Fin del script---")