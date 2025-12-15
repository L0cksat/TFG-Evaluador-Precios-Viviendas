# TFG-Evaluador-Precios-Viviendas
Este repositorio es para el control de versiones para el proyecto/trabajo final de grado (TFG) de 2º de DAW. Este repositorio es para la aplicación de una página de evaluación de precios de vivienda.

## Resumen
El presente proyecto consiste en diseñar y desarrollar una aplicación web enfocada en las valoraciones de inmuebles, cuyo objetivo es democratizar el acceso a estimaciones de valoraciones de viviendas de forma gratuita y sin pasarelas de pago. La aplicación permite a los usuarios obtener una rápida valoración estimada de lo más cercano posible a un precio fiable comparando entre 3 y 10 viviendas cercas, aparte de poder solicitar la valoración mínima permitido por el gobierno usando su número de catastro.

Para su implementación, hemos optado por una arquitectura modular basada en microservicios. El backend será desarrollado con ***Spring Boot (Java)*** para la gestión de usuarios y la lógica del negocio, mientras el frontend utilizará Angular para dar un aspecto del interfaz más moderno con mapas interactivos. 
Un componente clave del proyecto es el microservicio desarrollado en Python, esto será el encargado de realizar el web scraping sobre portales inmobiliarios como ***Trovimap***, para obtener datos del mercado de los más actualizados posibles, y ejecutar el algoritmo predictivo de precios. Además, la aplicación incluye funcionalidades como la generación de informes PDF con la valoración y avisos fiscales en el caso de solicitar el precio mínimo de venta, para evitar posibles sanciones fiscales.
