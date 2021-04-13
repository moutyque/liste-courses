package small.app.liste_courses.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import kotlinx.coroutines.launch
import small.app.liste_courses.MainActivity
import small.app.liste_courses.Scope.backgroundScope
import small.app.liste_courses.Scope.mainScope
import small.app.liste_courses.Utils
import small.app.liste_courses.adapters.DepartmentsAdapter
import small.app.liste_courses.adapters.IList
import small.app.liste_courses.adapters.ItemsAdapter
import small.app.liste_courses.adapters.UnclassifiedItemsAdapter
import small.app.liste_courses.adapters.listeners.ILastItemUsed

import small.app.liste_courses.adapters.sortedListAdapterCallback.ItemCallBack
import small.app.liste_courses.databinding.FragmentMainBinding
import small.app.liste_courses.model.Department
import small.app.liste_courses.model.MainViewModel
import small.app.liste_courses.room.entities.Item


//TODO : Si perte de focus clear le nom ?
class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var activity: MainActivity

    private lateinit var model: MainViewModel

    private lateinit var unclassifiedAdapter: ItemsAdapter

    private lateinit var departmentsAdapter: DepartmentsAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        activity = requireActivity() as MainActivity
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Create the binding
        binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        //Create the model
        model = MainViewModel(activity.repo, this)
        binding.model = model

        //Mange the autocompletion field
        binding.actvSelectionItem.validator = object : AutoCompleteTextView.Validator {
            override fun isValid(text: CharSequence?): Boolean {
                return !text?.contains("\n")!!
            }

            override fun fixText(invalidText: CharSequence?): CharSequence {
                return invalidText!!.toString().replace("\n", "")
            }

        }
        //Setup the autocomplete item list
        model.autoCompleteItems.observe(viewLifecycleOwner, { list ->
            val toTypedArray: Array<String> = list.map { i -> i.name }.toTypedArray()
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, toTypedArray
            )
            binding.actvSelectionItem.setAdapter(adapter)
        })


        //Setup btn to add an new item
        binding.ibAddItem.setOnClickListener {
            //Create or get the item
            val name = binding.actvSelectionItem.text.toString().trim()

            if (name.isNotEmpty()) {
                val item = Item(name = name)
                item.isUsed = true
                Utils.useItem(item, unclassifiedAdapter, departmentsAdapter)
                binding.actvSelectionItem.setText("")
            }

        }


        setUpUnclassifiedItemsRV()

        //Update the list of items to be displayed in the rv and in the autocompletion
//        model.itemsChange.observe(viewLifecycleOwner, { newValue ->
//            if (newValue) {
//                mainScope.launch {
//                    unclassifiedAdapter.notifyDataSetChanged()
//                    model.itemsChange.value = false
//                }
//            }
//        })


        setupDepartmentsRV()

//        model.departmentsChange.observe(viewLifecycleOwner, { newValue ->
//            if (newValue) {
//                mainScope.launch {
//                    model.departments.sortBy { d -> d.order }
//                    departmentsAdapter.notifyDataSetChanged()
//                    model.departmentsChange.value = false
//                }
//            }
//        })

        // binding.actDepartmentName.
        model.autoCompleteDepartment.observe(viewLifecycleOwner, { list ->
            //List is the filter list of all the not visible department
            val toTypedArray: Array<String> = list.map { i -> i.name }.toTypedArray()
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line, toTypedArray
            )
            binding.actDepartmentName.setAdapter(adapter)
        })

        //Setup btn to add an new department
        binding.ibAddDepartment.setOnClickListener {
            //Create the new department
            val depName = binding.actDepartmentName.text.toString().trim()
            if (depName.isNotEmpty()) {
                // Need to check if it exist first because we don't want to override an existing department
                var order = 0
                var depDb: Department? = null
                val job = backgroundScope.launch {
                    order = Utils.repo.getAllDepartment().size
                    depDb = Utils.repo.findDepartment(depName)
                }
                job.invokeOnCompletion {

                    val dep = if (depDb == null) {
                        Department(
                            depName,
                            true,
                            ArrayList(),
                            order
                        )
                    } else {
                        depDb
                    }
                    if(!departmentsAdapter.contains(dep!!))  departmentsAdapter.addDepartment(dep!!)
                }

                //model.updateDepartmentsList(dep)

                binding.actDepartmentName.setText("")
            }

        }


        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupDepartmentsRV() {
        //Create the department adapter
        departmentsAdapter = DepartmentsAdapter(
            requireContext()
        )


        //Setup departments recycler view
        binding.rvDepartment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDepartment.adapter = departmentsAdapter

        val callback = SimpleItemTouchHelperCallback(departmentsAdapter)
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvDepartment)
    }

    private fun setUpUnclassifiedItemsRV() {
        //Create the items adapter
        unclassifiedAdapter =
            UnclassifiedItemsAdapter(
                requireContext(),
                false,
                object : ILastItemUsed{
                    override fun onLastItemUse() {
                        Log.d("UnclassifiedAdapter","The last item has been used")
                    }
                }
            )


        //Setup the items recycler view
        binding.rvUnclassifiedItems.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.rvUnclassifiedItems.adapter = unclassifiedAdapter
    }

    fun updateUnclassifiedItems(position: Int) {

        //unclassifiedAdapter.notifyItemInserted(position)
        unclassifiedAdapter.notifyDataSetChanged()


    }


    class SimpleItemTouchHelperCallback(adapter: DepartmentsAdapter) :
        ItemTouchHelper.Callback() {
        private val mAdapter: DepartmentsAdapter = adapter
        override fun isLongPressDragEnabled(): Boolean {
            Log.d("SimpleItemTouchHelperCallback", "Can u click")
            return mAdapter.canMove
        }


        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val swipeFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            val dragFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(dragFlags, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            mAdapter.onItemMove(viewHolder.adapterPosition, target.adapterPosition)
            mAdapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            Log.d("DDSwipe", "In the onSwipe")
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                viewHolder?.itemView?.alpha = 0.5f
                mAdapter.canMove = false
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {

            super.clearView(recyclerView, viewHolder)
            viewHolder.itemView.alpha = 1.0f
        }


    }

}