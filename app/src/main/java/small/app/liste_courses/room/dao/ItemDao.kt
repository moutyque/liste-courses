package small.app.liste_courses.room.dao

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.SortedList
import androidx.room.*
import small.app.liste_courses.room.entities.Item


@Dao
interface ItemDao {
    //TODO : check to get the classified item
    @Query("SELECT * FROM Item")
    fun getAll(): List<Item>

    @Query("SELECT * FROM Item WHERE isUsed == :isUsed ORDER BY `order`")
    fun getAllWithUsage(isUsed: Boolean): List<Item>

    @Query("SELECT * FROM Item WHERE isUsed == :isUsed AND isClassified == :isClassified ORDER BY `order`")
    fun getAllWithUsageAndClassification(isUsed: Boolean, isClassified: Boolean): LiveData<List<Item>>

    @Query("SELECT * FROM Item WHERE name == :ref ORDER BY `order`")
    fun findByName(ref: String): Item?

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg items: Item)

    @Transaction
    @Delete
    fun delete(item: Item)

    @Query("SELECT * FROM Item WHERE departmentId == :depName ORDER BY `order`")
    fun findByDepName(depName: String): List<Item>
}