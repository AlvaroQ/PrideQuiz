package com.quiz.data.repository

import arrow.core.Either
import com.quiz.data.datasource.FirestoreDataSource
import com.quiz.domain.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RankingRepositoryImplTest {

    private lateinit var repository: RankingRepository
    private val firestoreDataSource: FirestoreDataSource = mockk()

    private fun buildUser(name: String, score: Int = 100) = User(
        uid = "uid_$name",
        name = name,
        score = score,
        userImage = "",
        timestamp = 0L
    )

    @Before
    fun setup() {
        repository = RankingRepositoryImpl(firestoreDataSource)
    }

    // =========================================================
    // addRecord (modo Normal)
    // =========================================================

    @Test
    fun `addRecord retorna Right con User cuando datasource tiene exito`() = runTest {
        val user = buildUser("TestUser", 500)
        coEvery { firestoreDataSource.addRecord(user) } returns Either.Right(user)

        val result = repository.addRecord(user)

        assertTrue(result.isRight())
        assertEquals(user, (result as Either.Right).value)
    }

    @Test
    fun `addRecord retorna Left cuando datasource falla`() = runTest {
        val user = buildUser("TestUser")
        coEvery { firestoreDataSource.addRecord(user) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = repository.addRecord(user)

        assertTrue(result.isLeft())
        assertEquals(RepositoryException.NoConnectionException, (result as Either.Left).value)
    }

    @Test
    fun `addRecord invalida cache de ranking normal`() = runTest {
        val user = buildUser("TestUser")
        val rankingList = listOf(buildUser("User1"), buildUser("User2"))
        coEvery { firestoreDataSource.getRanking() } returns Either.Right(rankingList)
        coEvery { firestoreDataSource.addRecord(user) } returns Either.Right(user)

        // Primer fetch — guarda en cache
        repository.getRanking()
        coVerify(exactly = 1) { firestoreDataSource.getRanking() }

        // Agregar record invalida la cache
        repository.addRecord(user)

        // Siguiente fetch debe ir al datasource de nuevo
        repository.getRanking()
        coVerify(exactly = 2) { firestoreDataSource.getRanking() }
    }

    // =========================================================
    // getRanking (modo Normal) — logica de cache con TTL
    // =========================================================

    @Test
    fun `getRanking retorna Right con lista cuando datasource tiene exito`() = runTest {
        val rankingList = listOf(buildUser("User1", 300), buildUser("User2", 200))
        coEvery { firestoreDataSource.getRanking() } returns Either.Right(rankingList)

        val result = repository.getRanking()

        assertTrue(result.isRight())
        assertEquals(2, (result as Either.Right).value.size)
    }

    @Test
    fun `getRanking retorna Left cuando datasource falla`() = runTest {
        coEvery { firestoreDataSource.getRanking() } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = repository.getRanking()

        assertTrue(result.isLeft())
        assertEquals(RepositoryException.DataNotFoundException, (result as Either.Left).value)
    }

    @Test
    fun `getRanking cachea resultado y no vuelve a llamar datasource`() = runTest {
        val rankingList = listOf(buildUser("User1"))
        coEvery { firestoreDataSource.getRanking() } returns Either.Right(rankingList)

        repository.getRanking()
        repository.getRanking()

        coVerify(exactly = 1) { firestoreDataSource.getRanking() }
    }

    @Test
    fun `getRanking no cachea errores — reintenta en siguiente llamada`() = runTest {
        coEvery { firestoreDataSource.getRanking() } returns Either.Left(RepositoryException.NoConnectionException)
        repository.getRanking() // Primera llamada falla

        coEvery { firestoreDataSource.getRanking() } returns Either.Right(listOf(buildUser("User1")))
        val result = repository.getRanking() // Segunda llamada debe reintentar

        assertTrue(result.isRight())
        coVerify(exactly = 2) { firestoreDataSource.getRanking() }
    }

    @Test
    fun `getRanking con lista vacia retorna Right vacio y cachea`() = runTest {
        coEvery { firestoreDataSource.getRanking() } returns Either.Right(emptyList())

        val result = repository.getRanking()

        assertTrue(result.isRight())
        assertTrue((result as Either.Right).value.isEmpty())

        repository.getRanking()
        coVerify(exactly = 1) { firestoreDataSource.getRanking() }
    }

    // =========================================================
    // getWorldRecords (modo Normal)
    // =========================================================

    @Test
    fun `getWorldRecords retorna Right con datos cuando datasource tiene exito`() = runTest {
        coEvery { firestoreDataSource.getWorldRecords(10, "") } returns Either.Right("1000")

        val result = repository.getWorldRecords(10)

        assertTrue(result.isRight())
        assertEquals("1000", (result as Either.Right).value)
    }

    @Test
    fun `getWorldRecords retorna Left cuando datasource falla`() = runTest {
        coEvery { firestoreDataSource.getWorldRecords(10, "") } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = repository.getWorldRecords(10)

        assertTrue(result.isLeft())
    }

    @Test
    fun `getWorldRecords delega limit exacto al datasource`() = runTest {
        coEvery { firestoreDataSource.getWorldRecords(5, "") } returns Either.Right("500")

        repository.getWorldRecords(5)

        coVerify { firestoreDataSource.getWorldRecords(5, "") }
    }

    @Test
    fun `getWorldRecords con gameMode filtra por modo correctamente`() = runTest {
        coEvery { firestoreDataSource.getWorldRecords(20, "NORMAL") } returns Either.Right("750")

        val result = repository.getWorldRecords(20, "NORMAL")

        assertTrue(result.isRight())
        assertEquals("750", (result as Either.Right).value)
        coVerify { firestoreDataSource.getWorldRecords(20, "NORMAL") }
    }

    @Test
    fun `getWorldRecords sin gameMode no filtra — backward compatible`() = runTest {
        coEvery { firestoreDataSource.getWorldRecords(50, "") } returns Either.Right("400")

        val result = repository.getWorldRecords(50, "")

        assertTrue(result.isRight())
        coVerify { firestoreDataSource.getWorldRecords(50, "") }
    }

    // =========================================================
    // addTimedRecord (modo Timed)
    // =========================================================

    @Test
    fun `addTimedRecord retorna Right con User cuando datasource tiene exito`() = runTest {
        val user = buildUser("TimedUser", 999)
        coEvery { firestoreDataSource.addTimedRecord(user) } returns Either.Right(user)

        val result = repository.addTimedRecord(user)

        assertTrue(result.isRight())
        assertEquals(user, (result as Either.Right).value)
    }

    @Test
    fun `addTimedRecord retorna Left cuando datasource falla`() = runTest {
        val user = buildUser("TimedUser")
        coEvery { firestoreDataSource.addTimedRecord(user) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = repository.addTimedRecord(user)

        assertTrue(result.isLeft())
    }

    @Test
    fun `addTimedRecord invalida cache de timed ranking`() = runTest {
        val user = buildUser("TimedUser")
        val rankingList = listOf(buildUser("Timed1"), buildUser("Timed2"))
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Right(rankingList)
        coEvery { firestoreDataSource.addTimedRecord(user) } returns Either.Right(user)

        // Primer fetch — guarda en cache
        repository.getTimedRanking()
        coVerify(exactly = 1) { firestoreDataSource.getTimedRanking() }

        // Agregar record invalida la cache
        repository.addTimedRecord(user)

        // Siguiente fetch debe ir al datasource de nuevo
        repository.getTimedRanking()
        coVerify(exactly = 2) { firestoreDataSource.getTimedRanking() }
    }

    // =========================================================
    // getTimedRanking (modo Timed) — logica de cache con TTL
    // =========================================================

    @Test
    fun `getTimedRanking retorna Right con lista cuando datasource tiene exito`() = runTest {
        val rankingList = listOf(buildUser("Timed1", 120), buildUser("Timed2", 90))
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Right(rankingList)

        val result = repository.getTimedRanking()

        assertTrue(result.isRight())
        assertEquals(2, (result as Either.Right).value.size)
    }

    @Test
    fun `getTimedRanking retorna Left cuando datasource falla`() = runTest {
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = repository.getTimedRanking()

        assertTrue(result.isLeft())
        assertEquals(RepositoryException.DataNotFoundException, (result as Either.Left).value)
    }

    @Test
    fun `getTimedRanking cachea resultado y no vuelve a llamar datasource`() = runTest {
        val rankingList = listOf(buildUser("Timed1"))
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Right(rankingList)

        repository.getTimedRanking()
        repository.getTimedRanking()

        coVerify(exactly = 1) { firestoreDataSource.getTimedRanking() }
    }

    @Test
    fun `getTimedRanking no cachea errores — reintenta en siguiente llamada`() = runTest {
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Left(RepositoryException.NoConnectionException)
        repository.getTimedRanking() // Primera llamada falla

        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Right(listOf(buildUser("Timed1")))
        val result = repository.getTimedRanking() // Segunda llamada debe reintentar

        assertTrue(result.isRight())
        coVerify(exactly = 2) { firestoreDataSource.getTimedRanking() }
    }

    @Test
    fun `getTimedRanking con lista vacia retorna Right vacio y cachea`() = runTest {
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Right(emptyList())

        val result = repository.getTimedRanking()

        assertTrue(result.isRight())
        assertTrue((result as Either.Right).value.isEmpty())

        repository.getTimedRanking()
        coVerify(exactly = 1) { firestoreDataSource.getTimedRanking() }
    }

    // =========================================================
    // getTimedWorldRecords (modo Timed)
    // =========================================================

    @Test
    fun `getTimedWorldRecords retorna Right con datos cuando datasource tiene exito`() = runTest {
        coEvery { firestoreDataSource.getTimedWorldRecords(10) } returns Either.Right("45")

        val result = repository.getTimedWorldRecords(10)

        assertTrue(result.isRight())
        assertEquals("45", (result as Either.Right).value)
    }

    @Test
    fun `getTimedWorldRecords retorna Left cuando datasource falla`() = runTest {
        coEvery { firestoreDataSource.getTimedWorldRecords(10) } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = repository.getTimedWorldRecords(10)

        assertTrue(result.isLeft())
    }

    @Test
    fun `getTimedWorldRecords delega limit exacto al datasource`() = runTest {
        coEvery { firestoreDataSource.getTimedWorldRecords(3) } returns Either.Right("30")

        repository.getTimedWorldRecords(3)

        coVerify { firestoreDataSource.getTimedWorldRecords(3) }
    }

    // =========================================================
    // Aislamiento de caches — Normal vs Timed son independientes
    // =========================================================

    @Test
    fun `cache de normal y timed son independientes`() = runTest {
        val normalList = listOf(buildUser("NormalUser"))
        val timedList = listOf(buildUser("TimedUser"))
        coEvery { firestoreDataSource.getRanking() } returns Either.Right(normalList)
        coEvery { firestoreDataSource.getTimedRanking() } returns Either.Right(timedList)

        repository.getRanking()
        repository.getTimedRanking()

        // Ambos caches deben ser independientes
        repository.getRanking()
        repository.getTimedRanking()

        coVerify(exactly = 1) { firestoreDataSource.getRanking() }
        coVerify(exactly = 1) { firestoreDataSource.getTimedRanking() }
    }
}
