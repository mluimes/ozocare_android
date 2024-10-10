# OzoCare - Android

## Descripción

Este repositorio contiene el código de la aplicación **Android** del proyecto OzoCare. La aplicación permite a los usuarios enviar mediciones ambientales como la temperatura y la concentración de gases, comunicándose con el backend mediante una API REST. Estas mediciones son tomadas por un sensor en una placa Sparkfun. Para recibirlas en esta app, interceptamos el beacon BT que envia la sparkfun y cogemos las medidas de ahi.

### Funcionalidades:
- Enviar mediciones al backend a través de una API REST.
- Visualizar respuestas y datos de mediciones almacenadas.

## Despliegue

### Requisitos previos:
- **Android Studio** instalado.
- Dispositivo físico o emulador Android.

### Pasos para desplegar:

1. Clonar el repositorio:
    ```bash
    git clone https://github.com/usuario/ozocare-android.git
    cd ozocare-android
    ```

2. Abrir el proyecto en **Android Studio**.

3. Configurar la URL del backend:
    - En el archivo `res/values/strings.xml`, modifica la URL con la dirección del servidor backend:
      ```xml
      <string name="backend_url">http://192.168.x.x:3000/api/v1</string>
      ```
4. Configurar el nombre del dispositivo:
    - En MainActivity.java debes cambiar el nombre que se busca para interceptar los beacons

4. Compilar y ejecutar la aplicación en un emulador o dispositivo físico.

### Comunicación con el backend
- **POST /api/v1/medidas**: Enviar nuevas mediciones al servidor.