package vcmsa.projects.bbuddy

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import vcmsa.projects.bbuddy.databinding.FragmentAddExpenseBinding

class addExpense : Fragment() {

    private var selectedCategoryId: String? = null
    private var selectedImageUri: Uri? = null
    private val dao = bbuddyFirestoreDAO()

    private val binding: FragmentAddExpenseBinding by lazy {
        FragmentAddExpenseBinding.inflate(layoutInflater)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgExpensePhoto.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = UserSession.fbUid ?: ""

        // Observe user's categories and populate spinner
        dao.getCategoriesByUser(userId).observe(this, Observer { categories ->
            categories?.let {
                Log.d("AddExpenseFragment", "Fetched Categories: $it")

                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), "No categories available.", Toast.LENGTH_SHORT).show()
                    binding.btnSaveExpense.apply {
                        isEnabled = false
                        setBackgroundColor(Color.rgb(136, 136, 136))
                    }
                } else {
                    binding.btnSaveExpense.apply {
                        isEnabled = true
                        setBackgroundColor(Color.rgb(137, 201, 163))
                    }

                    val categoryNames = it.map { category -> category.name }
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        categoryNames
                    ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

                    binding.spnCategories.adapter = adapter

                    binding.spnCategories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            selectedCategoryId = it[position].id
                            Log.d("AddExpenseFragment", "Selected Category ID: $selectedCategoryId")
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setupButtonListeners()
        return binding.root
    }

    private fun setupButtonListeners() {
        binding.btnAddBack.setOnClickListener {
            findNavController().navigate(R.id.action_addExpense_to_home)
        }

        binding.btnUploadPhoto.setOnClickListener {
            Log.d("AddExpenseFragment", "Upload photo button clicked")
            Toast.makeText(requireContext(), "Opening image picker", Toast.LENGTH_SHORT).show()
            getContent.launch("image/*")
        }

        binding.btnSaveExpense.setOnClickListener {
            saveExpense()
        }
    }

    private fun saveExpense() {
        val userId = UserSession.fbUid ?: ""

        if (binding.etExpenseAmount.text.isNullOrEmpty() ||
            binding.etMonthYear.text.isNullOrEmpty() ||
            selectedCategoryId.isNullOrEmpty()
        ) {
            Toast.makeText(requireContext(), "Please complete all required fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = FirestoreExpense(
            userId = userId,
            name = binding.etExpenseName.text.toString(),
            description = binding.etExpenseDescription.text.toString(),
            monthYear = binding.etMonthYear.text.toString(),
            amount = binding.etExpenseAmount.text.toString().toDouble(),
            categoryId = selectedCategoryId!!,
            imageUri = selectedImageUri?.toString() ?: ""
        )


        dao.insertExpense(expense)

        Toast.makeText(requireContext(), "Expense Created Successfully", Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.action_addExpense_to_home)
    }
}
