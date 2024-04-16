package com.example.kinnectify.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.kinnectify.common.utils.Constants
import com.example.kinnectify.common.utils.Resource
import com.example.kinnectify.models.Post
import com.example.kinnectify.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import javax.inject.Inject

class Repository @Inject constructor(
    private var refDatabase: DatabaseReference,
    private var refStorage: StorageReference,
    private var auth: FirebaseAuth,
    private var firebaseMessaging: FirebaseMessaging,
    private var context: Context
) {


    //Fetching User Details from Database

    private val uid=auth.currentUser?.uid.toString()
    private var currentUserLiveData=MutableLiveData<Resource<User>>()
    suspend fun getCurrentUserData():MutableLiveData<Resource<User>>
    {
        refDatabase.child(Constants.USER)
            .child(uid).get()
            .addOnSuccessListener {snapshot->
                val user=snapshot.getValue(User::class.java)
                currentUserLiveData.value=Resource.success(user)
            }
            .addOnFailureListener{
                currentUserLiveData.value=Resource.error(it.message.toString(),null)
            }
        return currentUserLiveData
    }

    //Upload Post in Database

    private var postLiveData=MutableLiveData<Resource<Boolean>>()
    suspend fun uploadPost(post:Post):MutableLiveData<Resource<Boolean>>{
        val timeStamp=System.currentTimeMillis().toString()
        val filePathAndName="Posts/post_$timeStamp"
        post.postId=timeStamp
        post.postTime=timeStamp

        if(post.postType=="image" || post.postType=="video")
        {
            val storageReference=refStorage.child(filePathAndName)
            storageReference.putFile(Uri.parse(post.postAttachment))
                .addOnSuccessListener {
                    val uriTask=it.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    val downloadUri=uriTask.result.toString()
                    if(uriTask.isSuccessful)
                    {
                        post.postAttachment=downloadUri
                        val ref=refDatabase.child(Constants.POSTS)
                        ref.child(timeStamp).setValue(post)
                            .addOnSuccessListener {
                                postLiveData.value= Resource.success(true)
                                Toast.makeText(context, "Post Published", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener{e->
                                postLiveData.value= Resource.error(e.message.toString(),false)
                            }
                    }
                }
                .addOnFailureListener{
                    Toast.makeText(context, "" + it.message, Toast.LENGTH_SHORT).show();
                }

        }
        else
        {
            val ref = refDatabase.child("Posts")
            ref.child(timeStamp).setValue(post).addOnSuccessListener {
                postLiveData.value= Resource.success(true)
            }.addOnFailureListener { e ->
                postLiveData.value= Resource.error(e.message.toString(),false)
            }
        }

        return postLiveData
    }







}