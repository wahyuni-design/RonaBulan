package com.ronabulan.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class PredictionActivity : AppCompatActivity() {

    private val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
    private val sdfShort = SimpleDateFormat("dd MMM", Locale("id"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        loadPredictions()
        animateViews()
    }

    private fun loadPredictions() {
        // Next period
        val nextPeriod = CycleRepository.getNextPeriodDate(this)
        val daysUntil = CycleRepository.getDaysUntilNextPeriod(this)
        findViewById<TextView>(R.id.tv_next_period_date).text = sdf.format(nextPeriod)
        val daysLabel = when {
            daysUntil == 0 -> "Hari ini!"
            daysUntil == 1 -> "1"
            else -> daysUntil.toString()
        }
        findViewById<TextView>(R.id.tv_days_until_period).text = daysLabel

        // Ovulation
        val ovulation = CycleRepository.getOvulationDate(this)
        val today = Date()
        val ovulationLabel = if (ovulation.before(today) || isSameDay(ovulation, today))
            "Perkiraan hari ini / sudah lewat"
        else
            sdf.format(ovulation)
        findViewById<TextView>(R.id.tv_ovulation_date).text = ovulationLabel

        // Fertile window
        val fertileStart = CycleRepository.getFertileWindowStart(this)
        val fertileEnd = CycleRepository.getFertileWindowEnd(this)
        findViewById<TextView>(R.id.tv_fertile_window).text =
            "${sdfShort.format(fertileStart)} – ${sdfShort.format(fertileEnd)}"

        // Phase description
        val phase = CycleRepository.getCurrentPhase(this)
        val cycleDay = CycleRepository.getCurrentCycleDay(this)
        val emoji = CycleRepository.getCurrentPhaseEmoji(this)
        val desc = "✨ Kamu sedang berada di Hari $cycleDay — fase $phase. " +
                CycleRepository.getPhaseDescription(this)
        findViewById<TextView>(R.id.tv_current_phase_desc).text = desc

        // Insights
        val avgCycle = CycleRepository.getAverageCycleLength(this)
        val avgDur = CycleRepository.getAveragePeriodDuration(this)
        val records = CycleRepository.getPeriodRecords(this)

        val insight1 = when {
            avgCycle in 24..35 -> "✅ Siklusmu tergolong reguler ($avgCycle hari)"
            avgCycle < 24 -> "⚠️ Siklus pendek terdeteksi ($avgCycle hari) — pertimbangkan konsultasi dokter"
            else -> "⚠️ Siklus panjang terdeteksi ($avgCycle hari) — perhatikan pola ini"
        }
        val insight2 = "📈 Durasi haid rata-rata $avgDur hari"
        val insight3 = if (records.size >= 3)
            "📊 Data dari ${records.size} catatan tersimpan — prediksi semakin akurat!"
        else
            "💡 Tambahkan lebih banyak catatan untuk prediksi yang lebih akurat"

        findViewById<TextView>(R.id.tv_insight_1).text = insight1
        findViewById<TextView>(R.id.tv_insight_2).text = insight2
        findViewById<TextView>(R.id.tv_insight_3).text = insight3
    }

    private fun isSameDay(d1: Date, d2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = d1 }
        val cal2 = Calendar.getInstance().apply { time = d2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun animateViews() {
        val ids = listOf(
            R.id.tv_next_period_date,
            R.id.tv_fertile_window,
            R.id.tv_ovulation_date
        )
        ids.forEachIndexed { i, id ->
            val v = findViewById<TextView>(id)
            v.alpha = 0f
            v.translationX = -40f
            v.animate().alpha(1f).translationX(0f)
                .setStartDelay((i * 120).toLong()).setDuration(400).start()
        }
    }
}

