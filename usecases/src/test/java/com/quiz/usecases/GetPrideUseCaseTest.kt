package com.quiz.usecases

import arrow.core.Either
import com.quiz.data.repository.PrideByIdRepository
import com.quiz.data.repository.RepositoryException
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

class GetPrideUseCaseTest {

    private val repository: PrideByIdRepository = mockk()

    private lateinit var getPrideById: GetPrideById
    private lateinit var getPrideList: GetPrideList

    private fun buildPride(id: Int = 1) = Pride(
        name = Name(ES = "Orgullo $id", EN = "Pride $id"),
        flag = "flag_$id"
    )

    @Before
    fun setup() {
        getPrideById = GetPrideById(repository)
        getPrideList = GetPrideList(repository)
    }

    // =========================================================
    // GetPrideById
    // =========================================================

    @Test
    fun `GetPrideById retorna Either Right cuando el repository tiene exito`() = runTest {
        val expectedPride = buildPride(42)
        coEvery { repository.getPrideById(42) } returns Either.Right(expectedPride)

        val result = getPrideById.invoke(42)

        assertTrue(result.isRight())
        assertEquals(expectedPride, (result as Either.Right).value)
    }

    @Test
    fun `GetPrideById retorna Either Left cuando el repository lanza NoConnectionException`() = runTest {
        coEvery { repository.getPrideById(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = getPrideById.invoke(1)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.NoConnectionException)
    }

    @Test
    fun `GetPrideById retorna Either Left cuando el repository lanza DataNotFoundException`() = runTest {
        coEvery { repository.getPrideById(any()) } returns Either.Left(RepositoryException.DataNotFoundException)

        val result = getPrideById.invoke(999)

        assertTrue(result.isLeft())
        assertTrue((result as Either.Left).value is RepositoryException.DataNotFoundException)
    }

    @Test
    fun `GetPrideById delega exactamente al repository con el ID correcto`() = runTest {
        coEvery { repository.getPrideById(7) } returns Either.Right(buildPride(7))

        getPrideById.invoke(7)

        coVerify(exactly = 1) { repository.getPrideById(7) }
    }

    @Test
    fun `GetPrideById retorna el modelo completo sin modificaciones`() = runTest {
        val originalPride = Pride(
            name = Name(ES = "Bandera Bisexual", EN = "Bisexual Flag", DE = "Bisexuelle Flagge"),
            description = Name(ES = "Descripcion", EN = "Description"),
            flag = "bi_flag"
        )
        coEvery { repository.getPrideById(5) } returns Either.Right(originalPride)

        val result = getPrideById.invoke(5)

        assertTrue(result.isRight())
        val pride = (result as Either.Right).value
        assertEquals("Bandera Bisexual", pride.name?.ES)
        assertEquals("Bisexual Flag", pride.name?.EN)
        assertEquals("bi_flag", pride.flag)
    }

    // =========================================================
    // GetPrideList
    // =========================================================

    @Test
    fun `GetPrideList retorna Either Right con lista de prides`() = runTest {
        val expectedList = listOf(buildPride(1), buildPride(2), buildPride(3))
        coEvery { repository.getPrideList(0) } returns Either.Right(expectedList)

        val result = getPrideList.invoke(0)

        assertTrue(result.isRight())
        assertEquals(3, (result as Either.Right).value.size)
    }

    @Test
    fun `GetPrideList retorna Either Left cuando el repository falla`() = runTest {
        coEvery { repository.getPrideList(any()) } returns Either.Left(RepositoryException.NoConnectionException)

        val result = getPrideList.invoke(0)

        assertTrue(result.isLeft())
    }

    @Test
    fun `GetPrideList delega al repository con la pagina correcta`() = runTest {
        coEvery { repository.getPrideList(3) } returns Either.Right(emptyList())

        getPrideList.invoke(3)

        coVerify(exactly = 1) { repository.getPrideList(3) }
    }

    @Test
    fun `GetPrideList retorna lista vacia cuando el repository retorna lista vacia`() = runTest {
        coEvery { repository.getPrideList(99) } returns Either.Right(emptyList())

        val result = getPrideList.invoke(99)

        assertTrue(result.isRight())
        assertTrue((result as Either.Right).value.isEmpty())
    }
}
