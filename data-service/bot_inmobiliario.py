from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time


# 1. Configuración del Driver
driver = webdriver.Chrome()
driver.maximize_window() # Esto es para maximizar la ventana del navegador, y así se evita elementos ocultos
#Definimos una espera de 15 segundos
wait = WebDriverWait(driver, 15)

print("---INICIANDO EL ROBOT---")
print("¿¡Buscas bronca chaval!?")

try:
    driver.get("https://www.trovimap.com")

    
    # 1. ---POPUP DE CONSENTIMIENTO---
    try:
        print("Verificando si hay algún popup de 'Consent'...")

        boton_consent = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, "button.fc-cta-consent")))

        boton_consent.click()
        print("Popup de 'Consent cerrado (click en botón padre).")
        time.sleep(1)
    except:
        print("No apareció el popup de 'Consent', avanzamos")


    # 2. ---GESTIÓN DE COOKIES---
    try:

        print("Esperando botón de cookies...")
        #XPATH traducción: Busca cualquier elemento <button> que contenga el texto "Aceptar"
        boton_cookies = wait.until(EC.element_to_be_clickable((By.XPATH, "//button[contains(text(), 'Aceptar')]")))

        boton_cookies.click()
        print("Cookies aceptadas. Barrera superada.")

    except:
        print("No se encontró el aviso de cookies o ya no está:")

    # 3. ---BÚSQUEDA---
    print("Estabilizando la página (2s)..")
    time.sleep(2)

    print("Buscando la barra de búsqueda...")

    # Enfocamos en el atributo 'placeholder' que es más seguro que buscar por clases génericas.

    selector_css = "input[placeholder='Escriba una ciudad, comunidad, CP']"

    # Usamos element_to_be_clickable para asegurar que no solo está ahí, sino que está listo para escribir.

    caja_busqueda = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, selector_css)))

    # Hacemos click primero para despertar el input
    caja_busqueda.click()
    # Hacemos limpieza por si acaso
    caja_busqueda.clear()

    #Ahora escribimos la dirección de prueba
    direccion = "Carrer Sibelius 148, 03184 Torrevieja"
    print(f"Escribiendo dirección: {direccion}")
    caja_busqueda.send_keys(direccion)

    # un pausa para que pueda ver que escribe en el navegador.
    print("Estabilizando la página (2s)...")
    time.sleep(2)

    # A veces hay que pulsar ENTER, a veces hay que hacer clic en la sugerencia

    caja_busqueda.send_keys(Keys.RETURN)
    print("ENTER pulsado. Búsqueda enviada...")

    # Aquí usamos este print para confirmar que estamos viendo casas en la cercanía de la 
    # driección de la vivienda introducida.
    print(f"URL de  resultado: {driver.current_url}")

    #Necesitamos pulsar un boton más para poder ver todas las propiedades en lista con el mapa.
    boton_ver_todos = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[contains(text(), 'Ver todas')]")))

    boton_ver_todos.click()
    print("Botón Ver todas pulsada...")
    print(f"URL de  resultado: {driver.current_url}")

    # 4. ---VERIFICACIÓN DE ÉXITO---
    print("Esperando carga de resultados..")
    #Esperamos 5s para obtener los resultados
    time.sleep(5)

    driver.save_screenshot("Captura_exito.png")
    print("Captura guardada como 'Captura_exito.png'")

    # 5. ---EXTRACCIÓN DE DATOS---
    # --Aquí realizamos una simulación de datos de un usuario para realizar pruebas.
    # Estos datos más tarde vendrán del Frontend (Angular)

    usuario_m2 = 90
    usuario_hab = 3

    # Hay que añadir márgenes de tolerancia
    margen_m2 = 0.30 # Se aceptan casas un 30% más grandes o pequeños
    margen_hab = 1 # Se aceptan casas con 1 habitación por arriba o abajo

    # ---

    print("Esperando que se carguen las tarjetas de propiedades...")
    wait.until(EC.presence_of_element_located((By.CLASS_NAME, "listing__container")))

    listas_casas = driver.find_elements(By.CLASS_NAME, "listing__container")
    print(f"He detectado {len(listas_casas)} casas en total. Seleccionando las más relevantes...")

    datos_comparables = [] # Aquí se guardaran los datos limpios.
    objetivo_casas = 10 # El limite de casa a un máximo de 10

    for indice, casa in enumerate(listas_casas):
        # Se va a parar el bucle en cuanto tengamos las propiedades que necesitamos.
        if len(datos_comparables) >= objetivo_casas:
            print("Objetivo cumplido. Paramos la búsqueda ^_^")
            break
        print(f"---Analizando Casa #{indice + 1}---")

        try:
            item = {} #Se crea un Diccionario temporal

            # 5.1 Extracción del precio de la vivienda
            try:
                precio_texto = casa.find_element(By.CSS_SELECTOR, ".price h4").text
                item['precio_raw'] = precio_texto.replace(".", "").replace("€", "").strip() # Limpiamos el dato para luego realizar la futura calculación. Se convierte 875.000 € -> 875000
            except:
                item['precio_raw'] = "0" # En este caso si no tiene precio, no nos sirve para el calculo.
            
            # Si el precio de la casa es 0 o "Consultar", se salta directamente.
            if not item["precio_raw"].isdigit():
                print("Casa descartada: Sin Precio Válido")
                continue

            # 5.2 Extracción de los metros cuadrados
            try:
                # Encontramos el primer span que está dentro de details.
                m2_texto = casa.find_element(By.CSS_SELECTOR, ".card__details span:nth-of-type(1)").text
                m2_limpio = m2_texto.replace("m²", "").strip() # Limpieza del dato, convertimos "167 m²" -> "167"
                item['m2'] = int(m2_limpio) # Aquí casteamos para convertir por fuerza el número a enmtero
            except:
                item['m2'] ="0"

            # Adición de filtros: Filtro 1 -- Comparación de m2 --
            # Se calcula el rango de metros
            min_m2 = usuario_m2 * (1 - margen_m2)
            max_m2 = usuario_m2 * (1 + margen_m2)

            # Añadimos este if not por si la casa es muy grande o muy pequeña, se ignora y no se mete en lista.
            if not (min_m2 <= item['m2'] <= max_m2):
                continue

            # 5.3 Extracción del número de habitaciones
            try:
                # Encontramos los número de habitaciones
                habs_texto = casa.find_element(By.CSS_SELECTOR, ".card__details span:nth-of-type(2)").text
                item['habitaciones'] = int(habs_texto.strip()) # Casteamos a int para tener un número entero limpio
            except:
                item['habitaciones'] = "0" 

                # Filtro 2 -- Comparación de habitaciones --
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
    print(f"Se han extraído {len(datos_comparables)} propiedades para el algoritmo.")
    print(datos_comparables) # Esto es lo que luego exportaremos al Excel o a otra parte de la app para el calcúlo.

    # 7. Exportación de los datos al formato JSON
    import json

    nombre_archivo = "resultados_scraping.json"

    with open(nombre_archivo, 'w', encoding='utf-8') as f:
        # dump volca la lista al archivo
        # ensure_acii=False permite que se lean las tildes
        # indent=4 lo deja bonito y legible
        json.dump(datos_comparables, f, ensure_ascii=False, indent=4)

    print(f"Datos guardados exitosamente en '{nombre_archivo}'")

except Exception as e:
    print("Hubo un error en la búsqueda:", e)
    # Con este bloque nuestro robot hará una captura de pantalla en el momento que falla:
    driver.save_screenshot("error_pantalla.png")
    print("Captura de pantalla guardada como error_pantalla.png")

finally:
    # 4 Cierre del navegador.
    driver.quit()
    print("---Fin del script---")