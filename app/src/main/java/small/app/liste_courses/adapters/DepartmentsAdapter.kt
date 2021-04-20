package small.app.liste_courses.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import kotlinx.android.synthetic.main.item_department.view.*
import kotlinx.coroutines.launch
import small.app.liste_courses.R
import small.app.liste_courses.adapters.listeners.IActions
import small.app.liste_courses.objects.Scope
import small.app.liste_courses.objects.Utils
import small.app.liste_courses.adapters.listeners.IItemUsed
import small.app.liste_courses.adapters.listeners.ItemsDropListener
import small.app.liste_courses.models.Department


class DepartmentsAdapter(
    context: Context, onlyUsed: Boolean = true
) :
    DepartmentsAbstractAdapter(context, onlyUsed) {


    lateinit var  synchroAction: IActions

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DepartmentViewHolder {

        return DepartmentViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_department,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
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


        val itemsAdapter = DepartmentItemsAdapter(
            model.items.toList(),
            context,
            false,
            object : IItemUsed {
                override fun onLastItemUse() {
                    list[0].isUsed = false
                    Utils.saveDepartment(list[0])
                    list.removeItemAt(0)
                }

                override fun onItemUse() {

                }

            }
        )
        holder.itemView.rv_items.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        holder.itemView.rv_items.adapter = itemsAdapter
        val dragListen = ItemsDropListener(itemsAdapter, model)
        holder.itemView.setOnDragListener(dragListen)

    }


    fun onItemMove(initialPosition: Int, targetPosition: Int) {
        if (initialPosition > -1 && targetPosition > -1) {
            with(list) {
                beginBatchedUpdates()
                val init = get(initialPosition)
                val target = get(targetPosition)

                val tmp = init.order
                init.order = target.order
                target.order = tmp

                Utils.saveDepartment(init)
                Utils.saveDepartment(target)
                endBatchedUpdates()
            }
        }


    }


    class DepartmentViewHolder(view: View) : ViewHolder(view)
    fun addDepartment(d: Department) {
        d.isUsed = true
        Utils.saveDepartment(d)
        Scope.mainScope.launch {
            synchroAction.onNewDepartment(d)


            with(list) {
                beginBatchedUpdates()
                add(d)
                endBatchedUpdates()
            }
        }
    }

    override fun add(i: Department) {
            list.add(i)
    }

    override fun remove(i: Department) {
            list.remove(i)
    }



}