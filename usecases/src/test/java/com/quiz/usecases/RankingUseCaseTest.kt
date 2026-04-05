package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.RankingRepository
import com.quiz.data.repository.RepositoryException
import com.quiz.domain.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RankingUseCaseTest {

    private val repository: RankingRepository = mockk()

    private lateinit var getRankingScore: GetRankingScore
    private lateinit var getRecordScore: GetRecordScore
    private lateinit var saveTopScore: SaveTopScore

    private fun buildUser(name: String, score: Int) = User(
        name = name,
        score = score,
        userImage = "",
        timestamp = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        getRankingScore = GetRankingScore(repository)
        getRecordScore = GetRecordScore(repository)
        saveTopScore = SaveTopScore(repository)
    }

    // =========================================================
    // GetRankingScore — NORMAL
    // =========================================================

    @Test
    fun `GetRankingScore NORMAL retorna Either Right con lista de usuarios`() = runTest {
        val users = listOf(buildUser("Alice", 100), buildUser("Bob", 80))
        coEvery { repository.getRanking() } returns Either.Right(users)

        val result = getRankingScore.invoke(RankingMode.NORMAL)

        assertTrue(result.isRight())
        assertEquals(2, (result as Either.Right).value.size)
    }

    @Test
    fun `GetRankingScore NORMAL delega a getRanking del repository`() = runTest {
        coEvery { repository.getRanking() } returns Either.Right(emptyList())

        getRankingScore.invoke(RankingMode.NORMAL)

        coVerify(exactly = 1) { repository.getRanking() }
    }

    @Test
    fun `GetRankingScore NORMAL retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getRanking() } returns Either.Left(RepositoryException.NoConnectionException)

        val result = getRankingScore.invoke(RankingMode.NORMAL)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // GetRankingScore — TIMED
    // =========================================================

    @Test
    fun `GetRankingScore TIMED retorna Either Right con lista de usuarios`() = runTest {
        val users = listOf(buildUser("Charlie", 200))
        coEvery { repository.getTimedRanking() } returns Either.Right(users)

        val result = getRankingScore.invoke(RankingMode.TIMED)

        assertTrue(result.isRight())
        assertEquals(1, (result as Either.Right).value.size)
    }

    @Test
    fun `GetRankingScore TIMED delega a getTimedRanking del repository`() = runTest {
        coEvery { repository.getTimedRanking() } returns Either.Right(emptyList())

        getRankingScore.invoke(RankingMode.TIMED)

        coVerify(exactly = 1) { repository.getTimedRanking() }
    }

    @Test
    fun `GetRankingScore TIMED retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getTimedRanking() } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = getRankingScore.invoke(RankingMode.TIMED)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.DataNotFoundException)
    }

    // =========================================================
    // GetRecordScore — NORMAL
    // =========================================================

    @Test
    fun `GetRecordScore NORMAL retorna Either Right con el puntaje como String`() = runTest {
        coEvery { repository.getWorldRecords(1L) } returns Either.Right("150")

        val result = getRecordScore.invoke(1L, RankingMode.NORMAL)

        assertTrue(result.isRight())
        assertEquals("150", (result as Either.Right).value)
    }

    @Test
    fun `GetRecordScore NORMAL delega a getWorldRecords con el limite correcto`() = runTest {
        coEvery { repository.getWorldRecords(50L) } returns Either.Right("80")

        getRecordScore.invoke(50L, RankingMode.NORMAL)

        coVerify(exactly = 1) { repository.getWorldRecords(50L) }
    }

    @Test
    fun `GetRecordScore NORMAL retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getWorldRecords(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = getRecordScore.invoke(1L, RankingMode.NORMAL)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // GetRecordScore — TIMED
    // =========================================================

    @Test
    fun `GetRecordScore TIMED delega a getTimedWorldRecords`() = runTest {
        coEvery { repository.getTimedWorldRecords(20L) } returns Either.Right("300")

        val result = getRecordScore.invoke(20L, RankingMode.TIMED)

        assertTrue(result.isRight())
        assertEquals("300", (result as Either.Right).value)
        coVerify(exactly = 1) { repository.getTimedWorldRecords(20L) }
    }

    @Test
    fun `GetRecordScore TIMED retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getTimedWorldRecords(any()) } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = getRecordScore.invoke(20L, RankingMode.TIMED)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // GetRecordScore — modo por defecto
    // =========================================================

    @Test
    fun `GetRecordScore con modo por defecto usa NORMAL`() = runTest {
        coEvery { repository.getWorldRecords(1L) } returns Either.Right("200")

        val result = getRecordScore.invoke(1L)

        assertTrue(result.isRight())
        coVerify(exactly = 1) { repository.getWorldRecords(1L) }
    }

    // =========================================================
    // SaveTopScore — NORMAL
    // =========================================================

    @Test
    fun `SaveTopScore NORMAL delega a addRecord del repository`() = runTest {
        val user = buildUser("Alice", 100)
        coEvery { repository.addRecord(user) } returns Either.Right(user)

        val result = saveTopScore.invoke(user, RankingMode.NORMAL)

        assertTrue(result.isRight())
        coVerify(exactly = 1) { repository.addRecord(user) }
    }

    @Test
    fun `SaveTopScore NORMAL retorna Either Left cuando el repository falla`() = runTest {
        val user = buildUser("Bob", 50)
        coEvery { repository.addRecord(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = saveTopScore.invoke(user, RankingMode.NORMAL)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // SaveTopScore — TIMED
    // =========================================================

    @Test
    fun `SaveTopScore TIMED delega a addTimedRecord del repository`() = runTest {
        val user = buildUser("SpeedRunner", 999)
        coEvery { repository.addTimedRecord(user) } returns Either.Right(user)

        val result = saveTopScore.invoke(user, RankingMode.TIMED)

        assertTrue(result.isRight())
        coVerify(exactly = 1) { repository.addTimedRecord(user) }
    }

    @Test
    fun `SaveTopScore TIMED retorna Either Left cuando el repository falla`() = runTest {
        val user = buildUser("SpeedRunner", 999)
        coEvery { repository.addTimedRecord(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = saveTopScore.invoke(user, RankingMode.TIMED)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // SaveTopScore — modo por defecto es NORMAL
    // =========================================================

    @Test
    fun `SaveTopScore con modo por defecto usa NORMAL`() = runTest {
        val user = buildUser("Default", 75)
        coEvery { repository.addRecord(user) } returns Either.Right(user)

        saveTopScore.invoke(user)

        coVerify(exactly = 1) { repository.addRecord(user) }
    }
}
