package vcmsa.projects.bbuddy

import UserSession
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import vcmsa.projects.bbuddy.databinding.FragmentExpensesListBinding
import java.util.Calendar

class expensesList : Fragment() {

    private var selectedCategoryId: String? = null
    private var selectedImageUri: Uri? = null
    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ExpenseAdapter
    private lateinit var dao: bbuddyFirestoreDAO
    private val userId: String by lazy { UserSession.fbUid ?: "" }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedImageUri = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesListBinding.inflate(inflater, container, false)

        if (UserSession.lang == "Af") {
            binding.txtTitle.text = "Jou Uitgawes"
            binding.btnListBack.text = "Terug"
            binding.btnFilter.text = "Filterlys"
            binding.startDateEditText.hint = "Kies Datum"
            binding.txtTotalExpenses.text = "Totaal: R0.00"
        } else {
            binding.txtTitle.text = "Your Expenses"
            binding.btnListBack.text = "Back"
            binding.btnFilter.text = "Filter List"
            binding.startDateEditText.hint = "Select date"
            binding.txtTotalExpenses.text = "Total: R0.00"
        }

        setupDatePickers()
        binding.startDateEditText.isFocusable = false

        dao = bbuddyFirestoreDAO()

        // Set up RecyclerView
        adapter = ExpenseAdapter(emptyList())
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter

        // Load all expenses initially
        dao.getExpensesByUser(userId).observe(viewLifecycleOwner) { allExpenses ->
            adapter.updateData(allExpenses)
            updateTotal(allExpenses)
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

            if (selectedCategoryId != null) {
                dao.getExpensesByCategory(selectedCategoryId!!).observe(viewLifecycleOwner) { categoryExpenses ->
                    val filtered = if (selectedMonthYear.isNotEmpty()) {
                        categoryExpenses.filter { it.monthYear.equals(selectedMonthYear, ignoreCase = true) }
                    } else {
                        categoryExpenses
                    }
                    adapter.updateData(filtered)
                    updateTotal(filtered)
                }
            } else if (selectedMonthYear.isNotEmpty()) {
                dao.getExpensesByMonthYear(selectedMonthYear).observe(viewLifecycleOwner) { monthExpenses ->
                    adapter.updateData(monthExpenses)
                    updateTotal(monthExpenses)
                }
            } else {
                dao.getAllExpenses().observe(viewLifecycleOwner) { allExpenses ->
                    adapter.updateData(allExpenses)
                    updateTotal(allExpenses)
                }
            }
        }

        return binding.root
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val showDatePicker: (EditText) -> Unit = { editText ->
            DatePickerDialog(
                this.requireContext(),
                { _, year, month, _ ->
                    editText.setText(String.format("%02d/%04d", month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.startDateEditText.setOnClickListener { showDatePicker(binding.startDateEditText) }
    }

    private fun updateTotal(expenses: List<FirestoreExpense>) {
        val total = expenses.sumOf { it.amount }
        if (UserSession.lang == "Af") {
            binding.txtTotalExpenses.text = "Totaal: R%.2f".format(total)
        } else {
            binding.txtTotalExpenses.text = "Total: R%.2f".format(total)
        }
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
/*
 *Attribution
 *Website : geeksforgeeks
 *Author : GeeksforGeeks Introduction to Fragments
 *Url : https://www.geeksforgeeks.org/introduction-fragments-android/
 *Accessed on :(Accessed on 19 April 2025)
 */

/*
 *Attribution
 *Website : geeksforgeeks
 *Author : GeeksforGeeks Fragment Lifecycle in Android
 *Url : https://www.geeksforgeeks.org/fragment-lifecycle-in-android/.
 *Accessed on :(Accessed on 20 April 2025)
 */