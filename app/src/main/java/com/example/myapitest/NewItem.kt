package com.example.myapitest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import androidx.core.content.ContextCompat.checkSelfPermission
class NewItem : AppCompatActivity() {

    private lateinit var binding: ActivityNewItemBinding

    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            uploadImageToFirebase()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupView()

    }




    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener {
            onSave()
        }
        binding.takePictureCta.setOnClickListener {
            onTakePicture()
        }
    }

    private fun onTakePicture() {
        if (checkSelfPermission(this, android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA
        )
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        // Obtém o diretório de armazenamento externo para imagens
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // cria um arquivo de imagem
        imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        return FileProvider.getUriForFile(
            this,
            "com.example.myapitest.fileprovider",// applicationId + .provider
            imageFile!!
        )
    }

    private fun uploadImageToFirebase() {
        // Obtém referência do Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference

        // Cria uma referência para a nossa imagem
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        onLoadingImage(true) // Coloca um load na tela
        imagesRef.putBytes(data)
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_upload_image, Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                imagesRef.downloadUrl
                    .addOnCompleteListener {
                        onLoadingImage(false)
                    }
                    .addOnSuccessListener { uri ->
                        binding.imageUrl.setText(uri.toString())
                    }
            }
    }

    private fun onLoadingImage(isLoading: Boolean) {
        binding.loadImageProgress.isVisible = isLoading
        binding.takePictureCta.isEnabled = !isLoading
        binding.saveCta.isEnabled = !isLoading
    }

    private fun onSave() {
//        if (!validateForm()) return
//        saveData()
    }

//    private fun saveData() {
//        val name = binding.name.text.toString()
//        val year = binding.year.text.toString()
//        val licese = binding.license.text.toString().toInt()
//        val imageUrl = binding.imageUrl.text.toString()
//        val location = selectedMarker?.position?.let { position ->
//            ItemLocation(
//                position.latitude,
//                position.longitude,
//                name
//            )
//        } ?: throw IllegalArgumentException("Usuário deveria ter a localização nesse ponto.")
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val itemValue = ItemValue(
//                SecureRandom().nextInt().toString(),
//                name,
//                surname,
//                profession,
//                imageUrl,
//                age,
//                location
//            )
//            val result = safeApiCall { RetrofitClient.apiService.addItem(itemValue) }
//            withContext(Dispatchers.Main) {
//                when (result) {
//                    is Result.Error -> {
//                        Toast.makeText(
//                            this@NewItemActivity,
//                            R.string.error_create,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                    is Result.Success -> {
//                        Toast.makeText(
//                            this@NewItemActivity,
//                            getString(R.string.success_create, name),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        finish()
//                    }
//                }
//            }
//        }
//    }


    companion object {

        const val REQUEST_CODE_CAMERA = 101

        fun newIntent(context: Context) = Intent(context, NewItem::class.java)
    }
}