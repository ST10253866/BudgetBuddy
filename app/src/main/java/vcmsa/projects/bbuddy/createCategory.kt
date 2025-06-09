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

        if (UserSession.lang == "Af"){
            binding.txtTitle.text = "Skep nuwe kategorie"
            binding.btnCatBack.text = "Terug"
            binding.etCategoryName.hint = "Kategorie Naam"
            binding.etCategoryDescription.hint = "Kategoriebeskrywing (Opsioneel)"
            binding.etMaxGoal.hint = "Maksimum maandelikse bestedingsdoelwit"
            binding.etMinGoal.hint = "Minimum maandelikse bestedingsdoelwit"
            binding.btnSaveCategory.hint = "Wysig kategorie"
        } else {
            binding.txtTitle.text = "Create new category"
            binding.btnCatBack.text = "back"
            binding.etCategoryName.hint = "category name"
            binding.etCategoryDescription.hint = "category description (optional)"
            binding.etMaxGoal.hint = "max spending goal"
            binding.etMinGoal.hint = "Min spending goal"
            binding.btnSaveCategory.hint = "Save category"
        }

       val dao = bbuddyFirestoreDAO()

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
                        val category = FirestoreCategory(
                            userId = UserSession.fbUid ?: "badData",
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
