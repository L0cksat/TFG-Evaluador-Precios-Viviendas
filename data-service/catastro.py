import sys
import requests
import os
import json
import xml.etree.ElementTree as ET

def consultar_catastro():
    try:
        # Primero vamos a capturar la referencia que nos manda main.py
        referencia_catastral = sys.argv[1]
    except IndexError:
        print("Error: Debes proporcinoar la Referencia Catastral.")
        sys.exit(1)

    print(f"Conectando con el Catastro para la referencia: {referencia_catastral}...")

    # 2. La URL oficial de la API Públic del Catastro Español
    url = f"http://ovc.catastro.meh.es/ovcservweb/OVCSWLocalizacionRC/OVCCallejero.asmx/Consulta_DNPRC?Provincia=&Municipio=&RC={referencia_catastral}"

    # Hacemos la llamada HTTP GET
    respuesta = requests.get(url)

    #4. Comprobamos si el Gobierno nos ha respondido con un 200 OK
    if respuesta.status_code == 200:
        print("¡Conexión exitosa con el Gobierno ;)!")
        #Aquí imprimieremos el XML en crudo para verlo con mis propios ojos
        print(respuesta.text)

        # 4.1 convertimos el texto en un "Árbol" navegable
        raiz = ET.fromstring(respuesta.text)

        # 4.2 Definimos el Namespace del Gobierno (vital para que funcione)
        #espacios = {'cat': 'http://www.catastro.meh.es/'}

        try:
            # 4.3 pescamos los datos usuando XPath
            nodo_direccion = raiz.find('.//*{http://www.catastro.meh.es/}ldt')
            nodo_metros = raiz.find('.//*{http://www.catastro.meh.es/}sfc')

            if nodo_direccion is not None and nodo_metros is not None:
                direccion_oficial = nodo_direccion.text
                metros_totales = nodo_metros.text

                print(f"Dirección encontrada: {direccion_oficial}")
                print(f"Metros encontrados: {metros_totales} m2")

            # 4.4 Empquetado de datos en el JSON
                datos_finales_catastro = {
                    "status": "success",
                    "direccion_oficial": direccion_oficial,
                    "metros_totales": metros_totales
                }
            else:
                print("No se ha encontrado datos suficientes para la extracción.")
                datos_finales_catastro = {
                    "status": "error",
                    "code": 404,
                    "mensaje": "No se ha encontrado datos dentro del XML del Catastro."
                }

            # 4.4 Ahora lo guardamos en el JSON para que el calculo.py también puede aprovechar de ello.
            carpeta_json = "json"
            nombre_archivo = "datos_extraidos_catastro.json"
            ruta_json = os.path.join(carpeta_json, nombre_archivo)

            with open(ruta_json, 'w', encoding='utf-8') as f:
                json.dump(datos_finales_catastro, f, ensure_ascii=False, indent=4)
            
            print(f"Se ha guardado el JSON con éxito: {nombre_archivo}")
            print(f"El archivo ha sido guardado con éxito en la ruta: {ruta_json}")

        except Exception as e:
            print(f"Error procesando el XML: {e}") 
    else:
        print(f"Error al conectar con el Catatstro. Código {respuesta.status_code}")

if __name__ == "__main__":
    consultar_catastro()