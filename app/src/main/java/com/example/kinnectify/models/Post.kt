package com.example.kinnectify.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    var userId:String?="",
    var userName:String?="",
    var userEmail:String?="",
    var userImage:String?="",
    var caption:String="",
    var postId:String="",
    var postFans:String?="Anyone",
    var postType:String?="article",
    var postAttachment:String?="",
    var postTime:String="",
    var postLikes:Int=0,
    var postComments:Int=0,
    var languageCode:String="und",
    var translatedContent:String=""
    ):Parcelable
