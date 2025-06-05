package vcmsa.projects.bbuddy

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private var expenses: List<FirestoreExpense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvExpenseName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvExpenseDescription)
        val tvAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        val tvDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
        val ivImage: ImageView = itemView.findViewById(R.id.ivExpenseImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.expense_item, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvName.text = expense.name
        holder.tvDesc.text = expense.description
        holder.tvAmount.text = "R %.2f".format(expense.amount)
        holder.tvDate.text = expense.monthYear

        if (expense.imageUri.isNotEmpty()) {
            holder.ivImage.visibility = View.VISIBLE
            holder.ivImage.setImageURI(Uri.parse(expense.imageUri))
        } else {
            holder.ivImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<FirestoreExpense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
