package com.example.to_dolist

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.to_dolist.databinding.FragmentHomeBinding
import com.example.to_dolist.db.Task


class HomeFragment : Fragment()  {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var taskRecyclerView: RecyclerView
    private lateinit var adapter : TaskRecyclerViewAdapter
    private lateinit var sharedTaskViewModel: SharedTaskViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)


        val factory =  TaskViewModelFactory(MainApplication())
        sharedTaskViewModel = ViewModelProvider(this, factory)[SharedTaskViewModel::class.java]

        taskRecyclerView = binding.toDoListRv
        taskRecyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = TaskRecyclerViewAdapter{selectedTask : Task -> listItemClicked(selectedTask)}
        taskRecyclerView.adapter = adapter


        // initial load
        sharedTaskViewModel.getTasksSortedByDateModified().observe(viewLifecycleOwner)
        {
            adapter.setTaskList(it)
        }

        // function buttons for adding, searching and sorting the task
        binding.apply {
            addBt.setOnClickListener{
                it.findNavController().navigate(R.id.action_homeFragment_to_contentFragment)

            }

            searchBt.setOnClickListener {
                it.findNavController().navigate(R.id.action_homeFragment_to_searchListFragment)
            }

            sortBt.setOnClickListener {
                showSortMenu(it)
            }
        }

       handleOnBackButtonPressed()

        return  binding.root


    }


    // Functions-------------------------------------------------

    private fun handleOnBackButtonPressed()
    {

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

    }

    //gets the id of item clicked in the recycler view
    private fun listItemClicked(task: Task)
    {
        SharedTaskViewModel.isListItemClickedLiveData.value = true
        SharedTaskViewModel.selectedTaskLiveFun(task)

        val action = HomeFragmentDirections.actionHomeFragmentToContentFragment()
        findNavController().navigate(action)

    }

    // Menu function, responsible for handling popup menu
    private fun showSortMenu( view: View)
    {
      //  val popup = PopupMenu(context, view)
        val popup = PopupMenu(context, view, Gravity.END, 0, R.style.PopupMenuStyle) // Apply custom style here

        popup.menuInflater.inflate(R.menu.sort_menu, popup.menu)
       setMenuItemBackground(SharedTaskViewModel.isSortByTimeOfCreation,popup)

        popup.setOnMenuItemClickListener { item ->
            when(item.itemId){

                R.id.byTimeOfCreation -> {

                    sharedTaskViewModel.getTasksSortedByDateCreated().observe(viewLifecycleOwner) { tasks ->
                        tasks?.let {
                            adapter.setTaskList(it)
                        }
                    }
                    SharedTaskViewModel.isSortByTimeOfCreation = true

                    true
                }

                R.id.byModified ->
                {
                    sharedTaskViewModel.getTasksSortedByDateModified().observe(viewLifecycleOwner
                    ) { tasks ->
                        tasks?.let {
                            adapter.setTaskList(it)
                        }
                    }
                    SharedTaskViewModel.isSortByTimeOfCreation = false

                        true

                }

                else -> false
            }
        }

        popup.show()
    }

    // this function isn't working as it intended to do, needs update

    private fun setMenuItemBackground(isSortByTimeOfCreation: Boolean, popup: PopupMenu) {
        val menu = popup.menu
        val menuItemByModified = menu.findItem(R.id.byModified)
        val menuItemByTimeOfCreation = menu.findItem(R.id.byTimeOfCreation)

        val color = ContextCompat.getColor(requireActivity(), R.color.brown)

        if (isSortByTimeOfCreation) {
            // Set brown background to byTimeOfCreation item
            menuItemByTimeOfCreation.icon?.setTint(color)
        } else {
            // Set brown background to byModified item
            menuItemByModified.icon?.setTint(color)
        }
    }


}