/**
 * Configuracion de WebdriverIO para captura de screenshots de Play Store.
 * Itera sobre multiples locales y captura las 6 pantallas principales.
 *
 * SIN integracion Percy ni reporte Allure — solo spec reporter.
 * Ejecutar: npm run screenshots:store
 */

const path = require('path');

exports.config = {
    // Runner local: los tests corren en esta maquina contra Appium
    runner: 'local',

    // Puerto donde corre el servidor Appium (default: 4723)
    port: 4723,

    // Specs del flujo de captura de pantallas para la Play Store
    specs: ['./tests/store-screenshots/capture.spec.js'],

    // Una instancia a la vez: el cambio de locale requiere reiniciar la app
    maxInstances: 1,

    capabilities: [{
        platformName: 'Android',

        // Driver de automatizacion para Android moderno (API 26+)
        'appium:automationName': 'UiAutomator2',

        // Ruta al APK debug generado por Gradle
        // Ejecutar: ./gradlew assembleDebug antes de correr los tests
        'appium:app': path.resolve(__dirname, '../app/build/outputs/apk/debug/app-debug.apk'),

        // noReset: true -> preserva el estado de la app (consent UMP ya aceptado)
        // El cambio de locale se hace via adb sin necesidad de limpiar datos
        'appium:noReset': true,

        // fullReset: false -> no reinstala el APK en cada sesion (mas rapido)
        'appium:fullReset': false,

        // Timeout generoso para cambios de locale (pueden ser lentos)
        'appium:newCommandTimeout': 300,

        // Timeout de adb extendido para operaciones lentas
        'appium:adbExecTimeout': 30000
    }],

    // Servicio de Appium: WDIO levanta y baja el servidor automaticamente
    services: [
        ['appium', {
            // Timeout generoso para Windows (Appium tarda mas en arrancar)
            startupTimeout: 60000
        }]
    ],

    // Framework de tests: Mocha con BDD (describe/it)
    framework: 'mocha',

    reporters: [
        // Solo spec reporter — sin Allure para mantenerlo simple
        'spec'
    ],

    mochaOpts: {
        ui: 'bdd',
        // 3 minutos: el cambio de locale + reinicio de app puede ser lento
        timeout: 180000
    }
};
