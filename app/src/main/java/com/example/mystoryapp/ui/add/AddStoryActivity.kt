package com.example.mystoryapp.ui.add

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mystoryapp.R
import com.example.mystoryapp.databinding.ActivityAddStoryBinding
import com.example.mystoryapp.reduceFileImage
import com.example.mystoryapp.AppResult
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.getImageUri
import com.example.mystoryapp.ui.main.MainActivity
import com.example.mystoryapp.uriToFile
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLat: Float? = null
    private var currentLon: Float? = null
    private val viewModel: AddStoryViewModel by viewModels {
        AddStoryViewModelFactory(Injection.provideRepository(this), this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                showToast(getString(R.string.permission_granted))
                if (binding.switchAddLocation.isChecked) {
                    requestLocation()
                }
            } else {
                showToast(getString(R.string.permission_denied))
                binding.switchAddLocation.isChecked = false
            }
        }


    private val launcherGallery =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                viewModel.currentImageUri = uri
                viewModel.selectedFile = uriToFile(uri, this)
                showImage()
            } else {
                Log.d("Photo Picker", "No media selected")
            }
        }

    private val launcherIntentCamera =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                viewModel.selectedFile = viewModel.currentImageUri?.let { uriToFile(it, this) }
                showImage()
            } else {
                viewModel.currentImageUri = null
                showToast(getString(R.string.take_img_failed))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = getString(R.string.add_story)

        viewModel.currentImageUri?.let {
            showImage()
        }

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnCamera.setOnClickListener {
            startCamera()
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }

        binding.switchAddLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestLocation()
            } else {
                viewModel.clearLocation()
            }
        }

        binding.buttonAdd.setOnClickListener {
            val description = binding.edAddDescription.text.toString()
            val file = viewModel.selectedFile ?: return@setOnClickListener

            if (description.isEmpty()) {
                showToast(getString(R.string.description_empty))
                return@setOnClickListener
            }

            if (binding.switchAddLocation.isChecked && (currentLat == null || currentLon == null)) {
                showToast(getString(R.string.wait_for_location))
                return@setOnClickListener
            }

            val reducedFile = file.reduceFileImage()
            viewModel.uploadStory(description, reducedFile, currentLat, currentLon)
        }

    }

    private fun observeViewModel() {
        viewModel.location.observe(this) { location ->
            if (location != null) {
                currentLat = location.first
                currentLon = location.second
            } else {
                currentLat = null
                currentLon = null
            }
        }
        viewModel.uploadAppResult.observe(this) { result ->
            when (result) {
                is AppResult.Loading -> showLoading(true)
                is AppResult.Success -> {
                    showLoading(false)
                    showToast(getString(R.string.story_success))

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("REFRESH_LIST", true)
                    }
                    startActivity(intent)
                    finish()
                }
                is AppResult.Error -> {
                    showLoading(false)
                    showToast(result.errorMessage)
                }
            }
        }
    }

    private fun startCamera() {
        viewModel.currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(viewModel.currentImageUri!!)
    }

    private fun openGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun showImage() {
        binding.ivPreview.setImageURI(viewModel.currentImageUri)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val lat = location.latitude.toFloat()
                val lon = location.longitude.toFloat()
                viewModel.updateLocation(lat, lon)
                showToast(getString(R.string.location_added))
            } else {
                showToast(getString(R.string.location_unavailable))
                binding.switchAddLocation.isChecked = false
            }
        }.addOnFailureListener {
            showToast(getString(R.string.location_error))
            binding.switchAddLocation.isChecked = false
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, REQUIRED_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
