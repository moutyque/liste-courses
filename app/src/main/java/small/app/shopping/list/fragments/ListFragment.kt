package small.app.shopping.list.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import small.app.shopping.list.adapters.DepartmentsListAdapter
import small.app.shopping.list.adapters.ItemsAdapter
import small.app.shopping.list.adapters.UnclassifiedItemsAdapter
import small.app.shopping.list.databinding.FragmentListBinding
import small.app.shopping.list.models.Department
import small.app.shopping.list.objects.Scope.backgroundScope
import small.app.shopping.list.objects.Utils
import small.app.shopping.list.objects.Utils.repo
import small.app.shopping.list.room.entities.Item
import small.app.shopping.list.viewmodels.FragmentViewModel


class ListFragment : Fragment() {

    private lateinit var binding: FragmentListBinding

    private lateinit var viewModel: FragmentViewModel

    private lateinit var unclassifiedAdapter: ItemsAdapter

    lateinit var departmentsListAdapter: DepartmentsListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("", "Oncreate")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        //Create the binding
        binding = FragmentListBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel = ViewModelProvider(this).get(FragmentViewModel::class.java)

        val itemsName: ArrayList<String> = ArrayList()
        val suggestedItemsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            itemsName
        )

        //Setup the autocomplete item list
        viewModel.getUnusedItemsName().observe(viewLifecycleOwner, {
            suggestedItemsAdapter.clear()
            suggestedItemsAdapter.addAll(it)

        })

        binding.actvSelectionItem.setAdapter(suggestedItemsAdapter)

        //Setup btn to add an new item
        binding.ibAddItem.setOnClickListener {
            //Create or get the item
            val name = binding.actvSelectionItem.text.toString().trim()
            if (name.isNotEmpty()) {
                val item = Item(name = name)
                item.isUsed = true
                item.order = System.currentTimeMillis()
                Utils.useItem(item, departmentsListAdapter)
                binding.actvSelectionItem.setText("")
            }

        }
        setupUnclassifiedItemsRV()
        setupDepartmentsRV()

        val departmentsName: ArrayList<String> = ArrayList()
        val suggestedDepartmentsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            departmentsName
        )
        binding.actDepartmentName.setAdapter(suggestedDepartmentsAdapter)


        viewModel.getUnusedDepartmentsName().observe(viewLifecycleOwner, {
            suggestedDepartmentsAdapter.clear()
            suggestedDepartmentsAdapter.addAll(it)
        }
        )

        //Setup btn to add an new department
        binding.ibAddDepartment.setOnClickListener {
            //Create the new department
            val depName = binding.actDepartmentName.text.toString().trim()
            if (depName.isNotEmpty()) {
                // Need to check if it exist first because we don't want to override an existing department
                var order = 0
                var depDb: Department? = null
                val job = backgroundScope.launch {
                    order = repo.getNumberOfDepartments()
                    depDb = repo.findDepartment(depName)
                }
                job.invokeOnCompletion {

                    val dep: Department = depDb
                        ?: Department(
                            depName,
                            true,
                            ArrayList(),
                            0,
                            order
                        )
                    dep.isUsed = true
                    Utils.saveDepartment(dep)

                }
                binding.actDepartmentName.setText("")
            }

        }
        // Inflate the layout for this fragment
        return binding.root
    }


    private fun setupDepartmentsRV() {
        //Create the department adapter
        departmentsListAdapter = DepartmentsListAdapter(
            requireContext()
        )


        //Setup departments recycler view
        binding.rvDepartment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvDepartment.adapter = departmentsListAdapter

        viewModel.getUsedDepartment().observe(viewLifecycleOwner, {
            val mlist = Utils.getFilteredDepartmentWithItems(it)
            departmentsListAdapter.updateList(mlist.toList())
        })

    }


    private fun setupUnclassifiedItemsRV() {
        //Create the items adapter
        unclassifiedAdapter =
            UnclassifiedItemsAdapter(
                requireContext()
            )

        viewModel.getUnclassifiedItems().observe(viewLifecycleOwner, { items ->
            unclassifiedAdapter.updateList(items)
        })


        //Setup the items recycler view
        binding.rvUnclassifiedItems.layoutManager =
            LinearLayoutManager(
                requireContext(), RecyclerView.VERTICAL, false
            )
        binding.rvUnclassifiedItems.adapter = unclassifiedAdapter
    }


}