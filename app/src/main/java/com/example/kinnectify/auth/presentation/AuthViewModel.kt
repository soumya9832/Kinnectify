package com.example.kinnectify.auth.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinnectify.auth.domain.models.CreateUserInput
import com.example.kinnectify.auth.domain.usecases.CreateUser
import com.example.kinnectify.auth.domain.usecases.ResetPasswordUseCase
import com.example.kinnectify.auth.domain.usecases.SignInWithEmailAndPassword
import com.example.kinnectify.common.utils.Resource
import com.example.kinnectify.common.utils.UIState
import com.example.kinnectify.models.User
import com.example.kinnectify.repository.RepositoryUser
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val createUserUseCase: CreateUser,
    private val signInWithEmailAndPasswordUseCase: SignInWithEmailAndPassword,
    private val resetPasswordUseCase: ResetPasswordUseCase,
)  : ViewModel() {



    private val _createUserState = MutableStateFlow<UIState<User>>(UIState.Empty)
    val createUserState: StateFlow<UIState<User>> get() = _createUserState

    fun createUser(userInput: CreateUserInput){
        _createUserState.value = UIState.Loading
        viewModelScope.launch {
            runCatching {
                createUserUseCase(userInput)
            }.onSuccess { user ->
                _createUserState.value = UIState.Success(user)

            }.onFailure { throwable ->
                Log.d(TAG, "createUser: ${throwable.localizedMessage.toString()}")
                _createUserState.value = UIState.Error(throwable.localizedMessage.toString())
            }
        }
    }



    private val _signInUserState = MutableStateFlow<UIState<User>>(UIState.Empty)
    val signInUserState: StateFlow<UIState<User>> get() = _signInUserState

    fun signInWithEmailAndPassword(email:String, password: String){
        _signInUserState.value = UIState.Loading
        viewModelScope.launch {
            runCatching {
                signInWithEmailAndPasswordUseCase(email, password)
            }.onSuccess { user ->
                _signInUserState.value = UIState.Success(user)

            }.onFailure { throwable ->
                Log.d(TAG, "signInWithEmailAndPassword: ${throwable.localizedMessage?.toString()}")
                _signInUserState.value = UIState.Error(throwable.localizedMessage.toString())
            }
        }
    }



    private val _resetPasswordState = MutableStateFlow<UIState<Boolean>>(UIState.Empty)
    val resetPasswordState: StateFlow<UIState<Boolean>> get() = _resetPasswordState
    fun resetPassword(email:String){
        _resetPasswordState.value = UIState.Loading
        viewModelScope.launch {
            runCatching {
                delay(5000)
                resetPasswordUseCase(email)
            }.onSuccess { success ->
                _resetPasswordState.value = UIState.Success(success)

            }.onFailure { throwable ->
                Log.d(TAG, "resetPassword: ${throwable.localizedMessage?.toString()}")
                _resetPasswordState.value = UIState.Error(throwable.localizedMessage.toString())
            }
        }
    }







}