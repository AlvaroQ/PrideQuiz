/**
 * Tests visuales para la pantalla de juego (GameScreen).
 * Cubre el estado con pregunta cargada y el estado post-respuesta.
 *
 * NOTA: Los selectores con '~' usan content description de Compose.
 * Verificar los contentDescription reales en GameScreen.kt y ajustar
 * los valores de los selectores segun corresponda.
 *
 * Flujo esperado:
 *   SelectScreen -> [tap modo Normal] -> SelectGameScreen -> [tap dificultad] -> GameScreen
 */

const percyScreenshot = require('@percy/appium-app');

describe('PrideQuiz - Pantalla de Juego', () => {

    it('deberia capturar la pantalla de juego con pregunta cargada', async () => {
        // Intentar navegar al modo Normal usando content description
        // AJUSTAR: reemplazar 'Normal Mode' con el contentDescription real del boton
        const normalMode = await driver.$('~Normal Mode');

        if (await normalMode.isExisting()) {
            await normalMode.click();
        } else {
            // Fallback: buscar por texto visible si no hay contentDescription
            const normalModeByText = await driver.$('android=new UiSelector().textContains("Normal")');
            if (await normalModeByText.isExisting()) {
                await normalModeByText.click();
            }
        }

        // Esperar a que cargue la pregunta desde Firebase
        await driver.pause(2000);

        // Captura con la pregunta visible
        await percyScreenshot(driver, 'Game Screen - Pregunta Cargada');
    });

    it('deberia capturar la pantalla de juego despues de seleccionar una respuesta', async () => {
        // Obtener todos los elementos de texto para encontrar las opciones de respuesta
        // AJUSTAR: usar el contentDescription real de las opciones de respuesta en GameScreen.kt
        const opcionesRespuesta = await driver.$$('android=new UiSelector().className("android.widget.TextView")');

        if (opcionesRespuesta.length > 0) {
            // Tocar la primera opcion disponible
            await opcionesRespuesta[0].click();
        }

        // Esperar animacion de feedback (correcto/incorrecto)
        await driver.pause(1000);

        // Captura mostrando el feedback visual de la respuesta seleccionada
        await percyScreenshot(driver, 'Game Screen - Respuesta Seleccionada');
    });
});
