/**
 * Tests visuales para la pantalla de seleccion de modo de juego (SelectScreen).
 * Es el punto de entrada principal de la app despues del onboarding.
 *
 * NOTA: Los selectores (~'...') usan content description de Compose.
 * Ajustar los valores segun los Modifier.semantics { contentDescription = "..." }
 * definidos en SelectScreen.kt.
 */

const percyScreenshot = require('@percy/appium-app');

describe('PrideQuiz - Pantalla de Seleccion', () => {

    it('deberia capturar la pantalla de seleccion en estado inicial', async () => {
        // Esperar a que la app cargue completamente
        // (splash screen + carga inicial de datos desde Firebase)
        await driver.pause(3000);

        // Captura de referencia: estado inicial sin interaccion
        await percyScreenshot(driver, 'Select Screen - Estado Inicial');
    });

    it('deberia capturar la pantalla de seleccion despues de hacer scroll', async () => {
        // Obtener dimensiones de la ventana para calcular coordenadas de scroll
        const { width, height } = await driver.getWindowSize();

        // Gesto de scroll hacia arriba (swipe de abajo hacia arriba)
        await driver.touchAction([
            { action: 'press', x: width / 2, y: height * 0.7 },
            { action: 'moveTo', x: width / 2, y: height * 0.3 },
            'release'
        ]);

        // Esperar a que el scroll termine y el contenido se estabilice
        await driver.pause(1000);

        // Captura con el contenido desplazado
        await percyScreenshot(driver, 'Select Screen - Con Scroll');
    });
});
