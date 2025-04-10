package com.example.criminalintent.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import java.util.Calendar

class TimePickerFragment : DialogFragment() {

    private val args: TimePickerFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val calendar = Calendar.getInstance()
        calendar.time = args.crimeTime
        val initialHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialMinute = calendar.get(Calendar.MINUTE)
        val is24HourView = true

        val timeListener = TimePickerDialog.OnTimeSetListener {
            _: TimePicker, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val resultTime = calendar.timeInMillis
            setFragmentResult(REQUEST_KEY_TIME, bundleOf(BUNDLE_KEY_TIME to resultTime))
        }

        return TimePickerDialog(
            requireContext(),
            timeListener,
            initialHour,
            initialMinute,
            is24HourView
        )
    }

    companion object {
        const val REQUEST_KEY_TIME = "REQUEST_KEY_TIME"
        const val BUNDLE_KEY_TIME = "BUNDLE_KEY_TIME"
    }
}