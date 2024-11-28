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
import androidx.core.content.ContextCompat
import com.example.mystoryapp.databinding.ActivityAddStoryBinding
import com.example.mystoryapp.reduceFileImage
import com.example.mystoryapp.Result
import com.example.mystoryapp.di.Injection
import com.example.mystoryapp.getImageUri
import com.example.mystoryapp.ui.main.MainActivity
import com.example.mystoryapp.uriToFile

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel: AddStoryViewModel by viewModels {
        AddStoryViewModelFactory(Injection.provideRepository(this), this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                showToast("Permission request denied")
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
                showToast("Failed to take a picture")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "Add Story"

        viewModel.currentImageUri?.let {
            showImage()
        }

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

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

        binding.buttonAdd.setOnClickListener {
            val description = binding.edAddDescription.text.toString()
            val file = viewModel.selectedFile ?: return@setOnClickListener

            if (description.isEmpty()) {
                showToast("Description cannot be empty")
                return@setOnClickListener
            }

            val reducedFile = file.reduceFileImage()
            viewModel.uploadStory(description, reducedFile)
        }
    }

    private fun observeViewModel() {
        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    showToast("Story uploaded successfully")

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("REFRESH_LIST", true)
                    }
                    startActivity(intent)
                    finish()
                }
                is Result.Error -> {
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

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        this, REQUIRED_PERMISSION
    ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}
