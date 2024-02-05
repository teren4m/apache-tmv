package com.github.teren4m.tvm.test.image

import android.graphics.Bitmap
import android.media.Image
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer

class ImagePreprocessDefault{

    private val imageConverter = ImageConverter()
    suspend fun preProcess(
        img: Image,
        imgsz: Int,
    ): Pair<TensorImage, Bitmap> {
        val b = imageConverter.toBitmap(img)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imgsz, imgsz, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(b)
        val inputTensor = imageProcessor.process(tensorImage)
        return Pair(inputTensor, b)
    }
}