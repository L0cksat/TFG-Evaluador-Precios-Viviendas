import json
import os
import sys

try:
    usuario_m2 = float(sys.argv[2])
    print(f"Procesando cálculo para una vivienda de {usuario_m2} m2...")
except (IndexError, ValueError):
    print("Error: Debes proporcionar los metros cuadrados.")
    sys.exit(1)

# Primero vamos a definir la ruta donde está el archivo
ruta_archivo = os.path.join("json", "resultados_scraping.json")

try:
    # Abrimos el archivo en modo 'r' (read)
    with open(ruta_archivo, 'r', encoding='utf-8') as f:
        datos_casas = json.load(f) # Esto es el matrix, la magia
    print(f" He cargado {len(datos_casas)} casas para analizar.")
except FileNotFoundError:
    print(" Error: No encuentro el archivo. ¿Has ejecutado el scraper primero?")
    sys.exit()

    # Creamos una lista vacía antes
    lista_precio_m2 = []

    # Creamos el bucle para llenar la lista
    for casa in datos_casas:
        try:
            # Convertimos a números
            precio = float(casa['precio'])
            metros = float(casa['m2'])

            # Tenemos una protección: Que no se puede dividir por creo
            if metros > 0:
                precio_unitario = precio / metros

                # Cuardamos el resultado en la lista
                lista_precio_m2.append(precio_unitario)


        except ValueError:
            # Si algún dato viene sucio, saltamos esa casa
            continue
            
if len(lista_precio_m2) > 0:
    # Caluclamos la media del m2 de la zona
    media_zona = sum(lista_precio_m2) / len(lista_precio_m2)

    # Calculamos el precio estimado.
    precio_estimado = media_zona * usuario_m2

    print(f"Precio medio zona: {media_zona:.2f} €/m2")
    print(f"Valor estimado de tu casa: {precio_estimado: .2f} €")

    #Aquí guardaríamos el resultado en un nuevo JSON para enviarlo al Frontend
    carpeta_json = "json"
    nombre_archivo = "precio_estimado.json"
    ruta_json = os.path.join(carpeta_json, nombre_archivo)

    # Empaqueteado de datos
    datos_finales = {
        "status": "success",
        "precio_estimado": round(precio_estimado, 2),
        "media_m2_zona": round(media_zona, 2),
        "cantidad_casas_analizadas": len(lista_precio_m2)
    }
            
else:
    datos_finales = {
        "status": "error",
        "code": 404,
        "message": "No se encontraron viviendas comprarables en esta zona."
    }
    print("No se han encontrado datos válidos para calcular la media.")

with open(ruta_json, 'w', encoding='utf-8') as f:
        json.dump(datos_finales, f, ensure_ascii=False, indent=4)

print(f"Datos guardados éxitosamente en {ruta_json}")
print(f"Archivo guardado como: {nombre_archivo}")
print()
            
