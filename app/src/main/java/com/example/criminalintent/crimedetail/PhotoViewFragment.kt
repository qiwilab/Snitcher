package com.example.criminalintent.crimedetail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.criminalintent.databinding.FragmentPhotoViewBinding

class PhotoViewFragment : Fragment() {

    private var _binding: FragmentPhotoViewBinding? = null
    private val binding get() = checkNotNull(_binding)

    private val args: PhotoViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentPhotoViewBinding.inflate(layoutInflater, container, false)
            .also { _binding = it }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoFileName = args.photoFileName

        val defaultPath = requireContext().applicationContext.filesDir.path
        val filePath = "$defaultPath/$photoFileName"
        val bitmap = BitmapFactory.decodeFile(filePath)
        binding.crimeFullPhoto.setImageBitmap(bitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}