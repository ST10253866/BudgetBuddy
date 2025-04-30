package vcmsa.projects.bbuddy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import vcmsa.projects.bbuddy.databinding.FragmentProfileBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class profile : Fragment() {

    private var param1: String? = null
    private var param2: String? = null
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

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

        //everything here should be called update not register... ffs
        binding.btnRegister.setOnClickListener {
            if (binding.etRegisterIncome.text.isNotEmpty() &&
                binding.etRegisterLastname.text.isNotEmpty() &&
                binding.etRegisterFirstname.text.isNotEmpty()
            ) {
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
                                requireActivity().runOnUiThread {
                                    Toast.makeText(
                                        requireContext(),
                                        "Profile updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.start()
                        }
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
