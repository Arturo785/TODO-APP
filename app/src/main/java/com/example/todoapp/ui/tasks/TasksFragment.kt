package com.example.todoapp.ui.tasks

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.todoapp.R
import com.example.todoapp.data.Task
import com.example.todoapp.data.preferences.SortOrder
import com.example.todoapp.databinding.FragmentTasksBinding
import com.example.todoapp.utils.onQueryTextListener
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), TasksAdapter.OnItemClickListener {

    private val viewModel : TaskViewModel by viewModels()
    private lateinit var tasksAdapter: TasksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTasksBinding.bind(view)
        tasksAdapter = TasksAdapter(this)

        setupRecyclerView(binding)
        subscribeObservers()

        setHasOptionsMenu(true)
    }


    ///------------- Made by me ---------------------------
    private fun setupRecyclerView(binding: FragmentTasksBinding){
        binding.apply {
            recyclerViewTasks.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
        }
    }

    private fun subscribeObservers(){
        viewModel.tasks.observe(viewLifecycleOwner, {
            tasksAdapter.submitList(it)
        })
    }

    override fun onItemClick(task: Task) {
        Snackbar.make(requireView(), "aaaa", Snackbar.LENGTH_LONG).show()
        viewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        viewModel.onTaskCheckedChanged(task, isChecked)
    }

    ///--------------- Made by the SO ----------------------

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tasks, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.onQueryTextListener{
            // update search query
            viewModel.searchQuery.value = it
        }

        // lives as long as the view of the fragment
        viewLifecycleOwner.lifecycleScope.launch {

            menu.findItem(R.id.action_hide_completed_task).isChecked =
                viewModel.preferencesFlow.first().hideCompleted // only reads a single flow and then cancels it
            // in order to stop receiving notification about it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.action_hide_completed_task -> {
                item.isChecked = !item.isChecked
                viewModel.onHideCompletedClick(item.isChecked)
                true
            }
            R.id.action_delete_all_completed_task -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}