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
import vcmsa.projects.bbuddy.databinding.FragmentExpensesListBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [expensesList.newInstance] factory method to
 * create an instance of this fragment.
 */
class expensesList : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    var selectedCategoryId: Int? = null //need be declared for both listeners
    private var selectedImageUri: Uri? = null

    private var _binding: FragmentExpensesListBinding? = null
    private val binding get() = _binding!!

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // binding.imgExpensePhoto.setImageURI(it)
        }
    } //for the image thing

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
        _binding = FragmentExpensesListBinding.inflate(inflater, container, false)

        val db = BBuddyDatabase.getDatabase(requireContext())
        val dao = db.bbuddyDAO()
        val userId = UserSession.userId ?: 0

        // Observing categories and populating spinner
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            categories?.let {
                Log.d("AddExpenseFragment", "Fetched Categories: $it")  // Log categories for debugging

                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), "No categories available.", Toast.LENGTH_SHORT).show()
                } else {
                    val categoryNames = it.map { category -> category.name }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spnExpensesCategories.adapter = adapter

                    // Spinner item selection listener
                    binding.spnExpensesCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            val selectedCategory = it[position]
                            selectedCategoryId = selectedCategory.id

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
         * @return A new instance of fragment expensesList.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            expensesList().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
