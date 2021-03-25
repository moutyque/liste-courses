package small.app.liste_courses.room.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Department(
    @PrimaryKey val name: String,
    var order: Int
) {
    fun classify(item: Item) {
        item.departmentId = name
    }
}
