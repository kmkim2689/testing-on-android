package com.practice.testing_practice.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.practice.testing_practice.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        
    }
}