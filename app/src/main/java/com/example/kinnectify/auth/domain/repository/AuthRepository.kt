package com.example.kinnectify.auth.domain.repository

import com.example.kinnectify.auth.domain.models.CreateUserInput
import com.example.kinnectify.models.User

interface AuthRepository {
    suspend fun createUser(userInput: CreateUserInput): User
    suspend fun signInWithEmailAndPassword(email: String, password: String): User
    suspend fun resetPassword(email: String):Boolean

}