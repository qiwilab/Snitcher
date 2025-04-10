package com.example.criminalintent.crimelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.criminalintent.CrimeRepository
import com.example.criminalintent.database.Crime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrimeListViewModel : ViewModel() {
    /* start of repository functionality */
    private val crimeRepository = CrimeRepository.get()

    private val _crimes: MutableStateFlow<List<Crime>> = MutableStateFlow(emptyList())
    val crimes: StateFlow<List<Crime>>
        get() = _crimes.asStateFlow()

    init {
        viewModelScope.launch {
            crimeRepository.getCrimes().collect {
                _crimes.value = it
            }
        }
    }

    suspend fun addCrime(crime: Crime) {
        crimeRepository.addCrime(crime)
    }

    suspend fun deleteCrimes(crimesToDelete: StateFlow<Set<Crime>>) {
        crimeRepository.deleteCrimes(crimesToDelete = crimesToDelete.value)
    }
    /* end of repository functionality */

    /* start of menu and element's selection functionality */

    private val _isSelectionMode = MutableStateFlow(false) // menu selection mode selector
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private var _selectedItems = MutableStateFlow<Set<Crime>>(emptySet())
    val selectedItems: StateFlow<Set<Crime>>
        get() = _selectedItems.asStateFlow()

    private var _selectAllCrimesFlag = MutableStateFlow(false)
    val selectAllCrimesFlag: StateFlow<Boolean>
        get() = _selectAllCrimesFlag

    fun toggleSelectionMode() {
        _isSelectionMode.value = !_isSelectionMode.value
    }

    fun updateSelectedItem(crime: Crime, isChecked: Boolean) {
        val currentItems = _selectedItems.value.toMutableSet()
        if (isChecked) {
            currentItems.add(crime)
        } else {
            currentItems.remove(crime)
        }
        _selectedItems.value = currentItems
    }

    fun selectAllCrimes(isChecked: Boolean) {
        _selectAllCrimesFlag.value = isChecked
        val updatedItems = if (_selectAllCrimesFlag.value) {
            crimes.value.toSet()
        } else {
            emptySet()
        }
        _selectedItems.value = updatedItems
    }

    fun cancelSelectionMode() {
        _isSelectionMode.value = false
        _selectAllCrimesFlag.value = false
    }
    /* end menu and element's selection functionality */
}