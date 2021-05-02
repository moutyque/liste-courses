package small.app.liste_courses.objects

import kotlinx.coroutines.launch
import small.app.liste_courses.adapters.DepartmentsAdapter
import small.app.liste_courses.adapters.ItemsAdapter
import small.app.liste_courses.models.Department
import small.app.liste_courses.objects.Scope.backgroundScope
import small.app.liste_courses.objects.Scope.mainScope
import small.app.liste_courses.room.Repository
import small.app.liste_courses.room.entities.Item

object Utils {


    lateinit var repo: Repository


    fun useItem(
        item: Item,
        unclassifiedItemsAdapter: ItemsAdapter,
        departmentsAdapter: DepartmentsAdapter
    ) {
        var dbItem: Item? = null
        val job = backgroundScope.launch {
            dbItem = repo.findItem(item.name)
        }
        job.invokeOnCompletion {
            //If exist and not classified
            if (dbItem != null && dbItem!!.isClassified) {
                useClassifiedItem(dbItem!!, departmentsAdapter)
            } else {
                //Can exist and not use, for example default option
                useUnclassifiedItem(item, unclassifiedItemsAdapter, dbItem != null)
                //Remove the item from the unclassified auto complete listpm
            }
        }


    }

    private fun useUnclassifiedItem(item: Item, itemsAdapter: ItemsAdapter, exist: Boolean) {

        itemsAdapter.list.add(item)//SortedList update the view when inserted
        itemsAdapter.notifyItemInserted(itemsAdapter.list.indexOf(item))

        //if (!exist)
        //item.order = itemsAdapter.list.size.toLong()
        saveItem(item)


        /*mainScope.launch {
            //New item
            itemsAdapter.add(item)
        }*/
    }

    private fun useClassifiedItem(item: Item, departmentsAdapter: DepartmentsAdapter) {
        //Get the department from the item
        //Check if the department is already displayed through the department list
        //If no get the adapter and add it to the dep adapter
        //If yes add the item to the items list in the dep adapter => Update department and notify change on dep adapter

        var d: Department? = null
        val job = backgroundScope.launch {
            item.isUsed = true
            repo.saveItem(item)
            d = repo.findDepartment(item.departmentId)
        }
        job.invokeOnCompletion {
            mainScope.launch {
                if (d != null) {
                    //Remove unuseItem
                    val dep = d!!
                    val index = departmentsAdapter.findIndex(dep)
                    if (index < 0) {
                        dep.isUsed = true
                        dep.items.add(item)
                        departmentsAdapter.list.add(dep)
                        saveDepartment(dep)
                    }

                }
            }
        }
    }

    private fun keepUsedItems(dep: Department) {
        val filter = dep.items.filter { it.isUsed }.sortedWith(ItemsComparator())
        dep.items.clear()
        for (i in filter) {
            dep.items.add(i)
        }
    }

    fun classifyItem(item: Item, target: ItemsAdapter) {
        backgroundScope.launch {
            //Save the new item
            repo.saveItem(item)
        }
        target.list.add(item)
        target.list.sortedWith(ItemsComparator())

        target.notifyItemInserted(target.list.indexOf(item))
    }


    fun unuseItem(item: Item, itemsAdapter: ItemsAdapter) {
        val job = backgroundScope.launch {
            repo.saveItem(item)
        }
        job.invokeOnCompletion {
            mainScope.launch {
                /*itemsAdapter.list.beginBatchedUpdates()
                itemsAdapter.remove(item)
                //Hide department if last item hidden
                if (itemsAdapter.list.size() == 0) {
                    itemsAdapter.itemUsed.onLastItemUse()
                } else {
                    itemsAdapter.itemUsed.onItemUse()
                }
                itemsAdapter.list.endBatchedUpdates()*/
            }

        }
    }


    fun saveItem(item: Item) {
        backgroundScope.launch {
            repo.saveItem(item)
        }
    }

    fun saveDepartment(d: Department) {
        backgroundScope.launch {
            repo.saveDepartment(d)

        }
    }
}