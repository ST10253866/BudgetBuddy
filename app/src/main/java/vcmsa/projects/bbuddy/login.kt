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
import vcmsa.projects.bbuddy.databinding.FragmentLoginBinding

class login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val dao = bbuddyFirestoreDAO()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        if (UserSession.lang == "Af"){
            binding.txtTitle.text = "welkom terug"
            binding.etLoginPassword.hint = "wagwoord"
            binding.etLoginUsername.hint = "e-pos"
            binding.btnLogin.text = "aanmeld"
            binding.tvGoToRegister.text = "Het jy nie 'n rekening nie? Registreer hier"
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etLoginUsername.text.toString()
            val password = binding.etLoginPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        dao.getAllUsers().observe(viewLifecycleOwner) { users ->
                            val foundUser = users.find { it.fbUid == auth.currentUser?.uid }
                            Log.d("Login", "Users found: $users")

                            if (foundUser != null) {
                                UserSession.userId = foundUser.id
                                UserSession.fbUid = foundUser.fbUid
                                findNavController().navigate(R.id.action_login_to_home)
                            } else {
                                Toast.makeText(requireContext(), "User record not found.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Incorrect username or password", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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