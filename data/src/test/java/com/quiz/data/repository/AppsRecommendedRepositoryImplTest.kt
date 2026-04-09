package com.quiz.data.repository

import arrow.core.Either
import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.App
import com.quiz.domain.Name
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppsRecommendedRepositoryImplTest {

    private lateinit var repository: AppsRecommendedRepository
    private val dataBaseSource: DataBaseSource = mockk()

    private fun buildApp(name: String) = App(
        image = "img_$name",
        localeName = Name(ES = name, EN = name),
        url = "https://play.google.com/store/apps/details?id=$name"
    )

    @Before
    fun setup() {
        repository = AppsRecommendedRepositoryImpl(dataBaseSource)
    }

    @Test
    fun `getAppsRecommended retorna Right con lista cuando datasource tiene exito`() = runTest {
        val apps = listOf(buildApp("App1"), buildApp("App2"))
        coEvery { dataBaseSource.getAppsRecommended() } returns apps

        val result = repository.getAppsRecommended()

        assertTrue(result.isRight())
        assertEquals(2, (result as Either.Right).value.size)
    }

    @Test
    fun `getAppsRecommended cachea y no vuelve a llamar datasource`() = runTest {
        coEvery { dataBaseSource.getAppsRecommended() } returns listOf(buildApp("App1"))

        repository.getAppsRecommended()
        repository.getAppsRecommended()

        coVerify(exactly = 1) { dataBaseSource.getAppsRecommended() }
    }

    @Test
    fun `getAppsRecommended retorna Left cuando datasource falla`() = runTest {
        coEvery { dataBaseSource.getAppsRecommended() } throws RuntimeException("Error")

        val result = repository.getAppsRecommended()

        assertTrue(result.isLeft())
        assertEquals(RepositoryException.DataNotFoundException, (result as Either.Left).value)
    }

    @Test
    fun `getAppsRecommended no cachea errores — reintenta en siguiente llamada`() = runTest {
        coEvery { dataBaseSource.getAppsRecommended() } throws RuntimeException("Error")
        repository.getAppsRecommended() // Fails

        coEvery { dataBaseSource.getAppsRecommended() } returns listOf(buildApp("App1"))
        val result = repository.getAppsRecommended() // Retries

        assertTrue(result.isRight())
        coVerify(exactly = 2) { dataBaseSource.getAppsRecommended() }
    }

    @Test
    fun `getAppsRecommended con lista vacia retorna Right vacio y cachea`() = runTest {
        coEvery { dataBaseSource.getAppsRecommended() } returns emptyList()

        val result = repository.getAppsRecommended()

        assertTrue(result.isRight())
        assertTrue((result as Either.Right).value.isEmpty())

        // Should be cached even if empty
        repository.getAppsRecommended()
        coVerify(exactly = 1) { dataBaseSource.getAppsRecommended() }
    }
}
