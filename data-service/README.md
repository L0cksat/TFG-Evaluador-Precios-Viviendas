# Script Python con Selenium Web Driver

### Creación y lógica detrás del script.
Aquí hemos realizado la creación del script de Python que se usará para realizar las búsquedas
para encontrar a los precios alrededor de la dirección introducida.

Básicamente la lógica del script es la siguiente:

1. Usamos el Web Driver de Selenium para poder "controlar" el navegador, en este caso Chrome para poder realizar la búsqueda através de la página seleccionada, en este caso es: _***https://www.trovimap.com/***_
   
2. Indicamos al Web Driver que partes de la estructura de HTML debe fijarse y posteriormente ***cliquear*** para poder avanzar y llegar a dónde queremos llegar para poder extraer la información necesaria para luego entregarlo al script del cáclulo del precio estimado.
   
3. Se escribe el código necesario para realizar dicha tarea y luego posteriormente lo que hará el script es navegar, aceptar el consentimiento del ***Trovimap*** igual que sus cookies. Buscar y encontrar la barra de búsqueda para poder introducir lo que (de momento) se introduce en la consola, (que luego será reemplazado con la información introducida por el usuario en el formulario de ***Angular*** en la parte del frontend), e introducirlo en la barra de búsqueda, iniciar la búsqueda y avanzar ***cliqueando*** en el botón "_Ver más_" para ver toda la lista de las propiedades alrededor de la dirección.
   
4. Posteriormente al encontrar la lista de propiedades similares a las propiedades introducidas, el script extraerá la información necesaria para el posterior cálculo para la estimación final.
