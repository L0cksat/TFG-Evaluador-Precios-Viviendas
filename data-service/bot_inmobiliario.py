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
    direccion = "Carrer Sibelius, 03184 Torrevieja"
    print(f"Escribiendo dirección: {direccion}")
    caja_busqueda.send_keys(direccion)

    # un pausa para que pueda ver que escribe en el navegador.
    print("Estabilizando la página (2s)...")
    time.sleep(2)

    # A veces hay que pulsar ENTER, a veces hay que hacer clic en la sugerencia

    caja_busqueda.send_keys(Keys.RETURN)
    print("ENTER pulsado. Búsqueda enviada...")

    #Necesitamos pulsar un boton más para poder ver todas las propiedades en lista con el mapa.
    boton_ver_todos = wait.until(EC.element_to_be_clickable((By.XPATH, "//a[contains(text(), 'Ver todas')]")))

    boton_ver_todos.click()
    print("Botón Ver todas pulsada...")

    # 4. ---VERIFICACIÓN DE ÉXITO---
    print("Esperando carga de resultados..")
    #Esperamos 5s para obtener los resultados
    time.sleep(5)

    driver.save_screenshot("Captura_exito.png")
    print("Captura guardada como 'Captura_exito.png'")

except Exception as e:
    print("Hubo un error en la búsqueda:", e)
    # Con este bloque nuestro robot hará una captura de pantalla en el momento que falla:
    driver.save_screenshot("error_pantalla.png")
    print("Captura de pantalla guardada como error_pantalla.png")

finally:
    # 4 Cierre del navegador.
    driver.quit()
    print("---Fin del script---)")