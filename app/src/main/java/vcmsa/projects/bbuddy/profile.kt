package vcmsa.projects.bbuddy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import vcmsa.projects.bbuddy.databinding.FragmentProfileBinding

class profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val dao = bbuddyFirestoreDAO()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        val fbUid = UserSession.fbUid
        if (fbUid != null) {
            dao.getUserByFbUid(fbUid).observe(viewLifecycleOwner) { user ->
                user?.let {
                    binding.etRegisterLastname.setText(it.surname)
                    binding.etRegisterFirstname.setText(it.name)
                    binding.etRegisterIncome.setText(it.income.toString())
                }
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }

        binding.btnRegister.setText("Update Profile")

        binding.btnRegister.setOnClickListener {
            if (binding.etRegisterIncome.text.isNotEmpty() &&
                binding.etRegisterLastname.text.isNotEmpty() &&
                binding.etRegisterFirstname.text.isNotEmpty()
            ) {
                if (fbUid != null) {
                    dao.getUserByFbUid(fbUid).observe(viewLifecycleOwner) { user ->
                        user?.let {
                            val updatedUser = it.copy(
                                name = binding.etRegisterFirstname.text.toString(),
                                surname = binding.etRegisterLastname.text.toString(),
                                income = binding.etRegisterIncome.text.toString().toDouble()
                            )
                            dao.updateUser(updatedUser)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                }

                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
