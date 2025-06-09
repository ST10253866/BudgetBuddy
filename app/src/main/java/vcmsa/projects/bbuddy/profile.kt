package vcmsa.projects.bbuddy

import UserSession
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
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

        if (UserSession.lang == "En") {
            binding.rbtEn.isChecked = true
            binding.rbtAf.isChecked = false
        } else {
            binding.rbtEn.isChecked = false
            binding.rbtAf.isChecked = true
        }

        if (UserSession.lang == "Af"){
            binding.txtTitle.text = "profiel"
            binding.etRegisterIncome.hint = "Inkomste"
            binding.etRegisterFirstname.hint = "Voornaam"
            binding.etRegisterLastname.hint = "vannaam"
            binding.btnRegister.text = "Opdateer Profiel"
        } else {
            binding.txtTitle.text = "profile"
            binding.etRegisterIncome.hint = "Income"
            binding.etRegisterFirstname.hint = "First Name"
            binding.etRegisterLastname.hint = "Last Name"
            binding.btnRegister.text = "Update Profile"
        }

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

        binding.rbtEn.setOnClickListener {
            binding.rbtAf.isChecked = false
        }

        binding.rbtAf.setOnClickListener {
            binding.rbtEn.isChecked = false
        }

        binding.btnRegister.setOnClickListener {
            if (binding.rbtEn.isChecked){
                UserSession.lang = "En"
            } else {
                UserSession.lang = "Af"
            }

            if (UserSession.lang == "Af"){
                binding.txtTitle.text = "profiel"
                binding.etRegisterIncome.hint = "Inkomste"
                binding.etRegisterFirstname.hint = "Voornaam"
                binding.etRegisterLastname.hint = "vannaam"
                binding.btnRegister.text = "Opdateer Profiel"
            } else {
                binding.txtTitle.text = "profile"
                binding.etRegisterIncome.hint = "Income"
                binding.etRegisterFirstname.hint = "First Name"
                binding.etRegisterLastname.hint = "Last Name"
                binding.btnRegister.text = "Update Profile"
            }

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
        // Dark Mode toggle logic
        val sharedPrefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPrefs.getBoolean("DarkMode", false)
        binding.switchDarkMode.isChecked = isDarkMode

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("DarkMode", isChecked).apply()
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            requireActivity().recreate()
        }


        return binding.root
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