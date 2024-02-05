package com.github.teren4m.tvm.test.tvm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import androidx.camera.core.ImageProxy;


import java.io.IOException;

public class TvmUtils {

    private static final String TAG = TvmUtils.class.getSimpleName();

    // TVM constants
    private static final int OUTPUT_INDEX = 0;
    private static final int IMG_CHANNEL = 3;
    private static final boolean EXE_GPU = false;
    private static final int MODEL_INPUT_SIZE = 224;
    private static final String MODEL_CL_LIB_FILE = "deploy_lib_opencl.so";
    private static final String MODEL_CPU_LIB_FILE = "deploy_lib_cpu.so";
    private static final String MODEL_GRAPH_FILE = "deploy_graph.json";
    private static final String MODEL_PARAM_FILE = "deploy_param.params";
    private static final String MODEL_LABEL_FILE = "image_net_labels.json";
    private static final String MODELS = "models";
    private static String INPUT_NAME = "input_1";
    private static String[] models;
    private static String mCurModel = "";
    private final float[] mCHW = new float[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * IMG_CHANNEL];
    private final float[] mCHW2 = new float[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * IMG_CHANNEL];
    private Context context;

    public TvmUtils(Context context){
        this.context = context;
    }

//    private static Matrix getTransformationMatrix(
//            final int srcWidth,
//            final int srcHeight,
//            final int dstWidth,
//            final int dstHeight,
//            final int applyRotation,
//            final boolean maintainAspectRatio) {
//        final Matrix matrix = new Matrix();
//
//        if (applyRotation != 0) {
//            if (applyRotation % 90 != 0) {
//                Log.w(TAG, "Rotation of %d % 90 != 0 " + applyRotation);
//            }
//
//            // Translate so center of image is at origin.
//            matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f);
//
//            // Rotate around origin.
//            matrix.postRotate(applyRotation);
//        }
//
//        // Account for the already applied rotation, if any, and then determine how
//        // much scaling is needed for each axis.
//        final boolean transpose = (Math.abs(applyRotation) + 90) % 180 == 0;
//
//        final int inWidth = transpose ? srcHeight : srcWidth;
//        final int inHeight = transpose ? srcWidth : srcHeight;
//
//        // Apply scaling if necessary.
//        if (inWidth != dstWidth || inHeight != dstHeight) {
//            final float scaleFactorX = dstWidth / (float) inWidth;
//            final float scaleFactorY = dstHeight / (float) inHeight;
//
//            if (maintainAspectRatio) {
//                // Scale by minimum factor so that dst is filled completely while
//                // maintaining the aspect ratio. Some image may fall off the edge.
//                final float scaleFactor = Math.max(scaleFactorX, scaleFactorY);
//                matrix.postScale(scaleFactor, scaleFactor);
//            } else {
//                // Scale exactly to fill dst from src.
//                matrix.postScale(scaleFactorX, scaleFactorY);
//            }
//        }
//
//        if (applyRotation != 0) {
//            // Translate back from origin centered reference to destination frame.
//            matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f);
//        }
//
//        return matrix;
//    }

    public float[] getFrame(ImageProxy image) {
//        Bitmap imageBitmap = imageConverter.toBitmap(image);
//        // crop input image at centre to model input size
//        Bitmap cropImageBitmap = Bitmap.createBitmap(MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, Bitmap.Config.ARGB_8888);
//        Matrix frameToCropTransform = getTransformationMatrix(imageBitmap.getWidth(), imageBitmap.getHeight(),
//                MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, 0, true);
//        Canvas canvas = new Canvas(cropImageBitmap);
//        canvas.drawBitmap(imageBitmap, frameToCropTransform, null);
//
//        // image pixel int values
//        int[] pixelValues = new int[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE];
//        // image RGB float values
//
//        // pre-process the image data from 0-255 int to normalized float based on the
//        // provided parameters.
//        cropImageBitmap.getPixels(pixelValues, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE);
//        for (int j = 0; j < pixelValues.length; ++j) {
//            mCHW2[j * 3 + 0] = ((pixelValues[j] >> 16) & 0xFF) / 255.0f;
//            mCHW2[j * 3 + 1] = ((pixelValues[j] >> 8) & 0xFF) / 255.0f;
//            mCHW2[j * 3 + 2] = (pixelValues[j] & 0xFF) / 255.0f;
//        }
//
//        // pre-process the image rgb data transpose based on the provided parameters.
//        for (int k = 0; k < IMG_CHANNEL; ++k) {
//            for (int l = 0; l < MODEL_INPUT_SIZE; ++l) {
//                for (int m = 0; m < MODEL_INPUT_SIZE; ++m) {
//                    int dst_index = m + MODEL_INPUT_SIZE * l + MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * k;
//                    int src_index = k + IMG_CHANNEL * m + IMG_CHANNEL * MODEL_INPUT_SIZE * l;
//                    mCHW[dst_index] = mCHW2[src_index];
//                }
//            }
//        }

        return mCHW;
    }

//    private String[] getModels() {
//        String[] models;
//        try {
//            models = context.getAssets().list(MODELS);
//        } catch (IOException e) {
//            return null;
//        }
//        return models;
//    }
}
