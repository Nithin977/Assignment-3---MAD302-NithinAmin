package com.example.smartutilityapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Course Code: MAD302-01 Android Development
 * Name: Nithin Amin
 * Student ID: A00194322
 * Date of Submission: April 2026
 * Description:
 * Smart Utility App demonstrates async operations using coroutines,
 * location permission handling, robust error handling, input validation,
 * and safe user messaging across two screens.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etUnits: EditText
    private lateinit var btnFetchData: Button
    private lateinit var btnGetLocation: Button
    private lateinit var btnOpenDetails: Button
    private lateinit var tvResult: TextView
    private lateinit var tvLocation: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var switchSimulateNetworkFailure: Switch

    private var latestBillResult: String = "No utility data fetched yet."
    private var latestLocationResult: String = "Location not fetched yet."

    /**
     * Permission launcher for requesting location permission at runtime.
     */
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (fineGranted || coarseGranted) {
                getCurrentLocation()
            } else {
                tvLocation.text = "Permission denied. Location access was not granted."
                latestLocationResult = "Permission denied."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etName = findViewById(R.id.etName)
        etUnits = findViewById(R.id.etUnits)
        btnFetchData = findViewById(R.id.btnFetchData)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        btnOpenDetails = findViewById(R.id.btnOpenDetails)
        tvResult = findViewById(R.id.tvResult)
        tvLocation = findViewById(R.id.tvLocation)
        progressBar = findViewById(R.id.progressBar)
        switchSimulateNetworkFailure = findViewById(R.id.switchSimulateNetworkFailure)

        btnFetchData.setOnClickListener {
            fetchUtilityData()
        }

        btnGetLocation.setOnClickListener {
            checkLocationPermission()
        }

        btnOpenDetails.setOnClickListener {
            openDetailsScreen()
        }
    }

    /**
     * Validates user inputs safely.
     *
     * @param name User's name
     * @param unitsText Utility usage input as text
     * @return Error message if invalid, otherwise null
     */
    private fun validateInput(name: String, unitsText: String): String? {
        if (name.isBlank()) {
            return "Please enter your name."
        }

        if (!name.matches(Regex("^[a-zA-Z ]+$"))) {
            return "Name must contain letters only."
        }

        if (unitsText.isBlank()) {
            return "Please enter utility units."
        }

        val units = unitsText.toDoubleOrNull()
        if (units == null || units < 0) {
            return "Please enter a valid positive number for units."
        }

        if (units > 10000) {
            return "Units value is too high. Please enter a realistic number."
        }

        return null
    }

    /**
     * Fetches simulated utility data asynchronously using Coroutines.
     * Shows loading, handles network failure, and updates UI safely.
     */
    private fun fetchUtilityData() {
        val name = etName.text.toString().trim()
        val unitsText = etUnits.text.toString().trim()

        val validationError = validateInput(name, unitsText)
        if (validationError != null) {
            tvResult.text = validationError
            return
        }

        val units = unitsText.toDouble()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                progressBar.visibility = View.VISIBLE
                tvResult.text = "Loading utility data..."

                val result = withContext(Dispatchers.IO) {
                    delay(2000)

                    if (switchSimulateNetworkFailure.isChecked) {
                        throw Exception("Simulated network failure")
                    }

                    val ratePerUnit = 0.75
                    val totalBill = units * ratePerUnit

                    "Hello $name\n" +
                            "Units Used: $units\n" +
                            "Rate Per Unit: $$ratePerUnit\n" +
                            "Estimated Bill: $${String.format("%.2f", totalBill)}"
                }

                latestBillResult = result
                tvResult.text = result

            } catch (e: Exception) {
                tvResult.text = "Network failure. Please try again later."
                latestBillResult = "Data fetch failed due to network issue."
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }

    /**
     * Checks whether location permission has been granted.
     */
    private fun checkLocationPermission() {
        val finePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarsePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (finePermission || coarsePermission) {
            getCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    /**
     * Fetches the current location if permission is granted.
     */
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            tvLocation.text = "Permission denied. Cannot fetch location."
            latestLocationResult = "Permission denied."
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    latestLocationResult =
                        "Latitude: ${location.latitude}\nLongitude: ${location.longitude}"
                    tvLocation.text = latestLocationResult
                } else {
                    latestLocationResult = "Location unavailable. Please enable GPS."
                    tvLocation.text = latestLocationResult
                }
            }
            .addOnFailureListener {
                latestLocationResult = "Failed to fetch location."
                tvLocation.text = latestLocationResult
            }
    }

    /**
     * Opens the details screen and passes fetched data safely.
     */
    private fun openDetailsScreen() {
        val name = etName.text.toString().trim().ifBlank { "User" }

        val intent = Intent(this, DetailsActivity::class.java)
        intent.putExtra("USER_NAME", name)
        intent.putExtra("BILL_RESULT", latestBillResult)
        intent.putExtra("LOCATION_RESULT", latestLocationResult)
        startActivity(intent)
    }
}