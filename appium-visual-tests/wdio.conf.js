/**
 * Configuracion de WebdriverIO para tests visuales de PrideQuiz.
 * Conecta Appium con el emulador/dispositivo Android y define
 * las capacidades necesarias para ejecutar los tests con Percy.
 */

const path = require('path');

exports.config = {
    // Runner local: los tests corren en esta maquina contra Appium
    runner: 'local',

    // Puerto donde corre el servidor Appium (default: 4723)
    port: 4723,

    // Archivos de spec a ejecutar (todos los .spec.js dentro de tests/)
    specs: ['./tests/**/*.spec.js'],

    // Una instancia a la vez para evitar conflictos en el dispositivo
    maxInstances: 1,

    capabilities: [{
        platformName: 'Android',

        // Driver de automatizacion para Android moderno (API 26+)
        'appium:automationName': 'UiAutomator2',

        // Ruta al APK debug generado por Gradle
        // Ejecutar: ./gradlew assembleDebug antes de correr los tests
        'appium:app': path.resolve(__dirname, '../app/build/outputs/apk/debug/app-debug.apk'),

        // noReset: false -> limpia el estado de la app entre sesiones
        'appium:noReset': false,

        // fullReset: false -> no reinstala el APK en cada sesion (mas rapido)
        'appium:fullReset': false,

        // Timeout para comandos sin actividad (en segundos)
        'appium:newCommandTimeout': 240
    }],

    // Servicio de Appium: WDIO levanta y baja el servidor automaticamente
    services: ['appium'],

    // Framework de tests: Mocha con BDD (describe/it)
    framework: 'mocha',

    reporters: [
        // Reporter en consola durante la ejecucion
        'spec',

        // Reporter de Allure para generar reportes HTML detallados
        ['allure', {
            outputDir: 'allure-results',
            disableWebdriverStepsReporting: true
        }]
    ],

    mochaOpts: {
        ui: 'bdd',
        // Timeout generoso para dar tiempo a que las pantallas carguen
        timeout: 120000
    }
};
