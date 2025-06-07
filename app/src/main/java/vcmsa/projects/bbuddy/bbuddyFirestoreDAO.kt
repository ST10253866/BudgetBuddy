package vcmsa.projects.bbuddy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

class bbuddyFirestoreDAO {

    private val db = FirebaseFirestore.getInstance()

    // User operations
    fun insertUser(user: userEntity) {
        db.collection("users").document(user.fbUid).set(user)
    }


    fun updateUser(user: FirestoreUser) =
        db.collection("users").document(user.id.toString()).set(user)


    fun deleteUser(user: FirestoreUser) {
        db.collection("users").document(user.id.toString()).delete()
    }

    fun getAllUsers(): LiveData<List<FirestoreUser>> {
        val liveData = MutableLiveData<List<FirestoreUser>>()
        db.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                return@addSnapshotListener
            }
            val users = snapshot?.toObjects(FirestoreUser::class.java) ?: emptyList()
            liveData.postValue(users)
        }
        return liveData
    }
//this is defunct now
    fun getUserById(userId: Int): LiveData<FirestoreUser?> {
        val liveData = MutableLiveData<FirestoreUser?>()
        db.collection("users").document(userId.toString()).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                return@addSnapshotListener
            }
            val user = snapshot?.toObject(FirestoreUser::class.java)
            liveData.postValue(user)
        }
        return liveData
    }

    fun getUserByFbUid(fbUid: String): LiveData<FirestoreUser?> {
        val liveData = MutableLiveData<FirestoreUser?>()
        db.collection("users").document(fbUid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                return@addSnapshotListener
            }
            val user = snapshot?.toObject(FirestoreUser::class.java)
            liveData.postValue(user)
        }
        return liveData
    }



    // Category operations
    fun insertCategory(category: FirestoreCategory) {
        val docRef = db.collection("categories").document()
        category.id = docRef.id // assign Firestore's generated doc ID to the category object
        docRef.set(category)
    }

    fun updateCategory(category: FirestoreCategory) {
        db.collection("categories").document(category.id.toString()).set(category)
    }

    fun deleteCategory(category: FirestoreCategory) {
        db.collection("categories").document(category.id.toString()).delete()
    }

    fun getCategoriesByUser(userId: String): LiveData<List<FirestoreCategory>> {
        val liveData = MutableLiveData<List<FirestoreCategory>>()
        db.collection("categories").whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                val categories = snapshot?.toObjects(FirestoreCategory::class.java) ?: emptyList()
                liveData.postValue(categories)
            }
        return liveData
    }

    fun getAllCategories(): LiveData<List<FirestoreCategory>> {
        val liveData = MutableLiveData<List<FirestoreCategory>>()
        db.collection("categories").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                return@addSnapshotListener
            }
            val categories = snapshot?.toObjects(FirestoreCategory::class.java) ?: emptyList()
            liveData.postValue(categories)
        }
        return liveData
    }

    // Expense operations
    fun insertExpense(expense: FirestoreExpense) {
        db.collection("expenses").add(expense)
    }

    fun updateExpense(expense: FirestoreExpense) {
        db.collection("expenses").document(expense.id.toString()).set(expense)
    }

    fun deleteExpense(expense: FirestoreExpense) {
        db.collection("expenses").document(expense.id.toString()).delete()
    }

    fun getExpensesByMonthYear(monthYear: String): LiveData<List<FirestoreExpense>> {
        val liveData = MutableLiveData<List<FirestoreExpense>>()
        db.collection("expenses").whereEqualTo("monthYear", monthYear)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(FirestoreExpense::class.java) ?: emptyList()
                liveData.postValue(expenses)
            }
        return liveData
    }

    fun getExpensesByUser(userId: String): LiveData<List<FirestoreExpense>> {
        val liveData = MutableLiveData<List<FirestoreExpense>>()
        db.collection("expenses").whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(FirestoreExpense::class.java) ?: emptyList()
                liveData.postValue(expenses)
            }
        return liveData
    }

    fun getExpensesByCategory(categoryId: String): LiveData<List<FirestoreExpense>> {
        val liveData = MutableLiveData<List<FirestoreExpense>>()
        db.collection("expenses").whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                val expenses = snapshot?.toObjects(FirestoreExpense::class.java) ?: emptyList()
                liveData.postValue(expenses)
            }
        return liveData
    }

    fun getExpensesByCategoryAndDateRange(
        userId: String,
        categoryId: String,
        startDate: String, // Format: "MM/YYYY"
        endDate: String   // Format: "MM/YYYY"
    ): LiveData<List<FirestoreExpense>> {
        val liveData = MutableLiveData<List<FirestoreExpense>>()

        // Parse the date strings into comparable values
        val startParts = startDate.split("/")
        val endParts = endDate.split("/")

        if (startParts.size != 2 || endParts.size != 2) {
            liveData.postValue(emptyList())
            return liveData
        }

        val startMonth = startParts[0].toInt()
        val startYear = startParts[1].toInt()
        val endMonth = endParts[0].toInt()
        val endYear = endParts[1].toInt()

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    liveData.postValue(emptyList())
                    return@addSnapshotListener
                }

                val filteredExpenses = snapshot?.documents?.mapNotNull { doc ->
                    val expense = doc.toObject(FirestoreExpense::class.java)
                    expense?.let {
                        // Parse the expense's monthYear (format: "MM/YYYY")
                        val expenseParts = it.monthYear.split("/")
                        if (expenseParts.size == 2) {
                            val expenseMonth = expenseParts[0].toInt()
                            val expenseYear = expenseParts[1].toInt()

                            // Check if expense date is within range
                            when {
                                expenseYear < startYear -> null
                                expenseYear == startYear && expenseMonth < startMonth -> null
                                expenseYear > endYear -> null
                                expenseYear == endYear && expenseMonth > endMonth -> null
                                else -> it
                            }
                        } else {
                            null
                        }
                    }
                } ?: emptyList()

                liveData.postValue(filteredExpenses)
            }

        return liveData
    }

    fun getAllExpenses(): LiveData<List<FirestoreExpense>> {
        val liveData = MutableLiveData<List<FirestoreExpense>>()
        db.collection("expenses").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                return@addSnapshotListener
            }
            val expenses = snapshot?.toObjects(FirestoreExpense::class.java) ?: emptyList()
            liveData.postValue(expenses)
        }
        return liveData
    }
}
