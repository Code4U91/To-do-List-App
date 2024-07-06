package com.example.to_dolist

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.to_dolist.databinding.FragmentSetAlarmBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SetAlarmFragment : Fragment()
{

    private lateinit var binding: FragmentSetAlarmBinding
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    private lateinit var sharedTaskViewModel: SharedTaskViewModel
    private var selectedAlarmTaskId: Long = -1L
    private var selectedAlarmTimeInMills : Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSetAlarmBinding.inflate(layoutInflater, container, false)

        val factory = TaskViewModelFactory(MainApplication())
        sharedTaskViewModel = ViewModelProvider(this, factory)[SharedTaskViewModel::class.java]


        // Retrieve the selectedTaskId from arguments
        arguments?.let {
            selectedAlarmTaskId = SetAlarmFragmentArgs.fromBundle(it).taskId
        }


        // set Click Listeners for date and time fields
        binding.apply {

            dateBox.setOnClickListener {

                showDatePicker()
            }

            timeBox.setOnClickListener {

                showTimePicker()
            }

            okBtAlarm.setOnClickListener {

                checkAndSetAlarm()

                saveAlarmTime(selectedAlarmTimeInMills) // saves and move to contentFragment with the id


            }
        }

        lifecycleScope.launch {

            val alarmTimeInMills = sharedTaskViewModel.getAlarmTimeByTaskId(selectedAlarmTaskId)
            alarmDataLoader(alarmTimeInMills)
        }

        binding.backBtalarm.setOnClickListener {

            val action = SetAlarmFragmentDirections.actionSetAlarmFragmentToContentFragment(selectedAlarmTaskId)
            findNavController().navigate(action)
        }
        onBackButtonPressed()

        return binding.root
    }


    // functions ----------------------------------------------------------------------------------------

    private fun saveAlarmTime(alarmTimeInMills: Long) {
        SharedTaskViewModel.getAlarmTime(alarmTimeInMills)
        selectedAlarmTaskId.let { taskId ->
            sharedTaskViewModel.updateAlarmTime(taskId, alarmTimeInMills)
            val action = SetAlarmFragmentDirections.actionSetAlarmFragmentToContentFragment(taskId)
            findNavController().navigate(action)
        }
    }


    private fun showTimePicker() {

        val calendar = Calendar.getInstance()

        val timePickerDialog = TimePickerDialog(requireContext(), { _, hourOfDay, minute ->

             //selectedTime.set(hourOfDay, minute)
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("hh::mm a", Locale.getDefault())
            binding.selectedTime.text = timeFormat.format(selectedTime.time)
        },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false)

        timePickerDialog.show()
    }


    private fun showDatePicker() {

        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(requireContext(), {_, year,month, dayOfMonth ->

            selectedDate.set(year,month,dayOfMonth)
            val dateFormat = SimpleDateFormat("MMM dd, yyy", Locale.getDefault())

            binding.selectedDate.text = dateFormat.format( selectedDate.time)
        },
           calendar.get(Calendar.YEAR), // year
            calendar.get(Calendar.MONTH), // month
            calendar.get(Calendar.DAY_OF_MONTH)) // dayOfMonth parameters

        datePickerDialog.show()
    }

    private fun checkAndSetAlarm() {
        // Check if the Android version is S (API level 31) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // Check if the app has the permission to schedule exact alarms
            if (alarmManager.canScheduleExactAlarms()) {
                setAlarm() // Permission granted, set the alarm
            } else {
                requestExactAlarmPermission() // Permission not granted, request the permission
            }
        } else {
            setAlarm() // For older Android versions, set the alarm directly
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestExactAlarmPermission() {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        startActivity((intent))
    }

    private fun setAlarm() {

        val alarmManager = requireContext().getSystemService((Context.ALARM_SERVICE)) as AlarmManager
        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra("taskId", selectedAlarmTaskId)
            }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            selectedAlarmTaskId.toInt(), // Request code
            intent,
            PendingIntent.FLAG_IMMUTABLE)

        // combine selected date and time

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)


        }

         selectedAlarmTimeInMills = calendar.timeInMillis
        Log.d("SetAlarmFragment", "Setting alarm for task ID: $selectedAlarmTaskId at $selectedAlarmTimeInMills")
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, selectedAlarmTimeInMills, pendingIntent)

        Toast.makeText(
            activity,
            "Alarm set successfully",
            Toast.LENGTH_SHORT
        ).show()

    }

    private fun onBackButtonPressed()
    {
        val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {

                findNavController().navigate(R.id.action_setAlarmFragment_to_contentFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun alarmDataLoader(alarmTimeInMills: Long)
    {
        // Function to convert milliseconds to date and time strings
        fun convertMillsToDateTime(mills : Long) : Pair<String, String>
        {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = mills
            }

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return  dateFormat.format(calendar.time) to timeFormat.format(calendar.time)
        }

        // Function to get the current date and time

        fun getCurrentDateTime() : Pair< String, String>{

            val now = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            return dateFormat.format(now.time) to timeFormat.format(now.time)
        }

        // Load the date and time based on whether the alarm is set

            Log.d("AlarmMenu", " Alarm time at setAlarmFragment = $alarmTimeInMills")

            if (alarmTimeInMills != -1L)
            {
                val (date, time) = convertMillsToDateTime(alarmTimeInMills)
                binding.selectedDate.text = date
                binding.selectedTime.text = time
            }
            else
            {
                val (currentDate, currentTime) = getCurrentDateTime()
                binding.selectedDate.text = currentDate
                binding.selectedTime.text = currentTime

            }


        }




}


