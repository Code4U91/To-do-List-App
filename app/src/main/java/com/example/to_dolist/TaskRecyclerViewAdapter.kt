package com.example.to_dolist

import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.to_dolist.db.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TaskRecyclerViewAdapter(private val clickListener: (Task) -> Unit) : RecyclerView.Adapter<TaskViewHolder>() {

    private var tasks = emptyList<Task>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val taskView = layoutInflater.inflate(R.layout.list_item, parent, false)
        return TaskViewHolder(taskView)
    }

    override fun getItemCount(): Int {
         return tasks.size
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {

        holder.bind(tasks[position], clickListener)
        holder.alarmView(tasks[position])
    }

    fun setTaskList(tasks: List<Task>)
    {
        this.tasks = tasks
        notifyDataSetChanged()
    }
}

class TaskViewHolder(private var taskView : View) : RecyclerView.ViewHolder(taskView){


    private val titleViewEt : TextView = taskView.findViewById(R.id.titleView)
    private val contentView : TextView = taskView.findViewById(R.id.contentView)
    private val dateAndTimeView : TextView = taskView.findViewById(R.id.dtView)



    fun bind(task: Task, clickListener: (Task)-> Unit)
    {

        titleViewEt.text = task.title
        val firstLineOfContent = task.taskContent.split("\n")[0]
        contentView.text =  firstLineOfContent

        if (SharedTaskViewModel.isSortByTimeOfCreation)
        {
            dateAndTimeView.text = task.taskDateAndTimeCreated
        }else
        {
            dateAndTimeView.text = task.modifiedDate
        }


        taskView.setOnClickListener {

            clickListener(task)
         //   it.findNavController().navigate(R.id.action_homeFragment_to_contentFragment)


        }


    }

    fun alarmView(task: Task) {
        val alarmIcon: ImageView = taskView.findViewById(R.id.alarmIcon)

        CoroutineScope(Dispatchers.IO).launch {
            val repository = TaskRepository(MainApplication.taskDatabase.getTaskDao())
            val alarmTimeInMills = repository.getAlarmTimeByTaskId(task.id)

            withContext(Dispatchers.Main) {
                if (alarmTimeInMills != -1L) {

                    alarmIcon.visibility = VISIBLE
                } else {
                    alarmIcon.visibility = INVISIBLE
                }
            }
        }
    }





}