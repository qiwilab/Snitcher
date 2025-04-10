package com.example.criminalintent.crimelist

import com.example.criminalintent.database.Crime
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class BindData(
    val isSelectionMode: StateFlow<Boolean>,
    val selectedItems: StateFlow<Set<Crime>>,
    val onCrimeClicked: (crimeId: UUID) -> Unit,
    val onCrimeLongClicked: () -> Unit,
    val onCheckedChange: (Crime, Boolean) ->  Unit
)
