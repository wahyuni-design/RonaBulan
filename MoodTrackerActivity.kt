package com.ronabulan.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class MoodTrackerActivity : AppCompatActivity() {

    private var selectedMood: String? = null
    private var selectedMoodEmoji: String? = null
    private val moodMap = mapOf(
        R.id.mood_happy to Pair("Bahagia", "😊"),
        R.id.mood_sad to Pair("Sedih", "😢"),
        R.id.mood_angry to Pair("Marah", "😤"),
        R.id.mood_anxious to Pair("Cemas", "😰"),
        R.id.mood_calm to Pair("Tenang", "😌"),
        R.id.mood_romantic to Pair("Romantis", "🥰"),
        R.id.mood_tired to Pair("Lelah", "😴"),
        R.id.mood_energetic to Pair("Bersemangat", "⚡")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mood_tracker)
        supportActionBar?.hide()

        setupMoodButtons()
        setupEnergySeekBar()
        setupSaveButton()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
    }

    private fun setupMoodButtons() {
        moodMap.forEach { (viewId, moodData) ->
            val layout = findViewById<LinearLayout>(viewId)
            layout.setOnClickListener {
                selectMood(viewId, moodData.first, moodData.second)
            }
        }
    }

    private fun selectMood(selectedId: Int, moodName: String, emoji: String) {
        selectedMood = moodName
        selectedMoodEmoji = emoji

        // Reset all
        moodMap.keys.forEach { id ->
            findViewById<LinearLayout>(id).isSelected = false
        }

        // Select current with animation
        val selected = findViewById<LinearLayout>(selectedId)
        selected.isSelected = true
        selected.animate().scaleX(1.06f).scaleY(1.06f).setDuration(120).withEndAction {
            selected.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()

        // Show preview
        val previewLayout = findViewById<LinearLayout>(R.id.layout_selected_mood)
        previewLayout.visibility = android.view.View.VISIBLE
        previewLayout.alpha = 0f
        previewLayout.animate().alpha(1f).setDuration(250).start()

        findViewById<TextView>(R.id.tv_selected_mood_emoji).text = emoji
        findViewById<TextView>(R.id.tv_selected_mood_name).text = moodName
    }

    private fun setupEnergySeekBar() {
        val seekBar = findViewById<SeekBar>(R.id.seekbar_energy)
        val tvEnergy = findViewById<TextView>(R.id.tv_energy_value)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                tvEnergy.text = "${progress + 1}/10"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        tvEnergy.text = "${seekBar.progress + 1}/10"
    }

    private fun setupSaveButton() {
        findViewById<Button>(R.id.btn_save_mood).setOnClickListener {
            saveMood()
        }
    }

    private fun saveMood() {
        val mood = selectedMood
        val emoji = selectedMoodEmoji

        if (mood == null || emoji == null) {
            showSnackbar("💕 Pilih mood dulu yuk!")
            return
        }

        val energyLevel = findViewById<SeekBar>(R.id.seekbar_energy).progress + 1
        val note = findViewById<TextInputEditText>(R.id.et_mood_note).text?.toString() ?: ""

        val record = MoodRecord(
            mood = mood,
            moodEmoji = emoji,
            energyLevel = energyLevel,
            note = note
        )

        CycleRepository.saveMoodRecord(this, record)

        // Success animation
        val btn = findViewById<Button>(R.id.btn_save_mood)
        btn.text = "✅ Mood Tersimpan!"
        btn.postDelayed({
            showSnackbar("💕 Mood hari ini berhasil disimpan!")
            finish()
        }, 700)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.pink_600))
            .setTextColor(getColor(R.color.white))
            .show()
    }
}

