# Group Manager in Pure HTML and RIA

### Grade: 28/30 (Hidden requirement successfully inferred and implemented.)

## Project Description
This project was developed for the **"Web Information Technologies"** course at **Politecnico di Milano**. The application provides a platform for managing and organizing groups using two different implementations: **Pure HTML** and **Rich Internet Application (RIA)**. The project leverages database design and client-server architecture to ensure robust functionality and an intuitive user experience.

The platform allows users to create groups, send invitations, and manage participants dynamically while ensuring consistency through validations at both the client and server sides.

## Project Features
- **User Registration and Login**:
  - SHA-256 hashed password storage for secure authentication.
  - Dynamic validation of form fields during registration.
- **Group Management**:
  - Create groups with defined activity duration and participant limits.
  - Invite users to groups and dynamically validate the invitations.
  - Display and manage group details, including removing participants.
- **Error Handling**:
  - Comprehensive error page for handling invalid data submissions or internal errors.
- **Single-Page RIA Implementation**:
  - Asynchronous operations to update only the affected content for enhanced performance.

## Prerequisites
- Java 8 or later
- Apache Tomcat 9.x
- MySQL Server

## Configuration
Before deploying the application, ensure the database is set up correctly by executing the SQL scripts included in the `/database` folder. Update the database connection properties in the `db.properties` file:
```properties
db.url=jdbc:mysql://localhost:3306/group_manager
db.user=root
db.password=password
```
## Documentation
For detailed information about the project structure, features, and usage, refer to the `documentation` folder in the repository.
