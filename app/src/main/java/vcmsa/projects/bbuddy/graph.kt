package vcmsa.projects.bbuddy

import UserSession
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcmsa.projects.bbuddy.databinding.FragmentGraphBinding
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [graph.newInstance] factory method to
 * create an instance of this fragment.
 */
class graph : Fragment() {
    private val binding: FragmentGraphBinding by lazy {
        FragmentGraphBinding.inflate(layoutInflater)
    }

    // store bar entries for actual spending, min and max goals
    private val spendingValues = ArrayList<BarEntry>()
    private val minGoalValues = ArrayList<BarEntry>()
    private val maxGoalValues = ArrayList<BarEntry>()
    private val userId: String by lazy { UserSession.fbUid ?: "" }
    private val userCategories = ArrayList<FirestoreCategory>() // Changed to store full category objects
    private val dao = bbuddyFirestoreDAO()
    private var startDate: String? = null
    private var endDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setupObservers() // Observe changes in categories and reload data
        setupClickListeners() // Set up navigation and apply-filter clicks
        setupDatePickers() // Initialize date pickers for start/end

        binding.edtStart.isFocusable = false // disable manual date typing
        binding.edtEnd.isFocusable = false

        // Add apply filter button
        binding.btnApply.setOnClickListener {
            if (binding.edtStart.text.isNullOrEmpty() || binding.edtEnd.text.isNullOrEmpty()) {
                Toast.makeText(context, "Please select both dates", Toast.LENGTH_SHORT).show()
            } else {
                startDate = binding.edtStart.text.toString()
                endDate = binding.edtEnd.text.toString()
                loadChartData() // reload chart with date filter
            }
        }

        return binding.root
    }

    private fun setupObservers() {
        // Observe categories
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            userCategories.clear()
            userCategories.addAll(categories)
            loadChartData() // load initial chart data when categories arrive
        }
    }

    private fun loadChartData() {
        if (userCategories.isEmpty()) return

        // Clear previous entries and reinitialize
        spendingValues.clear()
        minGoalValues.clear()
        maxGoalValues.clear()

        // Initialize all entries first with zero values for proper indexing
        userCategories.forEachIndexed { index, category ->
            spendingValues.add(BarEntry(index.toFloat(), 0f))
            minGoalValues.add(BarEntry(index.toFloat(), category.minAmount.toFloat()))
            maxGoalValues.add(BarEntry(index.toFloat(), category.maxAmount.toFloat()))
        }

        lifecycleScope.launch {
            try {
                userCategories.forEachIndexed { index, category ->
                    // use suspend DAO methods for reliable data
                    val expenses = withContext(Dispatchers.IO) {
                        if (startDate != null && endDate != null) {
                            dao.getExpensesByCategoryAndDateRangeSuspend(
                                userId,
                                category.id,
                                startDate!!,
                                endDate!!
                            )
                        } else {
                            dao.getExpensesByCategorySuspend(category.id)
                        }
                    }

                    // Ensure expenses are not null and sum them
                    val totalSpending = expenses.sumOf { it.amount }.toFloat()

                    // Update the pre-initialized entries in-place to preserve indices
                    spendingValues[index] = BarEntry(index.toFloat(), totalSpending)
                }

                // Update chart on main thread
                withContext(Dispatchers.Main) {
                    setupChart()
                }
            } catch (e: Exception) {
                Log.e("Graph", "Error loading data", e)
            }
        }
    }

    private fun setupChart() {
        // Create datasets
        val spendingDataSet = BarDataSet(spendingValues, "Actual Spending").apply {
            color = Color.rgb(255, 87, 34) // Orange
            setDrawValues(true)
        }

        val minGoalDataSet = BarDataSet(minGoalValues, "Min Goal").apply {
            color = Color.rgb(76, 175, 80) // Green
            setDrawValues(true)
        }

        val maxGoalDataSet = BarDataSet(maxGoalValues, "Max Goal").apply {
            color = Color.rgb(244, 67, 54) // Red
            setDrawValues(true)
        }

        // Combine datasets into BarData, set bar width
        val data = BarData(spendingDataSet, minGoalDataSet, maxGoalDataSet).apply {
            barWidth = 0.25f
        }

        // Group bars and set proper axis min/max fixed to avoid shifting
        //https://github.com/PhilJay/MPAndroidChart/wiki/
        val groupSpace = 0.2f
        val barSpace = 0.05f
        val groupCount = userCategories.size

        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(userCategories.map { it.name })
            labelCount = userCategories.size

            axisMinimum = 0f // start at zero for grouping
            axisMaximum = data.getGroupWidth(groupSpace, barSpace) * groupCount // end based on number of groups
        }

        binding.barChart.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(25)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            axisLeft.setDrawGridLines(false)
            legend.isEnabled = true
            this.data = data

            groupBars(0f, groupSpace, barSpace) // actual grouping call
            invalidate() // redraw chart
            animateY(1000)
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val showDatePicker: (EditText) -> Unit = { editText ->
            DatePickerDialog(
                this.requireContext(),
                { _, year, month, _ ->
                    // format date as MM/yyyy for month-based filtering
                    editText.setText(String.format("%02d/%d", month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.edtStart.setOnClickListener { showDatePicker(binding.edtStart) }
        binding.edtEnd.setOnClickListener { showDatePicker(binding.edtEnd) }
    }

    private fun setupClickListeners() {
        binding.btnGraphBack.setOnClickListener {
            findNavController().navigate(R.id.action_graph_to_home)
        }
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

/*
 *Attribution
 *Website : Youtube
 *Author : Admin Grabs Media -Android Display Bar Graph using Kotlin
 *Url- https://www.youtube.com/watch?v=-TGUV_LbcmE.
 *Accessed on :(Accessed on 03 June 2025)
 */
