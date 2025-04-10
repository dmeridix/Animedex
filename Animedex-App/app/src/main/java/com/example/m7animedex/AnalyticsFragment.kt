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
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AnalyticsFragment : Fragment() {

    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_analytics, container, false)
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
            println("No hay datos de visitas para mostrar en el gráfico de barras.")
            return
        }

        // Ordenamos por visitas descendente y cogemos los primeros 5 (por ejemplo)
        val topVisits = animeVisits.entries.sortedByDescending { it.value }.take(5)

        val entries = topVisits.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val labels = topVisits.map { "ID ${it.key}" }

        val dataSet = BarDataSet(entries, "Animes más vistos").apply {
            color = Color.parseColor("#3F51B5")
            valueTextSize = 14f
            valueTextColor = Color.BLACK
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        barChart.apply {
            data = barData
            description.isEnabled = false
            setFitBars(true)

            axisLeft.apply {
                axisMinimum = 0f
                textSize = 14f
            }

            axisRight.isEnabled = false

            xAxis.apply {
                granularity = 1f
                setDrawGridLines(false)
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(labels)
                textSize = 14f
            }

            legend.textSize = 14f
            legend.textColor = Color.BLACK

            invalidate()
            requestLayout()
        }
    }


    private fun setupPieChart(pieChart: PieChart, stats: Pair<Int, Int>) {
        val (total, favs) = stats
        val nonFavs = total - favs

        val entries = listOf(
            PieEntry(favs.toFloat(), "Favoritos"),
            PieEntry(nonFavs.toFloat(), "No añadidos")
        )

        val dataSet = PieDataSet(entries, "").apply {
            setColors(Color.GREEN, Color.GRAY)
            valueTextSize = 16f
            valueTextColor = Color.BLACK
            sliceSpace = 2f
        }

        val pieData = PieData(dataSet)

        pieChart.apply {
            data = pieData
            setUsePercentValues(true)
            setEntryLabelTextSize(16f)
            setEntryLabelColor(Color.BLACK)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 50f
            transparentCircleRadius = 55f

            // Configurar la leyenda correctamente
            legend.apply {
                textSize = 16f
                textColor = Color.BLACK
                formSize = 16f
                formToTextSpace = 8f
                xOffset = 80f // Desplaza la leyenda 20dp hacia la derecha
                xEntrySpace = 50f // Espacio horizontal entre los ítems de la leyenda
                yEntrySpace = 15f // Espacio vertical entre los ítems de la leyenda
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                maxSizePercent = 0.9f // Asegura que la leyenda no se corte
            }

            invalidate()
        }
    }
}
