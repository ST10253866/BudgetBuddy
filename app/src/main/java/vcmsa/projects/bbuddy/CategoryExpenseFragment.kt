package vcmsa.projects.bbuddy.ui

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.bbuddy.FirestoreCategory
import vcmsa.projects.bbuddy.FirestoreExpense
import vcmsa.projects.bbuddy.R
import vcmsa.projects.bbuddy.bbuddyFirestoreDAO

class CategoryExpenseFragment : Fragment() {
    private val dao = bbuddyFirestoreDAO()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_category_expense, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val containerLayout = view.findViewById<LinearLayout>(R.id.category_container)
        val userId = UserSession.fbUid

        dao.getCategoriesByUser(userId).observe(viewLifecycleOwner) { categories ->
            containerLayout.removeAllViews()
            categories.forEach { category ->
                addCategoryBar(containerLayout, category)
            }
        }
    }

    private fun addCategoryBar(parent: LinearLayout, category: FirestoreCategory) {
        viewLifecycleOwner.lifecycleScope.launch {
            // Fetch and sum
            val expenses: List<FirestoreExpense> =
                dao.getExpensesByCategorySuspend(category.id)
            val totalSpent = expenses.sumOf { it.amount }
            val minAmount = category.minAmount
            val maxAmount = category.maxAmount
            val range = (maxAmount - minAmount).coerceAtLeast(0.0)
            val progressValue = (totalSpent - minAmount)
                .coerceAtLeast(0.0)
                .coerceAtMost(range)

            // Container for this category
            val barLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dpToPx(8), 0, dpToPx(16))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Title
            val titleView = TextView(requireContext()).apply {
                text = category.name
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.darkText))
            }

            // Colors
            val greyColor = ContextCompat.getColor(requireContext(), R.color.lightGray)
            val usedColor = ContextCompat.getColor(
                requireContext(),
                if (totalSpent > maxAmount) R.color.overSpentRed else R.color.secondaryGreen
            )

            // Build a two‐layer drawable: full grey background + clipped overlay
            val bgDrawable = ColorDrawable(greyColor)
            val clipDrawable = ClipDrawable(
                ColorDrawable(usedColor),
                Gravity.START,
                ClipDrawable.HORIZONTAL
            )
            val layerDrawable = LayerDrawable(arrayOf(bgDrawable, clipDrawable)).apply {
                // IDs must match ProgressBar internals
                setId(0, android.R.id.background)
                setId(1, android.R.id.progress)
            }

            // ProgressBar
            val progressBar = ProgressBar(
                requireContext(), null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                max = range.toInt()
                progress = progressValue.toInt()
                progressDrawable = layerDrawable

                // Thicker bar
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(24)
                ).also { params ->
                    params.topMargin = dpToPx(8)
                    params.bottomMargin = dpToPx(4)
                }
            }

            // Status
            val statusView = TextView(requireContext()).apply {
                text = "${totalSpent.toInt()}/${maxAmount.toInt()} USED"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.mediumGray))
            }

            // Assemble
            barLayout.addView(titleView)
            barLayout.addView(progressBar)
            barLayout.addView(statusView)
            parent.addView(barLayout)
        }
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
}
