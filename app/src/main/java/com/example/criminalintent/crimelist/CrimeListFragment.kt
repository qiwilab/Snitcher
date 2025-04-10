package com.example.criminalintent.crimelist

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.criminalintent.R
import com.example.criminalintent.database.Crime
import com.example.criminalintent.databinding.FragmentCrimeListBinding
import com.example.criminalintent.databinding.MenuCheckboxBinding
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class CrimeListFragment : Fragment() {

    private var _binding: FragmentCrimeListBinding? = null
    private val binding get() = requireNotNull(_binding)

    private val crimeListViewModel: CrimeListViewModel by viewModels()
    private var menuProvider: MenuProvider? = null

    private lateinit var adapter: CrimeListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCrimeListBinding.inflate(inflater, container, false)
        binding.crimeRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCrimeListMenu()

        val bindData = BindData(
            isSelectionMode = crimeListViewModel.isSelectionMode,
            selectedItems = crimeListViewModel.selectedItems,
            onCrimeClicked = { crimeId ->
                findNavController().navigate(CrimeListFragmentDirections.showCrimeDetail(crimeId))
            },
            onCrimeLongClicked = {
                crimeListViewModel.toggleSelectionMode()
            },
            onCheckedChange = { crime, isChecked ->
                crimeListViewModel.updateSelectedItem(crime, isChecked)
            }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeListViewModel.crimes.collect { crimes ->
                    if (crimes.isEmpty()) {
                        binding.crimeRecyclerView.visibility = View.GONE
                        binding.emptyListView.visibility = View.VISIBLE
                    } else {
                        binding.crimeRecyclerView.visibility = View.VISIBLE
                        binding.emptyListView.visibility = View.GONE
                    }
                    adapter = CrimeListAdapter(bindData)
                    adapter.submitList(crimes)
                    binding.crimeRecyclerView.adapter = adapter
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                crimeListViewModel.isSelectionMode.collect {
                    requireActivity().invalidateOptionsMenu()
                    adapter.setSelectionMode()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    // menu processing
    private fun showNewCrime() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newCrime = Crime(
                id = UUID.randomUUID(),
                title = "",
                date = Date(),
                isSolved = false
            )
            crimeListViewModel.addCrime(newCrime)
            findNavController()
                .navigate(CrimeListFragmentDirections.showCrimeDetail(newCrime.id))
        }
    }

    private fun deleteSelectedCrimes() {
        viewLifecycleOwner.lifecycleScope.launch {
            crimeListViewModel
                .deleteCrimes(crimesToDelete = crimeListViewModel.selectedItems)
            crimeListViewModel.cancelSelectionMode()
        }
    }

    private fun setUpCrimeListMenu() {
        menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (crimeListViewModel.isSelectionMode.value) {
                    menuInflater.inflate(R.menu.fragment_crime_list_selection_menu, menu)
                    // get access to menuItemSelectCrimes and his actionView
                    val menuItemSelectCrimes = menu.findItem(R.id.menu_item_select_crimes)
                    // bind menuItemSelectCrimes with his actionView
                    val menuBinding = MenuCheckboxBinding.bind(menuItemSelectCrimes.actionView!!)
                    val selectAllCheckBox = menuBinding.selectAllCheckbox
                    selectAllCheckBox.isChecked = crimeListViewModel.selectAllCrimesFlag.value // start value
                    selectAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
                        crimeListViewModel.selectAllCrimes(isChecked)
                        adapter.setSelectionMode()
                    }
                } else {
                    menuInflater.inflate(R.menu.fragment_crime_list_default_menu, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_crime -> {
                        showNewCrime()
                        true
                    }
                    R.id.menu_item_delete_crimes -> {
                        val deletingAlert = getString(R.string.deleting_alert)
                        AlertDialog.Builder(requireContext())
                            .setTitle(deletingAlert)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Ok") { _, _ ->
                                deleteSelectedCrimes()
                            }
                            .show()
                        true
                    }
                    else -> false
                }
            }
        }
        requireActivity().addMenuProvider(
            checkNotNull(menuProvider), viewLifecycleOwner, Lifecycle.State.RESUMED
        )
    }
}