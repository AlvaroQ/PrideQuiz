#!/usr/bin/env kotlin

/**
 * PrideQuiz Screenshot Report Generator
 *
 * Genera un reporte HTML profesional a partir de los screenshots de Roborazzi.
 *
 * Uso:
 *   kotlin scripts/generate-screenshot-report.kts
 *   kotlin scripts/generate-screenshot-report.kts --no-embed
 *
 * Directorios esperados:
 *   - app/build/outputs/roborazzi/   → screenshots grabados + _compare.png de diffs
 *
 * Salida:
 *   - app/build/reports/screenshots/index.html
 *
 * Nota: Roborazzi tambien genera su reporte nativo en:
 *   - app/build/reports/roborazzi/index.html
 */

import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64

// ---------------------------------------------------------------------------
// Configuracion
// ---------------------------------------------------------------------------

val EMBED_IMAGES = !args.contains("--no-embed")

val PROJECT_ROOT = File(System.getProperty("user.dir"))
val ROBORAZZI_DIR = PROJECT_ROOT.resolve("app/build/outputs/roborazzi")
val OUTPUT_DIR = PROJECT_ROOT.resolve("app/build/reports/screenshots")
val OUTPUT_FILE = OUTPUT_DIR.resolve("index.html")

val ROBORAZZI_REPORT_PATH = "app/build/reports/roborazzi/index.html"
val PERCY_DASHBOARD_URL = "https://percy.io/PrideQuiz"
val ROBORAZZI_VERSION = "1.7.0"

// ---------------------------------------------------------------------------
// Modelos de datos
// ---------------------------------------------------------------------------

data class ScreenshotInfo(
    val file: File,
    val className: String,
    val methodName: String,
    val themeVariant: ThemeVariant,
    val hasFailed: Boolean,
    val compareFile: File? = null
)

enum class ThemeVariant(val label: String, val cssClass: String) {
    LIGHT("Light", "theme-light"),
    DARK("Dark", "theme-dark"),
    HIGH_CONTRAST("High Contrast", "theme-high-contrast"),
    UNKNOWN("Default", "theme-default")
}

// ---------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------

fun File.toBase64(): String {
    return Base64.getEncoder().encodeToString(this.readBytes())
}

fun File.toImgSrc(embed: Boolean): String {
    return if (embed) {
        "data:image/png;base64,${this.toBase64()}"
    } else {
        // Ruta relativa desde el directorio de salida al archivo
        this.relativeTo(OUTPUT_DIR).path.replace("\\", "/")
    }
}

fun parseThemeVariant(methodName: String): ThemeVariant {
    val lower = methodName.lowercase()
    return when {
        "highcontrast" in lower || "high_contrast" in lower -> ThemeVariant.HIGH_CONTRAST
        "dark" in lower -> ThemeVariant.DARK
        "light" in lower -> ThemeVariant.LIGHT
        else -> ThemeVariant.UNKNOWN
    }
}

/**
 * Convierte el nombre del metodo (camelCase o snake_case) a un label legible.
 * Ejemplo: "gameScreen_correctAnswer_dark" -> "Game Screen Correct Answer"
 */
fun formatMethodName(methodName: String): String {
    return methodName
        .replace(Regex("_(dark|light|highcontrast|high_contrast)$", RegexOption.IGNORE_CASE), "")
        .replace('_', ' ')
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
        .trim()
}

/**
 * Parsea el nombre de archivo PNG de Roborazzi.
 *
 * Roborazzi usa notacion de puntos completa:
 *   com.quiz.pride.screenshots.ClassName.methodName.png
 *   com.quiz.pride.screenshots.ClassName.methodName_compare.png
 *
 * Estrategia: el paquete base del proyecto es "com.quiz.pride.screenshots".
 * Lo que siga despues de ese prefijo seran ClassName y methodName.
 * Si el archivo no contiene ese prefijo, se usa una heuristica generica:
 *   - Los dos ultimos segmentos separados por punto son className y methodName.
 */
