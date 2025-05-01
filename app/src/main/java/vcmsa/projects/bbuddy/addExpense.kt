package vcmsa.projects.bbuddy

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import vcmsa.projects.bbuddy.databinding.FragmentAddExpenseBinding
import android.util.Log
import androidx.navigation.fragment.findNavController

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [addExpense.newInstance] factory method to
 * create an instance of this fragment.
 */
class addExpense : androidx.fragment.app.Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    var selectedCategoryId: Int? = null //need be declared for both listeners
    private var selectedImageUri: Uri? = null
    private val binding: FragmentAddExpenseBinding by lazy {
        FragmentAddExpenseBinding.inflate(layoutInflater)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgExpensePhoto.setImageURI(it)
        }
    } //for the image thing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val db = BBuddyDatabase.getDatabase(requireContext())
        val dao = db.bbuddyDAO()
        val userId = UserSession.userId ?: 0

        // Observing categories and populating spinner
        dao.getCategoriesByUser(userId).observe(requireActivity(), Observer { categories ->
            categories?.let {
                Log.d("AddExpenseFragment", "Fetched Categories: $it")  // Log categories for debugging

                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), "No categories available.", Toast.LENGTH_SHORT).show()
                    binding.btnSaveExpense.isEnabled = false
                    var col = Color.rgb(136, 136, 136)
                    binding.btnSaveExpense.setBackgroundColor(col)
                } else {
                    binding.btnSaveExpense.isEnabled = true
                    var col = Color.rgb(137, 201, 163)
                    binding.btnSaveExpense.setBackgroundColor(col)
                    // Map the category names
                    val categoryNames = it.map { category -> category.name }

                    // Set up the spinner adapter
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames // pass category names as spinner items
                    )

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spnCategories.adapter = adapter

                    // Spinner item selection listener
                    binding.spnCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            // Get the selected category
                            val selectedCategory = it[position]
                            selectedCategoryId = selectedCategory.id

                            Toast.makeText(
                                requireActivity(),
                                "Selected Category ID: $selectedCategoryId",
                                Toast.LENGTH_SHORT // for debugging
                            ).show()
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
            }
        })
    }// all this for cat selection

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding.btnAddBack.setOnClickListener {
            findNavController().navigate(R.id.action_addExpense_to_home)
        }

        // Save expense on button click
        binding.btnSaveExpense.setOnClickListener {

           /* if ((!binding.etMonthYear.text[2].equals("/")) && binding.etMonthYear.text.count{it.isDigit()} != 6){
                Toast.makeText(requireActivity(), "Please format date correctly", Toast.LENGTH_SHORT).show()
            } else {*/
                val db = BBuddyDatabase.getDatabase(requireContext())
                val dao = db.bbuddyDAO()


                if (binding.etExpenseAmount.text.isNotEmpty() &&
                    binding.etMonthYear.text.isNotEmpty() &&
                    selectedCategoryId != null
                ) {

                    val expense = expenseEntity(
                        userId = UserSession.userId ?: 0,  // setting the fk defaults to 0 if null, though unlikely to happen
                        name = binding.etExpenseName.text.toString(),
                        description = binding.etExpenseDescription.text.toString(),
                        monthYear = binding.etMonthYear.text.toString(),
                        Amount = binding.etExpenseAmount.text.toString().toDouble(),
                        categoryId = selectedCategoryId!!,
                        imageUri = selectedImageUri?.toString() ?: "" // my bad here supposed to make the uri field nullable so this'll have to do for the time being
                    )

                    // Perform the database insert operation in a background thread
                    Thread {
                        try {
                            dao.insertExpense(expense)
                            activity?.runOnUiThread {
                                // Success message
                                Toast.makeText(
                                    requireActivity(),
                                    "Expense Created Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (e: Exception) {
                            // Handle any database errors here
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireActivity(),
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }.start()
                }
            //}
        }//end of btnSave

        binding.btnUploadPhoto.setOnClickListener {
            Log.d("AddExpenseFragment", "Upload photo button clicked")
            Toast.makeText(requireContext(), "Opening image picker", Toast.LENGTH_SHORT).show()
            getContent.launch("image/*")
        }

        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            addExpense().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
