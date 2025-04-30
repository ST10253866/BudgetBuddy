package vcmsa.projects.bbuddy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import vcmsa.projects.bbuddy.databinding.FragmentProfileBinding
import vcmsa.projects.bbuddy.databinding.FragmentRegisterBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [profile.newInstance] factory method to
 * create an instance of this fragment.
 */
class profile : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val binding: FragmentProfileBinding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val db = BBuddyDatabase.getDatabase(requireContext())
        val dao = db.bbuddyDAO()

        UserSession.userId?.let { userId ->
            dao.getUserById(userId).observe(viewLifecycleOwner) { user ->
                user?.let {
                    binding.etRegisterLastname.setText(it.surname)
                    binding.etRegisterFirstname.setText(it.name)
                    binding.etRegisterIncome.setText(it.income.toString())

                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//everything here should be called update not register... ffs
        binding.btnRegister.setOnClickListener {
            val db = BBuddyDatabase.getDatabase(requireContext())
            val dao = db.bbuddyDAO()
            if(binding.etRegisterIncome.text.isNotEmpty() &&
                binding.etRegisterLastname.text.isNotEmpty() &&
                binding.etRegisterFirstname.text.isNotEmpty()){
                        //probably a more... civilized way of doing this every time but im wayy too tired
                UserSession.userId?.let { userId ->
                    dao.getUserById(userId).observe(viewLifecycleOwner) { existingUser ->
                        existingUser?.let {
                            val updatedUser = it.copy(
                                name = binding.etRegisterFirstname.text.toString(),
                                surname = binding.etRegisterLastname.text.toString(),
                                income = binding.etRegisterIncome.text.toString().toDouble()
                            )

                            Thread {
                                dao.updateUser(updatedUser)
                            }.start()
                            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        }
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment profile.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            profile().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}