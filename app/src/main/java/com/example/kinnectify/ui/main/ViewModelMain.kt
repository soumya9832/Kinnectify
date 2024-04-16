package com.example.kinnectify.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kinnectify.common.utils.Resource
import com.example.kinnectify.models.Post
import com.example.kinnectify.models.User
import com.example.kinnectify.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.security.PrivateKey
import javax.inject.Inject

@HiltViewModel
class ViewModelMain @Inject constructor(
    private val repository:Repository,
    private val auth:FirebaseAuth
):ViewModel() {

    var currentUserLiveData=MutableLiveData<Resource<User>>()
    fun getDataForCurrentUser()
    {
        viewModelScope.launch {
            currentUserLiveData=repository.getCurrentUserData()
        }
    }

    var languageIdentifierLiveData=MutableLiveData<Resource<String>>()
    fun identifyLanguage(caption:String)
    {

    }

    var uploadPostLiveData=MutableLiveData<Resource<Boolean>>()
    fun uploadPost(post: Post)
    {
        viewModelScope.launch {
            uploadPostLiveData=repository.uploadPost(post)
        }
    }

}