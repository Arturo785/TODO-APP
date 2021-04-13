package com.example.todoapp.ui.tasks

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.example.todoapp.data.Task
import com.example.todoapp.data.db.TaskDao
import com.example.todoapp.data.preferences.PreferencesManager
import com.example.todoapp.data.preferences.SortOrder
import com.example.todoapp.ui.ADD_TASK_RESULT_OK
import com.example.todoapp.ui.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager, // dagger knows how to inject it because it has @Inject in constructor
    @Assisted private val state : SavedStateHandle
): ViewModel(){

    // the one that sends the events
    private val tasksEventChannel = Channel<TaskEvent>()
    val taskEvent = tasksEventChannel.receiveAsFlow() // the one consumed

    // gets changed on the searchView in fragment
    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    // whenever the value of the flow changes execute this code and assign the result to itself
    // with combine it can receive multiple flows
    // and return the values which are used
    private val taskFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ){ query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest {
            (query, filterPreferences) -> // destructuring
        taskDao.getTasks(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }

    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedClick(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TaskEvent.NavigateToEditTaskScreen(task))
    }


    fun onTaskCheckedChanged(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checked)) // only changes the boolean one
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        taskDao.delete(task)
        // sets the new event to the channel to be consumed
        tasksEventChannel.send(TaskEvent.ShowUndoDeleteTaskMessage(task))
    }

    fun undoDeleteClick(task: Task) =  viewModelScope.launch {
        taskDao.insert(task)
    }

    fun onAddNewTaskClicked() = viewModelScope.launch {
        tasksEventChannel.send(TaskEvent.NavigateToAddTaskScreen)
    }

    fun onAddEditResult(result: Int) {
        when(result){
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfirmationMessage(message: String) = viewModelScope.launch {
        tasksEventChannel.send(TaskEvent.ShowTaskSavedConfirmationMessage(message))
    }

    sealed class TaskEvent {
        data class ShowUndoDeleteTaskMessage(val task: Task) : TaskEvent()
        object NavigateToAddTaskScreen : TaskEvent()
        data class NavigateToEditTaskScreen(val task: Task): TaskEvent()
        data class ShowTaskSavedConfirmationMessage(val message: String): TaskEvent()
    }

}
