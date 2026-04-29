package com.ronabulan.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        supportActionBar?.hide()

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        loadData()
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val records = CycleRepository.getPeriodRecords(this)
        val rv = findViewById<RecyclerView>(R.id.rv_history)
        val emptyLayout = findViewById<LinearLayout>(R.id.layout_empty)

        if (records.isEmpty()) {
            rv.visibility = View.GONE
            emptyLayout.visibility = View.VISIBLE
        } else {
            rv.visibility = View.VISIBLE
            emptyLayout.visibility = View.GONE

            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = HistoryAdapter(records)

            // Stats
            val avgCycle = CycleRepository.getAverageCycleLength(this)
            val avgDuration = CycleRepository.getAveragePeriodDuration(this)
            findViewById<TextView>(R.id.tv_avg_cycle).text = avgCycle.toString()
            findViewById<TextView>(R.id.tv_avg_duration).text = avgDuration.toString()
            findViewById<TextView>(R.id.tv_total_records).text = records.size.toString()
        }
    }
}

class HistoryAdapter(private val records: List<PeriodRecord>) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMonth: TextView = itemView.findViewById(R.id.tv_month)
        val tvDay: TextView = itemView.findViewById(R.id.tv_day)
        val tvRange: TextView = itemView.findViewById(R.id.tv_period_range)
        val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        val tvFlow: TextView = itemView.findViewById(R.id.tv_flow_badge)
        val tvSymptoms: TextView = itemView.findViewById(R.id.tv_symptoms_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val record = records[position]
        holder.tvMonth.text = record.monthLabel
        holder.tvDay.text = record.dayLabel
        holder.tvRange.text = record.rangeFormatted
        holder.tvDuration.text = "${record.durationDays} hari"
        val flowEmoji = when (record.flow) {
            "Ringan" -> "🌸 Ringan"
            "Berat" -> "🌊 Berat"
            else -> "💧 Normal"
        }
        holder.tvFlow.text = flowEmoji
        holder.tvSymptoms.text = if (record.symptoms.isEmpty()) "Tidak ada gejala"
        else record.symptoms.take(3).joinToString(", ")

        // Animate item
        holder.itemView.alpha = 0f
        holder.itemView.translationY = 30f
        holder.itemView.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay((position * 60).toLong())
            .setDuration(300)
            .start()
    }

    override fun getItemCount() = records.size
}

