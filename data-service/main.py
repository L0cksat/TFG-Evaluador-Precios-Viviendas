import subprocess
import sys
import os
import json

def ejecutar_bronca():
    cantidad_argumentos = len(sys.argv)

    #Variables vacías que rellenaremos dependiendo del modo
    direccion = ""
    metros = ""
    habitaciones = ""

    # MODO Pro 3 argumentos (Script + Referencia + Habs)
    if cantidad_argumentos == 3:
        print("Modo Pro detectado: Usando Referencia Catastral...")
        referencia = sys.argv[1]
        habitaciones = sys.argv[2]

        # Aquí llamaríamos al catastro.py pasándole la referencia
        subprocess.run([sys.executable, "catastro.py", referencia])

        # Se abre el JSON del catastro.py para leer la dirección y los metros
        ruta_json_catastro = os.path.join("json", "datos_extraidos_catastro.json")
        try:
            with open(ruta_json_catastro, 'r', encoding='utf-8') as f:
                datos_catastro = json.load(f)
                if datos_catastro["status"] == "success":
                    direccion = datos_catastro["direccion_oficial"]
                    metros = str(datos_catastro["metros_totales"])
                else:
                    print("Error: El Catastro no devolvió datos válidos.")
                    sys.exit(1)
        except FileNotFoundError:
            print("Error: No se encontró el archivo del Catastro.")
            sys.exit(1)
    # MODO Básico 4 argumetnos (script + Dirección + Metros + Habs)
    elif cantidad_argumentos == 4:
        print("Modo Básico detectado: Entrada manual...")
        direccion = sys.argv[1]
        metros = sys.argv[2]
        habitaciones = sys.argv[3]

    else:
        print("Error: Número de argumentos incorrecto.")
        print(" -> Uso Modo Pro: python main.py <Referencia> <Habitaciones>")
        print(" -> Uso Modo Básico: python main.py <Dirección> <Metros> <Habitaciones>")
        sys.exit(1)
        

    print("---Bienvenido a EvalutyBot! ^_^---")
    print(f"Iniciando tasación para: {direccion} | {metros}m2 | {habitaciones} habs.")

    # Segundo, llamamos a nuestro querido bot_inmobiliario.py (Web Scraping).
    print("\n--- PASO 1: Buscando testigos en el mercado ---")
    #Ahora hay que pasarle el nombre del archivo y las 3 variables en orden.
    comando_bot = [sys.executable, "bot_inmobiliario.py", direccion, metros, habitaciones]
    subprocess.run(comando_bot)

    # Tercero, llamamos a nuestro querido calculo.py para que calcule el precio estimado.
    print("\n--- PASO 2: Calculando estimación de precio ---")
    # Recuerda que el calculo.py recibe la dirección en el índice 1 y los metros en el índice 2
    comando_calculo = [sys.executable, "calculo.py", direccion, metros]
    subprocess.run(comando_calculo)

    print("\n Proceso completo. El Backend ya puede leer 'precio_estimado.json'")

if __name__ == "__main__":
    ejecutar_bronca()