package com.quiz.data.repository

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.quiz.data.datasource.DataBaseSource
import com.quiz.domain.Pride

class PrideByIdRepositoryImpl(private val dataBaseSource: DataBaseSource) : PrideByIdRepository {

    // Cache de paginas: page -> lista de Prides
    private val pageCache = mutableMapOf<Int, List<Pride>>()

    override suspend fun getPrideById(id: Int): Either<RepositoryException, Pride> {
        return try {
            Either.Right(dataBaseSource.getPrideById(id))
        } catch (e: Exception) {
            Either.Left(RepositoryException.DataNotFoundException)
        }
    }

    override suspend fun getPrideList(currentPage: Int): Either<RepositoryException, List<Pride>> {
        // Retornar cache si existe para esta pagina
        pageCache[currentPage]?.let { return it.right() }

        return try {
            val result = dataBaseSource.getPrideList(currentPage)
            pageCache[currentPage] = result
            result.right()
        } catch (e: Exception) {
            RepositoryException.DataNotFoundException.left()
        }
    }
}
