package vcmsa.projects.bbuddy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import vcmsa.projects.bbuddy.databinding.FragmentGraphBinding

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
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val values = ArrayList<BarEntry>()
    private val userId: String by lazy { UserSession.fbUid ?: "" }
    var userCategories = ArrayList<String>()
    val dao = bbuddyFirestoreDAO()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        binding.btnGraphBack.setOnClickListener {
            findNavController().navigate(R.id.action_graph_to_home)
        }
        dataListing()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment



        //get categories
        /*dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            if (categories.isNotEmpty()) {
                val categoryNames = categories.map { it.name }
                val categoryMap = categories.associateBy { it.name }

                for (category in categoryNames){
                    userCategories.add(category)
                }
            } else {
                userCategories.add("no categories")
            }
        }*/
        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            userCategories.clear()

            //if (categories.isNotEmpty()) {
                userCategories.addAll(categories.map { it.name })
            /*} else {
                userCategories.add("no categories")
            }*/
        }

        return binding.root
    }

    private fun dataListing(){
        values.add(BarEntry(0.toFloat(), 6.toFloat()))
        values.add(BarEntry(1.toFloat(), 4.toFloat()))
        values.add(BarEntry(2.toFloat(), 8.toFloat()))
        values.add(BarEntry(3.toFloat(), 18.toFloat()))
        setChart()
    }

    private fun setChart(){
        binding.barChart.description.isEnabled = false
        binding.barChart.setMaxVisibleValueCount(25)
        binding.barChart.setPinchZoom(false)
        binding.barChart.setDrawBarShadow(false)
        binding.barChart.setDrawGridBackground(false)

        val xAxis = binding.barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.isGranularityEnabled = true



        xAxis.valueFormatter = IndexAxisValueFormatter(userCategories)

        binding.barChart.axisLeft.setDrawGridLines(false)
        binding.barChart.legend.isEnabled = false

        val barDataSetter: BarDataSet

        if (binding.barChart.data != null && binding.barChart.data.dataSetCount > 0){
            barDataSetter = binding.barChart.data.getDataSetByIndex(0) as BarDataSet
            barDataSetter.values = values
            binding.barChart.data.notifyDataChanged()
            binding.barChart.notifyDataSetChanged()
        } else {
            barDataSetter = BarDataSet(values, "Data Set")
            barDataSetter.setColors(*ColorTemplate.VORDIPLOM_COLORS)
            barDataSetter.setDrawValues(false)

            val dataSet = ArrayList<IBarDataSet>()
            dataSet.add(barDataSetter)

            val data = BarData(dataSet)
            binding.barChart.data = data
            binding.barChart.setFitBars(true)
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment graph.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            graph().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

    }
}