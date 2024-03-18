package org.freedu.locationsharingapp

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthenticationViewModel

    private lateinit var firestoreViewModel: FirestoreViewModel
    private lateinit var userAdapter: UserAdapter
    private lateinit var recyclerViewUsers: RecyclerView

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLocation()
            } else {
                // Permission denied
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        authViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationViewModel.initializeFusedLocationClient(fusedLocationClient)

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the permission
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // Permission is already granted
            getLocation()
        }

        recyclerViewUsers = findViewById(R.id.userRV)

        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        userAdapter = UserAdapter(emptyList())
        recyclerViewUsers.apply {
            adapter = userAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        fetchUsers()
    }

    private fun fetchUsers() {
        firestoreViewModel.getAllUsers { userList ->
            userAdapter.updateData(userList)
        }
    }

    private fun getLocation() {
        locationViewModel.getLastLocation { location ->
            // Save location to Firestore for the current user
            authViewModel.getCurrentUserId()?.let { userId ->
                firestoreViewModel.updateUserLocation(userId, location)
            }
        }
    }
}