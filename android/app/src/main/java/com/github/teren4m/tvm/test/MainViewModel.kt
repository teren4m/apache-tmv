package com.github.teren4m.tvm.test

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.teren4m.tvm.test.image.ImageConverter
import com.github.teren4m.tvm.test.model.DisplayConfig
import com.github.teren4m.tvm.test.utils.Fps
import com.github.teren4m.tvm.test.utils.Optional
import com.github.teren4m.tvm.test.utils.SmaDouble
import com.github.teren4m.tvm.test.utils.channel.DataChannel
import com.github.teren4m.tvm.test.utils.mapFirst
import com.github.teren4m.tvm.test.utils.toOptional
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {


    val imageChannel = DataChannel<Bitmap>()
    val detectTime = MutableLiveData(0.0)
    private val modelConfigLiveData = MutableLiveData<ModelConfig>()
    private val fps = Fps()

    init {
        val sma = SmaDouble(30)
        viewModelScope.launch {
            imageChannel.flow
                .mapFirst {
                    it.toString()
                    fps.tick()
                }
                .collectLatest {
                    detectTime.postValue(sma.add(it))
                }
        }
    }


    fun openModel(model: String, delegate: String) {
        if (model.isNotEmpty() && delegate.isNotEmpty()) {
            val modelConfig = ModelConfig(model, delegate)
            if (modelConfigLiveData.value != modelConfig) {
                modelConfigLiveData.value = modelConfig
            }
        }
    }
}

data class ModelInfo(val isInit: Boolean, val config: ModelConfig)

data class ModelConfig(val model: String, val delegate: String)