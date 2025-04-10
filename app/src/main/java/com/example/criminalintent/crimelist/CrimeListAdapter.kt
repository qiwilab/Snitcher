package com.example.criminalintent.crimelist

import java.util.Locale
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.criminalintent.R
import com.example.criminalintent.database.Crime
import com.example.criminalintent.databinding.ListItemCrimeBinding
import java.text.SimpleDateFormat

class CrimeHolder(
    private val binding: ListItemCrimeBinding,
    private val bindData: BindData
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(crime: Crime) {

        /* short click */
        binding.root.setOnClickListener {
            bindData.onCrimeClicked(crime.id)
        }

        /* long click */
        binding.root.setOnLongClickListener {
            bindData.onCrimeLongClicked()
            true
        }

        binding.crimeTitle.text = crime.title
        val fullDateFormat =
            SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()
        )
        binding.crimeDate.text = fullDateFormat.format(crime.date)

        /* selecting Crimes for deleting */
        binding.selectedCrime.visibility =
            if (bindData.isSelectionMode.value) View.VISIBLE else View.GONE
        binding.selectedCrime.isChecked = bindData.selectedItems.value.contains(crime)
        binding.selectedCrime.setOnCheckedChangeListener { _, isChecked ->
            bindData.onCheckedChange(crime, isChecked)
        }

        if (crime.isSolved) {
            binding.crimeSolved.visibility = View.VISIBLE
        } else {
            binding.crimeSolved.visibility = View.GONE
        }

// creating a contentDescription for root view for TalkBack
        val rootContentDescription: String = "${crime.title}  ${fullDateFormat.format(crime.date)}".plus(
            if (crime.isSolved) {
                itemView.context.getString(R.string.crime_report_solved)
            } else {
                itemView.context.getString(R.string.crime_report_unsolved)
            }
        )
        binding.root.contentDescription = rootContentDescription
    }
}

class CrimeListAdapter(
    private val bindData: BindData
) : ListAdapter<Crime, CrimeHolder>(DiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CrimeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemCrimeBinding.inflate(inflater, parent, false)
        return CrimeHolder(binding, bindData)
    }

    override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelectionMode() {
        submitList(currentList)
        notifyDataSetChanged()
    }
}

class DiffCallback : DiffUtil.ItemCallback<Crime>() {
    override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
        return oldItem == newItem
    }
}