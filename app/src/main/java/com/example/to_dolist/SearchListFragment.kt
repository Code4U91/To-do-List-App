package com.example.to_dolist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.to_dolist.databinding.FragmentSearchListBinding
import com.example.to_dolist.db.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchListFragment : Fragment() {

    private lateinit var binding: FragmentSearchListBinding

    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var adapter : TaskRecyclerViewAdapter
    private lateinit var sharedTaskViewModel: SharedTaskViewModel
    private var taskList: List<Task> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        binding = FragmentSearchListBinding.inflate(layoutInflater, container, false)

        val factory =  TaskViewModelFactory(MainApplication())
        sharedTaskViewModel = ViewModelProvider(requireActivity(), factory)[SharedTaskViewModel::class.java]

        taskRecyclerView = binding.searchRv
        taskRecyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = TaskRecyclerViewAdapter{selectedTask : Task -> listItemClicked(selectedTask)}
        taskRecyclerView.adapter = adapter



        // Restore search text from ViewModel
        binding.searchView.setQuery(sharedTaskViewModel.searchText.value, true)

        Log.d("SearchText", "value of the text is ${binding.searchView.query}")
        Log.d("SearchText", "value of the text at  onCreate/sharedTaskViewModel is ${sharedTaskViewModel.searchText.value}")


        loadTasks()
        setUpSearchView()

       handleOnBackButtonPressed()
        return  binding.root
    }


    private fun loadTasks()
    {
        lifecycleScope.launch {
            taskList = getTasksFromDatabase()

        }
    }
    private fun setUpSearchView()
    {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText?.let {
                    filterTasks(it)
                }
                return true
            }
        })

        binding.searchView.setOnCloseListener {
            // Handle close button click

            binding.searchView.setQuery("", false)
            filterTasks("")
            true
        }

        // Focus the search view and open the keyboard
        binding.searchView.isFocusable = true
        binding.searchView.isIconified = false
        binding.searchView.requestFocus()

        // Delay the keyboard show request  (Keyboard doesn't show up without delay)
        binding.searchView.postDelayed({
            showKeyboard()
        }, 200)


      setBackButton()

        // Set hint text
        binding.searchView.queryHint = "Search for tasks"

       setQueryColor()

    }

    private fun setBackButton()
    {
        // Replace search icon with a back button
        val searchIcon = binding.searchView.findViewById<View>(androidx.appcompat.R.id.search_mag_icon)
        if (searchIcon is ImageView) {
            searchIcon.setImageResource(R.drawable.baseline_arrow_back_ios_24)
            searchIcon.setOnClickListener {

                sharedTaskViewModel.searchText.value = ""
                findNavController().navigate(R.id.action_searchListFragment_to_homeFragment)
            }
        }
    }
    private fun setQueryColor()
    {
        // Customize the hint and query text color
        val searchTextView = binding.searchView.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
        searchTextView.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.grey))
        searchTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

    }

    private  fun filterTasks( query : String)
    {
        val filteredList = if (query.isNotBlank())
        {
            taskList.filter {
                it.title.contains(query, true) ||
                        it.taskContent.contains(query,true)
            }
        }
        else
        {
            emptyList() // show no task when query is empty
        }
        adapter.setTaskList(filteredList)
    }

    private suspend fun getTasksFromDatabase(): List<Task> {

        return withContext(Dispatchers.IO)
        {
            sharedTaskViewModel.getAllTaskDirect()
        }
    }

    private fun listItemClicked(task: Task) {

        SharedTaskViewModel.isListItemClickedLiveData.value = true
        SharedTaskViewModel.selectedTaskLiveFun(task)

         sharedTaskViewModel.searchText.value = binding.searchView.query.toString()
        Log.d("SearchText", "value of the text at isListItemClicked is ${binding.searchView.query}")
        Log.d("SearchText", "value of the text at isListItemClicked/sharedTaskViewModel is ${sharedTaskViewModel.searchText.value}")

        val action = SearchListFragmentDirections.actionSearchListFragmentToContentFragment(fromSearchFragment = true)
        findNavController().navigate(action)
    }

    private fun handleOnBackButtonPressed()
    {

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                sharedTaskViewModel.searchText.value = ""
                 findNavController().navigate(R.id.action_searchListFragment_to_homeFragment)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

    }

    private fun showKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = binding.searchView.findFocus() ?: View(requireContext())
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }



}