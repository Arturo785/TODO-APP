package com.example.todoapp.ui.tasks

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.todoapp.data.db.TaskDao


class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
): ViewModel(){

    val tasks = taskDao.getTasks().asLiveData()

}