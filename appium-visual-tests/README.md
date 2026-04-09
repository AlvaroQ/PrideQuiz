# PrideQuiz - Tests de Regresion Visual (Appium + Percy)

Tests de regresion visual que ejecutan la app real en un emulador/dispositivo
Android y toman capturas gestionadas por Percy (BrowserStack) para detectar
cambios visuales no intencionales entre builds.

Este proyecto es **independiente** de los tests JVM con Roborazzi.

## Requisitos previos

- Node.js 18+ y npm
- Java 17+ (para Appium)
- Android SDK y un emulador/dispositivo conectado con API >= 26
- Cuenta en [Percy (BrowserStack)](https://percy.io) — el plan gratuito es suficiente para empezar

## Setup

### 1. Instalar dependencias

```bash
cd appium-visual-tests
npm install
```

### 2. Instalar el driver UiAutomator2 de Appium

```bash
npx appium driver install uiautomator2
```

### 3. Configurar el token de Percy

```bash
export PERCY_TOKEN=tu_token_aqui
```

Obtener el token desde: Percy Dashboard > Settings > Project Token

### 4. Compilar el APK debug

```bash
# Desde la raiz del proyecto Android
cd ..
./gradlew assembleDebug
```

El APK se genera en: `app/build/outputs/apk/debug/app-debug.apk`

### 5. Iniciar el servidor Appium

```bash
npx appium
```

## Ejecutar los tests

### Con Percy (regresion visual — registra y compara capturas)

```bash
npm run test:percy
```

### Sin Percy (ejecucion local — solo verifica que los tests no rompen)

```bash
npm run test:local
```

### Con script npm directamente (sin percy exec)

```bash
npm test
```

## Ver reportes

### Percy (capturas visuales y diffs)

Ir al dashboard de Percy: https://percy.io

Cada ejecucion con `npm run test:percy` sube las capturas y las compara
contra el baseline aprobado.

### Allure (reporte HTML de ejecucion)

```bash
# Generar el reporte
npx allure generate allure-results --clean -o allure-report

# Abrir en el navegador
npx allure open allure-report
```

## Estructura del proyecto

```
appium-visual-tests/
├── tests/
│   └── screens/
│       ├── select-screen.spec.js   # Pantalla principal de seleccion de modo
│       ├── game-screen.spec.js     # Pantalla de juego con pregunta
│       └── result-screen.spec.js  # Pantalla de resultados y puntaje
├── wdio.conf.js                    # Configuracion de WebdriverIO + Appium
├── .percyrc                        # Configuracion de Percy
├── package.json
└── README.md
```

## Ajustar los selectores

Los selectores de los tests usan `content description` de Compose (`~'nombre'`).
Para que funcionen, agregar `Modifier.semantics { contentDescription = "..." }` en
los composables de las pantallas correspondientes, o usar selectores por texto/clase.

Ejemplo en Compose:

```kotlin
Button(
    onClick = { /* ... */ },
    modifier = Modifier.semantics { contentDescription = "Normal Mode" }
) {
    Text("Normal")
}
```
