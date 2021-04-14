package com.example.todoapp.ui.addEdit

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.FragmentAddEditTaskBinding
import com.example.todoapp.ui.addEdit.AddEditTaskViewModel.*
import com.example.todoapp.utils.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import org.jetbrains.annotations.NotNull

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task){

    private val viewModel : AddEditTaskViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextTaskName.setText(viewModel.taskName)
            checkBoxImportant.isChecked = viewModel.taskImportance
            checkBoxImportant.jumpDrawablesToCurrentState()
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"

            editTextTaskName.addTextChangedListener {
                viewModel.taskName = it.toString()
            }

            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportance = isChecked
            }

            fabSaveTask.setOnClickListener {
                viewModel.onSaveClick()
            }

            // stops receiving when on background
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                subscribeToObservers(binding)
            }

        }
    }

    private suspend fun subscribeToObservers(binding: FragmentAddEditTaskBinding) {
        // receives the notifications of the channel
        viewModel.addEditTaskEvent.collect { event ->
            when(event){
                is AddEditTaskEvent.ShowInvalidInputMessage -> {
                    Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                }

                is AddEditTaskEvent.NavigateBackWithResult -> {
                    binding.editTextTaskName.clearFocus()
                    setFragmentResult(
                        "add_edit_request",
                        bundleOf("add_edit_result" to event.result) //any of both
                    )
                    findNavController().popBackStack()
                }
            }.exhaustive
        }
    }
}