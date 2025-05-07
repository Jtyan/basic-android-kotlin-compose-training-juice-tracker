package com.example.juicetracker

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.juicetracker.data.JuiceColor
import com.example.juicetracker.databinding.FragmentEntryDialogBinding
import com.example.juicetracker.ui.AppViewModelProvider
import com.example.juicetracker.ui.EntryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class EntryDialogFragment : BottomSheetDialogFragment() {

    /*
    This is used for fragments
    val entryViewModel: EntryViewModel = viewModel(factory = AppViewModelProvider.Factory) is only used for compose.
     */
    private val entryViewModel by viewModels<EntryViewModel> { AppViewModelProvider.Factory }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentEntryDialogBinding.inflate(inflater, container, false).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentEntryDialogBinding.bind(view)
        val args: EntryDialogFragmentArgs by navArgs()
        val juiceId = args.itemId

        var selectedColor: JuiceColor? = JuiceColor.Red
        val placeholder = "Please select a Colour"
        val colorOptions = listOf(placeholder) + JuiceColor.entries.map { getString(it.label) }

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            colorOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = spinnerAdapter

        // Pre-populate fields when editing an existing item
        if (juiceId != 0L ) {
            //
            viewLifecycleOwner.lifecycleScope.launch {
                entryViewModel.getJuiceStream(juiceId).collect { juice ->
                    juice?.let {
                        binding.editJuiceName.setText(it.name)
                        binding.editJuiceDescription.setText(it.description)
                        binding.ratingBar.rating = it.rating.toFloat()

                        val colorPosition = colorOptions.indexOf(it.color)
                        binding.spinner.setSelection(colorPosition)
                    }
                }
            }
            validateInputs(binding, selectedColor)
        }

        binding.editJuiceName.addTextChangedListener() {
            validateInputs(binding, selectedColor)
        }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                selectedColor = if (position == 0) null else JuiceColor.entries[position - 1]
                validateInputs(binding, selectedColor)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: fallback
                selectedColor = null
                validateInputs(binding, selectedColor)
            }
        }

        binding.buttonSave.setOnClickListener {
            selectedColor?.name?.let { colour ->
                entryViewModel.saveJuice(
                    juiceId,
                    binding.editJuiceName.text.toString(),
                    binding.editJuiceDescription.text.toString(),
                    colour,
                    binding.ratingBar.rating.toInt()
                )
            }
            dismiss()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
    }

    // enable button save when Juice name is filled and color is selected
    private fun validateInputs(
        binding: FragmentEntryDialogBinding,
        selectedColor: JuiceColor?
    ) {
        val isNameFilled = !binding.editJuiceName.text.isNullOrEmpty()
        binding.buttonSave.isEnabled = isNameFilled && selectedColor != null
    }
}