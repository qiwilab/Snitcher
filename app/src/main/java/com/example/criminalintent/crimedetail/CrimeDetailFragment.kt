package com.example.criminalintent.crimedetail

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.R
import com.example.criminalintent.database.Crime
import com.example.criminalintent.databinding.FragmentCrimeDetailBinding
import com.example.criminalintent.dialogs.DatePickerFragment
import com.example.criminalintent.dialogs.TimePickerFragment
import com.example.criminalintent.utils.getScaledBitmap
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeDetailFragment : Fragment() {

    private var _binding: FragmentCrimeDetailBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: CrimeDetailFragmentArgs by navArgs()

    private val crimeDetailViewModel: CrimeDetailViewModel by viewModels {
        CrimeDetailViewModelFactory(args.crimeId)
    }

    // create launcher for using implicit intent
    private val selectSuspect = registerForActivityResult(
        ActivityResultContracts.PickContact() // select contact from contact's app
    ) { uri: Uri? -> // return contact uri from contact's app
        uri?.let { parseContactSelection(it) }
    }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto ->
        if (didTakePhoto && photoName != null) {
            crimeDetailViewModel.updateCrime { oldCrime ->
                oldCrime.copy(photoFileName = photoName)
            }
        }
    }

    private var photoName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrimeDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // start registering callback to override of Back button behavior
        val backButtonCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (crimeDetailViewModel.crime.value?.title?.isBlank() == true) {
                    Toast.makeText(requireContext(),
                        getString(R.string.enter_the_title_toast),
                        Toast.LENGTH_SHORT).show()
                } else { /* if Crime title is not blank return Back button behavior and navigate to list */
                    isEnabled = false
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, backButtonCallback)
        // end of registering callback to override of Back button behavior

        binding.apply {
            crimeTitle.doOnTextChanged { text, _, _, _ ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(title = text.toString())
                }
            }

            crimeSolved.setOnCheckedChangeListener { _, isChecked ->
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(isSolved = isChecked)
                }
            }

            crimeSuspect.setOnClickListener {
                selectSuspect.launch(null)
            }

            // check whether appropriate activity to execute is
            val selectSuspectIntent = selectSuspect.contract.createIntent(
                requireContext(),
                null
            )
            crimeSuspect.isEnabled = canResolveIntent(selectSuspectIntent) // button's activation

            crimeCamera.setOnClickListener {
                photoName = "IMG_${Date()}.JPG" // creating a name
                val photoFile = File( // creating a file with name
                    requireContext().applicationContext.filesDir,
                    photoName as String
                )
                val photoUri = FileProvider.getUriForFile( // creating a Uri for launcher
                    requireContext(),
                    "com.example.criminalintent.fileprovider",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }

            val canTakePhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .resolveActivity(requireContext().packageManager) != null
            crimeCamera.isEnabled = canTakePhoto

            crimePhoto.setOnClickListener {
                val photoFileName = crimeDetailViewModel.crime.value?.photoFileName
                photoFileName?.let {
                    findNavController().navigate(
                        CrimeDetailFragmentDirections.showFullPhoto(photoFileName))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeDetailViewModel.crime.collect { crime ->
                    crime?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(DatePickerFragment.REQUEST_KEY_DATE) {
            _, bundle ->
            val newDateInMillis = bundle.getLong(DatePickerFragment.BUNDLE_KEY_DATE)
            val newDate = Date(newDateInMillis)
            crimeDetailViewModel.updateCrime { it.copy(date = newDate) }
        }

        setFragmentResultListener(TimePickerFragment.REQUEST_KEY_TIME) {
            _, bundle ->
            val newTimeInMillis = bundle.getLong(TimePickerFragment.BUNDLE_KEY_TIME)
            val newTime = Date(newTimeInMillis)
            crimeDetailViewModel.updateCrime { it.copy(date = newTime) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun updateUi(crime: Crime) {
        binding.apply {
            if (crimeTitle.text.toString() != crime.title) {
                crimeTitle.setText(crime.title)
            }
            val dateFormat = DateFormat.getMediumDateFormat(requireContext())
            val formattedDate = dateFormat.format(crime.date)
            crimeDate.text = getString(R.string.crime_date, formattedDate)
            crimeDate.setOnClickListener {
                findNavController().navigate(
                    CrimeDetailFragmentDirections.selectDate(crime.date)
                )
            }
            val timeFormat = DateFormat.getTimeFormat(requireContext())
            val formattedTime = timeFormat.format(crime.date)
            crimeTime.text = getString(R.string.crime_time, formattedTime)
            crimeTime.setOnClickListener {
                findNavController().navigate(
                    CrimeDetailFragmentDirections.selectTime(crime.date)
                )
            }
            crimeSolved.isChecked = crime.isSolved

            /* sending crimeReport to external app */
            crimeReport.setOnClickListener {
                val reportIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, getCrimeReport(crime))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
                }

                val chooserIntent = Intent.createChooser(
                    reportIntent,
                    getString(R.string.send_report)
                )
                startActivity(chooserIntent)
            }

            crimeSuspect.text = crime.suspect.ifEmpty {
                getString(R.string.crime_suspect_text)
            }

            // Call suspect' button processing
            val crimeSuspectIsBlank = crime.suspect.isBlank()
            callSuspect.isEnabled = !crimeSuspectIsBlank
            callSuspect.text = if (crimeSuspectIsBlank) {
                getString(R.string.call_suspect_text)
            } else {
                getString(R.string.dial_suspect, crime.suspect)
            }
            callSuspect.setOnClickListener {
                showPhoneNumberSelectionDialog(
                    contactName = crime.suspect, contactUri = Uri.parse(crime.contactUri)
                )
            }

            updatePhoto(crime.photoFileName)
        }
    }

    private fun getCrimeReport(crime: Crime): String {
        val solvedString = if (crime.isSolved) {
            getString(R.string.crime_report_solved)
        } else {
            getString(R.string.crime_report_unsolved)
        }

        val dateString = DateFormat.format(DATE_FORMAT, crime.date).toString()
        val suspectText = if (crime.suspect.isBlank()) {
            getString(R.string.crime_report_no_suspect)
        } else {
            getString(R.string.crime_report_suspect, crime.suspect)
        }
        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspectText)
    }

    private fun parseContactSelection(contactUri: Uri) {
        //array for contentResolver
        val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)

        val queryCursor = requireActivity().contentResolver
            .query(contactUri, queryFields, null, null, null)

        queryCursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val suspect = cursor.getString(0)
                crimeDetailViewModel.updateCrime { oldCrime ->
                    oldCrime.copy(suspect = suspect, contactUri = contactUri.toString())
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(intent, MATCH_DEFAULT_ONLY)
        return resolvedActivity != null
    }

    private fun dialPhoneNumber(phoneNumber: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(dialIntent)
    }

    private fun getContactId(contactUri: Uri): String {
        val queryFields = arrayOf(ContactsContract.Contacts._ID)
        val queryCursor = requireActivity().contentResolver.query(
            contactUri, queryFields, null, null, null
        )
        queryCursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            }
        }
        throw IllegalArgumentException("Unable to retrieve contact ID")
    }

    private fun getPhoneNumbersFromContactUri(contactUri: Uri): List<String> {
        val phoneNumbers = mutableListOf<String>()
        val contactId = getContactId(contactUri)

        val queryCursor = requireActivity().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        queryCursor?.use { cursor ->
            while (cursor.moveToNext()) {
                val phoneNumber = cursor.getString(
                    cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                )
                phoneNumbers.add(phoneNumber)
            }
        }
        return phoneNumbers
    }

    private fun showPhoneNumberSelectionDialog(contactName: String, contactUri: Uri) {
        val dialogTitle = getString(R.string.select_suspect_number, contactName)
//        "Select a number for $it"
        val phoneNumbers = getPhoneNumbersFromContactUri(contactUri)

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setItems(phoneNumbers.toTypedArray()) { _, selected ->
                val selectedNumber = phoneNumbers[selected]
                dialPhoneNumber(selectedNumber)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.crimePhoto.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }

            if (photoFile?.exists() == true) {
                binding.crimePhoto.doOnLayout { measuredView ->
                    val scaledBitmap = getScaledBitmap(
                        photoFile.path,
                        measuredView.width,
                        measuredView.height
                    )
                    binding.crimePhoto.isEnabled = true
                    binding.crimePhoto.setImageBitmap(scaledBitmap)
                    binding.crimePhoto.tag = photoFileName
                    binding.crimePhoto.contentDescription =
                        getString(R.string.crime_photo_image_description)
                    binding.crimePhoto.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
                }
            } else {
                binding.crimePhoto.setImageBitmap(null)
                binding.crimePhoto.tag = null
                binding.crimePhoto.isEnabled = false
                binding.crimePhoto.contentDescription =
                    getString(R.string.crime_photo_no_image_description)
                binding.crimePhoto.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            }
        }
    }
}