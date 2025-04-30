package vcmsa.projects.bbuddy
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date
import java.sql.Time
import androidx.room.*

//User enttiy here to keep track of each user details
@Entity(tableName="users")
data class userEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int=0,
    val name: String,
    val surname: String,
    val income: Double,
    val fbUid: String
)

//catagories bridging enttiy and  is relates catagory enttiy  user enttiy
@Entity(
    tableName = "categories",
    foreignKeys = [ForeignKey(
        entity = userEntity::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)

//catagory TABLE
data class categoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val minAmount: Double,
    val maxAmount: Double,
    val userId: Int
)

//expense bridging enttiy and  is relates catagory enttiy  user enttiy
@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = categoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = userEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId"]), Index(value = ["userId"])]
)

//expense table
data class expenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val Amount: Double,
    val monthYear: String,
    val imageUri: String,
    val categoryId: Int,
    val userId: Int
)
