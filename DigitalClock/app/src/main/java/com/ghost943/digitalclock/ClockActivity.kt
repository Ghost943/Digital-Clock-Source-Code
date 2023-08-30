package com.ghost943.digitalclock

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class ClockActivity : AppCompatActivity() {

    private lateinit var clockTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateClock()
            handler.postDelayed(this, 1000)
        }
    }

    private val COLOR_PREFERENCE_KEY = "color_preference"
    private val sharedPrefs: SharedPreferences by lazy {
        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_clock)

        // Prevent the screen from going to sleep
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        clockTextView = findViewById(R.id.clockTextView)

        // Load saved color values
        val savedTextColor = sharedPrefs.getInt(COLOR_PREFERENCE_KEY + "_text", Color.BLACK)
        val savedBackgroundColor =
            sharedPrefs.getInt(COLOR_PREFERENCE_KEY + "_background", Color.WHITE)

        clockTextView.setTextColor(savedTextColor)
        findViewById<LinearLayout>(R.id.mainLayout).setBackgroundColor(savedBackgroundColor)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateTimeRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateTimeRunnable)
    }

    private fun updateClock() {
        val currentTime = Calendar.getInstance().time
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val formattedTime = timeFormat.format(currentTime)
        clockTextView.text = formattedTime
    }

    fun changeTextColor(view: View) {
        showColorInputDialog { inputColor ->
            clockTextView.setTextColor(inputColor)
            // Save to SharedPreferences when color changes
            sharedPrefs.edit().putInt(COLOR_PREFERENCE_KEY + "_text", inputColor).apply()
        }
    }

    fun changeBackgroundColor(view: View) {
        showColorInputDialog { inputColor ->
            findViewById<LinearLayout>(R.id.mainLayout).setBackgroundColor(inputColor)
            // Save to SharedPreferences when color changes
            sharedPrefs.edit().putInt(COLOR_PREFERENCE_KEY + "_background", inputColor).apply()
        }
    }

    private fun showColorInputDialog(onColorSelected: (color: Int) -> Unit) {
        val colorInputEditText = EditText(this)
        colorInputEditText.hint =
            "Enter Color Code (#RRGGBB)ㅤㅤㅤㅤFor Example: White: #FFFFFFㅤㅤㅤBlack: #000000"
            // These spaces might look weird but this is how I got the hint section to look right
            // These spaces are actually a Unicode Character (U+3164). If I had placed individual spaces, I would have needed to add 10 spaces on both sides. However, by using U+3164, I achieved a neat appearance with 4 spaces on one side and 3 spaces on the other.

        val dialog = AlertDialog.Builder(this)
            .setTitle("Select Color")
            .setView(colorInputEditText)
            .setPositiveButton("Ok") { _, _ ->
                val inputColorString = colorInputEditText.text.toString().trim()

                if (inputColorString.isNotEmpty()) {
                    try {
                        val inputColor = Color.parseColor(inputColorString)
                        onColorSelected(inputColor)
                    } catch (e: IllegalArgumentException) {
                        showErrorDialog("You entered an invalid color code.")
                    }
                } else {
                    showErrorDialog("You need to enter color code.")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    private fun showErrorDialog(message: String) {
        val errorDialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("Ok", null)
            .create()
        errorDialog.show()
    }
}
