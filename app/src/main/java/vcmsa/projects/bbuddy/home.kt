package vcmsa.projects.bbuddy

import UserSession
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import vcmsa.projects.bbuddy.databinding.FragmentHomeBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [home.newInstance] factory method to
 * create an instance of this fragment.
 */
class home : Fragment() {
    private val binding: FragmentHomeBinding by lazy {
        FragmentHomeBinding.inflate(layoutInflater)
    }
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Toast.makeText(requireContext(), "Welcome", Toast.LENGTH_SHORT).show()

        //nav stuff below
        binding.btnAddExpense.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_addExpense)
        }

        binding.btnAddCategory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_createCategory)
        }

        binding.btnViewExpenses.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_expensesList)
        }

        binding.btnViewGraph.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_graph)
        }

        binding.btnCatExp.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_categoryExpenseFragment)
        }
        //end of nav stuff
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (UserSession.lang == "Af"){
            binding.txtTitle.text = "Welkom by begrotingsmaat"
            binding.btnViewGraph.text = "Bekyk Grafiek"
            binding.btnViewExpenses.text = "Bekyk uitgawes"
            binding.btnAddExpense.text = "Voeg uitgawes by"
            binding.btnAddCategory.text = "Skep kategorie"
        } else {
            binding.txtTitle.text = "Welcome to budget buddy"
            binding.btnViewGraph.text = "View Graph"
            binding.btnViewExpenses.text = "View Expenses"
            binding.btnAddExpense.text = "Add Expense"
            binding.btnAddCategory.text = "Create Category"
        }

        return binding.root
    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment home.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            home().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}