package com.ronabulan.app

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


data class PeriodRecord(
    val id: String = UUID.randomUUID().toString(),
    val startDate: Long,
    val endDate: Long,
    val flow: String = "Normal",
    val symptoms: List<String> = emptyList(),
    val notes: String = ""
) {
    val durationDays: Int
        get() {
            val diff = endDate - startDate
            return (diff / (1000 * 60 * 60 * 24)).toInt() + 1
        }

    val startDateFormatted: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date(startDate))

    val endDateFormatted: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale("id")).format(Date(endDate))

    val rangeFormatted: String
        get() {
            val sdf = SimpleDateFormat("dd MMM", Locale("id"))
            val sdfYear = SimpleDateFormat("yyyy", Locale("id"))
            return "${sdf.format(Date(startDate))} – ${sdf.format(Date(endDate))} ${sdfYear.format(Date(endDate))}"
        }

    val monthLabel: String
        get() = SimpleDateFormat("MMM", Locale("id")).format(Date(startDate))

    val dayLabel: String
        get() = SimpleDateFormat("dd", Locale("id")).format(Date(startDate))
}

data class MoodRecord(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val mood: String,
    val moodEmoji: String,
    val energyLevel: Int,
    val note: String = ""
)


object CycleRepository {

    private const val PREF_NAME = "ronabulan_prefs"
    private const val KEY_PERIOD_RECORDS = "period_records"
    private const val KEY_MOOD_RECORDS = "mood_records"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // ── Period Records ──

    fun savePeriodRecord(context: Context, record: PeriodRecord) {
        val records = getPeriodRecords(context).toMutableList()
        records.removeAll { it.id == record.id }
        records.add(0, record)
        val arr = JSONArray()
        records.forEach { arr.put(periodToJson(it)) }
        prefs(context).edit().putString(KEY_PERIOD_RECORDS, arr.toString()).apply()
    }

