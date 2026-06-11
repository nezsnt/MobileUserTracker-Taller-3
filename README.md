# MobileUserTracker - Taller 3

Aplicación móvil Android desarrollada para el curso de Computación Móvil. El proyecto integra servicios de Firebase y herramientas de geolocalización para realizar seguimiento de usuarios y visualizar información basada en ubicación en tiempo real.

## Características

- Registro de usuarios.
- Inicio de sesión mediante Firebase Authentication.
- Gestión de usuarios.
- Seguimiento de ubicación en tiempo real.
- Visualización de puntos de interés (POI).
- Integración con Google Maps.
- Notificaciones mediante Firebase Cloud Messaging (FCM).
- Almacenamiento y sincronización de datos en tiempo real.
- Integración con Firebase Cloud Functions.

## Tecnologías Utilizadas

- Kotlin
- Android Studio
- Jetpack Compose
- Firebase Authentication
- Firebase Realtime Database
- Firebase Cloud Messaging (FCM)
- Firebase Cloud Functions
- Google Maps API
- Material Design

## Estructura del Proyecto

```text
app/
├── screens/
├── navigation/
├── model/
├── services/
├── view/
└── ui/

functions/
└── Firebase Cloud Functions
```

## Requisitos

- Android Studio
- JDK 17 o superior
- Dispositivo Android o emulador
- Proyecto configurado en Firebase
- API Key de Google Maps

## Configuración

### Firebase

Por motivos de seguridad, el archivo `google-services.json` no se incluye en este repositorio.

Para ejecutar el proyecto:

1. Crear un proyecto en Firebase.
2. Registrar la aplicación Android.
3. Descargar el archivo `google-services.json`.
4. Colocarlo dentro de la carpeta:

```text
app/
```

### Google Maps

El proyecto requiere una API Key de Google Maps.

Agregar la clave en el archivo:

```properties
local.properties
```

Ejemplo:

```properties
MAPS_API_KEY=TU_API_KEY
```

## Instalación

1. Clonar el repositorio:

```bash
git clone https://github.com/nezsnt/MobileUserTracker-Taller-3.git
```

2. Abrir el proyecto en Android Studio.

3. Configurar Firebase.

4. Configurar la API Key de Google Maps.

5. Sincronizar Gradle.

6. Ejecutar la aplicación.

## Archivos excluidos del repositorio

Por motivos de seguridad, los siguientes archivos no se encuentran incluidos:

- `app/google-services.json`
- Configuración local de `local.properties`

Para ejecutar el proyecto es necesario configurarlos manualmente.

## Autores

- Santiago Ibáñez
- David Calerón Idárraga
