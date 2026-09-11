# Capítulo 2. Tecnologías

## 2.1. Introducción

La elección de las tecnologías condiciona el diseño de la aplicación de forma
permanente. En este capítulo se justifican las tres decisiones principales:
**Spring Boot 3 / Java 21** como stack de servidor, **PostgreSQL 16** como
SGBD y **Docker Compose** como orquestación local de despliegue. Se
contraponen brevemente con las tecnologías de origen (Laravel, PHP, MySQL)
ya que el propio trabajo consiste en una migración controlada entre ambos
mundos.

## 2.2. Spring Boot 3 y Java 21

Spring Boot 3 es un framework de Java construido sobre Spring Framework,
pensado para acelerar el desarrollo de aplicaciones de servicio HTTP con
configuración mínima. Los motivos de su adopción sobre el stack original
son:

- **Tipado estáico en Java 21.** Detecta errores de refactorización y uso
  de API en tiempo de compilación; el uso de *records* para DTO elimina el
  código de plumb de getters/setters heredado de POJOs PHP.
- **Ecosistema de pruebas.** JUnit 5, MockMvc, y Testcontainers permiten
  pruebas de integración contra bases de datos reales sin mocks de persistencia.
- **Seguridad declarativa.** Spring Security permite separar la definición de
  " qué puede hacer cada rol " de la implementación de los checks, algo
  que en Laravel se resolvía con middlewares procedurales.
- **Empaquetado.** El *fat JAR* (Spring Boot executable jar) simplifica el
  despliegue en contenedor: un único binario autocontenido.

Versiones concretas de la pila: Spring Boot 3.5.x, Jakarta EE 9+ (`jakarta.*`),
jjwt 0.12.x para JWT, springdoc-openapi 2.9.x para la documentación de API.

## 2.3. PostgreSQL 16

PostgreSQL es un SGBD relacional orientado a objetos que, frente a MySQL,
ofrece características que CookFlow explota directamente:

- **Tipos `ENUM` nativos.** Estados de pedido (`open`, `in_kitchen`,
  `closed`, ...) como tipo enumerado a nivel de columna, con constraint de
  integridad a nivel de base de datos y no solo a nivel de aplicación.
- **`CHECK` y `UNIQUE PARTIAL`.** Útiles para invariantes como "un email
  único por tenant" mediante constraints `UNIQUE ... WHERE tenant_id IS NOT NULL`.
- **JSONB.** Disponible para extender atributos de ingredientes o pedidos sin
  migraciones adicionales.
- **Madurez de réplicas y tooling.** `pg_isready`, `psql`, y un ecosistema
  JDBC (`org.postgresql`) muy probado.
  
El driver JDBC de PostgreSQL (`org.postgresql:postgresql`) se integra de
forma nativa en Spring Data JPA, sin adaptadores intermedios.

## 2.4. Docker y Docker Compose

Docker encapsula el entorno de ejecución del backend (JRE 21, dependencias,
configuración) en una imagen reproducible. Docker Compose orquesta el
conjunto backend + PostgreSQL declarativamente en `docker-compose.yml`:
un único punto de verdad para el despliegue local, con healthchecks que
ordenan el arranque y permiten el reinicio automático de servicios.

La estrategia de imagen es deliberadamente simple: **imagen de corrida**
(temurin JRE-alpine) sobre la que se copia el JAR ya construido. Esto
sacrifica el "build desde fuente" a cambio de construir la imagen en segundos
y de poder actualizar el artefacto sin recompilar Java dentro del
contenedor.

## 2.5. Frontend (fuera de alcance)

Existe un frontend en la raíz del proyecto (`frontend/`) que consome la API
REST. No se describe aquí ya que la memoria se centra en el backend, pero es
la contraparte de consumo de la API documentada en `docs/api.md`.

## 2.6. Conclusiones de la selección

La combinación Spring Boot + PostgreSQL + Docker Compose ofrece un trade-off
favorable para un proyecto de tamaño medio como CookFlow: tipado estático,
pruebas sobre infraestructura real, constraints de integridad en el SGBD y
despliegue reproducible. El costo de la migración desde Laravel/MySQL se
justifica por estos mismos motivos: menos dependencias de convenciones
procedurales de PHP y mayor expresividad del motor de datos.
