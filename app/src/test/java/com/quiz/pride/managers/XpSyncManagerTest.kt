package com.quiz.pride.managers

import android.content.Context
import arrow.core.Either
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.PlayerStatistics
import com.quiz.domain.XpLeaderboardEntry
import com.quiz.pride.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Tests de XpSyncManager.
 *
 * Se testea la logica de decision de sincronizacion: cuando sincronizar vs marcar
 * como pendiente, el manejo de errores del repositorio, y casos borde como usuario
 * no autenticado o sin nickname configurado.
 *
 * Firebase.auth se mockea con mockkStatic ya que es un acceso estatico.
 * Las dependencias inyectadas (NetworkManager, ProgressionManager, etc.) se mockean
 * con MockK standard.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class XpSyncManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Mocks de dependencias inyectadas
    private val context: Context = mockk(relaxed = true)
    private val progressionManager: ProgressionManager = mockk(relaxed = true)
    private val gameStatsManager: GameStatsManager = mockk(relaxed = true)
    private val xpLeaderboardRepository: com.quiz.data.repository.XpLeaderboardRepository = mockk()
    private val networkManager: NetworkManager = mockk()

    // Mocks de Firebase
    private val firebaseAuth: FirebaseAuth = mockk()
    private val firebaseUser: FirebaseUser = mockk()

    private lateinit var testScope: TestScope
    private lateinit var xpSyncManager: XpSyncManager

    /** Estadisticas base de jugador para los tests. */
    private val statsBase = PlayerStatistics(
        totalGamesPlayed = 10,
        gamesWon = 8,
        totalCorrectAnswers = 80,
        totalWrongAnswers = 20,
        accuracy = 80f,
        bestStreakEver = 5,
        perfectGames = 2,
        totalTimePlayedMs = 120_000L,
        normalGamesPlayed = 5,
        advanceGamesPlayed = 3,
        timedGamesPlayed = 0
    )

    /** Entrada de leaderboard con nickname valido. */
    private val entryConNickname = XpLeaderboardEntry(
        uid = "user_uid_123",
        nickname = "TestPlayer",
        totalXp = 500L,
        level = 5
    )

    /** Entrada de leaderboard sin nickname (blank). */
    private val entrySinNickname = XpLeaderboardEntry(
        uid = "user_uid_123",
        nickname = "",
        totalXp = 500L,
        level = 5
    )

    @Before
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())

        // Mockear Firebase.auth (extension property en com.google.firebase.auth)
        mockkStatic("com.google.firebase.auth.AuthKt")
        every { Firebase.auth } returns firebaseAuth

        xpSyncManager = XpSyncManager(
            context = context,
            progressionManager = progressionManager,
            gameStatsManager = gameStatsManager,
            xpLeaderboardRepository = xpLeaderboardRepository,
            networkManager = networkManager,
            applicationScope = testScope
        )
    }

    @After
    fun tearDown() {
        // Limpiar SOLO el mock estatico que este test declaro. unmockkAll() seria
        // demasiado agresivo: limpiaria mocks globales de otros test classes y
        // provocaria test pollution (UncaughtExceptionsBeforeTest) al ejecutar la suite.
        unmockkStatic("com.google.firebase.auth.AuthKt")
    }

    // =========================================================
    // getCurrentUserId — delega a Firebase.auth
    // =========================================================

    @Test
    fun `getCurrentUserId retorna uid cuando hay usuario autenticado`() {
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test_123"

        val uid = xpSyncManager.getCurrentUserId()

        assertNotNull(uid)
        assertTrue(uid == "uid_test_123")
    }

    @Test
    fun `getCurrentUserId retorna null cuando no hay usuario autenticado`() {
        every { firebaseAuth.currentUser } returns null

        val uid = xpSyncManager.getCurrentUserId()

        assertNull(uid)
    }

    // =========================================================
    // triggerSync — decision segun disponibilidad de red
    // =========================================================

    @Test
    fun `triggerSync no llama al repositorio cuando no hay red disponible`() = runTest {
        every { networkManager.isNetworkAvailable() } returns false
        every { firebaseAuth.currentUser } returns null

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        coVerify(exactly = 0) { xpLeaderboardRepository.syncUserXp(any()) }
    }

    @Test
    fun `triggerSync no llama al repositorio cuando red disponible pero no hay usuario autenticado`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns null

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        coVerify(exactly = 0) { xpLeaderboardRepository.syncUserXp(any()) }
    }

    @Test
    fun `triggerSync no llama al repositorio cuando el usuario no tiene nickname`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(any(), any()) } returns entrySinNickname

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        coVerify(exactly = 0) { xpLeaderboardRepository.syncUserXp(any()) }
    }

    @Test
    fun `triggerSync llama al repositorio cuando hay red y usuario con nickname`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(any(), any()) } returns entryConNickname
        coEvery { xpLeaderboardRepository.syncUserXp(any()) } returns Either.Right(entryConNickname)

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        coVerify(exactly = 1) { xpLeaderboardRepository.syncUserXp(entryConNickname) }
    }

    // =========================================================
    // performSync — manejo de errores del repositorio
    // =========================================================

    @Test
    fun `sync exitoso no lanza excepcion`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(any(), any()) } returns entryConNickname
        coEvery { xpLeaderboardRepository.syncUserXp(any()) } returns Either.Right(entryConNickname)

        // No debe lanzar excepcion
        xpSyncManager.triggerSync()
        advanceUntilIdle()
    }

    @Test
    fun `sync con error del repositorio no lanza excepcion al llamador`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(any(), any()) } returns entryConNickname
        coEvery { xpLeaderboardRepository.syncUserXp(any()) } returns Either.Left(
            RepositoryException.NoConnectionException
        )

        // No debe lanzar excepcion — los errores se manejan internamente con fold
        xpSyncManager.triggerSync()
        advanceUntilIdle()
    }

    @Test
    fun `sync con excepcion no controlada no se propaga al llamador`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } throws RuntimeException("Error inesperado de red")

        // El bloque try-catch de performSync debe absorber la excepcion
        xpSyncManager.triggerSync()
        advanceUntilIdle()
    }

    // =========================================================
    // triggerSync — pasaje de datos al repositorio
    // =========================================================

    @Test
    fun `triggerSync pasa el uid correcto al llamar a getLeaderboardEntry`() = runTest {
        val uid = "uid_especifico_456"
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns uid

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(uid, statsBase) } returns entryConNickname
        coEvery { xpLeaderboardRepository.syncUserXp(any()) } returns Either.Right(entryConNickname)

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        coVerify { progressionManager.getLeaderboardEntry(uid, statsBase) }
    }

    @Test
    fun `triggerSync usa las estadisticas del gameStatsManager para construir la entrada`() = runTest {
        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(any(), statsBase) } returns entryConNickname
        coEvery { xpLeaderboardRepository.syncUserXp(any()) } returns Either.Right(entryConNickname)

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        coVerify { gameStatsManager.getStatistics() }
    }

    // =========================================================
    // Edge cases — nickname con solo espacios
    // =========================================================

    @Test
    fun `triggerSync no sincroniza cuando nickname tiene solo espacios en blanco`() = runTest {
        val entrySoloEspacios = entryConNickname.copy(nickname = "   ")

        every { networkManager.isNetworkAvailable() } returns true
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "uid_test"

        coEvery { gameStatsManager.getStatistics() } returns statsBase
        coEvery { progressionManager.getLeaderboardEntry(any(), any()) } returns entrySoloEspacios

        xpSyncManager.triggerSync()
        advanceUntilIdle()

        // nickname.isBlank() debe detectar el nickname de solo espacios
        coVerify(exactly = 0) { xpLeaderboardRepository.syncUserXp(any()) }
    }
}
