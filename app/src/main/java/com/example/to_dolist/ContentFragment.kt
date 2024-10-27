package com.example.to_dolist

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.to_dolist.databinding.FragmentContentBinding
import com.example.to_dolist.db.Task
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ContentFragment : Fragment() {

    private lateinit var binding: FragmentContentBinding
    private lateinit var sharedTaskViewModel: SharedTaskViewModel
    private var fromSearchFragment : Boolean = false
    private var taskId : Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContentBinding.inflate(layoutInflater, container, false)
        val factory =  TaskViewModelFactory(MainApplication())
        sharedTaskViewModel = ViewModelProvider(this, factory)[SharedTaskViewModel::class.java]



        arguments?.let { args ->
            val contentArgs = ContentFragmentArgs.fromBundle(args)
            fromSearchFragment = contentArgs.fromSearchFragment
            taskId = contentArgs.taskId

            val taskIdFromNotification = contentArgs.fromNotificationTaskId
            if (taskIdFromNotification != -1L) {
                lifecycleScope.launch {
                    val task = sharedTaskViewModel.getTaskByIdDirect(taskIdFromNotification)
                    task?.let {
                        SharedTaskViewModel.isListItemClickedLiveData.value = true
                        SharedTaskViewModel.selectedTaskLiveFun(it)
                        loadNoteContent(taskIdFromNotification)
                        Log.d("globalFragmentId", "$taskIdFromNotification")
                    }
                }
            }

            // for alarm
            if (taskId != -1L) {
                loadNoteContent(taskId)

            }
        }



        uiAddDeleteController()
        onBackButtonPressed()
        setUpTextWatchers()

        binding.backBt.setOnClickListener {

            SharedTaskViewModel.isListItemClickedLiveData.value = false
            SharedTaskViewModel.selectedTaskLiveData.value = null
            findNavController().navigate(R.id.action_contentFragment_to_homeFragment)
        }

        binding.contentFieldEt.requestFocus()
        binding.contentFieldEt.isFocusableInTouchMode = true


        return  binding.root
    }

    // Functions------------------------------------------------------------------------


    // this functions controls add or updation of the data in the database
    private fun uiAddDeleteController()
    {
        SharedTaskViewModel.isListItemClickedLiveData.observe(viewLifecycleOwner){ isClicked ->

            if (isClicked) // user clicked on the recycler view, opens the existing data or task
            {
                SharedTaskViewModel.selectedTaskLiveData.value?.let { loadNoteContent(it.id) }

            }
            else {  // User clicked on the add button to add new Task

                disableButton()
                if (binding.dateAndTimeTv.text == "")
                {
                    binding.dateAndTimeTv.text = getCurrentDateTime()
                }

                binding.okBt.setOnClickListener {

                    addTask()

                    it.findNavController().navigate(R.id.action_contentFragment_to_homeFragment)

                }

                lifecycleScope.launch {

                    val lastInsertedTask = sharedTaskViewModel.getLastInsertedTask()
                    if (lastInsertedTask != null) {

                        SharedTaskViewModel.selectedTaskLiveData.value = lastInsertedTask

                    }
                }

            }
        }
    }

    // state of the button when enabled
    private  fun enableButton()
    {
        val color = ContextCompat.getColor(requireActivity(), R.color.white)

        binding.apply {

            deleteBt.setColorFilter(color)
            alarmBt.setColorFilter(color)
            deleteBt.isEnabled = true
            alarmBt.isEnabled = true
        }
    }

    // state of the button when disabled
    @SuppressLint("SuspiciousIndentation")
    private fun disableButton()
    {
        val color = ContextCompat.getColor(requireActivity(), R.color.grey)

                binding.deleteBt.setColorFilter(color)
                binding.alarmBt.setColorFilter(color)
        binding.apply {
            deleteBt.isEnabled = false
            deleteBt.isEnabled = false
        }
    }

    // Helps loading the Task data from the database to the View or fields, populating the fields
    private fun loadNoteContent(taskId: Long) {
        sharedTaskViewModel.getTaskById(taskId).observe(viewLifecycleOwner) { task ->
            task?.let {
                binding.apply {
                    titleTv.setText(it.title)
                    contentFieldEt.setText(it.taskContent)

                    // sorting
                    if (SharedTaskViewModel.isSortByTimeOfCreation) {
                        dateAndTimeTv.text = it.taskDateAndTimeCreated
                    } else {
                        dateAndTimeTv.text = it.modifiedDate
                    }

                    enableButton()
                    alarmTextView()
                    openAlarmMenuOrNot()
                    updateButtonState()

                    binding.apply {
                        okBt.setOnClickListener {
                            updateTask(task.alarmTimeInMills)
                        }

                        deleteBt.setOnClickListener {
                            if (task.alarmTimeInMills != -1L) {
                                SharedTaskViewModel.selectedTaskLiveData.value?.let { selectedTask ->
                                    cancelAlarm(selectedTask.id)
                                }
                            }

                            deleteTask()
                            SharedTaskViewModel.isListItemClickedLiveData.value = false
                            it.findNavController().navigate(R.id.action_contentFragment_to_homeFragment)
                        }
                    }
                }
            } ?: run {
                Log.e("ContentFragment", "Task with ID $taskId not found.")
                // Handle the case where the task is null, e.g., show a message or navigate back
            }
        }
    }



    // when user clicks alarm button this one is responsible for navigation and passes the taskId of that
    // task to the SetAlarmFragment
    private fun navigateToSetAlarmFragment(taskId: Long) {
        val action =  ContentFragmentDirections.actionContentFragmentToSetAlarmFragment(taskId)
        findNavController().navigate(action)
    }

    // Gets the TaskId of the selected data when the user clicks on the recycler view
    private fun getSelectedTaskId(): Long {

        var lastInsertedTaskId : Long = -1L

        if (SharedTaskViewModel.isListItemClickedLiveData.value == true)
        {
            lastInsertedTaskId = SharedTaskViewModel.selectedTaskLiveData.value?.id!!

        }else
        {
            lifecycleScope.launch {
                lastInsertedTaskId = sharedTaskViewModel.getLastInsertedTaskId()
            }

        }
        return lastInsertedTaskId
    }

    // Handles what happens when the user clicks on the system back button ( back press)
    private fun onBackButtonPressed()
    {

        val callback = object :  OnBackPressedCallback(true) {
            @SuppressLint("SuspiciousIndentation")
            override fun handleOnBackPressed() {

              SharedTaskViewModel.isListItemClickedLiveData.value = false // no effect if the user clicks add button and moves back as in that case recycler view won't be clicked
                                                            // so the is ListItemClicked remains false (default case)

//                sharedTaskViewModel.isAlarmSet = false
                SharedTaskViewModel.selectedTaskLiveData.value = null

                if (!fromSearchFragment)
                {
                    findNavController().navigate(R.id.action_contentFragment_to_homeFragment)
                }else
                {
                    findNavController().navigate(R.id.action_contentFragment_to_searchListFragment)
                }

            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    // Function which adds new Task data in the database
    private fun addTask()
    {
        val titleText : String

        val firstLineOfContent = binding.contentFieldEt.text.toString().split("\n")[0]
        titleText = if (!TextUtils.isEmpty(binding.titleTv.text)){
            binding.titleTv.text.toString()

        } else{
            firstLineOfContent

        }

        val newTask = Task(
            0,
            titleText,
            binding.contentFieldEt.text.toString(),
            getCurrentDateTime(),
            getCurrentDateTime(),
             -1,
            System.currentTimeMillis()
        )

        sharedTaskViewModel.insertTask(newTask)

    }

// Updates the existing Task data in the database
    private fun updateTask(alarmTimeInMills: Long)
    {
        sharedTaskViewModel.updateTask(
            Task(
                SharedTaskViewModel.selectedTaskLiveData.value!!.id,
                binding.titleTv.text.toString(),
                binding.contentFieldEt.text.toString(),
                SharedTaskViewModel.selectedTaskLiveData.value!!.taskDateAndTimeCreated,
                getCurrentDateTime(),
                 alarmTimeInMills,
                System.currentTimeMillis()
            )
        )

    }

    // Deletes the existing Task data from the database
    private fun deleteTask()
    {
        sharedTaskViewModel.deleteTask(
            Task(
                SharedTaskViewModel.selectedTaskLiveData.value!!.id,
                binding.titleTv.text.toString(),
                binding.contentFieldEt.text.toString(),
                SharedTaskViewModel.selectedTaskLiveData.value!!.taskDateAndTimeCreated,
                SharedTaskViewModel.selectedTaskLiveData.value!!.modifiedDate,
                SharedTaskViewModel.selectedTaskLiveData.value!!.alarmTimeInMills,
                 SharedTaskViewModel.selectedTaskLiveData.value!!.sortTimestamp

            )
        )
    }

    // Gets the current date and time from the system
    private fun getCurrentDateTime() : String{
        // Implement method to get current date and time as string
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("MMM dd yyyy, hh:mm a")
        return currentDateTime.format(formatter)
    }

    // Helps noticing the changes in the text field, which is then used to change the state of the button
    private fun setUpTextWatchers()
    {
        val textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                 // No action needed before text changes

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                 updateButtonState()
            }

            override fun afterTextChanged(s: Editable?) {
                 // No action needed after text changes

            }

        }
        binding.titleTv.addTextChangedListener(textWatcher)
        binding.contentFieldEt.addTextChangedListener(textWatcher)

        // Update button state initially
        updateButtonState()
    }

    // Updates the state of the button based on the conditions set for the text's
    private fun updateButtonState()
    {

        val title = binding.titleTv.text.toString()
        val content = binding.contentFieldEt.text.toString()

        if (SharedTaskViewModel.isListItemClickedLiveData.value == true)
        {

            var taskDatabaseLoadedTitle : String? = null
            var taskDatabaseLoadedContent : String? = null

            lifecycleScope.launch {

                 taskDatabaseLoadedTitle = SharedTaskViewModel.selectedTaskLiveData.value?.title
                 taskDatabaseLoadedContent = SharedTaskViewModel.selectedTaskLiveData.value?.taskContent

            }
            if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content))
            {
                enableDisableOkBt(false)
            }
            else if (title == taskDatabaseLoadedTitle && content == taskDatabaseLoadedContent)
            {
                enableDisableOkBt(false)
            }
            else{
                enableDisableOkBt(true)
            }

        }

        else
        {
            when(TextUtils.isEmpty(title) && TextUtils.isEmpty(content))
            {
                true -> {
                    enableDisableOkBt(false)
                }
                false -> {
                    enableDisableOkBt(true)
                }
            }
        }



    }


    // function responsible for controlling the state of the okBt, which is responsible for adding or updating
    private fun enableDisableOkBt(isEnabled : Boolean)
    {
        if (isEnabled)
        {
            binding.okBt.isEnabled = true
            val color = ContextCompat.getColor(requireActivity(), R.color.blue)

            binding.okBt.setColorFilter(color)

        }
        else
        {
            binding.okBt.isEnabled = false
            val color = ContextCompat.getColor(requireActivity(), R.color.grey)

            binding.okBt.setColorFilter(color)
        }

    }

    // Shows Alarm popup window when the conditions are met

    private fun showAlarmMenu(view: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.popup_menu_layout, null)

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Set the background color and text color
        val modifyAlarmTextView = popupView.findViewById<TextView>(R.id.modifyAlarm)
        val cancelAlarmTextView = popupView.findViewById<TextView>(R.id.cancelAlarm)

        // click listeners, listing for clicks on the options of the menu
        modifyAlarmTextView.setOnClickListener {  // For modifying the alarm
            val taskId = getSelectedTaskId() // Retrieve the selected note ID
            navigateToSetAlarmFragment(taskId)
            popupWindow.dismiss()
            updateUI()
        }

        // for canceling the alarm
        cancelAlarmTextView.setOnClickListener {
            SharedTaskViewModel.selectedTaskLiveData.value?.let { cancelAlarm(it.id) }
            popupWindow.dismiss()
            updateUI() // refreshing the UI after selecting option from the menu
        }

        // Show the popup window in the center of the screen
        popupWindow.showAtLocation(view.rootView, Gravity.CENTER, 0, 0)
    }

    // updates the UI after user selects options from the menu
    private fun updateUI() {
        alarmTextView()
        openAlarmMenuOrNot()
    }

    // function used for canceling the alarm
    private fun cancelAlarm(taskId : Long){

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            taskId.toInt(), // Request Code
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d("CancelAlarm", "Cancelled alarm for the task Id : $taskId")

        sharedTaskViewModel.updateAlarmTime(taskId, -1L)
    }

    // Function which checks if to open the alarm menu or the setAlarmFragment
    private fun openAlarmMenuOrNot() {

        lifecycleScope.launch {
            val alarmTime: Long = SharedTaskViewModel.selectedTaskLiveData.value?.let {
                sharedTaskViewModel.getAlarmTimeByTaskId(it.id)
            } ?: -1L

            Log.d("AlarmMenu", " Alarm time at coroutine = $alarmTime")

            if (alarmTime == -1L) { // alarm is not set
                binding.alarmBt.setOnClickListener {
                    val taskId = getSelectedTaskId() // Retrieve the selected note ID
                    navigateToSetAlarmFragment(taskId)
                }
            } else {
                binding.alarmBt.setOnClickListener {
                    showAlarmMenu(it)
                }
            }
        }
    }

    // Shows the content in the alarmTextView
    private fun alarmTextView()
    {

        lifecycleScope.launch {
            val alarmTime: Long = SharedTaskViewModel.selectedTaskLiveData.value?.let {
                sharedTaskViewModel.getAlarmTimeByTaskId(it.id)
            } ?: -1L

            Log.d("AlarmMenu", " Alarm time at coroutine = $alarmTime")

            if (alarmTime == -1L) // alarm is not set
            {
                binding.alarmTv.visibility = View.GONE


            } else {

                binding.alarmTv.visibility = View.VISIBLE

                Log.d("Alarm View", "$alarmTime")
                binding.alarmTv.text =  SharedTaskViewModel.formatAlarmTime(alarmTime)

                binding.alarmTv.setOnClickListener {

                    showAlarmMenu(it)
                }

                }
            }
        }
    }