    fun getPeriodRecords(context: Context): List<PeriodRecord> {
        val json = prefs(context).getString(KEY_PERIOD_RECORDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { periodFromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun periodToJson(r: PeriodRecord): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("startDate", r.startDate)
        put("endDate", r.endDate)
        put("flow", r.flow)
        put("symptoms", JSONArray(r.symptoms))
        put("notes", r.notes)
    }

    private fun periodFromJson(o: JSONObject): PeriodRecord {
        val symptomsArr = o.optJSONArray("symptoms") ?: JSONArray()
        val symptoms = (0 until symptomsArr.length()).map { symptomsArr.getString(it) }
        return PeriodRecord(
            id = o.getString("id"),
            startDate = o.getLong("startDate"),
            endDate = o.getLong("endDate"),
            flow = o.optString("flow", "Normal"),
            symptoms = symptoms,
            notes = o.optString("notes", "")
        )
    }

    // ── Mood Records ──

    fun saveMoodRecord(context: Context, record: MoodRecord) {
        val records = getMoodRecords(context).toMutableList()
        records.removeAll { it.id == record.id }
        records.add(0, record)
        val arr = JSONArray()
        records.forEach { arr.put(moodToJson(it)) }
        prefs(context).edit().putString(KEY_MOOD_RECORDS, arr.toString()).apply()
    }

    fun getMoodRecords(context: Context): List<MoodRecord> {
        val json = prefs(context).getString(KEY_MOOD_RECORDS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { moodFromJson(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun moodToJson(r: MoodRecord): JSONObject = JSONObject().apply {
        put("id", r.id)
        put("date", r.date)
        put("mood", r.mood)
        put("moodEmoji", r.moodEmoji)
        put("energyLevel", r.energyLevel)
        put("note", r.note)
    }

    private fun moodFromJson(o: JSONObject): MoodRecord = MoodRecord(
        id = o.getString("id"),
        date = o.getLong("date"),
        mood = o.getString("mood"),
        moodEmoji = o.getString("moodEmoji"),
        energyLevel = o.getInt("energyLevel"),
        note = o.optString("note", "")
    )

    // ── Cycle Calculations ──

    fun getAverageCycleLength(context: Context): Int {
        val records = getPeriodRecords(context)
        if (records.size < 2) return 28
        val cycleLengths = mutableListOf<Int>()
        for (i in 0 until records.size - 1) {
            val diff = records[i].startDate - records[i + 1].startDate
            val days = (diff / (1000 * 60 * 60 * 24)).toInt()
            if (days in 20..45) cycleLengths.add(days)
        }
        return if (cycleLengths.isEmpty()) 28 else cycleLengths.average().toInt()
    }

    fun getAveragePeriodDuration(context: Context): Int {
        val records = getPeriodRecords(context)
        if (records.isEmpty()) return 5
        return records.map { it.durationDays }.average().toInt()
    }

    fun getCurrentCycleDay(context: Context): Int {
        val lastRecord = getPeriodRecords(context).firstOrNull() ?: return 1
        val today = System.currentTimeMillis()
        val diff = today - lastRecord.startDate
        return ((diff / (1000 * 60 * 60 * 24)).toInt() + 1).coerceAtLeast(1)
    }

    fun getNextPeriodDate(context: Context): Date {
        val lastRecord = getPeriodRecords(context).firstOrNull()
        val avgCycle = getAverageCycleLength(context)
        val startMs = lastRecord?.startDate ?: System.currentTimeMillis()
        return Date(startMs + avgCycle * 24L * 60 * 60 * 1000)
    }

    fun getDaysUntilNextPeriod(context: Context): Int {
        val nextPeriod = getNextPeriodDate(context)
        val diff = nextPeriod.time - System.currentTimeMillis()
        return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
    }

    fun getOvulationDate(context: Context): Date {
        val lastRecord = getPeriodRecords(context).firstOrNull()
        val avgCycle = getAverageCycleLength(context)
        val startMs = lastRecord?.startDate ?: System.currentTimeMillis()
        val ovulationDay = avgCycle - 14
        return Date(startMs + ovulationDay * 24L * 60 * 60 * 1000)
    }

    fun getFertileWindowStart(context: Context): Date {
        val ovulation = getOvulationDate(context)
        return Date(ovulation.time - 5 * 24L * 60 * 60 * 1000)
    }

    fun getFertileWindowEnd(context: Context): Date {
        val ovulation = getOvulationDate(context)
        return Date(ovulation.time + 1 * 24L * 60 * 60 * 1000)
    }

    fun getCurrentPhase(context: Context): String {
        val cycleDay = getCurrentCycleDay(context)
        val avgCycle = getAverageCycleLength(context)
        val avgDuration = getAveragePeriodDuration(context)
        return when {
            cycleDay <= avgDuration -> "Menstruasi"
            cycleDay <= 13 -> "Folikuler"
            cycleDay <= 16 -> "Ovulasi"
            else -> "Luteal"
        }
    }

    fun getCurrentPhaseEmoji(context: Context): String {
        return when (getCurrentPhase(context)) {
            "Menstruasi" -> "🩸"
            "Folikuler" -> "🌱"
            "Ovulasi" -> "🌕"
            "Luteal" -> "🌖"
            else -> "🌸"
        }
    }

    fun getPhaseDescription(context: Context): String {
        return when (getCurrentPhase(context)) {
            "Menstruasi" -> "Istirahat yang cukup dan jaga nutrisi tubuhmu 💕"
            "Folikuler" -> "Energimu mulai meningkat — waktu yang baik untuk berolahraga!"
            "Ovulasi" -> "Masa subur aktif — kamu sedang di puncak energimu ✨"
            "Luteal" -> "Jaga mood dan kelola stres dengan baik, ya!"
            else -> "Pantau terus siklus tubuhmu 🌸"
        }
    }

    fun getDailyTip(context: Context): String {
        val tips = listOf(
            "Minum air yang cukup dan istirahat teratur membantu menjaga keseimbangan hormonal.",
            "Olahraga ringan seperti yoga atau jalan kaki bisa membantu mengurangi kram.",
            "Konsumsi makanan kaya zat besi seperti bayam dan daging merah saat menstruasi.",
            "Hindari kafein berlebihan untuk mengurangi gejala PMS.",
            "Pantau perubahan tubuhmu setiap hari untuk mengenali polamu sendiri.",
            "Tidur 7–8 jam per hari membantu regulasi hormon secara alami.",
            "Cokelat hitam mengandung magnesium yang baik untuk mengurangi kram!"
        )
        return tips[(System.currentTimeMillis() / (24 * 60 * 60 * 1000) % tips.size).toInt()]
    }
}