fun parsePngFilename(file: File): Pair<String, String>? {
    val name = file.nameWithoutExtension

    // Quitar el sufijo _compare si existe
    val baseName = name.removeSuffix("_compare")

    // Intentar con el prefijo conocido del proyecto
    val knownPrefix = "com.quiz.pride.screenshots."
    if (baseName.startsWith(knownPrefix)) {
        val afterPrefix = baseName.removePrefix(knownPrefix)
        // afterPrefix tiene la forma "ClassName.methodName"
        val dotIdx = afterPrefix.indexOf('.')
        if (dotIdx != -1) {
            val className = afterPrefix.substring(0, dotIdx)
            val methodName = afterPrefix.substring(dotIdx + 1)
            return Pair(className, methodName)
        }
        // Solo hay ClassName sin metodo
        return Pair(afterPrefix, "default")
    }

    // Heuristica generica: tomar los dos ultimos segmentos
    val parts = baseName.split('.')
    return when {
        parts.size >= 2 -> Pair(parts[parts.size - 2], parts[parts.size - 1])
        parts.size == 1 -> Pair(parts[0], "default")
        else -> null
    }
}

// ---------------------------------------------------------------------------
// Escaneo de archivos
// ---------------------------------------------------------------------------

fun scanScreenshots(): List<ScreenshotInfo> {
    if (!ROBORAZZI_DIR.exists()) {
        println("[INFO] Directorio de Roborazzi no encontrado: $ROBORAZZI_DIR")
        println("[INFO] Ejecuta primero: ./gradlew recordRoborazziDebug")
        return emptyList()
    }

    // Separar los archivos de comparacion (_compare.png) de los screenshots normales
    val allPngs = ROBORAZZI_DIR.walkTopDown()
        .filter { it.isFile && it.extension == "png" }
        .toList()

    val compareFiles = allPngs
        .filter { it.nameWithoutExtension.endsWith("_compare") }
        .associateBy { it.nameWithoutExtension.removeSuffix("_compare") }

    val screenshotFiles = allPngs
        .filter { !it.nameWithoutExtension.endsWith("_compare") }

    return screenshotFiles
        .mapNotNull { file ->
            val parsed = parsePngFilename(file) ?: run {
                println("[WARN] No se pudo parsear el nombre: ${file.name}")
                return@mapNotNull null
            }
            val (className, methodName) = parsed
            val themeVariant = parseThemeVariant(methodName)

            // Buscar el archivo de comparacion correspondiente
            val compareFile = compareFiles[file.nameWithoutExtension]

            ScreenshotInfo(
                file = file,
                className = className,
                methodName = methodName,
                themeVariant = themeVariant,
                hasFailed = compareFile != null,
                compareFile = compareFile
            )
        }
        .sortedWith(compareBy({ it.className }, { it.methodName }))
        .toList()
}

// ---------------------------------------------------------------------------
// Generacion HTML
// ---------------------------------------------------------------------------

fun generateHtml(screenshots: List<ScreenshotInfo>): String {
    val now = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

    val total = screenshots.size
    val failed = screenshots.count { it.hasFailed }
    val passed = total - failed
    val allPassed = failed == 0

    val grouped = screenshots.groupBy { it.className }

    val statusBadgeClass = if (allPassed) "badge-pass" else "badge-fail"
    val statusLabel = if (allPassed) "ALL PASSED" else "$failed FAILED"

    val groupSections = grouped.entries.joinToString("\n") { (className, items) ->
        buildGroupSection(className, items)
    }

    val emptyState = if (screenshots.isEmpty()) """
        <div class="empty-state">
            <div class="empty-icon">&#128247;</div>
            <h2>No se encontraron screenshots</h2>
            <p>Ejecuta los tests de Roborazzi primero:</p>
            <code>./gradlew recordRoborazziDebug</code>
            <p class="empty-hint">Los screenshots se generan en:<br><code>app/build/outputs/roborazzi/</code></p>
        </div>
    """ else ""

    return """<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PrideQuiz Screenshot Report</title>
    <style>
        ${generateCss()}
    </style>
</head>
<body>
    <header class="header">
        <div class="header-content">
            <div class="header-brand">
                <div class="pride-bar"></div>
                <h1 class="header-title">PrideQuiz</h1>
                <span class="header-subtitle">Screenshot Report</span>
            </div>
            <div class="header-meta">
                <span class="badge $statusBadgeClass">$statusLabel</span>
                <div class="meta-stats">
                    <div class="stat">
                        <span class="stat-value">$total</span>
                        <span class="stat-label">Total</span>
                    </div>
                    <div class="stat stat-pass">
                        <span class="stat-value">$passed</span>
                        <span class="stat-label">Passed</span>
                    </div>
                    <div class="stat stat-fail">
                        <span class="stat-value">$failed</span>
                        <span class="stat-label">Failed</span>
                    </div>
                </div>
                <div class="timestamp">Generado: $now</div>
            </div>
        </div>
    </header>

    <main class="main">
        $emptyState
        $groupSections
        ${buildPercySection()}
    </main>

    <footer class="footer">
        <div class="footer-content">
            <span>Generated by <strong>PrideQuiz Screenshot Testing Pipeline</strong></span>
            <span class="footer-sep">&#8226;</span>
            <span>Roborazzi $ROBORAZZI_VERSION</span>
            <span class="footer-sep">&#8226;</span>
            <span>Reporte nativo Roborazzi:
                <a href="../roborazzi/index.html" class="footer-link" target="_blank">
                    app/build/reports/roborazzi/index.html
                </a>
            </span>
            <span class="footer-sep">&#8226;</span>
            <span>$now</span>
        </div>
    </footer>

    <script>
        ${generateJs()}
    </script>
</body>
</html>"""
}

