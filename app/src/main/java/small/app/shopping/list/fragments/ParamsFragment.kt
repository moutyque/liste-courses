package small.app.shopping.list.fragments

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import small.app.shopping.list.R
import small.app.shopping.list.adapters.DepartmentsParamsAdapter
import small.app.shopping.list.callback.SimpleItemTouchHelperCallback
import small.app.shopping.list.databinding.FragmentParamsBinding
import small.app.shopping.list.objects.Utils
import small.app.shopping.list.objects.Utils.TAG
import small.app.shopping.list.room.converters.DepartmentConverter
import small.app.shopping.list.room.converters.ItemConverter
import small.app.shopping.list.viewmodels.FragmentViewModel
import java.io.*

class ParamsFragment : Fragment() {

    private lateinit var binding: FragmentParamsBinding
    lateinit var departmentsAdapter: DepartmentsParamsAdapter
    private lateinit var viewModel: FragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentParamsBinding.inflate(inflater)
        viewModel = ViewModelProvider(this).get(FragmentViewModel::class.java)
        setupDepartmentsRV()

        binding.btnExport.setOnClickListener {
            createFile()
        }

        binding.btnImport.setOnClickListener {
            openFile()
        }
        return binding.root
    }

    // Request code for creating a PDF document.
    val CREATE_FILE = 1
    val OPEN_FILE = 2

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            //putExtra(Intent.EXTRA_TITLE, "export.json")
        }
        startActivityForResult(intent, OPEN_FILE, null)
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "export.json")
        }
        startActivityForResult(intent, CREATE_FILE, null)
    }


    private fun exportList(uri: Uri) {
        val contentResolver: ContentResolver = requireContext().contentResolver
        try {
            val export = JsonObject()
            val deps = JsonArray()
            val items = JsonArray()

            runBlocking(Dispatchers.IO) {
                Utils.getAllDepartmentAsJson().forEach { deps.add(it) }
                Utils.getAllItemsAsJson().forEach { items.add(it) }
                export.add("departments", deps)
                export.add("items", items)
            }
            contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fos ->
                    fos.write(
                        Gson().toJson(export).toByteArray()
                    )
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun readTextFromUri(uri: Uri): String {
        val stringBuilder = StringBuilder()
        requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun importList(uri: Uri) {
        val gson = Gson()
        val jsonString = readTextFromUri(uri)

        val export = gson.fromJson(jsonString, Export::class.java)
        Log.d(TAG, "file content : $jsonString")
        val depConverter = DepartmentConverter()
        val itemConverter = ItemConverter()
        export.departments.forEach { Utils.saveDepartment(depConverter.toDepartment(it)) }
        export.items.forEach { Utils.saveItem(itemConverter.toItem(it)) }

        Toast.makeText(requireContext(), getString(R.string.import_done), Toast.LENGTH_LONG).show()

    }

    internal data class Export(
        val departments: List<String>,
        val items: List<String>
    )


    private fun setupDepartmentsRV() {
        //Create the department adapter
        departmentsAdapter = DepartmentsParamsAdapter(
            requireContext()
        )

        //Setup departments recycler view
        binding.rvDepartment.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvDepartment.adapter = departmentsAdapter

        viewModel.getAllDepartments().observe(viewLifecycleOwner, {


            if (it?.isEmpty() == true) {
                binding.rvDepartment.visibility = View.GONE
                binding.tvNoData.visibility = View.VISIBLE
            } else {
                binding.rvDepartment.visibility = View.VISIBLE
                binding.tvNoData.visibility = View.GONE
                departmentsAdapter.updateList(it)
            }


        })


        val callback = SimpleItemTouchHelperCallback(
            departmentsAdapter,
            SimpleItemTouchHelperCallback.Direction.VERTICAL
        )
        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(binding.rvDepartment)


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG, "Result")
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK
        ) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            data?.data?.also { uri ->
                exportList(uri)
                Log.d(TAG, "file create here $uri")
            }
        } else if (requestCode == OPEN_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                Log.d(TAG, "Open file : $uri")
                importList(uri)
            }
        }
    }


}

