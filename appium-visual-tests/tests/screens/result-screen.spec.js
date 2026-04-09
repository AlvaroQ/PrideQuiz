/**
 * Tests visuales para la pantalla de resultados (ResultScreen).
 * Muestra el puntaje final, XP ganado y opciones de continuar/reintentar.
 *
 * NOTA: En un escenario completo, se deberia completar una partida antes
 * de llegar a esta pantalla. Alternativamente, si la app soporta deep links
 * o argumentos de intent, se puede navegar directamente.
 *
 * Para navegar directamente via intent (Android):
 *   await driver.execute('mobile: deepLink', {
 *     url: 'pridequiz://result?score=5&total=10',
 *     package: 'com.quiz.pride'
 *   });
 *
 * AJUSTAR: adaptar el flujo de navegacion segun la implementacion real.
 */

const percyScreenshot = require('@percy/appium-app');

describe('PrideQuiz - Pantalla de Resultados', () => {

    it('deberia capturar la pantalla de resultados con el puntaje final', async () => {
        // Esperar a que la pantalla de resultados cargue completamente
        // (incluye calculo de XP, actualizacion de ranking, etc.)
        await driver.pause(2000);

        // Captura principal: puntaje, XP y botones de accion visibles
        await percyScreenshot(driver, 'Result Screen - Puntaje Final');
    });
});
