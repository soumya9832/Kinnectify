package com.example.kinnectify.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.SoundEffectConstants
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.RequestManager
import com.example.kinnectify.R
import com.example.kinnectify.common.utils.Resource
import com.example.kinnectify.common.utils.Status
import com.example.kinnectify.databinding.ActivityPublishBinding
import com.example.kinnectify.models.Post
import com.example.kinnectify.models.User
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class PublishActivity : AppCompatActivity() {


    private var _binding:ActivityPublishBinding?=null
    private val binding get() = _binding!!

    private val viewModel by viewModels<ViewModelMain>()
    private lateinit var thisUser:User

    @Inject
    lateinit var myContext:Context

    @Inject
    lateinit var glide:RequestManager

    private lateinit var imagePickerLauncher:ActivityResultLauncher<String>
    private lateinit var videoPickerLauncher: ActivityResultLauncher<String>
    private lateinit var cameraImageLauncher: ActivityResultLauncher<Uri>
    private lateinit var cameraVideoLauncher: ActivityResultLauncher<Uri>

    private var imageUri:Uri?=null
    private var videoUri:Uri?=null


    private val permissionReadMediaImages="android.permission.READ_MEDIA_IMAGES"
    private val permissionReadMediaVideo = "android.permission.READ_MEDIA_VIDEO"

    //permission constants
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 101

    //image pick constants
    private val IMAGE_PICK_CAMERA_CODE = 102
    private val IMAGE_PICK_GALLERY_CODE = 103

    //video pick constants
    private val VIDEO_PICK_CAMERA_CODE = 104
    private val VIDEO_PICK_GALLERY_CODE = 105


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding=ActivityPublishBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setUpActivityResultLaunchers()


        //loading user data
        viewModel.getDataForCurrentUser()
        viewModel.currentUserLiveData.observe(this)
        {
            when(it.status)
            {
                Status.SUCCESS->
                {
                    thisUser=it.data!!
                    glide.load(thisUser.image).into(binding.publishMyImage)
                    binding.publishMyName.text=thisUser.name
                }
                Status.ERROR->
                {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }
                else->{
                    Toast.makeText(this, "issue in loading user data", Toast.LENGTH_SHORT).show()
                }
            }
        }


        if (checkCameraPermission() && checkStoragePermission()) {
            // Both camera and storage permissions are granted, proceed with the operation
            Toast.makeText(this, "all permissions are garnted", Toast.LENGTH_SHORT).show()
        } else {
            // Request camera permission
            if (!checkCameraPermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_REQUEST_CODE
                )
            }

            // Request storage permission
            if (!checkStoragePermission()) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_REQUEST_CODE
                )
            }
        }






        //OnClicklistener for Publish Button

        binding.publishBtnPublish.setOnClickListener {
            val caption = binding.publishCaption.text.toString()
            val fans = binding.publishTextAnyone.text.toString()
            if(caption.isEmpty())
            {
                Toast.makeText(this,"Enter Caption",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var postType:String="article"
            var postAttachment:String=""

             if (imageUri != null && videoUri == null) {
                postType = "image"
                postAttachment = imageUri.toString()

            } else if (imageUri == null && videoUri!=null ) {
                postType = "video"
                postAttachment = videoUri.toString()

            }

            val post=Post(thisUser.id,thisUser.name,thisUser.email,thisUser.image,caption,
                "",""+fans,postType,postAttachment)

//            //language identification and upload post
//            viewModel.identifyLanguage(caption)
//            viewModel.languageIdentifierLiveData.observe(this){
//                when(it.status)
//                {
//                    Status.SUCCESS->
//                    {
//                        post.languageCode=it.data.toString()
            viewModel.uploadPost(post)
            viewModel.uploadPostLiveData.observe(this){
                when(it.status)
                {
                    Status.SUCCESS->
                    {
                        Toast.makeText(myContext, "Post Published", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    Status.ERROR->
                    {
                        Toast.makeText(myContext, "${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    else->{
                        Toast.makeText(myContext, "problem in upload post", Toast.LENGTH_SHORT).show()
                    }
                }

//                        }
//                    }
//                    Status.ERROR->
//                    {
//                        Toast.makeText(myContext, "${it.message}", Toast.LENGTH_SHORT).show()
//                    }
//                    else->{
//                        Toast.makeText(myContext, "problem in language detection", Toast.LENGTH_SHORT).show()
//                    }

                }
            }




        binding.publishBtnAnyone.setOnClickListener {
            val popupMenu=PopupMenu(this,binding.publishBtnAnyone)
            popupMenu.menuInflater.inflate(R.menu.anyone_menu,popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {item->
                when(item.itemId)
                {
                    R.id.menu_anyone->
                        binding.publishTextAnyone.text="Anyone"
                    R.id.menu_friends->
                        binding.publishTextAnyone.text="Friends"
                    R.id.menu_only_me->
                        binding.publishTextAnyone.text="Only me"

                }
                true
            }
            popupMenu.show()
        }


        binding.publishBtnBottom.setOnClickListener {
            val bottomSheetDialog= BottomSheetDialog(this,R.style.BottomSheetStyle)

            val sheetView= LayoutInflater.from(applicationContext)
                .inflate(R.layout.bottom_dialog,
                    findViewById(R.id.dialog_container))

            sheetView.findViewById<LinearLayout>(R.id.bottom_image).setOnClickListener {
                imagePickDialog()
                binding.publishCaption.setLines(4)
                bottomSheetDialog.dismiss()
            }
            sheetView.findViewById<LinearLayout>(R.id.bottom_video).setOnClickListener {
                videoPickDialog()
                binding.publishCaption.setLines(4)
                bottomSheetDialog.dismiss()
            }
            sheetView.findViewById<LinearLayout>(R.id.bottom_article).setOnClickListener {
                Toast.makeText(this, "article", Toast.LENGTH_SHORT).show()
                bottomSheetDialog.dismiss()
                binding.publishCaption.setLines(12)
                binding.publishVideo.visibility=View.GONE
                binding.publishImage.visibility=View.GONE
                imageUri=null
                videoUri=null
            }
            sheetView.findViewById<LinearLayout>(R.id.bottom_attache).setOnClickListener {
                binding.publishVideo.visibility=View.GONE
                binding.publishImage.visibility=View.GONE
                imageUri=null
                videoUri=null

                Toast.makeText(this, "attache", Toast.LENGTH_SHORT).show()
            }

            sheetView.findViewById<ImageView>(R.id.bottom_close).setOnClickListener {
                bottomSheetDialog.dismiss()
            }


            bottomSheetDialog.setContentView(sheetView)
            bottomSheetDialog.show()




        }









    }




    private fun setUpActivityResultLaunchers()
    {
        //launcher for image picking from gallery
        imagePickerLauncher=registerForActivityResult(ActivityResultContracts.GetContent()){uri:Uri?->
            uri?.let {
                imageUri=uri
                binding.publishImage.visibility=View.VISIBLE
                binding.publishImage.setImageURI(imageUri)

                videoUri=null
                binding.publishVideo.visibility=View.GONE

            }
        }

        //launcher for video picking from gallery
        videoPickerLauncher=registerForActivityResult(ActivityResultContracts.GetContent()){uri:Uri?->
            uri?.let {
                videoUri=uri
                binding.publishVideo.visibility=View.VISIBLE
                setVideoToVideoView()

                imageUri=null
                binding.publishImage.visibility=View.GONE

            }
        }

        //launcher for capturing image from camera
        cameraImageLauncher=registerForActivityResult(ActivityResultContracts.TakePicture()){success:Boolean->
            if(success)
            {
                binding.publishImage.visibility=View.VISIBLE
                binding.publishImage.setImageURI(imageUri)

                videoUri=null
                binding.publishVideo.visibility=View.GONE
            }

        }

        //launcher for capturing video from camera
        cameraVideoLauncher=registerForActivityResult(ActivityResultContracts.CaptureVideo()){success:Boolean->
            if(success)
            {
                binding.publishVideo.visibility=View.VISIBLE
                setVideoToVideoView()

                imageUri=null
                binding.publishImage.visibility=View.GONE
            }

        }
    }


    //Image Picker Dialog
    private fun imagePickDialog()
    {
        val options= arrayOf("Camera","Gallery")

        //create the builder
        val builder=AlertDialog.Builder(this)
        builder.setTitle("Pick Image From?!")
        builder.setCancelable(false)

        builder.setItems(options){dialog,which->
            //camera is selected
            if(which==0)
            {
                imagePickCamera()
            }
            //video is selected
            else if(which==1)
            {
                imagePickGallery()
            }

        }
        //create the dialog
        builder.create().show()


    }


    //Video Picker Dialog
    private fun videoPickDialog()
    {
        val options = arrayOf("Camera", "Gallery")

        //creating builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick video from ?!")
        builder.setCancelable(false)

        builder.setItems(options) { dialog, which ->
            if (which == 0) {
                //camera clicked

                videoPickCamera()

            } else if (which == 1) {
                // gallery clicked
                videoPickGallery()

            }
        }

        //create show dialog
        builder.create().show()
    }

    private fun imagePickCamera()
    {
        val imageUri:Uri=createImageUri()
        this.imageUri=imageUri
        cameraImageLauncher.launch(imageUri)
    }
    private fun videoPickCamera()
    {
        val videoUri:Uri=createVideoUri()
        this.videoUri=videoUri
        cameraVideoLauncher.launch(videoUri)
    }
    private fun imagePickGallery()
    {
        imagePickerLauncher.launch("image/*")
    }
    private fun videoPickGallery()
    {
        videoPickerLauncher.launch("video/*")
    }

    private fun createImageUri():Uri
    {
        // Ensure the directory for storing the image exists
        val imagesFolder= File(getExternalFilesDir(null),"images")
        if(!imagesFolder.exists())imagesFolder.mkdirs()

        // Create a file for the image
        val file=File(imagesFolder,"post_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this,"${applicationContext.packageName}.provider",file)
    }

    private fun createVideoUri():Uri
    {
        // Ensure the directory for storing the video exists
        val videosFolder= File(getExternalFilesDir(null),"videos")
        if(!videosFolder.exists())videosFolder.mkdirs()

        // Create a file for the video
        val file=File(videosFolder,"post_video_${System.currentTimeMillis()}.mp4")
        return FileProvider.getUriForFile(this,"${applicationContext.packageName}.provider",file)
    }


    private fun setVideoToVideoView()
    {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.publishVideo)
        // set media controller to Video view
        binding.publishVideo.setMediaController(mediaController)
        binding.publishVideo.setVideoURI(videoUri)

        binding.publishVideo.setOnPreparedListener{
            binding.publishVideo.start()
            mediaController.show()
            mediaController.playSoundEffect(SoundEffectConstants.NAVIGATION_DOWN)
        }
    }

    private fun checkCameraPermission():Boolean
    {
        //check if camera permission is enabled or not
        val result = ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(this,android.Manifest.permission.WAKE_LOCK)==PackageManager.PERMISSION_GRANTED
        return result && result1

    }

    private fun checkStoragePermission():Boolean{
        //check if storage permission is enabled or not
        return ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
            )== PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode)
        {
            CAMERA_REQUEST_CODE -> if (grantResults.isNotEmpty())
            {
                //check
                val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if(cameraAccepted && storageAccepted)
                {

                }
                else
                {
                    Toast.makeText(this, "Camera & Storage Permission are required", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE -> if(grantResults.isNotEmpty())
            {
                //check
                val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                if(storageAccepted)
                {

                }
                else
                {
                    Toast.makeText(this, "Storage Permission are required", Toast.LENGTH_SHORT).show()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode== RESULT_OK)
        {
            if(requestCode==IMAGE_PICK_GALLERY_CODE || requestCode==IMAGE_PICK_CAMERA_CODE)
            {
                videoUri=null
                //show picked image
                binding.publishImage.visibility=View.VISIBLE
                binding.publishVideo.visibility=View.GONE
                if(requestCode==IMAGE_PICK_GALLERY_CODE)
                {
                    imageUri=data?.data
                }
                else
                {
                    //File object of camera image
                    val file = File(Environment.getExternalStorageDirectory(),"MyPhoto.jpg")
                    //uri of camera image
                    val uri = FileProvider.getUriForFile(this,this.applicationContext.packageName+".provider",file)
                    imageUri=uri
                    Toast.makeText(this,""+imageUri,Toast.LENGTH_SHORT).show()
                }
                binding.publishImage.setImageURI(imageUri)

            }
            else if (requestCode ==  VIDEO_PICK_GALLERY_CODE || requestCode ==  VIDEO_PICK_CAMERA_CODE){
                videoUri = data?.data
                imageUri=null
                //show picked video
                binding.publishImage.visibility= View.GONE
                binding.publishVideo.visibility=View.VISIBLE
                setVideoToVideoView()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    




}