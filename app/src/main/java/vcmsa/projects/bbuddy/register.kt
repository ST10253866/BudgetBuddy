package vcmsa.projects.bbuddy

import UserSession
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import vcmsa.projects.bbuddy.databinding.FragmentRegisterBinding


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [register.newInstance] factory method to
 * create an instance of this fragment.
 */class register : Fragment() {
    private lateinit var auth: FirebaseAuth
    private val binding: FragmentRegisterBinding by lazy {
        FragmentRegisterBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        binding.tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }

        val dao = bbuddyFirestoreDAO()

        if (UserSession.lang == "Af"){
            binding.txtTitle.text = "Skep rekening"
            binding.etRegisterIncome.hint = "Inkomste"
            binding.etRegisterFirstname.hint = "Voornaam"
            binding.etRegisterLastname.hint = "vannaam"
            binding.etRegisterEmail.hint = "e-pos"
            binding.etRegisterPassword.hint = "wagwoord"
            binding.btnRegister.text = "Registreer"
            binding.tvGoToLogin.text = "Het jy reeds 'n rekening? Meld aan"
        }

        binding.btnRegister.setOnClickListener {
            // Check if all fields are filled
            if (binding.etRegisterEmail.text.toString().isNotEmpty() &&
                binding.etRegisterFirstname.text.toString().isNotEmpty() &&
                binding.etRegisterLastname.text.toString().isNotEmpty() &&
                binding.etRegisterPassword.text.toString().isNotEmpty()
            ) {

                // Validate email with regex
                val regexVal = "^[\\w._%+-]+@[\\w.-]+\\.com$".toRegex()
                if (binding.etRegisterEmail.text.toString().matches(regexVal)) {
                    auth.createUserWithEmailAndPassword(
                        binding.etRegisterEmail.text.toString(),
                        binding.etRegisterPassword.text.toString()
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            activity?.runOnUiThread {

                                // Registration succeeded
                                // add to db now...
                                val user = userEntity(
                                    name = binding.etRegisterFirstname.text.toString(),
                                    surname = binding.etRegisterLastname.text.toString(),
                                    fbUid = auth.currentUser?.uid.orEmpty(),
                                    income = binding.etRegisterIncome.text.toString().toDouble()
                                         // will work because XML only takes numeric values
                                )
                                dao.insertUser(user)

                                // Insert into Room DB off the main thread
                                Thread {
                                    try {
                                        // Perform Room DB insertion
                                        BBuddyDatabase.getDatabase(requireContext())
                                            .bbuddyDAO()
                                            .insertUser(user)

                                        // On successful insert, show a success message
                                        activity?.runOnUiThread {
                                            Toast.makeText(
                                                requireActivity(),
                                                "User Registered Successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Optionally, navigate to a different fragment (e.g., dashboard, login)
                                            findNavController().navigate(R.id.action_register_to_login)
                                        }
                                    } catch (e: Exception) {
                                        // Handle any exceptions that occur during DB insertion
                                        activity?.runOnUiThread {
                                            Toast.makeText(
                                                requireActivity(),
                                                "Failed to save user to database",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }.start() // Start the thread for the Room DB insertion

                            }
                        } else {
                            // Registration failed (Firebase)
                            Toast.makeText(
                                requireActivity(),
                                "Register Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                            task.exception?.let {
                                Log.e(
                                    "RegisterError",
                                    it.message.toString()
                                )  // Log the error message
                                // Registration wasn't working hence the need for this log
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireActivity(), "Invalid email format", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
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