package vcmsa.projects.bbuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import vcmsa.projects.bbuddy.databinding.FragmentCreateCategoryBinding

class createCategory : Fragment() {

    private val binding: FragmentCreateCategoryBinding by lazy {
        FragmentCreateCategoryBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val db = BBuddyDatabase.getDatabase(requireContext())
        val dao = db.bbuddyDAO()

        binding.btnCatBack.setOnClickListener {
            findNavController().navigate(R.id.action_createCategory_to_home)
        }

        binding.btnSaveCategory.setOnClickListener {
            if (binding.etMinGoal.text.toString().toDouble() >= binding.etMaxGoal.text.toString().toDouble()){
                Toast.makeText(requireActivity(), "Minimum goal must be less than the maximum goal", Toast.LENGTH_SHORT).show()
            } else {
                // Check if all fields are filled
                if (binding.etCategoryName.text.isNotEmpty() &&
                    binding.etMaxGoal.text.isNotEmpty() &&
                    binding.etMinGoal.text.isNotEmpty()
                ) {
                    try {
                        // Create a new category entity
                        val category = categoryEntity(
                            userId = UserSession.userId ?: 0,  // setting the fk defaults to 0 if null
                            name = binding.etCategoryName.text.toString(),
                            description = binding.etCategoryDescription.text.toString(),
                            maxAmount = binding.etMaxGoal.text.toString().toDouble(),
                            minAmount = binding.etMinGoal.text.toString().toDouble()
                        )

                        // Insert into the database in a background thread
                        Thread {
                            dao.insertCategory(category)

                            // Show success message on the main thread after insertion
                            activity?.runOnUiThread {
                                Toast.makeText(
                                    requireActivity(),
                                    "Category Created Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }.start()
                    } catch (e: Exception) {
                        // Handle any errors
                        activity?.runOnUiThread {
                            Toast.makeText(
                                requireActivity(),
                                "Error creating category: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // If fields are empty, show a toast
                    Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }
}


