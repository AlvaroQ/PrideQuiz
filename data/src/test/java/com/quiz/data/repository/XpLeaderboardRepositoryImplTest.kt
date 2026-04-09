package com.quiz.data.repository

import arrow.core.Either
import com.quiz.data.datasource.XpLeaderboardDataSource
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

class XpLeaderboardRepositoryImplTest {

    private lateinit var repository: XpLeaderboardRepository
    private val dataSource: XpLeaderboardDataSource = mockk()

    private fun buildEntry(uid: String, xp: Long = 1000L) = XpLeaderboardEntry(
        uid = uid,
        nickname = "User_$uid",
        imageBase64 = "",
        totalXp = xp,
        level = 5,
        title = "Explorer",
        totalGamesPlayed = 10,
        accuracy = 80f
    )

    @Before
    fun setup() {
        repository = XpLeaderboardRepositoryImpl(dataSource)
    }

    // =========================================================
    // syncUserXp
    // =========================================================

    @Test
    fun `syncUserXp delega al datasource y retorna Right`() = runTest {
        val entry = buildEntry("user-1")
        coEvery { dataSource.syncUserXp(entry) } returns Either.Right(entry)

        val result = repository.syncUserXp(entry)

        assertTrue(result.isRight())
        coVerify { dataSource.syncUserXp(entry) }
    }

    @Test
    fun `syncUserXp retorna Left cuando datasource falla`() = runTest {
        val entry = buildEntry("user-1")
        coEvery { dataSource.syncUserXp(entry) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = repository.syncUserXp(entry)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // getXpLeaderboard
    // =========================================================

    @Test
    fun `getXpLeaderboard retorna lista del datasource`() = runTest {
        val leaderboard = listOf(buildEntry("u1", 5000), buildEntry("u2", 3000))
        coEvery { dataSource.getXpLeaderboard(100) } returns leaderboard

        val result = repository.getXpLeaderboard(100)

        assertEquals(2, result.size)
        assertEquals("u1", result.first().uid)
    }

    @Test
    fun `getXpLeaderboard pasa limit exacto al datasource`() = runTest {
        coEvery { dataSource.getXpLeaderboard(50) } returns emptyList()

        repository.getXpLeaderboard(50)

        coVerify { dataSource.getXpLeaderboard(50) }
    }

    // =========================================================
    // getUserRank
    // =========================================================

    @Test
    fun `getUserRank retorna Right con rank del datasource`() = runTest {
        coEvery { dataSource.getUserRank("user-1", 5000L) } returns Either.Right(3)

        val result = repository.getUserRank("user-1", 5000L)

        assertTrue(result.isRight())
        assertEquals(3, (result as Either.Right).value)
    }

    @Test
    fun `getUserRank retorna Left cuando datasource falla`() = runTest {
        coEvery { dataSource.getUserRank("user-1", 0L) } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = repository.getUserRank("user-1", 0L)

        assertTrue(result.isLeft())
    }

    // =========================================================
    // getUserXpEntry
    // =========================================================

    @Test
    fun `getUserXpEntry retorna Right con entry cuando existe`() = runTest {
        val entry = buildEntry("user-1")
        coEvery { dataSource.getUserXpEntry("user-1") } returns Either.Right(entry)

        val result = repository.getUserXpEntry("user-1")

        assertTrue(result.isRight())
        assertEquals("user-1", (result as Either.Right).value?.uid)
    }

    @Test
    fun `getUserXpEntry retorna Right null cuando no existe`() = runTest {
        coEvery { dataSource.getUserXpEntry("unknown") } returns Either.Right(null)

        val result = repository.getUserXpEntry("unknown")

        assertTrue(result.isRight())
        assertNull((result as Either.Right).value)
    }
}
