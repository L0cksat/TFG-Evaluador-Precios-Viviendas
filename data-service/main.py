import subprocess
import sys

def ejecutar_bronca():
    try:
        # Primero recibimos los datos desde Java o el terminal.
        direccion = sys.argv[1]
        metros = sys.argv[2]
        habitaciones = sys.argv[3]
    except IndexError:
        print("Error: Faltan argumentos. Uso: python main.py 'Direccion' Metros Habitaciones")
        sys.exit(1)

    print("---Bienvenido a EvalutyBot! ^_^---")
    print(f"Iniciando tasación para: {direccion} | {metros}m2 | {habitaciones} habs.")

    # Segundo, llamamos a nuestro querido bot_inmobiliario.py (Web Scraping).
    print("\n--- PASO 1: Buscando tesigos en el mercado ---")
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