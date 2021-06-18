package small.app.liste_courses.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_department.view.rv_items
import kotlinx.android.synthetic.main.item_department.view.tv_dep_name
import kotlinx.android.synthetic.main.item_department_param.view.*
import small.app.liste_courses.R
import small.app.liste_courses.adapters.listeners.ItemsDropListener
import small.app.liste_courses.callback.SimpleItemTouchHelperCallback
import small.app.liste_courses.objects.Utils

class DepartmentsParamsAdapter(context: Context, onlyUsed: Boolean = false) :
    DepartmentsAbstractAdapter(context, onlyUsed) {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return DepartmentsParamsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_department_param,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        holder.itemView.tv_dep_name.text = model.name
        //Perform the D&D action on department only if we click on the department title and not and the item list
        holder.itemView.tv_dep_name.setOnTouchListener { v, event ->
            Log.d("ClickOnDep", "I touched $event")

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    canMove = true
                    v.performClick()
                }
                MotionEvent.ACTION_UP -> canMove = false
            }
            true
        }
        //Recycler view for the items in the department

        Log.d("DAdapter", "department name : ${model.name} & items ${model.items}")


        var itemsAdapter = holder.itemView.rv_items.adapter
        if (itemsAdapter == null) {

            itemsAdapter = ItemsParamsAdapter(
                context,
                true
            )
            holder.itemView.rv_items.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            holder.itemView.rv_items.adapter = itemsAdapter


            val callback = SimpleItemTouchHelperCallback(
                itemsAdapter,
                SimpleItemTouchHelperCallback.Direction.VERTICAL
            )
            val itemTouchHelper = ItemTouchHelper(callback)
            itemTouchHelper.attachToRecyclerView(holder.itemView.rv_items)

        }

        (itemsAdapter as ItemsAdapter).updateList(model.items)

        holder.itemView.ib_expand.setOnClickListener {
            holder.itemView.ib_expand.visibility = View.GONE
            holder.itemView.ib_collapse.visibility = View.VISIBLE
            holder.itemView.rv_items.visibility = View.VISIBLE
        }

        holder.itemView.ib_collapse.setOnClickListener {
            holder.itemView.ib_expand.visibility = View.VISIBLE
            holder.itemView.ib_collapse.visibility = View.GONE
            holder.itemView.rv_items.visibility = View.GONE


        }

        val dragListen = ItemsDropListener(itemsAdapter, model)
        holder.itemView.setOnDragListener(dragListen)

        holder.itemView.ib_delete_department.setOnClickListener {
            Utils.deleteDepartment(list[position])
        }

        //Setup the drag and drop only on the reorder icon
        holder.itemView.iv_reorder.setOnTouchListener { v, event ->
            Log.d("Reorder", "Reorder department in Parameters Adapter. \nI touched $event")

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    canMove = true
                    v.performClick()
                }
                MotionEvent.ACTION_UP -> canMove = false
            }
            true
        }


    }


    class DepartmentsParamsViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)

}