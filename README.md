# FanCollector 
API diseñada para gestionar colecciones de items coleccionables (figuras, cartas, merchandising, etc.).

## Tecnologías utilizadas
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=Spring-Security&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=JSON%20web%20tokens&logoColor=white)
![Cloudinary](https://img.shields.io/badge/Cloudinary-3448C5?style=for-the-badge&logo=Cloudinary&logoColor=white)
![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)

## Requisitos previos
Antes de ejecutar el proyecto, asegúrate de tener instalado:
- **Java 21** (JDK) [Descargar aquí](https://www.oracle.com/java/technologies/downloads/#java21)
- **Docker Desktop** [Descargar aquí](https://www.docker.com/products/docker-desktop/)
- **Maven 3.8+** [Descargar aquí](https://maven.apache.org/download.cgi)
- **Git** (Si quieres clonar el repositorio)
- **Cliente de base de datos** (opcional solo si quieres visualizar la bd)
- **Make** — en Windows instalar via Chocolatey: `choco install make` (no es obligatorio, puedes lanzar los comandos directamente desde la consola en vez de usar el Makefile)

## Estructura del proyecto
- **/config**: Configuración (ModelMapper)
- **/controller**: Controladores REST y rutas HTTP
- **/domain**: Entidades JPA
- **/dto**: Entrada/salida de datos
- **/exception**: Excepciones personalizadas
- **/repository**: Repositorios JPA
- **/security**: Configuración JWT y autenticación
- **/service**: Lógica de negocio 
- **/util**: Utilidades y lógica de imágenes

### Endpoints
- **Autenticación** (`/auth`)
- **Usuarios** (`/usuarios`)
- **Colecciones** (`/colecciones`)
- **Items** (`/items`)
- **Usuario-Item** (`/usuario-items`)
- **Usuario-Coleccion** (`/usuario-colecciones`)

## Instalación y arranque

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/NereaTM/FanCollector.git
   cd fancollector
   ```

2. **Crear el archivo `application-dev.properties`** en `src/main/resources/`.  
   Este archivo está en `.gitignore` y no se incluye en el repositorio porque contiene credenciales locales. Puedes usar `application-prod.properties` como referencia de la estructura, sustituyendo las variables por los valores directamente:
   ```properties
   spring.datasource.url=jdbc:mariadb://localhost:3307/fancollector_dev?serverTimezone=UTC
   spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
   spring.datasource.username=fancollector_user
   spring.datasource.password=tu_password

   spring.jpa.hibernate.ddl-auto=update

   jwt.secret=una_clave_larga_minimo_32_caracteres
   jwt.expiration=86400

   cloudinary.cloud-name=tu_cloud_name
   cloudinary.api-key=tu_api_key
   cloudinary.api-secret=tu_api_secret
   cloudinary.folder=fancollector-desarrollo
   ```

3. **Crear archivo `.env`** en la raíz del proyecto.

`.env.dev`:
   ```env
   MARIADB_USER=fancollector_user
   MARIADB_PASSWORD=tu_password
   MARIADB_DATABASE=fancollector
   MARIADB_ROOT_PASSWORD=root_password
   ```
   `.env.prod`:
   ```env
   # DB
   MARIADB_DATABASE=fancollector
   MARIADB_ROOT_PASSWORD=tu_root_password
   MARIADB_USER=fancollector_user
   MARIADB_PASSWORD=tu_password

   # JWT
   JWT_SECRET=una_clave_larga_minimo_32_caracteres
   JWT_EXPIRATION=86400

   # IMAGEN
   CLOUDINARY_CLOUD_NAME=tu_cloud_name
   CLOUDINARY_API_KEY=tu_api_key
   CLOUDINARY_API_SECRET=tu_api_secret
   CLOUDINARY_FOLDER=fancollector
   ```

### Entornos

### Desarrollo
Levanta la BD en Docker y arranca la API desde el IDE con el perfil `dev`. En IntelliJ: Run > Edit... Configurations > Active profiles: `dev`.

_No necesitas crear la base de datos manualmente, Docker Compose la crea automáticamente._
   ```bash
   make dev          # levanta la BD
   make stop-dev     # para la BD
   ```
O sin Makefile:
   ```bash
   docker compose --env-file .env.dev -f docker-compose.dev.yaml up -d
   docker compose -f docker-compose.dev.yaml down
   ```

### Producción
Levanta la BD y la API juntas en Docker. Requiere `.env.prod` en la raíz.
   ```bash
   make prod         # levanta todo
   make stop-prod    # para todo
   ```
O sin Makefile:
   ```bash
   docker compose --env-file .env.prod up --build -d
   docker compose down
   ```

La API estará disponible en `http://localhost:8080`

## Autenticación
La API utiliza JWT para autenticación
1. **Te registras**
2. **Te logeas y devuelve un token** 
3. **Usar el token**: Incluir en header `Authorization: Bearer {token}`

### Roles disponibles
- **ADMIN**: Poder absoluto (público y privado). Único que puede crear usuarios y asignar roles, te lo tienes que asignar desde la base de datos
- **MODS**: Permisos de USER + editar/borrar contenido público
- **USER**: Ver público, crear y gestionar sus propias colecciones/items, modificar perfil propio
- **NO-AUTH**: Crear usuario nuevo y ver colecciones públicas


## Colección de Postman
[Ver Postman](https://github.com/NereaTM/FanCollector/tree/develop/docs/README.md)

---
Proyecto escolar de DAM Curso 2025–2026
