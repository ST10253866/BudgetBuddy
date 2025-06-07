package vcmsa.projects.bbuddy

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.launch
import vcmsa.projects.bbuddy.databinding.FragmentGraphBinding
import java.util.Calendar
import kotlin.math.max

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
        setupObservers()
        setupClickListeners()
        setupDatePickers()

        binding.edtStart.isFocusable = false
        binding.edtEnd.isFocusable = false

        // Add apply filter button
        binding.btnApply.setOnClickListener {
            if (binding.edtStart.text.isNullOrEmpty() || binding.edtEnd.text.isNullOrEmpty()) {
                Toast.makeText(context, "Please select both dates", Toast.LENGTH_SHORT).show()
            } else {
                startDate = binding.edtStart.text.toString()
                endDate = binding.edtEnd.text.toString()
                loadChartData()
            }
        }

        return binding.root
    }

    private fun setupObservers() {
        // Observe categories
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            userCategories.clear()
            userCategories.addAll(categories)
            loadChartData()
        }
    }

    private fun loadChartData() {
        if (userCategories.isEmpty()) return

        spendingValues.clear()
        minGoalValues.clear()
        maxGoalValues.clear()

        // For each category, get expenses and calculate totals
        for ((index, category) in userCategories.withIndex()) {
            lifecycleScope.launch {
                try {
                    val expenses = if (startDate != null && endDate != null) {
                        dao.getExpensesByCategoryAndDateRange(
                            userId,
                            category.id.toString(),
                            startDate!!,
                            endDate!!
                        ).value ?: emptyList()
                    } else {
                        dao.getExpensesByCategory(category.id.toString()).value ?: emptyList()
                    }

                    val totalSpending = expenses.sumOf { it.amount }.toFloat()

                    spendingValues.add(BarEntry(index.toFloat(), totalSpending))
                    minGoalValues.add(BarEntry(index.toFloat(), category.minAmount.toFloat()))
                    maxGoalValues.add(BarEntry(index.toFloat(), category.maxAmount.toFloat()))

                    setupChart()
                } catch (e: Exception) {
                    Log.e("Graph", "Error loading data", e)
                }
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

        // Combine datasets
        val data = BarData(spendingDataSet, minGoalDataSet, maxGoalDataSet).apply {
            barWidth = 0.25f
            groupBars(0f, 0.4f, 0.05f)
        }

        // X-axis config (kept same as before)
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(userCategories.map { it.name })
            labelCount = userCategories.size
        }

        // Chart config (kept same as before)
        binding.barChart.apply {
            description.isEnabled = false
            setMaxVisibleValueCount(25)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            axisLeft.setDrawGridLines(false)
            legend.isEnabled = true
            this.data = data
            invalidate()
            animateY(1000)
        }
    }

    // Keep your existing date picker and click listener methods exactly as they were
    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val showDatePicker: (EditText) -> Unit = { editText ->
            DatePickerDialog(
                this.requireContext(),
                { _, year, month, dayOfMonth ->
                    editText.setText(String.format("%02d/%02d", month + 1, year))
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