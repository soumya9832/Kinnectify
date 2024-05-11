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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageReference
import javax.inject.Inject

class Repository @Inject constructor(
    var refDatabase: DatabaseReference,
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

    private val postsLiveData=MutableLiveData<Resource<List<Post>>>()
    suspend   fun getPosts(): MutableLiveData<Resource<List<Post>>> {
        postsLiveData.value = Resource.loading(null)

        try {
            var postList: ArrayList<Post> = ArrayList()
            refDatabase.child(Constants.POSTS)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        postList.clear()
                        snapshot.children.forEach { child ->
                            val post = child.getValue<Post>()
                            postList.add(post!!)
                        }
                        postsLiveData.value = Resource.success(postList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        postsLiveData.value = Resource.error(error.message, null)

                    }
                })
        }catch (e:Exception) {
            postsLiveData.value = Resource.error(e.message.toString(), null)
        }


        return postsLiveData

    }



    suspend fun setLike(post: Post){


        var postLikes: Int = post.postLikes
        var mProcessLike = true
        //get id of the post clicked
        val postId: String = post.postId

        if (postId.isNotEmpty()){
            val myId= auth.currentUser?.uid
            refDatabase.child(Constants.LIKES).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (mProcessLike) { //already liked ,so remove  like
                        mProcessLike = if (dataSnapshot.child(postId).hasChild(myId!!)) {

                            refDatabase.child(Constants.POSTS).child(postId).child(Constants.POSTLIKES).setValue(( -- postLikes))
                            refDatabase.child(Constants.LIKES).child(postId).child(myId).removeValue()
                            false
                        } else { //not liked , liked it
                            refDatabase.child(Constants.POSTS).child(postId).child(Constants.POSTLIKES).setValue(( ++ postLikes ))
                            refDatabase.child(Constants.LIKES).child(postId).child(myId).setValue("Liked")
                            false
                        }
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(context, "Error from likeRepo"+databaseError.message, Toast.LENGTH_SHORT).show()
                }
            })
        }

    }







}