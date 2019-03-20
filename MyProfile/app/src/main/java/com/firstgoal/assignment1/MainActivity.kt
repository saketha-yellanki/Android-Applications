package com.firstgoal.assignment1

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

     var image : ImageView? = null
     var eduButton : Button? = null
     var workButton : Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        image = findViewById(R.id.profilePhoto)

        image?.setOnClickListener({
            var clickIntent = Intent(this@MainActivity,ProfilePhoto::class.java)
            startActivity(clickIntent)

        eduButton = findViewById(R.id.educationButton)

        eduButton?.setOnClickListener({
            var clickIntent = Intent(this@MainActivity,EducationDetails::class.java)
            startActivity(clickIntent)

        workButton = findViewById(R.id.workExpButton)

        workButton?.setOnClickListener({
            var clickIntent = Intent(this@MainActivity,WorkExperienceDetails::class.java)
            startActivity(clickIntent)
        })
        })
        })
    }
}
