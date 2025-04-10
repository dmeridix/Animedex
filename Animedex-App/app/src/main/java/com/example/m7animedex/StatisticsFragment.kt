package com.example.m7animedex

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StatisticsFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())

        val barChart: BarChart = view.findViewById(R.id.barChart)
        val pieChart: PieChart = view.findViewById(R.id.pieChart)

        lifecycleScope.launch {
            val animeVisits = userPreferences.getAnimeVisits().first()
            val stats = userPreferences.getStats().first()

            setupBarChart(barChart, animeVisits)
            setupPieChart(pieChart, stats)
        }
    }

    private fun setupBarChart(barChart: BarChart, animeVisits: Map<Int, Int>) {
        if (animeVisits.isEmpty()) {
            println("animeVisits is empty")
            return
        }

        val entries = animeVisits.map { BarEntry(it.key.toFloat(), it.value.toFloat()) }
        val dataSet = BarDataSet(entries, "Animes más vistos").apply {
            setColors(Color.BLUE, Color.RED)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            axisLeft.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
            setFitBars(true)
            setPinchZoom(false)
            setDoubleTapToZoomEnabled(false)
            invalidate()
        }
    }

    private fun setupPieChart(pieChart: PieChart, stats: Pair<Int, Int>) {
        val (total, favs) = stats
        val nonFavs = total - favs

        val entries = listOf(
            PieEntry(favs.toFloat(), "Añadidos a favoritos"),
            PieEntry(nonFavs.toFloat(), "No añadidos")
        )

        val dataSet = PieDataSet(entries, "Porcentaje de favoritos").apply {
            setColors(Color.GREEN, Color.GRAY)
            valueTextColor = Color.WHITE
            valueTextSize = 16f
            sliceSpace = 2f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 55f
            legend.isEnabled = true
            legend.textSize = 14f
            invalidate()
        }
    }
}