package com.example.plantsaver

import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel

class VerificationViewModel: ViewModel() {
    var nameText = mutableStateOf("")
    var speciesText = mutableStateOf("")
    var frequencyText = mutableStateOf("")

    var nameError = mutableStateOf(false)
    var speciesError = mutableStateOf(false)
    var frequencyError = mutableStateOf(false)

    fun validation(): Boolean {
        nameError.value = false
        speciesError.value = false
        frequencyError.value = false

        if(nameText.value.isEmpty()){
            nameError.value = true
        }

        if(speciesText.value.isEmpty()){
            speciesError.value = true
        }

        if(!frequencyText.value.isDigitsOnly() || frequencyText.value.isEmpty()){
            frequencyError.value = true
        }

        if(!nameError.value && !speciesError.value && !frequencyError.value){
            return true
        }
        return false
    }

    fun clear(){
        nameText.value = ""
        speciesText.value = ""
        frequencyText.value = ""
    }
}