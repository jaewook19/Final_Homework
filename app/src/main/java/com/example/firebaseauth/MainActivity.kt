package com.example.firebaseauth

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    lateinit var storage: FirebaseStorage

    private val remoteConfig = Firebase.remoteConfig
    private val imageView by lazy { findViewById<ImageView>(R.id.imageView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Firebase.auth.currentUser ?: finish() // if not authenticated, finish this activity
        storage = Firebase.storage

        val storageRef = storage.reference

        val textView1 = findViewById<TextView>(R.id.textUID)
        textView1.text = "UID (Authentication)"
        val textView2 = findViewById<TextView>(R.id.textUID2)
        val uid = Firebase.auth.currentUser?.uid
        textView2.text = uid ?: "No User"

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.button_signout)?.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Remote Config 초기화 및 설정
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 1 // 원하는 업데이트 주기를 설정
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config)

        // 이미지 업데이트 버튼
        findViewById<Button>(R.id.button2).setOnClickListener {
            refreshImage()
        }

        // 이미지 업데이트
        refreshImage()
    }

    private fun refreshImage() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val season = remoteConfig.getString("season")
                    val imgRef = when (season) {
                        "spring" -> Firebase.storage.getReferenceFromUrl("gs://fir-auth-900d6.appspot.com/spring.png")
                        "summer" -> Firebase.storage.getReferenceFromUrl("gs://fir-auth-900d6.appspot.com/summer.png")
                        "fall" -> Firebase.storage.getReferenceFromUrl("gs://fir-auth-900d6.appspot.com/fall.png")
                        else -> Firebase.storage.getReferenceFromUrl("gs://fir-auth-900d6.appspot.com/winter.png")
                    }
                    displayImageRef(imgRef, imageView)
                }
            }
    }

    private fun displayImageRef(imageRef: StorageReference?, view: ImageView) {
        imageRef?.getBytes(Long.MAX_VALUE)?.addOnSuccessListener {
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            view.setImageBitmap(bmp)
        }?.addOnFailureListener {
            // 이미지 다운로드 실패
        }
    }
}