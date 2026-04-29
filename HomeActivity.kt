package com.ronabulan.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        setupUI()
        setupClickListeners()
        animateCards()
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    private fun setupUI() {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id"))
        findViewById<TextView>(R.id.tv_date).text = sdf.format(Date())

        val cycleDay = CycleRepository.getCurrentCycleDay(this)
        findViewById<TextView>(R.id.tv_cycle_day_number).text = cycleDay.toString()

        val phase = CycleRepository.getCurrentPhase(this)
        val emoji = CycleRepository.getCurrentPhaseEmoji(this)
        findViewById<TextView>(R.id.tv_phase).text = "$emoji Fase $phase"
        findViewById<TextView>(R.id.tv_phase_desc).text =
            CycleRepository.getPhaseDescription(this)

        val daysUntil = CycleRepository.getDaysUntilNextPeriod(this)
        val nextPeriodText: String = when {
            daysUntil == 0 -> "Hari ini! 🌸"
            daysUntil == 1 -> "Besok"
            else -> "$daysUntil hari lagi"
        }
        findViewById<TextView>(R.id.tv_next_period).text = nextPeriodText

        val avgCycle = CycleRepository.getAverageCycleLength(this)
        findViewById<TextView>(R.id.tv_cycle_length).text = "$avgCycle hari"

        findViewById<TextView>(R.id.tv_tip).text = CycleRepository.getDailyTip(this)
    }

    private fun setupClickListeners() {
        // Catat Haid
        findViewById<CardView>(R.id.card_input).setOnClickListener { view ->
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                val intent = Intent(this@HomeActivity, InputPeriodActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }.start()
        }

        // Riwayat
        findViewById<CardView>(R.id.card_history).setOnClickListener { view ->
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                val intent = Intent(this@HomeActivity, HistoryActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }.start()
        }

        // Prediksi
        findViewById<CardView>(R.id.card_prediction).setOnClickListener { view ->
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                val intent = Intent(this@HomeActivity, PredictionActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }.start()
        }

        // Mood Tracker
        findViewById<CardView>(R.id.card_mood).setOnClickListener { view ->
            view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                val intent = Intent(this@HomeActivity, MoodTrackerActivity::class.java)
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }.start()
        }

        // Quick Mood Card
        findViewById<CardView>(R.id.card_today_mood).setOnClickListener {
            val intent = Intent(this@HomeActivity, MoodTrackerActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun animateCards() {
        val cardIds: List<Int> = listOf(
            R.id.card_input,
            R.id.card_history,
            R.id.card_prediction,
            R.id.card_mood
        )
        cardIds.forEachIndexed { index, id ->
            val card = findViewById<CardView>(id)
            card.alpha = 0f
            card.translationY = 60f
            card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay((index * 80).toLong())
                .setDuration(400)
                .start()
        }
    }
}