fun buildGroupSection(className: String, items: List<ScreenshotInfo>): String {
    val failCount = items.count { it.hasFailed }
    val groupBadge = if (failCount > 0)
        """<span class="badge badge-fail badge-sm">$failCount failed</span>"""
    else
        """<span class="badge badge-pass badge-sm">all passed</span>"""

    val cards = items.joinToString("\n") { buildCard(it) }

    return """
    <section class="group">
        <button class="group-header" onclick="toggleGroup(this)" aria-expanded="true">
            <div class="group-title-row">
                <span class="group-icon">&#9654;</span>
                <h2 class="group-name">$className</h2>
                $groupBadge
            </div>
            <span class="group-count">${items.size} screenshot${if (items.size != 1) "s" else ""}</span>
        </button>
        <div class="group-body">
            <div class="cards-grid">
                $cards
            </div>
        </div>
    </section>"""
}

fun buildCard(info: ScreenshotInfo): String {
    val label = formatMethodName(info.methodName)
    val statusClass = if (info.hasFailed) "card-failed" else "card-passed"
    val statusBadge = if (info.hasFailed)
        """<span class="badge badge-fail badge-xs">FAIL</span>"""
    else
        """<span class="badge badge-pass badge-xs">PASS</span>"""

    val themeBadge = """<span class="theme-badge ${info.themeVariant.cssClass}">${info.themeVariant.label}</span>"""

    val imageSection = if (info.hasFailed && info.compareFile != null) {
        buildFailedImageSection(info)
    } else {
        val imgSrc = info.file.toImgSrc(EMBED_IMAGES)
        """<div class="card-image-single">
                <img src="$imgSrc" alt="$label" loading="lazy" onclick="openLightbox(this.src, '$label')">
            </div>"""
    }

    return """
        <div class="card $statusClass">
            <div class="card-header">
                <div class="card-title-row">
                    <span class="card-title" title="${info.methodName}">$label</span>
                    $statusBadge
                </div>
                $themeBadge
            </div>
            $imageSection
        </div>"""
}

fun buildFailedImageSection(info: ScreenshotInfo): String {
    val baselineSrc = info.file.toImgSrc(EMBED_IMAGES)
    val compareSrc = info.compareFile!!.toImgSrc(EMBED_IMAGES)
    val label = formatMethodName(info.methodName)

    return """<div class="card-images-diff">
                <div class="diff-column">
                    <span class="diff-label diff-baseline">Baseline</span>
                    <img src="$baselineSrc" alt="Baseline: $label" loading="lazy" onclick="openLightbox(this.src, 'Baseline: $label')">
                </div>
                <div class="diff-column">
                    <span class="diff-label diff-compare">Compare</span>
                    <img src="$compareSrc" alt="Compare: $label" loading="lazy" onclick="openLightbox(this.src, 'Compare: $label')">
                </div>
            </div>"""
}

