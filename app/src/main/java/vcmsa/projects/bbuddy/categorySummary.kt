package vcmsa.projects.bbuddy

import android.R
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import vcmsa.projects.bbuddy.databinding.FragmentCategorySummaryBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [categorySummary.newInstance] factory method to
 * create an instance of this fragment.
 */
class categorySummary : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedCategoryId: Int? = null //need be declared for both listeners

    private var _binding: FragmentCategorySummaryBinding? = null
    private val binding get() = _binding!!

    private var userCategories: List<categoryEntity> = emptyList() // Store fetched categories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategorySummaryBinding.inflate(inflater, container, false)

        val db = BBuddyDatabase.getDatabase(requireContext())
        val dao = db.bbuddyDAO()
        val userId = UserSession.userId ?: 0

        // Observing categories and populating spinner
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            categories?.let {
                userCategories = it // Save locally

                Log.d("AddExpenseFragment", "Fetched Categories: $it")  // Log categories for debugging

                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), "No categories available.", Toast.LENGTH_SHORT).show()
                } else {
                    val categoryNames = it.map { category -> category.name }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.simple_spinner_item,
                        categoryNames
                    )

                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    binding.spnCategory.adapter = adapter

                    // Spinner item selection listener
                    binding.spnCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedCategory = it[position]
                            selectedCategoryId = selectedCategory.id

                            binding.etCategoryName.setText(selectedCategory.name)
                            binding.etCategoryDescription.setText(selectedCategory.description)
                            binding.etMinGoal.setText(selectedCategory.minAmount.toString())
                            binding.etMaxGoal.setText(selectedCategory.maxAmount.toString())

                            Toast.makeText(
                                requireActivity(),
                                "Selected Category ID: $selectedCategoryId",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
            }
        }

        //everything here should be called update not register... ffs
        binding.btnSaveCategory.setOnClickListener {
            if (binding.etCategoryName.text.isNotEmpty() &&
                binding.etCategoryDescription.text.isNotEmpty() &&
                binding.etMaxGoal.text.isNotEmpty() &&
                binding.etMinGoal.text.isNotEmpty() &&
                selectedCategoryId != null
            ) {
                val categoryToUpdate = userCategories.find { it.id == selectedCategoryId }

                categoryToUpdate?.let {
                    val updatedCategory = it.copy(
                        name = binding.etCategoryName.text.toString(),
                        description = binding.etCategoryDescription.text.toString(),
                        minAmount = binding.etMinGoal.text.toString().toDoubleOrNull() ?: 0.0,
                        maxAmount = binding.etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0
                    )

                    Thread {
                        dao.updateCategory(updatedCategory)
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Category updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }.start()
                } ?: Toast.makeText(requireContext(), "Category not found.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment categorySummary.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            categorySummary().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
