package vcmsa.projects.bbuddy

import android.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import vcmsa.projects.bbuddy.databinding.FragmentCategorySummaryBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class categorySummary : Fragment() {

    private var param1: String? = null
    private var param2: String? = null

    private var selectedCategoryId: String? = null
    private var _binding: FragmentCategorySummaryBinding? = null
    private val binding get() = _binding!!

    private var userCategories: List<FirestoreCategory> = emptyList()

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

        val dao = bbuddyFirestoreDAO()
        val userId = UserSession.fbUid ?: ""

        // Fetch categories and populate spinner
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            categories?.let {
                userCategories = it

                Log.d("CategorySummaryFragment", "Fetched Categories: $it")

                if (it.isEmpty()) {
                    binding.btnSaveCategory.setBackgroundColor(Color.rgb(136, 136, 136))
                    Toast.makeText(requireContext(), "No categories available.", Toast.LENGTH_SHORT).show()
                } else {
                    binding.btnSaveCategory.setBackgroundColor(Color.rgb(137, 201, 163))

                    val categoryNames = it.map { category -> category.name }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.simple_spinner_item,
                        categoryNames
                    )
                    adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
                    binding.spnCategory.adapter = adapter

                    binding.spnCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
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

        binding.btnSumBack.setOnClickListener {
            findNavController().navigate(vcmsa.projects.bbuddy.R.id.action_categorySummary_to_home)
        }

        binding.btnSaveCategory.setOnClickListener {
            if (binding.etCategoryName.text.isNotEmpty() &&
                binding.etCategoryDescription.text.isNotEmpty() &&
                binding.etMinGoal.text.isNotEmpty() &&
                binding.etMaxGoal.text.isNotEmpty() &&
                selectedCategoryId != null
            ) {
                val minGoal = binding.etMinGoal.text.toString().toDoubleOrNull()
                val maxGoal = binding.etMaxGoal.text.toString().toDoubleOrNull()

                if (minGoal == null || maxGoal == null) {
                    Toast.makeText(requireContext(), "Invalid goal amounts.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (minGoal >= maxGoal) {
                    Toast.makeText(requireActivity(), "Minimum goal must be less than the maximum goal.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val categoryToUpdate = userCategories.find { it.id == selectedCategoryId }

                categoryToUpdate?.let {
                    val updatedCategory = it.copy(
                        name = binding.etCategoryName.text.toString(),
                        description = binding.etCategoryDescription.text.toString(),
                        minAmount = minGoal,
                        maxAmount = maxGoal
                    )

                    Thread {
                        dao.updateCategory(updatedCategory)
                        requireActivity().runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "Category updated successfully.",
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