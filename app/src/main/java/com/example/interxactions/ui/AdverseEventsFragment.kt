package com.example.interxactions.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.interxactions.R
import com.example.interxactions.data.database.SearchedDrugViewModel
import com.example.interxactions.utils.OutcomesEnum
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * A simple [Fragment] subclass.
 * Use the [AdverseEventsFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 * This fragment is responsible for displaying the adverse events
 * for a given drug. It uses the OpenFDA API to get the data and
 * displays it in a pie chart.
 */
class AdverseEventsFragment : Fragment(R.layout.adverse_events_layout) {
    // View model for the adverse events fragment
    private val viewModel: AdverseEventsViewModel by viewModels()

    // View model for the searched drug. Used to get the most recent searched drug to use in the API call
    private val searchedDrugsViewModel: SearchedDrugViewModel by viewModels()

    // Total number of outcomes for the drug
    private var totalOutcomes: Float = 0f
    private var outcomeCount = mutableMapOf<String, Int>()

    // Pie chart for displaying the data
    private lateinit var pieChart: PieChart
    private lateinit var adverseHeadline: TextView

    // Loading indicator for when the API call is being made
    private lateinit var loadingIndicator: CircularProgressIndicator
    private lateinit var adverseEventsView: View

    // The text view to display error messages
    private lateinit var errorMessage: TextView

    // Cached drug name for the most recent searched drug
    private lateinit var cachedDrugName: String

    @SuppressLint("ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adverseEventsView = view.findViewById(R.id.constraint_layout_adverse)
        loadingIndicator = view.findViewById(R.id.loading_indicator)
        errorMessage = view.findViewById(R.id.tv_error_message)

        /*
         * Whenever an API call is made through the view model that doesn't
         * used cache data the outcomeCounts will be changed calling this
         * listener. This function maintains the data set for our pie chart
         * and sets it up with the values we have stored.
         */
        viewModel.outcomeCounts.observe(viewLifecycleOwner) { outcomes ->
            // If we have data then we can display it
            if (outcomes != null) {
                Log.d("AdverseEventsFragment", outcomes.toString())

                adverseEventsView.visibility = View.VISIBLE

                pieChart = view.findViewById(R.id.pieChart_view)

                // Set up the headline for the adverse events
                adverseHeadline = view.findViewById(R.id.adverse_headline)
                adverseHeadline.text = getString(R.string.adverse_headline, cachedDrugName)

                // Set up the pie chart
                val pieDataset: PieDataSet
                val pieEntries = mutableListOf<PieEntry>()
                val pieColors = mutableListOf<Int>()

                totalOutcomes = 0f

                val reorderedResults = outcomes.results.sortedByDescending { it.term }
                // Iterate through the data and store it in a map
                reorderedResults.forEach { count ->
                    totalOutcomes += count.count

                    // Map the 6 values into the only 4 that we want using enum
                    when(count.term) {
                        // If the outcome is not recovered or resolved, recovering or resolving, or recovered or resolved
                        OutcomesEnum.RECOVERED_WITH_LONG_TERM_ISSUES.value,
                        OutcomesEnum.NOT_RECOVERED_OR_RESOLVED.value,
                        OutcomesEnum.RECOVERING_RESOLVING.value,
                        OutcomesEnum.RECOVERED_RESOLVED.value -> {
                            // If the outcome is death, hospitalization, or long-lasting effects
                            if (outcomeCount.containsKey("Hospitalization")) {
                                outcomeCount["Hospitalization"] =
                                    outcomeCount["Hospitalization"]!! + count.count
                            } else {
                                outcomeCount["Hospitalization"] = count.count
                            }
                        }

                        // If the outcome is death
                        OutcomesEnum.FATAL.value -> {
                            outcomeCount["Death"] = count.count
                        }

                        // If the outcome is unknown
                        OutcomesEnum.UNKNOWN.value -> {
                            outcomeCount["Other"] = count.count
                        }
                    }
                }

                // Add colors for pie chart
                pieColors.add(Color.parseColor(getString(R.color.chart_hospitalization)))
                pieColors.add(Color.parseColor(getString(R.color.chart_death)))
                pieColors.add(Color.parseColor(getString(R.color.chart_other)))

                // Populate pie chart with data the outcomeCount map
                outcomeCount.forEach { (key, value) ->
                    val percent: Float = (value / totalOutcomes) * 100

                    pieEntries.add(0, PieEntry(percent, key))
                }

                // Set data and colors
                pieDataset = PieDataSet(pieEntries, "")
                pieDataset.colors = pieColors

                // Create pie data with data set
                val pieData = PieData(pieDataset)
                pieData.setValueTextSize(18f)
                pieData.setValueFormatter(PercentFormatter(pieChart))

                // Initialization and customization for pie chart
                pieChart.data = pieData
                pieChart.dragDecelerationFrictionCoef = 0.9f
                pieChart.rotationAngle = 90f

                pieChart.setHoleColor(Color.parseColor("#eceff4"))
                pieChart.centerText = getString(
                    R.string.chart_total_events,
                    "%,d".format(totalOutcomes.toInt())
                )
                pieChart.setCenterTextSize(18f)

                pieChart.animateY(1400, Easing.EaseInOutQuad)
                pieChart.setEntryLabelColor(Color.BLACK)

                pieChart.setUsePercentValues(true)
                pieChart.isRotationEnabled = true
                pieChart.isHighlightPerTapEnabled = false
                pieChart.description.isEnabled = false
                pieChart.legend.isEnabled = false

                pieChart.invalidate()
            }
        }

        // Set up an observer for the loading status of the API query
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            if (loading) {
                adverseEventsView.visibility = View.INVISIBLE
                loadingIndicator.visibility = View.VISIBLE
            }
            else{
                loadingIndicator.visibility = View.INVISIBLE
            }
        }

        // Set up an observer for the error status of the API query
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Log.e("AdverseEventsFragment", "Error fetching data: $error")
                adverseEventsView.visibility = View.INVISIBLE
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = getString(R.string.error_message, cachedDrugName, "Adverse Event Reporting System (FAERS)")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Set up an observer for the most recent searched drug
        searchedDrugsViewModel.mostRecentSearchedDrug.observe(viewLifecycleOwner) { drugs ->
            val drug = drugs[0]
            // If we have a drug then we can make the API call
            if (drug != null) {
                // Cache the most recent searched drug
                cachedDrugName = drug.drugName
                // Make the API call using the most recent searched drug
                val query = "patient.drug.openfda.generic_name:${drug.drugName}"
                viewModel.loadReactionOutcomeCount(query)
            }
        }
    }
}