fun buildPercySection(): String {
    return """
    <section class="percy-section">
        <div class="percy-header">
            <div class="percy-title-row">
                <span class="percy-icon">&#128064;</span>
                <h2 class="percy-title">Appium + Percy</h2>
                <span class="badge badge-info badge-sm">Visual Review</span>
            </div>
            <p class="percy-description">
                Los tests de integracion visual con Appium y Percy se ejecutan en CI
                y generan comparaciones pixel-perfect entre builds.
            </p>
        </div>
        <div class="percy-actions">
            <a href="$PERCY_DASHBOARD_URL" target="_blank" class="percy-btn">
                <span>&#128279;</span>
                Ver Percy Dashboard
            </a>
            <div class="percy-info">
                <code>./gradlew connectedDebugAndroidTest -Dpercytoken=TOKEN</code>
            </div>
        </div>
    </section>"""
}

// ---------------------------------------------------------------------------
// CSS
// ---------------------------------------------------------------------------

fun generateCss(): String = """
        :root {
            --pride-pink: #FF69B4;
            --pride-red: #FF0018;
            --pride-orange: #FFA52C;
            --pride-yellow: #FFFF41;
            --pride-green: #008018;
            --pride-blue: #0000F9;
            --pride-purple: #86007D;

            --accent-from: #FF69B4;
            --accent-via: #9B59B6;
            --accent-to: #3498DB;

            --bg: #F8F9FA;
            --surface: #FFFFFF;
            --surface-2: #F1F3F5;
            --border: #E9ECEF;
            --text-primary: #1A1A2E;
            --text-secondary: #6C757D;
            --text-muted: #ADB5BD;

            --pass-bg: #D4EDDA;
            --pass-text: #155724;
            --pass-border: #C3E6CB;
            --fail-bg: #F8D7DA;
            --fail-text: #721C24;
            --fail-border: #F5C6CB;
            --info-bg: #D1ECF1;
            --info-text: #0C5460;
            --info-border: #BEE5EB;

            --shadow-sm: 0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.06);
            --shadow-md: 0 4px 6px rgba(0,0,0,0.07), 0 2px 4px rgba(0,0,0,0.06);
            --shadow-lg: 0 10px 15px rgba(0,0,0,0.1), 0 4px 6px rgba(0,0,0,0.05);
            --radius: 12px;
            --radius-sm: 6px;
        }

        @media (prefers-color-scheme: dark) {
            :root {
                --bg: #0D1117;
                --surface: #161B22;
                --surface-2: #21262D;
                --border: #30363D;
                --text-primary: #E6EDF3;
                --text-secondary: #8B949E;
                --text-muted: #484F58;

                --pass-bg: #1B4A2A;
                --pass-text: #7EE2A8;
                --pass-border: #2A6B3E;
                --fail-bg: #4A1B1B;
                --fail-text: #F85149;
                --fail-border: #6B2A2A;
                --info-bg: #0D2E35;
                --info-text: #7ECFDF;
                --info-border: #1A4A58;

                --shadow-sm: 0 1px 3px rgba(0,0,0,0.3);
                --shadow-md: 0 4px 6px rgba(0,0,0,0.25);
                --shadow-lg: 0 10px 15px rgba(0,0,0,0.35);
            }
        }

        *, *::before, *::after {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background: var(--bg);
            color: var(--text-primary);
            line-height: 1.5;
            min-height: 100vh;
        }

        /* Header */
        .header {
            background: var(--surface);
            border-bottom: 1px solid var(--border);
            box-shadow: var(--shadow-sm);
            position: sticky;
            top: 0;
            z-index: 100;
        }

        .header-content {
            max-width: 1400px;
            margin: 0 auto;
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 16px;
            flex-wrap: wrap;
        }

        .header-brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .pride-bar {
            width: 4px;
            height: 40px;
            border-radius: 2px;
            background: linear-gradient(to bottom,
                var(--pride-red),
                var(--pride-orange),
                var(--pride-yellow),
                var(--pride-green),
                var(--pride-blue),
                var(--pride-purple)
            );
        }

        .header-title {
            font-size: 1.5rem;
            font-weight: 700;
            background: linear-gradient(135deg, var(--accent-from), var(--accent-via), var(--accent-to));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }

        .header-subtitle {
            font-size: 0.875rem;
            color: var(--text-secondary);
            font-weight: 500;
        }

        .header-meta {
            display: flex;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }

        .meta-stats {
            display: flex;
            gap: 16px;
        }

        .stat {
            text-align: center;
        }

        .stat-value {
            display: block;
            font-size: 1.25rem;
            font-weight: 700;
            color: var(--text-primary);
        }

        .stat-label {
            display: block;
            font-size: 0.6875rem;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            color: var(--text-muted);
        }

        .stat-pass .stat-value { color: #2E7D32; }
        .stat-fail .stat-value { color: #C62828; }

        @media (prefers-color-scheme: dark) {
            .stat-pass .stat-value { color: #7EE2A8; }
            .stat-fail .stat-value { color: #F85149; }
        }

        .timestamp {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        /* Badges */
        .badge {
            display: inline-flex;
            align-items: center;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 0.75rem;
            font-weight: 600;
            letter-spacing: 0.03em;
            text-transform: uppercase;
            border: 1px solid transparent;
        }

        .badge-pass {
            background: var(--pass-bg);
            color: var(--pass-text);
            border-color: var(--pass-border);
        }

        .badge-fail {
            background: var(--fail-bg);
            color: var(--fail-text);
            border-color: var(--fail-border);
        }

        .badge-info {
            background: var(--info-bg);
            color: var(--info-text);
            border-color: var(--info-border);
        }

        .badge-sm {
            font-size: 0.6875rem;
            padding: 2px 8px;
        }

        .badge-xs {
            font-size: 0.625rem;
            padding: 2px 6px;
        }

        /* Main */
        .main {
            max-width: 1400px;
            margin: 0 auto;
            padding: 24px;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        /* Empty state */
        .empty-state {
            text-align: center;
            padding: 80px 24px;
            color: var(--text-secondary);
        }

        .empty-icon {
            font-size: 4rem;
            margin-bottom: 16px;
        }

        .empty-state h2 {
            font-size: 1.5rem;
            margin-bottom: 8px;
            color: var(--text-primary);
        }

        .empty-state p {
            margin-bottom: 12px;
        }

        .empty-hint {
            font-size: 0.8125rem;
            color: var(--text-muted);
        }

        .empty-state code {
            background: var(--surface-2);
            border: 1px solid var(--border);
            padding: 8px 16px;
            border-radius: var(--radius-sm);
            font-family: "SF Mono", "Fira Code", monospace;
            font-size: 0.875rem;
            display: inline-block;
        }

        /* Groups */
        .group {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            box-shadow: var(--shadow-sm);
            overflow: hidden;
        }

        .group-header {
            width: 100%;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 16px 20px;
            background: none;
            border: none;
            cursor: pointer;
            color: var(--text-primary);
            text-align: left;
            transition: background 0.15s;
            gap: 12px;
        }

        .group-header:hover {
            background: var(--surface-2);
        }

        .group-title-row {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .group-icon {
            font-size: 0.6875rem;
            color: var(--text-muted);
            transition: transform 0.2s;
            display: inline-block;
        }

        .group-header[aria-expanded="false"] .group-icon {
            transform: rotate(-90deg);
        }

        .group-name {
            font-size: 1rem;
            font-weight: 600;
        }

        .group-count {
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        .group-body {
            padding: 0 20px 20px;
            border-top: 1px solid var(--border);
        }

        .group-body.collapsed {
            display: none;
        }

        /* Cards grid */
        .cards-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 16px;
            padding-top: 16px;
        }

        /* Card */
        .card {
            background: var(--surface-2);
            border: 1px solid var(--border);
            border-radius: var(--radius-sm);
            overflow: hidden;
            box-shadow: var(--shadow-sm);
            transition: box-shadow 0.2s, transform 0.2s;
        }

        .card:hover {
            box-shadow: var(--shadow-md);
            transform: translateY(-2px);
        }

        .card-failed {
            border-color: var(--fail-border);
        }

        .card-header {
            padding: 10px 12px 8px;
            border-bottom: 1px solid var(--border);
        }

        .card-title-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 8px;
            margin-bottom: 6px;
        }

        .card-title {
            font-size: 0.8125rem;
            font-weight: 500;
            color: var(--text-primary);
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .theme-badge {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            font-size: 0.6875rem;
            font-weight: 500;
            padding: 2px 8px;
            border-radius: 4px;
        }

        .theme-badge::before {
            content: '';
            width: 8px;
            height: 8px;
            border-radius: 50%;
            display: inline-block;
        }

        .theme-light {
            background: #FFF9E6;
            color: #856404;
            border: 1px solid #FFE69C;
        }
        .theme-light::before { background: #FFC107; }

        .theme-dark {
            background: #E8E8FF;
            color: #3730A3;
            border: 1px solid #C7D2FE;
        }
        .theme-dark::before { background: #4F46E5; }

        .theme-high-contrast {
            background: #FCE4EC;
            color: #880E4F;
            border: 1px solid #F48FB1;
        }
        .theme-high-contrast::before { background: #E91E63; }

        .theme-default {
            background: var(--surface);
            color: var(--text-secondary);
            border: 1px solid var(--border);
        }
        .theme-default::before { background: var(--text-muted); }

        @media (prefers-color-scheme: dark) {
            .theme-light { background: #3D3000; color: #FFD54F; border-color: #5C4600; }
            .theme-dark { background: #1E1B4B; color: #A5B4FC; border-color: #3730A3; }
            .theme-high-contrast { background: #3B0A1F; color: #F48FB1; border-color: #880E4F; }
        }

        .card-image-single img,
        .card-images-diff img {
            width: 100%;
            display: block;
            cursor: zoom-in;
            transition: opacity 0.2s;
        }

        .card-image-single img:hover,
        .card-images-diff img:hover {
            opacity: 0.9;
        }

        .card-images-diff {
            display: grid;
            grid-template-columns: 1fr 1fr;
        }

        .diff-column {
            position: relative;
            border-top: 1px solid var(--border);
        }

        .diff-column + .diff-column {
            border-left: 1px solid var(--border);
        }

        .diff-label {
            display: block;
            font-size: 0.625rem;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            padding: 3px 6px;
            text-align: center;
        }

        .diff-baseline {
            background: var(--pass-bg);
            color: var(--pass-text);
        }

        .diff-compare {
            background: var(--fail-bg);
            color: var(--fail-text);
        }

        /* Percy Section */
        .percy-section {
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: var(--radius);
            box-shadow: var(--shadow-sm);
            overflow: hidden;
            padding: 20px;
        }

        .percy-header {
            margin-bottom: 16px;
        }

        .percy-title-row {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 8px;
        }

        .percy-icon {
            font-size: 1.25rem;
        }

        .percy-title {
            font-size: 1rem;
            font-weight: 600;
            color: var(--text-primary);
        }

        .percy-description {
            font-size: 0.8125rem;
            color: var(--text-secondary);
            max-width: 600px;
        }

        .percy-actions {
            display: flex;
            align-items: center;
            gap: 16px;
            flex-wrap: wrap;
        }

        .percy-btn {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 8px 16px;
            background: linear-gradient(135deg, var(--accent-from), var(--accent-via), var(--accent-to));
            color: #fff;
            text-decoration: none;
            border-radius: var(--radius-sm);
            font-size: 0.875rem;
            font-weight: 600;
            transition: opacity 0.15s, transform 0.15s;
            box-shadow: var(--shadow-sm);
        }

        .percy-btn:hover {
            opacity: 0.9;
            transform: translateY(-1px);
        }

        .percy-info code {
            background: var(--surface-2);
            border: 1px solid var(--border);
            padding: 6px 12px;
            border-radius: var(--radius-sm);
            font-family: "SF Mono", "Fira Code", monospace;
            font-size: 0.8125rem;
            color: var(--text-secondary);
        }

        /* Footer */
        .footer {
            background: var(--surface);
            border-top: 1px solid var(--border);
            margin-top: 40px;
        }

        .footer-content {
            max-width: 1400px;
            margin: 0 auto;
            padding: 16px 24px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            flex-wrap: wrap;
            font-size: 0.75rem;
            color: var(--text-muted);
        }

        .footer-sep { opacity: 0.4; }

        .footer-link {
            color: var(--accent-via);
            text-decoration: none;
        }
        .footer-link:hover {
            text-decoration: underline;
        }

        /* Lightbox */
        .lightbox {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(0,0,0,0.9);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            gap: 12px;
            cursor: zoom-out;
        }

        .lightbox.open {
            display: flex;
        }

        .lightbox img {
            max-width: 90vw;
            max-height: 85vh;
            object-fit: contain;
            border-radius: var(--radius-sm);
            box-shadow: var(--shadow-lg);
        }

        .lightbox-label {
            color: #fff;
            font-size: 0.875rem;
            opacity: 0.8;
        }

        .lightbox-close {
            position: absolute;
            top: 16px;
            right: 20px;
            background: none;
            border: none;
            color: #fff;
            font-size: 2rem;
            cursor: pointer;
            opacity: 0.7;
            transition: opacity 0.15s;
            line-height: 1;
        }

        .lightbox-close:hover { opacity: 1; }

        /* Responsive */
        @media (max-width: 600px) {
            .header-content { flex-direction: column; align-items: flex-start; }
            .header-meta { width: 100%; justify-content: space-between; }
            .main { padding: 16px; }
            .cards-grid { grid-template-columns: 1fr; }
            .percy-actions { flex-direction: column; align-items: flex-start; }
        }
"""

