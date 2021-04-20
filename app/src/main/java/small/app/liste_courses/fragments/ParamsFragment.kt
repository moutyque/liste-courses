package small.app.liste_courses.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import small.app.liste_courses.adapters.DepartmentsParamsAdapter
import small.app.liste_courses.databinding.FragmentParamsBinding
import small.app.liste_courses.models.Department
import small.app.liste_courses.objects.SyncManager


/*
TODO : update recycler view when :
    new department
    classify item
    update item info
 */
class ParamsFragment : Fragment() {

private lateinit var binding : FragmentParamsBinding
    lateinit var departmentsAdapter : DepartmentsParamsAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentParamsBinding.inflate(inflater)
        setupDepartmentsRV()
        return binding.root
    }

    private fun setupDepartmentsRV() {
        //Create the department adapter
        departmentsAdapter = DepartmentsParamsAdapter(
            requireContext()
        )


        //Setup departments recycler view
        binding.rvDepartments.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvDepartments.adapter = departmentsAdapter
        SyncManager.setCreated(this)


    }

    fun onNewDepartment(d:Department){
        departmentsAdapter.add(d)
    }
}