# Documentación de la API

Este directorio contiene la documentación necesaria para entender y probar la API del proyecto

## OpenAPI
`openapi.yaml` define el contrato de la API siguiendo la especificación OpenAPI
Se describen los endpoints disponibles, los parámetros de entrada y las respuestas esperadas


## Postman
En la carpeta `postman/` se incluye una colección de Postman para probar la API de forma manual con la herramienta postman 
- V1.0.0: sin autenticación 
- v2.0.0: con seguridad (autenticación JWT)
- v3.0.0: entono de producción
- Test-Coleccion: suite de tests de integración ejecutada automáticamente en CI con Newman

### Lanzar los tests manualmente
```bash
npm install -g newman
newman run postman/Test-Coleccion.postman_collection.json --env-var "base_url=http://localhost:8080"
```
 
