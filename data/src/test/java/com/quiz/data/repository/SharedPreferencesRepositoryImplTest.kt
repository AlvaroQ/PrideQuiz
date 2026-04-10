package com.quiz.data.repository

import com.quiz.data.datasource.SharedPreferencesLocalDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SharedPreferencesRepositoryImplTest {

    private lateinit var repository: SharedPreferencesRepository
    private val localDataSource: SharedPreferencesLocalDataSource = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = SharedPreferencesRepositoryImpl(localDataSource)
    }

    // =========================================================
    // getPaymentDone
    // =========================================================

    @Test
    fun `getPaymentDone retorna true cuando datasource indica pago realizado`() {
        every { localDataSource.getPaymentDone() } returns true

        val result = repository.getPaymentDone()

        assertTrue(result)
    }

    @Test
    fun `getPaymentDone retorna false cuando datasource indica pago no realizado`() {
        every { localDataSource.getPaymentDone() } returns false

        val result = repository.getPaymentDone()

        assertFalse(result)
    }

    @Test
    fun `getPaymentDone delega al datasource`() {
        every { localDataSource.getPaymentDone() } returns false

        repository.getPaymentDone()

        verify { localDataSource.getPaymentDone() }
    }

    // =========================================================
    // setPaymentDone
    // =========================================================

    @Test
    fun `setPaymentDone con true delega valor al datasource`() {
        repository.setPaymentDone(true)

        verify { localDataSource.setPaymentDone(true) }
    }

    @Test
    fun `setPaymentDone con false delega valor al datasource`() {
        repository.setPaymentDone(false)

        verify { localDataSource.setPaymentDone(false) }
    }

    @Test
    fun `setPaymentDone puede sobreescribir un valor previo`() {
        repository.setPaymentDone(true)
        repository.setPaymentDone(false)

        verify(exactly = 1) { localDataSource.setPaymentDone(true) }
        verify(exactly = 1) { localDataSource.setPaymentDone(false) }
    }

    // =========================================================
    // getPersonalRecord
    // =========================================================

    @Test
    fun `getPersonalRecord retorna el valor almacenado en el datasource`() {
        every { localDataSource.getPersonalRecord("") } returns 850

        val result = repository.getPersonalRecord()

        assertEquals(850, result)
    }

    @Test
    fun `getPersonalRecord retorna 0 como valor por defecto cuando no hay record`() {
        every { localDataSource.getPersonalRecord("") } returns 0

        val result = repository.getPersonalRecord()

        assertEquals(0, result)
    }

    @Test
    fun `getPersonalRecord retorna valor maximo posible sin problemas`() {
        every { localDataSource.getPersonalRecord("") } returns Int.MAX_VALUE

        val result = repository.getPersonalRecord()

        assertEquals(Int.MAX_VALUE, result)
    }

    @Test
    fun `getPersonalRecord delega al datasource`() {
        every { localDataSource.getPersonalRecord("") } returns 0

        repository.getPersonalRecord()

        verify { localDataSource.getPersonalRecord("") }
    }

    @Test
    fun `getPersonalRecord con gameMode NORMAL usa clave especifica`() {
        every { localDataSource.getPersonalRecord("NORMAL") } returns 500

        val result = repository.getPersonalRecord("NORMAL")

        assertEquals(500, result)
        verify { localDataSource.getPersonalRecord("NORMAL") }
    }

    @Test
    fun `getPersonalRecord con gameMode TIMED usa clave especifica`() {
        every { localDataSource.getPersonalRecord("TIMED") } returns 30

        val result = repository.getPersonalRecord("TIMED")

        assertEquals(30, result)
        verify { localDataSource.getPersonalRecord("TIMED") }
    }

    // =========================================================
    // setPersonalRecord
    // =========================================================

    @Test
    fun `setPersonalRecord delega el valor al datasource`() {
        repository.setPersonalRecord(750)

        verify { localDataSource.setPersonalRecord(750, "") }
    }

    @Test
    fun `setPersonalRecord con valor cero delega al datasource`() {
        repository.setPersonalRecord(0)

        verify { localDataSource.setPersonalRecord(0, "") }
    }

    @Test
    fun `setPersonalRecord puede sobreescribir un record anterior`() {
        repository.setPersonalRecord(500)
        repository.setPersonalRecord(900)

        verify(exactly = 1) { localDataSource.setPersonalRecord(500, "") }
        verify(exactly = 1) { localDataSource.setPersonalRecord(900, "") }
    }

    @Test
    fun `setPersonalRecord con valor maximo delega correctamente`() {
        repository.setPersonalRecord(Int.MAX_VALUE)

        verify { localDataSource.setPersonalRecord(Int.MAX_VALUE, "") }
    }

    @Test
    fun `setPersonalRecord con gameMode ADVANCE usa clave especifica`() {
        repository.setPersonalRecord(300, "ADVANCE")

        verify { localDataSource.setPersonalRecord(300, "ADVANCE") }
    }

    @Test
    fun `setPersonalRecord con gameMode EXPERT usa clave especifica`() {
        repository.setPersonalRecord(150, "EXPERT")

        verify { localDataSource.setPersonalRecord(150, "EXPERT") }
    }
}
