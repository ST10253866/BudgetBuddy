package vcmsa.projects.budgetbuddy

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.budgetbuddy.databinding.FragmentHomeBinding

class Hom : AppCompatActivity() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setContentView(R.layout.fragment_home)

        binding.btnAddExpense.setOnClickListener {
            val i = Intent(this, AddExpense::class.java).apply {  }
            startActivity(i)
        }

        binding.btnAddCategory.setOnClickListener {
            val i = Intent(this, CreateCategory::class.java).apply {  }
            startActivity(i)
        }

        binding.btnSetGoals.setOnClickListener {
            val i = Intent(this, Goals::class.java).apply {  }
            startActivity(i)
        }
        binding.btnViewExpenses.setOnClickListener {
            val i = Intent(this, ExpenseList::class.java).apply {  }
            startActivity(i)
        }

        binding.btnViewGraph.setOnClickListener {
            val i = Intent(this, Graph::class.java).apply {  }
            startActivity(i)
        }
    }
}