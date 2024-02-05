package com.github.teren4m.tvm.test.tvm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;

import androidx.camera.core.ImageProxy;


import org.apache.tvm.Device;
import org.apache.tvm.Function;
import org.apache.tvm.Module;
import org.apache.tvm.NDArray;
import org.apache.tvm.TVMType;
import org.apache.tvm.TVMValue;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.PriorityQueue;

public class TvmUtils {

    static {
        System.loadLibrary("tvm4j_runtime_packed");
    }
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
    private Module graphExecutorModule;
    private AssetManager assetManager;
    private JSONObject labels;

    public TvmUtils(){
    }

    private byte[] getBytesFromFile(AssetManager assets, String fileName) throws IOException {
        InputStream is = assets.open(fileName);
        int length = is.available();
        byte[] bytes = new byte[length];
        // Read in the bytes
        int offset = 0;
        int numRead;
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + fileName);
        }
        return bytes;
    }

    private String getTempLibFilePath(String fileName) throws IOException {
        File tempDir = File.createTempFile("tvm4j_demo_", "");
        if (!tempDir.delete() || !tempDir.mkdir()) {
            throw new IOException("Couldn't create directory " + tempDir.getAbsolutePath());
        }
        return (tempDir + File.separator + fileName);
    }

    public int init(Context context){
        if (assetManager == null) {
            assetManager = context.getAssets();
        }
        String model = MODELS + "/mobilenet_v2";
        String labelFilename = MODEL_LABEL_FILE;
        Log.i(TAG, "Reading labels from: " + model + "/" + labelFilename);
        try {
            labels = new JSONObject(new String(getBytesFromFile(assetManager, model + "/" + labelFilename)));
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Problem reading labels name file!", e);
            return -1;//failure
        }

        // load json graph
        String modelGraph;
        String graphFilename = MODEL_GRAPH_FILE;
        Log.i(TAG, "Reading json graph from: " + model + "/" + graphFilename);
        try {
            modelGraph = new String(getBytesFromFile(assetManager, model + "/" + graphFilename));
        } catch (IOException e) {
            Log.e(TAG, "Problem reading json graph file!", e);
            return -1;//failure
        }

        // upload tvm compiled function on application cache folder
        String libCacheFilePath;
        String libFilename = EXE_GPU ? MODEL_CL_LIB_FILE : MODEL_CPU_LIB_FILE;
        Log.i(TAG, "Uploading compiled function to cache folder");
        try {
            libCacheFilePath = getTempLibFilePath(libFilename);
            byte[] modelLibByte = getBytesFromFile(assetManager, model + "/" + libFilename);
            FileOutputStream fos = new FileOutputStream(libCacheFilePath);
            fos.write(modelLibByte);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "Problem uploading compiled function!", e);
            return -1;//failure
        }

        // load parameters
        byte[] modelParams;
        try {
            modelParams = getBytesFromFile(assetManager, model + "/" + MODEL_PARAM_FILE);
        } catch (IOException e) {
            Log.e(TAG, "Problem reading params file!", e);
            return -1;//failure
        }

        Log.i(TAG, "creating java tvm device...");
        // create java tvm device
        Device tvmDev = EXE_GPU ? Device.opencl() : Device.cpu();

        Log.i(TAG, "loading compiled functions...");
        Log.i(TAG, libCacheFilePath);
        // tvm module for compiled functions
        Module modelLib = Module.load(libCacheFilePath);


        // get global function module for graph executor
        Log.i(TAG, "getting graph executor create handle...");

        Function runtimeCreFun = Function.getFunction("tvm.graph_executor.create");
        Log.i(TAG, "creating graph executor...");

        Log.i(TAG, "device type: " + tvmDev.deviceType);
        Log.i(TAG, "device id: " + tvmDev.deviceId);

        TVMValue runtimeCreFunRes = runtimeCreFun.pushArg(modelGraph)
                .pushArg(modelLib)
                .pushArg(tvmDev.deviceType)
                .pushArg(tvmDev.deviceId)
                .invoke();

        Log.i(TAG, "as module...");
        graphExecutorModule = runtimeCreFunRes.asModule();
        Log.i(TAG, "getting graph executor load params handle...");
        // get the function from the module(load parameters)
        Function loadParamFunc = graphExecutorModule.getFunction("load_params");
        Log.i(TAG, "loading params...");
        loadParamFunc.pushArg(modelParams).invoke();
        // release tvm local variables
        modelLib.release();
        loadParamFunc.release();
        runtimeCreFun.release();
        mCurModel = model;
        return 0;
    }

    @SuppressLint("DefaultLocale")
    public String[] inference(float[] chw) {
        NDArray inputNdArray = NDArray.empty(new long[]{1, IMG_CHANNEL, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE}, new TVMType("float32"));
        inputNdArray.copyFrom(chw);
        Function setInputFunc = graphExecutorModule.getFunction("set_input");
        setInputFunc.pushArg(INPUT_NAME).pushArg(inputNdArray).invoke();
        // release tvm local variables
        inputNdArray.release();
        setInputFunc.release();

        // get the function from the module(run it)
        Function runFunc = graphExecutorModule.getFunction("run");
        runFunc.invoke();
        // release tvm local variables
        runFunc.release();

        // get the function from the module(get output data)
        NDArray outputNdArray = NDArray.empty(new long[]{1, 1000}, new TVMType("float32"));
        Function getOutputFunc = graphExecutorModule.getFunction("get_output");
        getOutputFunc.pushArg(OUTPUT_INDEX).pushArg(outputNdArray).invoke();
        float[] output = outputNdArray.asFloatArray();
        // release tvm local variables
        outputNdArray.release();
        getOutputFunc.release();

        if (null != output) {
            String[] results = new String[5];
            // top-5
            PriorityQueue<Integer> pq = new PriorityQueue<>(1000, (Integer idx1, Integer idx2) -> output[idx1] > output[idx2] ? -1 : 1);

            // display the result from extracted output data
            for (int j = 0; j < output.length; ++j) {
                pq.add(j);
            }
            for (int l = 0; l < 5; l++) {
                //noinspection ConstantConditions
                int idx = pq.poll();
                if (idx < labels.length()) {
                    try {
                        results[l] = String.format("%.2f", output[idx]) + " : " + labels.getString(Integer.toString(idx));
                    } catch (JSONException e) {
                        Log.e(TAG, "index out of range", e);
                    }
                } else {
                    results[l] = "???: unknown";
                }
            }
            return results;
        }
        return new String[0];
    }
}
