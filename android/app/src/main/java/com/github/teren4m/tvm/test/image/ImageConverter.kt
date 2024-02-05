package com.github.teren4m.tvm.test.image

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class ImageConverter() {
    lateinit var bitmapBuffer: Bitmap

    private fun NV21toJPEG(nv21: ByteArray, width: Int, height: Int): ByteArray? {
        val out = ByteArrayOutputStream()
        val yuv = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        yuv.compressToJpeg(Rect(0, 0, width, height), 75, out)
        return out.toByteArray()
    }

    private fun YUV_420_888toNV21(image: Image): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)

        //U and V are swapped
        yBuffer[nv21, 0, ySize]
        vBuffer[nv21, ySize, vSize]
        uBuffer[nv21, ySize + vSize, uSize]
        return nv21
    }

    suspend fun toBitmap(image: Image): Bitmap = withContext(Dispatchers.Default) {
        val yuvBytes = arrayOfNulls<ByteArray>(3)
        val planes = image.planes
        val imageHeight = image.height
        val imagewWidth = image.width
        fillBytes(planes, yuvBytes)
        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride
        val rgbBytes = IntArray(imageHeight * imagewWidth)
        YUV420ToARGB8888(
            yuvBytes[0]!!,
            yuvBytes[1]!!,
            yuvBytes[2]!!,
            imagewWidth,
            imageHeight,
            yRowStride,
            uvRowStride,
            uvPixelStride,
            rgbBytes
        )
        if (!this@ImageConverter::bitmapBuffer.isInitialized) {
            bitmapBuffer =
                Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
        }
        bitmapBuffer.setPixels(rgbBytes, 0, imagewWidth, 0, 0, imagewWidth, imageHeight)
        bitmapBuffer
    }

    val kMaxChannelValue = 262143

    fun fillBytes(
        planes: Array<Image.Plane>,
        yuvBytes: Array<ByteArray?>
    ) {
        for (i in planes.indices) {
            val buffer: ByteBuffer = planes[i].getBuffer()
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer[yuvBytes[i]]
        }
    }

    fun YUV2RGB(y: Int, u: Int, v: Int): Int {
        var y = y
        var u = u
        var v = v
        y = if (y - 16 < 0)
            0
        else
            y - 16
        u -= 128
        v -= 128
        val y1192 = 1192 * y
        var r = y1192 + 1634 * v
        var g = y1192 - 833 * v - 400 * u
        var b = y1192 + 2066 * u

        // Clipping RGB values to be inside boundaries [ 0 , kMaxChannelValue ]
        r = if (r > kMaxChannelValue) {
            kMaxChannelValue
        } else if (r < 0) {
            0
        } else {
            r
        }
        g = if (g > kMaxChannelValue) {
            kMaxChannelValue
        } else if (g < 0) {
            0
        } else {
            g
        }
        b = if (b > kMaxChannelValue) {
            kMaxChannelValue
        } else if (b < 0) {
            0
        } else {
            b
        }
        return -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
    }

    fun YUV420ToARGB8888(
        yData: ByteArray,
        uData: ByteArray,
        vData: ByteArray,
        width: Int,
        height: Int,
        yRowStride: Int,
        uvRowStride: Int,
        uvPixelStride: Int,
        out: IntArray
    ) {
        for (i in out.indices) {
            val h = i / width
            val w = i % width

            val pY = yRowStride * h
            val pUV = uvRowStride * (h shr 1)
            val uvOffset = pUV + (w shr 1) * uvPixelStride
            out[i] = YUV2RGB(
                0xff and yData[pY + w].toInt(),
                0xff and uData[uvOffset].toInt(),
                0xff and vData[uvOffset].toInt()
            )
        }
    }
}