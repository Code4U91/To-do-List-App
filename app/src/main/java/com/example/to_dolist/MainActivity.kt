package com.example.to_dolist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.to_dolist.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


// Check and request POST_NOTIFICATIONS permission if SDK version is 33 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE)
            }
        }

        // Handle the intent to navigate to ContentFragment
        intent?.let {
            if (it.hasExtra("taskId")) {
                val taskId = it.getLongExtra("taskId", -1L)
                if (taskId != -1L) {
                    // Navigate to ContentFragment with the taskId
                    navigateToContentFragment(taskId)
                }
            }
        }

    }

    private fun navigateToContentFragment(taskId: Long) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        val action = NavGraphDirections.actionGlobalContentFragment(taskId)
        navController.navigate(action)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                } else {
                    // Permission denied
                    Toast.makeText(this,
                        "Permission not granted",
                        Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle the new intent to navigate to ContentFragment
        if (intent.hasExtra("taskId")) {
            val taskId = intent.getLongExtra("taskId", -1)
            if (taskId != -1L) {
                // Navigate to ContentFragment with the taskId
                navigateToContentFragment(taskId)
            }
        }
    }
}