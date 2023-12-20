package com.github.teren4m.tvm.test.image

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import com.github.teren4m.tvm.test.model.DisplayConfig
import kotlin.math.max

class ImageConverter(private val displayConfig: DisplayConfig) {
    private val imageProcess = ImageProcess()

    fun toBitmap(image: ImageProxy): Bitmap {
        val yuvBytes = arrayOfNulls<ByteArray>(3)
        val planes = image.planes
        val imageHeight = image.height
        val imagewWidth = image.width
        imageProcess.fillBytes(planes, yuvBytes)
        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride
        val rgbBytes = IntArray(imageHeight * imagewWidth)
        imageProcess.YUV420ToARGB8888(
            yuvBytes[0],
            yuvBytes[1],
            yuvBytes[2],
            imagewWidth,
            imageHeight,
            yRowStride,
            uvRowStride,
            uvPixelStride,
            rgbBytes
        )
        val imageBitmap = Bitmap.createBitmap(imagewWidth, imageHeight, Bitmap.Config.ARGB_8888)
        imageBitmap.setPixels(rgbBytes, 0, imagewWidth, 0, 0, imagewWidth, imageHeight)
        val scale = max(
            displayConfig.previewHeight / (if (displayConfig.rotation % 180 == 0) imagewWidth else imageHeight).toDouble(),
            displayConfig.previewWidth / (if (displayConfig.rotation % 180 == 0) imageHeight else imagewWidth).toDouble()
        )
        val fullScreenTransform = imageProcess.getTransformationMatrix(
            imagewWidth,
            imageHeight,
            (scale * imageHeight).toInt(),
            (scale * imagewWidth).toInt(),
            if (displayConfig.rotation % 180 == 0) 90 else 0,
            false
        )
        val fullImageBitmap = Bitmap.createBitmap(
            imageBitmap,
            0,
            0,
            imagewWidth,
            imageHeight,
            fullScreenTransform,
            false
        )
        val cropImageBitmap =
            Bitmap.createBitmap(fullImageBitmap, 0, 0, displayConfig.previewWidth, displayConfig.previewHeight)
        imageBitmap.recycle()
        fullImageBitmap.recycle()
        return cropImageBitmap
    }
}