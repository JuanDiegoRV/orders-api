# Predicción previa a DELETE

Antes de implementar la extensión `DELETE /orders/{id}`, se predijo lo siguiente:

- Cambiar `OrderService` para agregar la eliminación por ID.
- Agregar pruebas para eliminación exitosa y orden inexistente.
- Agregar `DELETE /orders/{id}` en API Gateway y `template.yaml`.
- No crear una Lambda nueva.
- Mantener la decisión de existencia en la lógica de aplicación.
- Traducir éxito a `204 No Content` y ausencia a `404 Not Found` en el adapter HTTP.

## Resultado

La implementación coincide con la predicción. `OrderService.deleteOrder` elimina o lanza `OrderNotFoundException`, mientras `OrderApiHandler` convierte esos resultados en `204` y `404`.
