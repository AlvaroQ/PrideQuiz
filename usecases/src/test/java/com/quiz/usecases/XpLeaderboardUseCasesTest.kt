package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.RepositoryException
import com.quiz.data.repository.XpLeaderboardRepository
import com.quiz.domain.XpLeaderboardEntry
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class XpLeaderboardUseCasesTest {

    private val repository: XpLeaderboardRepository = mockk()

    private lateinit var syncUserXp: SyncUserXp
    private lateinit var getXpLeaderboard: GetXpLeaderboard
    private lateinit var getUserGlobalRank: GetUserGlobalRank
    private lateinit var getUserXpEntry: GetUserXpEntry

    private fun buildEntry(
        uid: String = "uid-1",
        nickname: String = "Player1",
        xp: Long = 500L,
        level: Int = 5
    ) = XpLeaderboardEntry(
        uid = uid,
        nickname = nickname,
        imageBase64 = "",
        totalXp = xp,
        level = level,
        title = "Explorer",
        totalGamesPlayed = 10,
        accuracy = 75f,
        lastUpdated = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        syncUserXp = SyncUserXp(repository)
        getXpLeaderboard = GetXpLeaderboard(repository)
        getUserGlobalRank = GetUserGlobalRank(repository)
        getUserXpEntry = GetUserXpEntry(repository)
    }

    // =========================================================
    // SyncUserXp
    // =========================================================

    @Test
    fun `SyncUserXp retorna Either Right cuando el repository tiene exito`() = runTest {
        val entry = buildEntry()
        coEvery { repository.syncUserXp(entry) } returns Either.Right(entry)

        val result = syncUserXp.invoke(entry)

        assertTrue(result.isRight())
        assertEquals(entry, (result as Either.Right).value)
    }

    @Test
    fun `SyncUserXp retorna Either Left cuando el repository falla por falta de conexion`() = runTest {
        val entry = buildEntry()
        coEvery { repository.syncUserXp(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = syncUserXp.invoke(entry)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.NoConnectionException)
    }

    @Test
    fun `SyncUserXp retorna Either Left cuando el repository falla con DataNotFoundException`() = runTest {
        val entry = buildEntry()
        coEvery { repository.syncUserXp(any()) } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = syncUserXp.invoke(entry)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.DataNotFoundException)
    }

    @Test
    fun `SyncUserXp delega exactamente al repository con la entrada correcta`() = runTest {
        val entry = buildEntry(uid = "uid-especifico", nickname = "NickEspecifico")
        coEvery { repository.syncUserXp(entry) } returns Either.Right(entry)

        syncUserXp.invoke(entry)

        coVerify(exactly = 1) { repository.syncUserXp(entry) }
    }

    @Test
    fun `SyncUserXp retorna la entrada actualizada del servidor`() = runTest {
        val localEntry = buildEntry(xp = 500L, level = 5)
        val serverEntry = buildEntry(xp = 600L, level = 6) // Servidor devuelve XP actualizado
        coEvery { repository.syncUserXp(localEntry) } returns Either.Right(serverEntry)

        val result = syncUserXp.invoke(localEntry)

        assertTrue(result.isRight())
        assertEquals(600L, (result as Either.Right).value.totalXp)
        assertEquals(6, result.value.level)
    }

    // =========================================================
    // GetXpLeaderboard
    // =========================================================

    @Test
    fun `GetXpLeaderboard retorna lista de entradas del repository`() = runTest {
        val entries = listOf(
            buildEntry(uid = "uid-1", nickname = "Alice", xp = 1000L),
            buildEntry(uid = "uid-2", nickname = "Bob", xp = 800L),
            buildEntry(uid = "uid-3", nickname = "Charlie", xp = 600L)
        )
        coEvery { repository.getXpLeaderboard(100) } returns entries

        val result = getXpLeaderboard.invoke()

        assertEquals(3, result.size)
        assertEquals("Alice", result[0].nickname)
    }

    @Test
    fun `GetXpLeaderboard retorna lista vacia cuando no hay entradas`() = runTest {
        coEvery { repository.getXpLeaderboard(any()) } returns emptyList()

        val result = getXpLeaderboard.invoke()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `GetXpLeaderboard delega al repository con el limite por defecto de 100`() = runTest {
        coEvery { repository.getXpLeaderboard(100) } returns emptyList()

        getXpLeaderboard.invoke()

        coVerify(exactly = 1) { repository.getXpLeaderboard(100) }
    }

    @Test
    fun `GetXpLeaderboard delega al repository con el limite personalizado`() = runTest {
        coEvery { repository.getXpLeaderboard(25) } returns emptyList()

        getXpLeaderboard.invoke(limit = 25)

        coVerify(exactly = 1) { repository.getXpLeaderboard(25) }
    }

    @Test
    fun `GetXpLeaderboard retorna exactamente el numero de entradas solicitadas`() = runTest {
        val top10 = (1..10).map { buildEntry(uid = "uid-$it", xp = (1000L - it * 50)) }
        coEvery { repository.getXpLeaderboard(10) } returns top10

        val result = getXpLeaderboard.invoke(limit = 10)

        assertEquals(10, result.size)
    }

    // =========================================================
    // GetUserGlobalRank
    // =========================================================

    @Test
    fun `GetUserGlobalRank retorna Either Right con el rango cuando el repository tiene exito`() = runTest {
        coEvery { repository.getUserRank("uid-1", 500L) } returns Either.Right(42)

        val result = getUserGlobalRank.invoke("uid-1", 500L)

        assertTrue(result.isRight())
        assertEquals(42, (result as Either.Right).value)
    }

    @Test
    fun `GetUserGlobalRank retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getUserRank(any(), any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = getUserGlobalRank.invoke("uid-1", 100L)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.NoConnectionException)
    }

    @Test
    fun `GetUserGlobalRank delega al repository con uid y xp correctos`() = runTest {
        coEvery { repository.getUserRank("uid-especifico", 1500L) } returns Either.Right(7)

        getUserGlobalRank.invoke("uid-especifico", 1500L)

        coVerify(exactly = 1) { repository.getUserRank("uid-especifico", 1500L) }
    }

    @Test
    fun `GetUserGlobalRank retorna rango 1 para el usuario en primera posicion`() = runTest {
        coEvery { repository.getUserRank("top-player", 9999L) } returns Either.Right(1)

        val result = getUserGlobalRank.invoke("top-player", 9999L)

        assertTrue(result.isRight())
        assertEquals(1, (result as Either.Right).value)
    }

    @Test
    fun `GetUserGlobalRank retorna Either Left con DataNotFoundException cuando el usuario no existe`() = runTest {
        coEvery { repository.getUserRank("unknown-uid", any()) } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = getUserGlobalRank.invoke("unknown-uid", 0L)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.DataNotFoundException)
    }

    // =========================================================
    // GetUserXpEntry
    // =========================================================

    @Test
    fun `GetUserXpEntry retorna Either Right con la entrada cuando el usuario existe`() = runTest {
        val entry = buildEntry(uid = "uid-1")
        coEvery { repository.getUserXpEntry("uid-1") } returns Either.Right(entry)

        val result = getUserXpEntry.invoke("uid-1")

        assertTrue(result.isRight())
        assertEquals(entry, (result as Either.Right).value)
    }

    @Test
    fun `GetUserXpEntry retorna Either Right con null cuando el usuario no tiene entrada`() = runTest {
        coEvery { repository.getUserXpEntry("uid-nuevo") } returns Either.Right(null)

        val result = getUserXpEntry.invoke("uid-nuevo")

        assertTrue(result.isRight())
        assertNull((result as Either.Right).value)
    }

    @Test
    fun `GetUserXpEntry retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getUserXpEntry(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = getUserXpEntry.invoke("uid-1")

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.NoConnectionException)
    }

    @Test
    fun `GetUserXpEntry delega al repository con el uid correcto`() = runTest {
        coEvery { repository.getUserXpEntry("uid-correcto") } returns Either.Right(buildEntry())

        getUserXpEntry.invoke("uid-correcto")

        coVerify(exactly = 1) { repository.getUserXpEntry("uid-correcto") }
    }

    @Test
    fun `GetUserXpEntry retorna los datos completos del usuario sin modificaciones`() = runTest {
        val expectedEntry = XpLeaderboardEntry(
            uid = "uid-test",
            nickname = "TestNick",
            imageBase64 = "base64data",
            totalXp = 750L,
            level = 8,
            title = "Enthusiast",
            totalGamesPlayed = 25,
            accuracy = 85f
        )
        coEvery { repository.getUserXpEntry("uid-test") } returns Either.Right(expectedEntry)

        val result = getUserXpEntry.invoke("uid-test")

        assertTrue(result.isRight())
        val entry = (result as Either.Right).value
        assertEquals("TestNick", entry?.nickname)
        assertEquals(750L, entry?.totalXp)
        assertEquals(8, entry?.level)
        assertEquals(85f, entry?.accuracy)
    }
}
