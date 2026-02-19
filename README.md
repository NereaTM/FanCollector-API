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

## Requisitos previos
Antes de ejecutar el proyecto, asegúrate de tener instalado:
- **Java 21** (JDK) [Descargar aquí](https://www.oracle.com/java/technologies/downloads/#java21)
- **Docker Desktop** [Descargar aquí](https://www.docker.com/products/docker-desktop/)
- **Maven 3.8+** [Descargar aquí](https://maven.apache.org/download.cgi)
- **Git** (Si quieres clonar el repositorio)
- **Cliente de base de datos** (opcional solo si quieres visualizar la bd)

## Estructura del proyecto
- **/config**: Configuración (ModelMapper)
- **/controller**: Controladores REST y rutas HTTP
- **/domain**: Entidades JPA
- **/dto**: Entrada/salida de datos
- **/exception**: Excepciones personalizadas
- **/repository**: Repositorios JPA
- **/security**: Configuración JWT y autenticación
- **/service**: Lógica de negocio 
- **/util**: Utilidades

### Endpoints
- **Autenticación** (`/auth`)
- **Usuarios** (`/usuarios`)
- **Colecciones** (`/colecciones`)
- **Items** (`/items`)
- **Usuario-Item** (`/usuario-items`)
- **Usuario-Colección** (`/usuario-colecciones`)

## Instalación y arranque

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/NereaTM/FanCollector.git
   cd fancollector
   ```

2. **Crear archivo `.env`** en la raíz del proyecto:
   ```env
   MARIADB_USER=fancollector_user
   MARIADB_PASSWORD=tu_password
   MARIADB_DATABASE=fancollector
   MARIADB_ROOT_PASSWORD=root_password
   ```

3. **Levantar la base de datos**
   ```bash
   docker-compose up -d
   ```
 _Nota: No necesitas crear la base de datos manualmente. Docker Compose la crea automáticamente al levantar el contenedor_

4. **Ejecutar la aplicación**
   ```bash
   mvn spring-boot:run
   ```

La API estará disponible en `http://localhost:8080`

## Autenticación
La API utiliza JWT para autenticación
1. **Te registras**
2. **Te logeas y devuelve un token** 
3. **Usar el token**: Incluir en header `Authorization: Bearer {token}`

### Roles disponibles
- **ADMIN**: Poder absoluto (público y privado). Único que puede crear usuarios y asignar roles
- **MODS**: Permisos de USER + editar/borrar contenido público
- **USER**: Ver público, crear y gestionar sus propias colecciones/items, modificar perfil propio
- **NO-AUTH**: Crear usuario nuevo y ver colecciones públicas

## Detener el proyecto

```bash
# Detener la aplicación: Ctrl+C

# Detener Docker:
docker-compose down
```

## Colección de Postman
[Ver Postman](https://github.com/NereaTM/FanCollector/tree/develop/docs/README.md)

---
Proyecto escolar de DAM Curso 2025–2026
