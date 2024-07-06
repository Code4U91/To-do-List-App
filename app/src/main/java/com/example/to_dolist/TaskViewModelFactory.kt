package com.example.to_dolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TaskViewModelFactory (private val mainApplication: MainApplication) : ViewModelProvider.Factory  {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      //  return super.create(modelClass)

        if (modelClass.isAssignableFrom(SharedTaskViewModel::class.java))
        {
            @Suppress( "UNCHECKED_CAST")
            return SharedTaskViewModel(mainApplication) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}