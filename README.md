# 🏦 Sistema de Simulación Bancaria - "Banco Digital"

Bienvenido al **Simulador Bancario**, una aplicación de escritorio de alto nivel desarrollada en **Java 21** y **JavaFX**. Este proyecto integra una arquitectura robusta (DAO/Service) con un diseño centrado en el usuario basado en las **Heurísticas de Nielsen** y los **Principios de Norman**.

---

## 🚀 Características por Rol

### 👤 Módulo del Cliente
Diseñado para ofrecer autonomía y claridad financiera:
* **Dashboard de Cuentas:** Visualización clara de saldos y tipos de cuenta.
* **Operaciones Transaccionales:** Lógica para **Retiros, Depósitos y Transferencias** con validaciones de saldo en tiempo real.
* **Perfil Gestionable:** Permite al usuario ver su información legal y modificar datos de contacto o seguridad (contraseña).
* **Reportería Profesional:** Generación de estados de cuenta en formato **PDF** con historial completo de movimientos.

### 🛠️ Módulo del Administrador
Herramientas de control y supervisión del sistema:
* **Gestión de Usuarios:** Activación y bloqueo de clientes de forma instantánea.
* **Registro Manual:** Capacidad de dar de alta nuevos clientes con validaciones de integridad.
* **Auditoría:** Visualización detallada de la información técnica y personal de cada cliente.

---

## 🎨 Ingeniería de Usabilidad (UX/UI)

El sistema no solo es funcional, sino que aplica principios de **Interacción Humano-Computadora (HCI)**:

1.  **Visibilidad del Estado (H1):** Indicadores de carga (`ProgressIndicator`) y barras de seguridad que informan al usuario qué está ocurriendo.
2.  **Prevención de Errores (H5):** Botones de acción que permanecen deshabilitados hasta que los formularios cumplen con los requisitos (ej. fuerza de contraseña).
3.  **Feedback Dinámico (Norman):** Listeners en tiempo real que cambian el color de la interfaz (Verde/Amarillo/Rojo) según la validez de los datos.
4.  **Mapeo y Affordance:** Diseño de "Tarjetas" (Cards) para organizar la información y cursores reactivos que indican elementos clickeables.



---

## 🛠️ Stack Tecnológico

* **Lenguaje:** Java 21 (Toolchain configurada).
* **Interfaz:** JavaFX 21 con FXML y CSS personalizado.
* **Gestor de Dependencias:** Gradle.
* **Base de Datos:** MySQL (Connector J 8.0.33).
* **Librería de PDF:** iText7 (Kernel, Layout, IO).

---

## 🔬 Detalles de Implementación Técnica

### 🛡️ Seguridad de Contraseñas
Se implementó un algoritmo de evaluación de entropía que analiza:
* Longitud mínima (8 caracteres).
* Presencia de Mayúsculas, Números y Caracteres Especiales.
* **Feedback:** El sistema bloquea el avance del registro hasta que la contraseña se considera "Segura" (`fuerza > 0.7`).



### 📄 Generación de Reportes PDF
El sistema exporta documentos dinámicos utilizando `PdfWriter` de iText7:
* **Rutas Dinámicas:** Los reportes se guardan automáticamente en la carpeta `Downloads` del usuario mediante `System.getProperty("user.home")`.
* **Diseño Institucional:** Uso de colores corporativos, tablas con efecto cebra y metadatos de generación.

### 💸 Lógica Transaccional
Las transferencias operan bajo el principio de **Atomicidad**:
1. Validación de cuenta origen y saldo suficiente.
2. Apertura de ventana modal (`APPLICATION_MODAL`) para evitar estados inconsistentes.
3. Refresco automático de la vista principal al finalizar la operación.



---

## 📂 Configuración del Proyecto

### Requisitos
* JDK 21
* Servidor MySQL activo

### Ejecución con Gradle
```bash
# Ejecutar la aplicación
./gradlew run

# Limpiar y construir el proyecto
./gradlew clean build

# Generar el archivo ejecutable
./gradlew assemble