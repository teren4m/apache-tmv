package com.github.teren4m.tvm.test

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.teren4m.tvm.test.utils.Fps
import com.github.teren4m.tvm.test.utils.SmaDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val detectTime = MutableLiveData(0.0)
    private val modelConfigLiveData = MutableLiveData<ModelConfig>()
    private val fps = Fps()
    private val sma = SmaDouble(30)

    fun update(imgArray: FloatArray) {
        val tick = fps.tick()
        detectTime.postValue(sma.add(tick))
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