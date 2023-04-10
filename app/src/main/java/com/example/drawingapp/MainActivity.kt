package com.example.drawingapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity() : AppCompatActivity()
{
    lateinit var colorTab: ColorTab
    lateinit var drawingView: DrawingView
    lateinit var imageBack: ImageView
    lateinit var seekBar: SeekBar
    lateinit var tvBrushSize: TextView
    private var dialog: Dialog? = null



    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted) {
                    if(permissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    {
                        lifecycleScope.launch{
                            loadImage()
                        }
                    }
                }
                else {
                    if(permissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    {
                        Toast.makeText(
                            this,
                            "You denied the permission.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }


    val result =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                result ->
                showProgressDialog()
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = Glide.with(this@MainActivity)
                    .asBitmap()
                    .load(result.data?.data)
                    .submit()
                    .get()
                withContext(Dispatchers.Main) {
                    imageBack?.setImageBitmap(bitmap)
                    dismissProgressDialog()
                }
            }
        }


    val shareResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult())
        {
            result -> getImage()

        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView = findViewById(R.id.drawingView)
        drawingView.brushSize = ((3 + 0.3)  * 4).toFloat()
        seekBar = findViewById(R.id.sb_brush_size)
        tvBrushSize = findViewById(R.id.tv_brush_size)
        tvBrushSize.text = "Brush size: " + ((drawingView.brushSize.toInt() - 0.3) / 4).toInt()
        seekBar.progress = ((drawingView.brushSize.toInt() - 0.3) / 4).toInt()

        val button1: Button = findViewById(R.id.buttonBlack)
        val button2: Button = findViewById(R.id.buttonBlue)
        val button3: Button = findViewById(R.id.buttonRed)
        val button4: Button = findViewById(R.id.buttonGreen)
        val button5: Button = findViewById(R.id.buttonOrange)
        val button6: Button = findViewById(R.id.buttonCyan)
        val button7: Button = findViewById(R.id.buttonYellow)
        imageBack = findViewById(R.id.image_back)


        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)
            {
                tvBrushSize.text = "Brush size: $progress"
                drawingView.brushSize = ((progress + 0.3)  * 4).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)
            {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?)
            {
            }

        })

        colorTab = ColorTab(button1, button2, button3,
            button4, button5, button6, button7)

        val buttonExportImage: ImageView = findViewById(R.id.buttonExportImage)
        buttonExportImage.setOnClickListener {
            requestStoragePermission()
        }

        for (button in colorTab.colors!!) {
            button.setOnClickListener {
                colorTab.select(button)
                val color = (button.background as GradientDrawable).color
                val newColor = ColorUtils.setAlphaComponent(color!!.defaultColor, 255)
                drawingView.color = newColor
            }
        }

        val backButton: ImageView = findViewById(R.id.buttonStepBack)
        backButton.setOnClickListener {
            drawingView.goBack()
        }

        val saveImageButton: ImageView = findViewById(R.id.buttonSaveImage)
        saveImageButton.setOnClickListener{
            lifecycleScope.launch {
                showProgressDialog()
                saveImage(getImage())
            }
        }
    }

    private fun shareImage(result: String)
    {
        MediaScannerConnection.scanFile(this, arrayOf(result), null) {
            path, uri ->
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Share"))
        }
    }

    private fun getImage(): Bitmap
    {
        val view: FrameLayout = findViewById(R.id.frame_layout)
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun saveImage(bitmap: Bitmap)
    {
        withContext(Dispatchers.IO)
        {
            try
            {
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                val f = File(
                    externalCacheDir?.absolutePath.toString() +
                        File.separator + "drawing_app_" + System.currentTimeMillis() / 1000 + ".png")
                Log.i("fileNameQ", f.absolutePath.toString())
                val fo = FileOutputStream(f)
                fo.write(bytes.toByteArray())
                fo.close()
                dismissProgressDialog()
                if(f.absolutePath.isNotEmpty())
                {
                    shareImage(f.absolutePath)
                }
            }
            catch (e: Exception)
            {
                println(e.stackTrace)
            }
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                // Show an explanation to the user as to why the permission is needed
                showRationaleDialog()
            } else {
                requestPermission.launch(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        } else {
            lifecycleScope.launch{
                loadImage()
            }
        }
    }

    private fun showRationaleDialog()
    {
        AlertDialog.Builder(this)
            .setTitle("Permission needed")
            .setMessage("This permission is needed to access your photos")
            .setPositiveButton("Ok") { _, _ ->
                // Request the permission again
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create().show()
    }

    private fun loadImage()
    {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        result.launch(intent)

    }

    private fun showProgressDialog()
    {
        dialog = Dialog(this)
        dialog?.setContentView(R.layout.dialog_load)
        dialog?.show()
    }

    private fun dismissProgressDialog()
    {
        if(dialog != null)
        {
            dialog?.dismiss()
            dialog = null
        }
    }
}