package com.github.teren4m.tvm.test

import android.Manifest.permission.CAMERA
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.lifecycleScope
import com.github.teren4m.tvm.test.databinding.ActivityMainBinding
import com.github.teren4m.tvm.test.image.ImageConverter
import com.github.teren4m.tvm.test.model.DisplayConfig
import com.github.teren4m.tvm.test.utils.CameraProcess
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer {

    private val viewModel: MainViewModel by viewModels()

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val imageConverter by lazy {
        val displayConfig = DisplayConfig(
            rotation = display?.rotation ?: error("rotation cannot be null"),
            previewWidth = binding.cameraPreviewWrap.width,
            previewHeight = binding.cameraPreviewWrap.height,
        )
        ImageConverter(displayConfig)
    }

    private val cameraProcess by lazy {
        CameraProcess(
            this@MainActivity, this@MainActivity, binding.cameraPreviewWrap
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        with(binding) {
            cameraPreviewMatch.scaleType = PreviewView.ScaleType.FILL_START


            viewModel.detectTime.observe(this@MainActivity) {
                inferenceTime.text = it.toString()
            }
        }
        startCamera()
    }

    private fun startCamera() {
        PermissionRequester.initialize(applicationContext)
        val requester = PermissionRequester.instance()

        lifecycleScope.launch {
            val granted: Boolean = requester.areGranted(CAMERA)
            if (granted) {
                cameraProcess.startCamera()
            } else {
                requester.request(CAMERA).collect { p ->
                    when (p) {
                        is PermissionResult.Granted -> {
                            cameraProcess.startCamera()
                        }

                        else -> {}
                    }
                }
            }
        }
    }

    override fun analyze(image: ImageProxy) {
        lifecycleScope.launch {
            withContext(Dispatchers.Default){
                val bitmap = imageConverter.toBitmap(image)
                viewModel.imageChannel.update(bitmap)
            }
            image.close()
        }
    }

}