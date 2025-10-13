package com.example.smartlab

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartlab.addResources.MeasurementFilters
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterActivity : AppCompatActivity() {
    lateinit var start: Calendar
    lateinit var end: Calendar
    lateinit var from: TextView
    lateinit var to: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filter)

        from = findViewById<TextView>(R.id.fromInputText)
        to = findViewById<TextView>(R.id.toInputText)

        val minInput = findViewById<EditText>(R.id.minValueInput)
        val maxInput = findViewById<EditText>(R.id.maxValueInput)

        // Отримуємо передані фільтри (якщо вони є)
        val filters = intent.getParcelableExtra<MeasurementFilters>("filters")
        filters?.let {
            if (!it.from.isNullOrEmpty()) {
                from.text = it.from
                start = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.from)!!
                }
            }
            if (!it.to.isNullOrEmpty()) {
                to.text = it.to
                end = Calendar.getInstance().apply {
                    time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(it.to)!!
                }
            }
            minInput.setText(it.minValue?.toString() ?: "")
            maxInput.setText(it.maxValue?.toString() ?: "")
        }


        val applyButton = findViewById<Button>(R.id.applyButton)
        val backButton = findViewById<ImageView>(R.id.backButton)
        backButton.setOnClickListener { finish() }
        findViewById<LinearLayout>(R.id.from).setOnClickListener { pickDateTime(true) }
        findViewById<LinearLayout>(R.id.to).setOnClickListener { pickDateTime(false) }

        applyButton.setOnClickListener {
            val isoSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val fromIso = if (::start.isInitialized) isoSdf.format(start.time) else ""
            val toIso = if (::end.isInitialized) isoSdf.format(end.time) else ""
            val filters = MeasurementFilters(
                from = fromIso,
                to = toIso,
                minValue = findViewById<EditText>(R.id.minValueInput).text.toString()
                    .toDoubleOrNull(),
                maxValue = findViewById<EditText>(R.id.maxValueInput).text.toString()
                    .toDoubleOrNull()
            )
            val intent = Intent().putExtra("filters", filters)
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun pickDateTime(isFrom: Boolean) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(this, { _, y, m, d ->
            calendar.set(y, m, d)

            // Після вибору дати відкриваємо TimePicker
            val timePicker = TimePickerDialog(this, { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)

                val sdfDisplay = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                if (isFrom) {
                    start = calendar
                    from.text = sdfDisplay.format(calendar.time)
                } else {
                    end = calendar
                    to.text = sdfDisplay.format(calendar.time)
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

            // Обмеження часу
            if (isFrom && ::end.isInitialized) timePicker.updateTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
            if (!isFrom && ::start.isInitialized) timePicker.updateTime(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )

            timePicker.show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        // Обмеження дати
        if (isFrom && ::end.isInitialized) datePicker.datePicker.maxDate = end.timeInMillis
        if (!isFrom && ::start.isInitialized) datePicker.datePicker.minDate = start.timeInMillis

        datePicker.setTitle(if (isFrom) "Виберіть початкову дату" else "Виберіть кінцеву дату")
        datePicker.show()
    }
}
