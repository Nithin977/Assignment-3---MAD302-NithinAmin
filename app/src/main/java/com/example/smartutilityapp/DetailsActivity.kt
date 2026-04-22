package com.example.smartutilityapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Course Code: MAD302-01 Android Development
 * Name: Nithin Amin
 * Student ID: A00194322
 * Date of Submission: 2026
 * Description:
 * DetailsActivity receives and displays the utility result
 * and location data from MainActivity.
 */
class DetailsActivity : AppCompatActivity() {

    private lateinit var tvDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        tvDetails = findViewById(R.id.tvDetails)

        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        val billResult = intent.getStringExtra("BILL_RESULT") ?: "No bill data available."
        val locationResult = intent.getStringExtra("LOCATION_RESULT") ?: "No location data available."

        tvDetails.text = "Welcome, $userName\n\n$billResult\n\nLocation Information:\n$locationResult"
    }
}