// ---------------------------------------------------------------------------
// JavaScript
// ---------------------------------------------------------------------------

fun generateJs(): String = """
        function toggleGroup(btn) {
            const expanded = btn.getAttribute('aria-expanded') === 'true';
            btn.setAttribute('aria-expanded', String(!expanded));
            const body = btn.nextElementSibling;
            body.classList.toggle('collapsed', expanded);
        }

        function openLightbox(src, label) {
            const lb = document.getElementById('lightbox');
            document.getElementById('lightbox-img').src = src;
            document.getElementById('lightbox-label').textContent = label;
            lb.classList.add('open');
        }

        function closeLightbox() {
            document.getElementById('lightbox').classList.remove('open');
        }

        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') closeLightbox();
        });

        // Insertar lightbox en el DOM
        (function() {
            const lb = document.createElement('div');
            lb.id = 'lightbox';
            lb.className = 'lightbox';
            lb.innerHTML = '<button class="lightbox-close" onclick="closeLightbox()" aria-label="Cerrar">&#215;</button>' +
                '<img id="lightbox-img" src="" alt="">' +
                '<span id="lightbox-label" class="lightbox-label"></span>';
            lb.addEventListener('click', function(e) {
                if (e.target === lb) closeLightbox();
            });
            document.body.appendChild(lb);
        })();
"""

