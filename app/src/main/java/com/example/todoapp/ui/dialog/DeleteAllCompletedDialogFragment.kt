package com.example.todoapp.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteAllCompletedDialogFragment : DialogFragment(){

    private val viewModel: DeleteAllCompletedViewModel by viewModels()


    override fun onCreateDialog(savedInstanceState: Bundle?) =
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm deletion")
            .setMessage("Are you sure you want to delete all tasks?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes"){ _, _ ->
                //viewModel operation
                viewModel.onConfirmClick()
            }
            .create()

}