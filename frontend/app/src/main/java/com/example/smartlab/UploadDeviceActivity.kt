package com.example.smartlab

import android.R
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartlab.addResources.RetrofitClient
import com.example.smartlab.addResources.SharedPreferencesFactory
import com.example.smartlab.api.DeviceApi
import com.example.smartlab.databinding.ActivityUploadDeviceBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadDeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadDeviceBinding
    private var selectedImageFile: File? = null
    private val PICK_IMAGE_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectImage.setOnClickListener { openGallery() }
        binding.btnUpload.setOnClickListener { uploadDevice() }
        setupTypeSpinner()
    }
    private fun setupTypeSpinner() {
        val types = listOf("LAMP", "CLIMATE", "ENERGY", "SENSOR")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.typeDevice.adapter = adapter
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri = data.data!!
            val inputStream = contentResolver.openInputStream(uri)
            val file = File(cacheDir, "upload.jpg")
            file.outputStream().use { inputStream?.copyTo(it) }
            selectedImageFile = file

            binding.ivPreview.setImageURI(uri)
            binding.ivPreview.visibility = View.VISIBLE
        }
    }

    private fun uploadDevice() {
        val title = binding.edTitle.text.toString()
        val description = binding.edDescription.text.toString()
        val inventoryNumber = binding.etInventoryNumber.text.toString()
        val type = binding.typeDevice.selectedItem.toString()
        val file = selectedImageFile

        if (title.isBlank() || description.isBlank() || inventoryNumber.isBlank() || type.isBlank() || file == null) {
            Toast.makeText(this, "Заповніть усі поля та виберіть зображення", Toast.LENGTH_SHORT).show()
            return
        }

        val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val inventoryPart = inventoryNumber.toRequestBody("text/plain".toMediaTypeOrNull())
        val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        lifecycleScope.launch {
            try {
                val token = SharedPreferencesFactory(this@UploadDeviceActivity).getSharedPreferences("TOKEN")!!
                val response = RetrofitClient.getInstance().create(DeviceApi::class.java) .uploadDevice(
                    titlePart, descriptionPart, inventoryPart, typePart, imagePart, "Bearer $token"
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val intent = Intent(this@UploadDeviceActivity, HomeActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@UploadDeviceActivity, "Помилка завантаження", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@UploadDeviceActivity, "Помилка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
