package com.codex.blockmesolver

import android.app.Activity
import android.os.Bundle
import android.provider.Settings
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 40, 32, 32)
        }
        val title = TextView(this).apply {
            text = "Gemgala BlockMe Solver"
            textSize = 24f
        }
        val info = TextView(this).apply {
            text = "\n1) Enable BlockMe Solver under Android Accessibility.\n" +
                   "2) Open Gemgala → BlockMe.\n" +
                   "3) Tap the floating/notification action from the service to scan.\n" +
                   "4) Solver calculates legal placements; Auto Play can drag pieces.\n\n" +
                   "This is a prototype for the board layout shown in your screenshot."
            textSize = 16f
        }
        val settings = Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        layout.addView(title)
        layout.addView(info)
        layout.addView(settings)
        setContentView(layout)
    }
}
