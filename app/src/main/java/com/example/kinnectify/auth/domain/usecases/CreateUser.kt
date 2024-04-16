package com.example.kinnectify.auth.domain.usecases

import com.example.kinnectify.auth.domain.models.CreateUserInput
import com.example.kinnectify.auth.domain.repository.AuthRepository
import com.example.kinnectify.models.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

class CreateUser @Inject constructor (private val repository: AuthRepository) {

    suspend operator fun invoke(createUserInput: CreateUserInput): User =
        repository.createUser(createUserInput)

}