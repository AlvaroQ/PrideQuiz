package com.quiz.data.repository

sealed class RepositoryException(message: String? = null) : Throwable(message) {
    data object DataNotFoundException : RepositoryException()
    data object NoConnectionException : RepositoryException()
}