package vcmsa.projects.bbuddy

import android.graphics.Color
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

        binding.tvGoToLogin.setOnClickListener{
            findNavController().navigate(R.id.action_register_to_login)
        }

        binding.btnRegister.setOnClickListener {
            // Check if all fields are filled
            if (binding.etRegisterEmail.text.toString().isNotEmpty() &&
                binding.etRegisterFirstname.text.toString().isNotEmpty() &&
                binding.etRegisterLastname.text.toString().isNotEmpty() &&
                binding.etRegisterPassword.text.toString().isNotEmpty()) {

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
                                Toast.makeText(requireActivity(), "Register Succeeded", Toast.LENGTH_SHORT).show()
                              //  binding.btnRegister.setTextColor(Color.parseColor("#FF0000"))  just used to check smtthng
                            }
                        } else {
                            activity?.runOnUiThread {
                                // Registration failed
                                Toast.makeText(requireActivity(), "Register Failed", Toast.LENGTH_SHORT).show()
                                task.exception?.let {
                                    Log.e("RegisterError", it.message.toString())  // Log the error message
                                    //reg wasnt working hence the neeed for this log
                                }
                            }
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(requireActivity(), "Invalid email format", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                activity?.runOnUiThread {
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
