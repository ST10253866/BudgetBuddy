package vcmsa.projects.bbuddy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

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

    fun getUserById(userId: Int): LiveData<FirestoreUser?> {
        val liveData = MutableLiveData<FirestoreUser?>()
        db.collection("users").document(userId.toString())
            .addSnapshotListener { snapshot, error ->
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
        db.collection("users").document(fbUid)
            .addSnapshotListener { snapshot, error ->
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
        category.id = docRef.id
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
        db.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                val cats = snapshot?.toObjects(FirestoreCategory::class.java) ?: emptyList()
                liveData.postValue(cats)
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
            val cats = snapshot?.toObjects(FirestoreCategory::class.java) ?: emptyList()
            liveData.postValue(cats)
        }
        return liveData
    }

    // Expense operations (LiveData-based)
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
        db.collection("expenses")
            .whereEqualTo("monthYear", monthYear)
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
        db.collection("expenses")
            .whereEqualTo("userId", userId)
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
        db.collection("expenses")
            .whereEqualTo("categoryId", categoryId)
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
        endDate: String    // Format: "MM/YYYY"
    ): LiveData<List<FirestoreExpense>> {
        val liveData = MutableLiveData<List<FirestoreExpense>>()

        val startParts = startDate.split("/")
        val endParts = endDate.split("/")
        if (startParts.size != 2 || endParts.size != 2) {
            liveData.postValue(emptyList())
            return liveData
        }

        val sM = startParts[0].toInt()
        val sY = startParts[1].toInt()
        val eM = endParts[0].toInt()
        val eY = endParts[1].toInt()

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreDAO", "Listen failed: ${error.message}")
                    liveData.postValue(emptyList())
                    return@addSnapshotListener
                }
                val filtered = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreExpense::class.java)?.let {
                        val parts = it.monthYear.split("/")
                        if (parts.size == 2) {
                            val mm = parts[0].toInt()
                            val yy = parts[1].toInt()
                            if (!((yy < sY) || (yy == sY && mm < sM) || (yy > eY) || (yy == eY && mm > eM))) {
                                it
                            } else null
                        } else null
                    }
                } ?: emptyList()
                liveData.postValue(filtered)
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

    /**issue basically was the way data was read-- cause it was a bg coroutine. livedata gets updated on main, and since it was bg it wasnt
     * too happy. this is syncornous or however ou spell it
     * https://github.com/Kotlin/kotlinx.coroutines/blob/master/README.md the official docs explain it better ig
     * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/launch.html more specifcally */

    /** Suspended fetch of expenses by category (no date filter) */
    suspend fun getExpensesByCategorySuspend(categoryId: String): List<FirestoreExpense> {
        val snapshot = db.collection("expenses")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()
        return snapshot.toObjects(FirestoreExpense::class.java)
    }

    /** Suspended fetch of expenses by category + date-range */
    suspend fun getExpensesByCategoryAndDateRangeSuspend(
        userId: String,
        categoryId: String,
        startDate: String, // Format: "MM/YYYY"
        endDate: String    // Format: "MM/YYYY"
    ): List<FirestoreExpense> {
        val snapshot = db.collection("expenses")
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .get()
            .await()

        val (sM, sY) = startDate.split("/").map(String::toInt)
        val (eM, eY) = endDate.split("/").map(String::toInt)

        return snapshot.toObjects(FirestoreExpense::class.java).filter { it.monthYear.split("/").let { (m, y) ->
            val mm = m.toInt()
            val yy = y.toInt()
            !((yy < sY) || (yy == sY && mm < sM) || (yy > eY) || (yy == eY && mm > eM))
        }}
    }
}