// ---------------------------------------------------------------------------
// Punto de entrada
// ---------------------------------------------------------------------------

fun main() {
    println("=".repeat(60))
    println("  PrideQuiz Screenshot Report Generator")
    println("  (Roborazzi Edition)")
    println("=".repeat(60))
    println("  Modo:       ${if (EMBED_IMAGES) "imagenes embebidas (base64)" else "rutas relativas (--no-embed)"}")
    println("  Roborazzi:  $ROBORAZZI_DIR")
    println("  Salida:     $OUTPUT_FILE")
    println("=".repeat(60))

    println("\n[1/3] Escaneando screenshots de Roborazzi...")
    val screenshots = scanScreenshots()

    if (screenshots.isEmpty()) {
        println("[WARN] No se encontraron screenshots.")
        println("[WARN] Ejecuta: ./gradlew recordRoborazziDebug")
        println("[WARN] El reporte mostrara estado vacio.")
    } else {
        val failed = screenshots.count { it.hasFailed }
        println("  Encontrados: ${screenshots.size} screenshots")
        println("  Pasaron:     ${screenshots.size - failed}")
        println("  Fallaron:    $failed (con _compare.png)")

        val groups = screenshots.groupBy { it.className }
        println("  Grupos:      ${groups.keys.joinToString(", ")}")
    }

    println("\n[2/3] Generando HTML...")
    val html = generateHtml(screenshots)

    println("\n[3/3] Escribiendo archivo...")
    OUTPUT_DIR.mkdirs()
    OUTPUT_FILE.writeText(html, Charsets.UTF_8)

    val sizeKb = OUTPUT_FILE.length() / 1024
    println("\n  Reporte generado exitosamente.")
    println("  Archivo:  $OUTPUT_FILE")
    println("  Tamano:   ${sizeKb}KB")
    println("\n  Abrir en navegador:")
    println("  file://${OUTPUT_FILE.absolutePath.replace("\\", "/")}")
    println("\n  Reporte nativo Roborazzi:")
    println("  ${PROJECT_ROOT.resolve(ROBORAZZI_REPORT_PATH).absolutePath.replace("\\", "/")}")
    println()
}

main()
