import json
import os
from jinja2 import Environment, FileSystemLoader
from xhtml2pdf import pisa

def generar_pdf():
    # 1. Rutas de los archivos para poder trabajar
    ruta_json = os.path.join("json", "precio_estimado.json")
    ruta_template = "templates"
    ruta_pdf_salida = os.path.join("json", "informe_tasacion.pdf")

    # 2. Leer los datos del JSON -> que proviene de calculo.py
    try:
        with open(ruta_json, 'r', encoding='utf-8') as f:
            datos_tasacion = json.load(f)
    except FileNotFoundError:
        print("Error: No se encontró 'precio_estimado.json'. ¿Se ejecutó el cálculo?")
        return
    
    # 3. Preparamos el diccionariom de variables para inyectar en el HTML
    contexto = {
        "direccion": datos_tasacion.get("direccion", "Direccion no especificada."),
        "metros": datos_tasacion.get("metros_cuadrados", 0),
        "testigos": datos_tasacion.get("cantidad_casas_analizadas", 0),
        "precio_estimado": datos_tasacion.get("precio_estimado", 0),
        "precio_m2": datos_tasacion.get("media_m2_zona", 0),
        "valor_hacienda": datos_tasacion.get("valor_minimo_hacienda", None) # Puede ser None(vacio) en modo básico
    }

    # 4. Cargamos la plantilla HTML con Jinja2
    env = Environment(loader=FileSystemLoader(ruta_template))
    template = env.get_template("informe.html")

    # 5. Rellenar el HTML con nuestros datos
    html_relleno = template.render(contexto)

    # 6. Convertir el HTML relleno a PDF
    with open(ruta_pdf_salida, "w+b") as archivo_pdf:
        #pisa.CreatePDF es la función que hace la magia
        estado = pisa.CreatePDF(html_relleno, dest=archivo_pdf)

    if not estado.err:
        print(f"PDF generado con éxito en: {ruta_pdf_salida}")
    else:
        print("Error al generar el PDF.")

if __name__ == "__main__":
    generar_pdf()
