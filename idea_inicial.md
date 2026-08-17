 Contexto del Proyecto
Estás construyendo una aplicación para que los usuarios registren y gestionen información de sus autos. Cada usuario puede tener uno o varios autos registrados. El sistema debe incluir autenticación, gestión de usuarios y autos, y buenas prácticas de desarrollo.
 
 Requisitos Funcionales
 Autenticación
•	Registro de usuario (BD).
•	Login con JWT.
•	Acceso a funciones solo con token válido.
Gestión de Autos
•	CRUD de autos para el usuario autenticado:
o	Crear auto con: marca, modelo, año, número de placa, color.
o	Listar los autos.
o	Editar los datos de un auto.
o	Eliminar un auto.
 
Requisitos Técnicos
 Backend (Spring Boot)
•	API REST con endpoints bien organizados.
•	Autenticación con JWT (Spring Security).
•	Conexión a SQL Server con JPA/Hibernate.
Frontend (React)
•	inicio de sesión.
•	Pantalla de autos: listar, agregar, editar, eliminar.
•	Almacenamiento del token JWT.
Base de Datos (SQL Server)
•	Script de creación de tablas:
o	Tabla users
o	Tabla cars (con user_id como llave foránea)
•	Opcional: datos precargados (algunos autos).
 
Plus (Opcional)
•	Búsqueda por placa o modelo.
•	Filtrado por año o marca.
•	Subida de foto del auto (solo campo simulado, no funcional).
•	Responsive design en frontend.
 
Entregables
•	Código fuente completo (backend y frontend).
•	Opcional: colección Postman.

