package vcmsa.projects.bbuddy

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.bbuddy.databinding.FragmentExpensesListBinding

class expensesList : Fragment() {

    private var selectedCategoryId: Int? = null
    private var selectedImageUri: Uri? = null
    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExpenseAdapter
    private lateinit var dao: bbuddyDAO
    private val userId: Int by lazy { UserSession.userId ?: 0 }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesListBinding.inflate(inflater, container, false)

        val db = BBuddyDatabase.getDatabase(requireContext())
        dao = db.bbuddyDAO()

        // Set up RecyclerView
        adapter = ExpenseAdapter(emptyList())
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        // Load all expenses initially
        dao.getExpensesByUser(userId).observe(viewLifecycleOwner) { allExpenses ->
            adapter.updateData(allExpenses)
        }

        // Populate category spinner
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            if (categories.isNotEmpty()) {
                val categoryNames = categories.map { it.name }
                val categoryMap = categories.associateBy { it.name }

                val spinnerAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )

                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnExpensesCategories.adapter = spinnerAdapter

                binding.spnExpensesCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedName = categoryNames[position]
                        selectedCategoryId = categoryMap[selectedName]?.id
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedCategoryId = null
                    }
                }
            }
        }

        binding.btnListBack.setOnClickListener {
            findNavController().navigate(R.id.action_addExpense_to_home)
        }

        // Filter button click listener
        binding.btnFilter.setOnClickListener {
            val selectedMonthYear = binding.startDateEditText.text.toString().trim()

            // Logic: filter by category, then by month if present
            if (selectedCategoryId != null) {
                dao.getExpensesByCategory(selectedCategoryId!!).observe(viewLifecycleOwner) { categoryExpenses ->
                    val filtered = if (selectedMonthYear.isNotEmpty()) {
                        categoryExpenses.filter { it.monthYear.equals(selectedMonthYear, ignoreCase = true) }
                    } else {
                        categoryExpenses
                    }
                    adapter.updateData(filtered)
                }
            } else if (selectedMonthYear.isNotEmpty()) {
                dao.getExpensesByMonthYear(selectedMonthYear).observe(viewLifecycleOwner) { monthExpenses ->
                    adapter.updateData(monthExpenses)
                }
            } else {
                dao.getAllExpenses().observe(viewLifecycleOwner) { allExpenses ->
                    adapter.updateData(allExpenses)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            expensesList().apply {
                arguments = Bundle().apply {
                    putString("param1", param1)
                    putString("param2", param2)
                }
            }
    }
}
