package com.example.kinnectify.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.RequestManager
import com.example.kinnectify.databinding.ItemPostBinding
import com.example.kinnectify.models.Post
import com.example.kinnectify.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import android.text.format.DateFormat
import android.view.View
import com.example.kinnectify.R
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale
import java.util.SimpleTimeZone
import javax.inject.Inject

class AdapterPost @Inject constructor(
    @ApplicationContext var context:Context,
    var repository: Repository,
    private val glide:RequestManager,
    private var auth:FirebaseAuth
) : RecyclerView.Adapter<AdapterPost.PostViewHolder>(){

    var posts:List<Post> = ArrayList()
    val myLanguage = Locale.getDefault().language

    var myUid:String? = null
    init {
        myUid = auth.currentUser?.uid
    }

    fun setList(posts: List<Post>){
        this.posts=posts
        notifyDataSetChanged()
    }




    inner class PostViewHolder(val binding:ItemPostBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder =
        PostViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context),parent,false))

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post=posts[position]

        holder.binding.apply {
            postUserName.text = post.userName
            glide.load(post.userImage).into(postUserPicture)
            //Post Date & Time
            val cal = Calendar.getInstance(Locale.getDefault())
            cal.timeInMillis = if(post.postTime.isNotEmpty()) post.postTime.toLong() else 0
            val time = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString()
            postTimeIv.text = time
            postCaption.text = post.caption
            postLikesTV.text = post.postLikes.toString()
            postCommentTV.text = post.postComments.toString()
            postTextAnyone.text = post.postFans

            when(post.postFans)
            {
                "Anyone"->{
                    postImageAnyone.setImageResource(R.drawable.ic_public)
                }
                "Friends"->{
                    postImageAnyone.setImageResource(R.drawable.ic_group)
                }
                "Only me"->{
                    postImageAnyone.setImageResource(R.drawable.ic_profile)
                }
            }

            when(post.postType)
            {
                "article"->{
                    postImage.visibility= View.GONE
                    postVideo.visibility = View.GONE
                }
                "image"->{
                    postVideo.visibility=View.GONE
                    postImage.visibility=View.VISIBLE
                    glide.load(post.postAttachment).into(postImage)
                }
                "video"->{
                    postImage.visibility=View.GONE
                    postVideo.visibility=View.VISIBLE

                    //Depricated have to change it latter------
                    var simpleExoPlayer:SimpleExoPlayer = SimpleExoPlayer.Builder(context).build()
                    val video:Uri = Uri.parse(post.postAttachment)
                    val mediaSource: MediaSource =buildMediaSource(video)
                    simpleExoPlayer.prepare(mediaSource)
                    simpleExoPlayer.playWhenReady =false
                    postVideo.player=simpleExoPlayer
                }
            }



            postLikeBtn.setOnClickListener{
                onItemClickListenerForLike?.let { it(post) }
                notifyDataSetChanged()
            }

            setLikes(holder,post.postId)

            if (post.languageCode != myLanguage && post.languageCode != "und"){
                translate(post.caption,post.languageCode,myLanguage,holder)
            }

            postUserPicture.setOnClickListener{
                onItemClickListenerForGoingToOwner?.let { it(post) }
            }

            postUserName.setOnClickListener {
                onItemClickListenerForGoingToOwner?.let { it(post) }
            }

            postCommentBtn.setOnClickListener {
                onItemClickListener?.let { it(post) }
            }

            holder.itemView.setOnClickListener{
                onItemClickListener?.let { it(post) }
            }



        }
    }

    //Used some deprecated methods , will fix it latter
    private fun buildMediaSource(uri :Uri) :MediaSource{
        val dataSourceFactory : DataSource.Factory = DefaultDataSourceFactory(context,"exoPlayer")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource((MediaItem.fromUri(uri)))
    }

    override fun onViewDetachedFromWindow(holder: PostViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.binding.apply {
            if (postVideo.visibility == View.VISIBLE && postVideo.player?.isPlaying == true ){
                postVideo.player?.stop()
            }
        }
    }



    //Defining functions which will set up the click listener where listener will be provided from the fragment
    private var onItemClickListener : ((Post)->Unit)? = null
    fun setOnItemClickListener(listener:(Post)->Unit)
    {
        onItemClickListener=listener
    }

    private var onItemClickListenerForGoingToOwner: ((Post) -> Unit)? = null
    fun setOnItemClickListenerForGoingToOwner(listener: (Post) -> Unit) {
        onItemClickListenerForGoingToOwner = listener
    }

    private var onItemClickListenerForLike: ((Post) -> Unit)? = null
    fun setonItemClickListenerForLike(listener: (Post) -> Unit) {
        onItemClickListenerForLike = listener
    }



    //This function basically checks whether the current user liked the post or not
    private fun setLikes(holder1: PostViewHolder, postKey: String) {
        repository.refDatabase.child("Likes").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                holder1.binding.apply {
                    if (dataSnapshot.child(postKey).hasChild(myUid!!)) {
                        //user has liked for this post
                        postLikeBtn.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_like, 0, 0, 0)
                        postLikeBtn.text = "Liked"
                    } else {
                        //user has not liked for this post
                        postLikeBtn.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_like_not, 0, 0, 0)
                        postLikeBtn.text = "Like"
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    fun translate(text: String,sourceLanguage:String ,myLanguage:String,holder: PostViewHolder):String{
        var targetLanguage=""

        return targetLanguage
    }

}