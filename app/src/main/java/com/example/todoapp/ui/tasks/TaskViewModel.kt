package com.example.todoapp.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.todoapp.data.db.TaskDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest


class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
): ViewModel(){

    // gets changed on the searchView in fragment
    val searchQuery = MutableStateFlow("")

    // whenever the value of the flow changes execute this code and assign the result to itself
    private val taskFlow = searchQuery.flatMapLatest {
        taskDao.getTasks(it)
    }

    val tasks = taskFlow.asLiveData()

}