package vcmsa.projects.bbuddy

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import vcmsa.projects.bbuddy.databinding.FragmentLoginBinding
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

// TODO: Rename parameter arguments, choose names that match
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [login.newInstance] factory method to
 * create an instance of this fragment.
 */
class login : Fragment() {

    private val binding: FragmentLoginBinding by lazy {
        FragmentLoginBinding.inflate(layoutInflater)
    }

    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        val auth = FirebaseAuth.getInstance()
        val db = BBuddyDatabase.getDatabase(requireContext())
        val dao = db.bbuddyDAO()

        binding.btnLogin.setOnClickListener {
            auth.signInWithEmailAndPassword(
                binding.etLoginUsername.text.toString(),
                binding.etLoginPassword.text.toString()
            ).addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Login success
                    // Observe the users only once login succeeds
                    dao.getAllUsers().observe(viewLifecycleOwner, Observer { users ->
                        val foundUser = users.find { it.fbUid == auth.currentUser?.uid }
                        // Log the found user and the users list for debugging
                        Log.d("Login", "Users found: $users")
                        if (foundUser != null) {
                            // Store the user ID in UserSession object
                            UserSession.userId = foundUser.id
                            Toast.makeText(requireContext(), "ID: ${foundUser.id}", Toast.LENGTH_SHORT).show()
                            // Proceed to the next screen
                            findNavController().navigate(R.id.action_login_to_home)
                        } else {
                            Toast.makeText(requireContext(), "User not found in DB.", Toast.LENGTH_SHORT).show()
                        }
                    })
                    Toast.makeText(requireContext(), "Authentication passed.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            login().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
