package com.example.kinnectify.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Messenger
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.kinnectify.R
import com.example.kinnectify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var navController:NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController=findNavController(R.id.container)
        binding.bottomMenu.setItemSelected(R.id.home)
        binding.bottomMenu.setOnItemSelectedListener {
            id->
            when(id){
                R.id.home->{
                    navController.navigate(R.id.homeFragment)
                }
                R.id.profile->{
                    navController.navigate(R.id.profileFragment)
                }
                R.id.add_post->{
                    startActivity(Intent(this,PublishActivity::class.java))
                    binding.bottomMenu.setItemSelected(R.id.home)
                    navController.navigate(R.id.homeFragment)
                }
                R.id.messenger->{
                    startActivity(Intent(this,Messenger::class.java))
                    binding.bottomMenu.setItemSelected(R.id.home)
                    navController.navigate(R.id.homeFragment)
                }
                R.id.videos->{
                    navController.navigate(R.id.videosFragment)
                }
            }
        }
        binding.bottomMenu.showBadge(R.id.home,8)
        binding.bottomMenu.showBadge(R.id.videos,5)

    }
}