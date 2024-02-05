package com.github.teren4m.tvm.test

import android.Manifest.permission.CAMERA
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import androidx.lifecycle.lifecycleScope
import com.github.teren4m.tvm.test.databinding.ActivityMainBinding
import com.github.teren4m.tvm.test.image.ImagePreprocessDefault
import com.github.teren4m.tvm.test.tvm.TvmUtils
import com.github.teren4m.tvm.test.utils.CameraProcess
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val cameraProcess by lazy {
        CameraProcess(
            this@MainActivity, this, this@MainActivity, binding.cameraPreviewWrap
        )
    }

    private val preProcess = ImagePreprocessDefault()
    private lateinit var tvmUtils: TvmUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window.statusBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        tvmUtils = TvmUtils()
        with(binding) {
            cameraPreviewMatch.scaleType = PreviewView.ScaleType.FILL_START
        }
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                val prev = System.currentTimeMillis()
                tvmUtils.init(this@MainActivity)
                val curr = System.currentTimeMillis()
                withContext(Dispatchers.Main) {
                    binding.initTime.setText("${curr - prev} ms")
                    startCamera()
                }
            }
        }
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

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        lifecycleScope.launch {
            withContext(Dispatchers.Default) {
                val result = preProcess.preProcess(image.image!!, 224)
                val chw = result.first.tensorBuffer.floatArray
                val prev = System.currentTimeMillis()
                val inferenceResult = tvmUtils.inference(chw)
                for (item in inferenceResult) {
                    Log.i("inferenceResult", item)
                }
                val curr = System.currentTimeMillis()
                withContext(Dispatchers.Main) {
                    binding.inferenceTime.setText("${curr - prev} ms")
                }
                image.close()
            }
        }
    }

}