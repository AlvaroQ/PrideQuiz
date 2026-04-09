package com.quiz.data.repository

import arrow.core.Either
import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.Name
import com.quiz.domain.Pride
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrideByIdRepositoryImplTest {

    private lateinit var repository: PrideByIdRepository
    private val dataBaseSource: DataBaseSource = mockk()

    private fun buildPride(id: Int) = Pride(
        name = Name(ES = "Test $id", EN = "Test $id"),
        flag = "https://example.com/$id.png",
        description = Name(ES = "Desc", EN = "Desc")
    )

    @Before
    fun setup() {
        repository = PrideByIdRepositoryImpl(dataBaseSource)
    }

    // =========================================================
    // getPrideById
    // =========================================================

    @Test
    fun `getPrideById retorna Right con Pride cuando datasource tiene exito`() = runTest {
        val pride = buildPride(1)
        coEvery { dataBaseSource.getPrideById(1) } returns pride

        val result = repository.getPrideById(1)

        assertTrue(result.isRight())
        assertEquals(pride, (result as Either.Right).value)
    }

    @Test
    fun `getPrideById retorna Left DataNotFoundException cuando datasource falla`() = runTest {
        coEvery { dataBaseSource.getPrideById(1) } throws RuntimeException("Network error")

        val result = repository.getPrideById(1)

        assertTrue(result.isLeft())
        assertEquals(RepositoryException.DataNotFoundException, (result as Either.Left).value)
    }

    @Test
    fun `getPrideById delega el id exacto al datasource`() = runTest {
        coEvery { dataBaseSource.getPrideById(42) } returns buildPride(42)

        repository.getPrideById(42)

        coVerify { dataBaseSource.getPrideById(42) }
    }

    // =========================================================
    // getPrideList — cache behavior
    // =========================================================

    @Test
    fun `getPrideList retorna Right con lista cuando datasource tiene exito`() = runTest {
        val list = listOf(buildPride(1), buildPride(2))
        coEvery { dataBaseSource.getPrideList(0) } returns list

        val result = repository.getPrideList(0)

        assertTrue(result.isRight())
        assertEquals(2, (result as Either.Right).value.size)
    }

    @Test
    fun `getPrideList cachea resultado y no vuelve a llamar datasource`() = runTest {
        val list = listOf(buildPride(1))
        coEvery { dataBaseSource.getPrideList(0) } returns list

        repository.getPrideList(0)
        repository.getPrideList(0) // Second call should hit cache

        coVerify(exactly = 1) { dataBaseSource.getPrideList(0) }
    }

    @Test
    fun `getPrideList cachea por pagina independientemente`() = runTest {
        coEvery { dataBaseSource.getPrideList(0) } returns listOf(buildPride(1))
        coEvery { dataBaseSource.getPrideList(1) } returns listOf(buildPride(2))

        repository.getPrideList(0)
        repository.getPrideList(1)

        coVerify(exactly = 1) { dataBaseSource.getPrideList(0) }
        coVerify(exactly = 1) { dataBaseSource.getPrideList(1) }
    }

    @Test
    fun `getPrideList retorna Left cuando datasource falla`() = runTest {
        coEvery { dataBaseSource.getPrideList(0) } throws RuntimeException("Error")

        val result = repository.getPrideList(0)

        assertTrue(result.isLeft())
    }

    @Test
    fun `getPrideList no cachea errores`() = runTest {
        coEvery { dataBaseSource.getPrideList(0) } throws RuntimeException("Error")
        repository.getPrideList(0) // First call fails

        // Fix the datasource
        coEvery { dataBaseSource.getPrideList(0) } returns listOf(buildPride(1))
        val result = repository.getPrideList(0) // Should retry

        assertTrue(result.isRight())
        coVerify(exactly = 2) { dataBaseSource.getPrideList(0) }
    }
}
