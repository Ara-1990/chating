package com.the.chating

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.the.chating.databinding.ActivityMainBinding
import com.the.chating.chating.ChatFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var fragment = ChatFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frag, fragment)
            .addToBackStack(null)
            .commit()

    }
}