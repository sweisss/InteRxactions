package com.example.interxactions.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.interxactions.data.AdverseEventsRepository
import com.example.interxactions.data.DrugInfoService
import com.example.interxactions.data.Outcomes
import kotlinx.coroutines.launch

class AdverseEventsViewModel: ViewModel() {
    private val repository = AdverseEventsRepository(DrugInfoService.create())

    private val _outcomeCounts = MutableLiveData<Outcomes?>(null)
    val outcomeCounts: LiveData<Outcomes?> = _outcomeCounts

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    // Create LiveData objects for the error
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    fun loadReactionOutcomeCount(search: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getReactionOutcomeCount(search)
            _loading.value = false
            _outcomeCounts.value = result.getOrNull()
            _error.value = result.exceptionOrNull()?.message // Get the error message
        }
    }
}