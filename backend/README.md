# Evaluty Backend — Spring Boot

Backend de la aplicación de valoración de inmuebles **Evaluty**.

## Stack
- **Java 21** + Spring Boot 3.2
- **MongoDB** (Spring Data)
- **Spring Security** + JWT stateless
- **OpenPDF** — generación de informes
- **Python** (subprocess) — microservicio de scraping

---

## Estructura del proyecto

```
src/main/java/com/evaluty/
├── config/
│   ├── AppConfig.java              # Beans globales (RestTemplate)
│   ├── SecurityConfig.java         # JWT + CORS
│   └── GlobalExceptionHandler.java # Manejo centralizado de errores
├── controller/
│   ├── AuthController.java         # POST /api/auth/register|login
│   ├── UserController.java         # GET|DELETE /api/users/me
│   ├── ValuationController.java    # POST|GET /api/valoraciones
│   ├── CatastroController.java     # POST /api/catastro/consultar
│   └── ReportController.java       # GET /api/informes/{id}
├── dto/                            # Request / Response objects
├── model/
│   ├── User.java
│   ├── Valoracion.java
│   └── PropiedadComparable.java    # Embebido en Valoracion
├── repository/
│   ├── UserRepository.java
│   └── ValoracionRepository.java
├── security/
│   ├── JwtUtil.java
│   ├── JwtAuthFilter.java
│   └── UserDetailsServiceImpl.java
└── service/
    ├── AuthService.java
    ├── ValuationService.java       # Orquesta bot + catastro + algoritmo
    ├── PythonBotService.java       # Lanza bot_inmobiliario.py como subprocess
    ├── CatastroService.java        # API REST del Catastro español
    └── PdfService.java             # Informe PDF con OpenPDF
```

---

## API Endpoints

| Método | Ruta                       | Auth | Descripción                        |
|--------|----------------------------|------|------------------------------------|
| POST   | `/api/auth/register`       | No   | Registro de usuario                |
| POST   | `/api/auth/login`          | No   | Login → devuelve JWT               |
| GET    | `/api/users/me`            | JWT  | Perfil del usuario                 |
| DELETE | `/api/users/me`            | JWT  | Baja de cuenta                     |
| POST   | `/api/valoraciones`        | JWT  | Solicitar valoración (lanza bot)   |
| GET    | `/api/valoraciones`        | JWT  | Historial de valoraciones          |
| GET    | `/api/valoraciones/{id}`   | JWT  | Detalle de valoración              |
| POST   | `/api/catastro/consultar`  | JWT  | Consultar por nº catastro          |
| GET    | `/api/informes/{id}`       | JWT  | Descargar PDF de valoración        |

---

## Configuración (application.properties)

```properties
# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/evaluty_db

# JWT (CAMBIA EL SECRET EN PRODUCCIÓN)
evaluty.jwt.secret=TuClaveSecretaLarga...
evaluty.jwt.expiration=86400000   # 24h en ms

# Ruta al bot Python
evaluty.python.executable=python
evaluty.python.bot-path=../microservicio-python/bot_inmobiliario.py

# CORS
evaluty.cors.allowed-origins=http://localhost:4200
```

---

## Flujo de valoración

```
Angular ──POST /api/valoraciones──► ValuationController
                                         │
                                    ValuationService
                                    ┌────┴──────────────────┐
                               PythonBotService        CatastroService
                               (subprocess Python)     (API REST Catastro)
                                    │                       │
                               comparables.json         valoracion mínima
                                    └────────────┬──────────┘
                                          algoritmo precio
                                                 │
                                          MongoDB (save)
                                                 │
                                         ValuationResponse ◄── Angular
```

---

## Algoritmo de precio

El precio estimado se calcula mediante **media ponderada del precio/m²** de los comparables:

1. Se filtran outliers (el más caro y más barato se descartan si hay >4 comparables)
2. Se asigna mayor peso a las propiedades con m² más cercanos a la consultada
3. El precio estimado = `precioM2Ponderado × m²Consultados`
4. El rango de confianza = `[precioM2_mín × m², precioM2_máx × m²]`

---

## Ejecución en local

```bash
# 1. MongoDB local
mongod --dbpath /data/db

# 2. Backend
cd evaluty-backend
./mvnw spring-boot:run

# 3. Frontend Angular
cd evaluty-frontend
ng serve
```
