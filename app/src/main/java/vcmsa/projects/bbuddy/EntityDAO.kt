package vcmsa.projects.bbuddy

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface bbuddyDAO {

    // User operations
    @Insert
    fun insertUser(user: userEntity)

    @Update
    fun updateUser(user: userEntity)

    @Delete
    fun deleteUser(user: userEntity)

    @Query("SELECT * FROM users")
    fun getAllUsers(): LiveData<List<userEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): LiveData<userEntity>


    // Category operations
    @Insert
    fun insertCategory(category: categoryEntity)

    @Update
    fun updateCategory(category: categoryEntity)

    @Delete
    fun deleteCategory(category: categoryEntity)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategoriesByUser(userId: Int): LiveData<List<categoryEntity>>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): LiveData<List<categoryEntity>>


    // Expense operations
    @Insert
    fun insertExpense(expense: expenseEntity)

    @Update
    fun updateExpense(expense: expenseEntity)

    @Delete
    fun deleteExpense(expense: expenseEntity)

    @Query("SELECT * FROM expenses WHERE monthYear = :monthYear")
    fun getExpensesByMonthYear(monthYear: String): LiveData<List<expenseEntity>>

    @Query("SELECT * FROM expenses WHERE userId = :userId")
    fun getExpensesByUser(userId: Int): LiveData<List<expenseEntity>>

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    fun getExpensesByCategory(categoryId: Int): LiveData<List<expenseEntity>>

    @Query("SELECT * FROM expenses")
    fun getAllExpenses(): LiveData<List<expenseEntity>>
}
