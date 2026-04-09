package com.quiz.pride.screenshots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.quiz.pride.ui.components.LoadingIndicator
import com.quiz.pride.ui.theme.PrideQuizTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tests de screenshot Roborazzi para comparar las 4 variantes de tema de PrideQuiz.
 *
 * Variantes cubiertas:
 * 1. Light (default)
 * 2. Dark
 * 3. High Contrast Light
 * 4. High Contrast Dark
 * 5. Large Text (accesibilidad)
 *
 * Se usa una "tema sample card" consistente en todos los tests para que la
 * comparacion entre variantes sea directa y significativa.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel5, application = com.quiz.pride.TestApplication::class)
class ThemeScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ============================================================
    // Comparacion de las 4 variantes de tema
    // ============================================================

    @Test
    fun temaMuestra_light() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = false,
                dynamicColor = false,
                highContrast = false
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun temaMuestra_dark() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = true,
                dynamicColor = false,
                highContrast = false
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun temaMuestra_highContrastLight() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = false,
                dynamicColor = false,
                highContrast = true
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun temaMuestra_highContrastDark() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = true,
                dynamicColor = false,
                highContrast = true
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // Variante de texto grande (accesibilidad)
    // ============================================================

    @Test
    fun temaMuestra_largeText_light() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = false,
                dynamicColor = false,
                highContrast = false,
                largeText = true
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun temaMuestra_largeText_dark() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = true,
                dynamicColor = false,
                highContrast = false,
                largeText = true
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun temaMuestra_largeText_highContrastLight() {
        composeTestRule.setContent {
            PrideQuizTheme(
                darkTheme = false,
                dynamicColor = false,
                highContrast = true,
                largeText = true
            ) {
                TemaMuestraSurface()
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    // ============================================================
    // LoadingIndicator en todas las variantes
    // ============================================================

    @Test
    fun loadingIndicator_todasLasVariantes_light() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false) {
                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingIndicator()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun loadingIndicator_todasLasVariantes_dark() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingIndicator()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun loadingIndicator_todasLasVariantes_highContrastLight() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = false, dynamicColor = false, highContrast = true) {
                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingIndicator()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }

    @Test
    fun loadingIndicator_todasLasVariantes_highContrastDark() {
        composeTestRule.setContent {
            PrideQuizTheme(darkTheme = true, dynamicColor = false, highContrast = true) {
                Surface(
                    modifier = Modifier
                        .width(200.dp)
                        .height(200.dp),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadingIndicator()
                }
            }
        }
        composeTestRule.onRoot().captureRoboImage()
    }
}

// ============================================================
// Composable de muestra de tema (privado a este archivo)
// ============================================================

/**
 * Componente de muestra que expone los tokens de tema relevantes para
 * verificar visualmente cada variante: colores primarios, secundarios,
 * superficies, textos y botones.
 *
 * Sirve como "golden sample" para comparar las 4 variantes del tema.
 */
@Composable
private fun TemaMuestraSurface() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Titulo
            Text(
                text = "Pride Quiz",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Probando variantes del tema",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tarjeta de colores del esquema
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EtiquetaColor(
                        label = "Primary",
                        value = "Titulo / Acento principal",
                        labelColor = MaterialTheme.colorScheme.primary,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EtiquetaColor(
                        label = "Secondary",
                        value = "Acento secundario",
                        labelColor = MaterialTheme.colorScheme.secondary,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EtiquetaColor(
                        label = "Tertiary",
                        value = "Acento terciario",
                        labelColor = MaterialTheme.colorScheme.tertiary,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EtiquetaColor(
                        label = "Error",
                        value = "Estado de error",
                        labelColor = MaterialTheme.colorScheme.error,
                        valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Escala tipografica
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Display Large",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 24.sp, // Reducido para caber en pantalla
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Title Medium",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Body Large — Texto de contenido",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Label Small — Etiqueta pequeña",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Jugar", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "Ranking")
                }
            }

            // Tarjeta de estadistica de muestra
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tu mejor puntaje",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "2.450 pts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun EtiquetaColor(
    label: String,
    value: String,
    labelColor: androidx.compose.ui.graphics.Color,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = labelColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}
