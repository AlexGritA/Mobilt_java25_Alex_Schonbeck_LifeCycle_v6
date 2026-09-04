package com.alex.lifecyclev6

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StepActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var accelerometer: Sensor? = null
    private var usingAccelerometer = false

    private lateinit var tvStepCount: TextView
    private lateinit var tvSensorStatus: TextView

    private var initialStepCount = -1f
    private var stepsFromAccelerometer = 0
    private var lastAccelMagnitude = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step)

        tvStepCount = findViewById(R.id.tvStepCount)
        tvSensorStatus = findViewById(R.id.tvSensorStatus)

        val btnGoToProfile: Button = findViewById(R.id.btnGoToProfile)
        btnGoToProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        val btnLogout: Button = findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            val loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
            loginPrefs.edit().putBoolean("remember_me", false).apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (stepSensor == null) {
            usingAccelerometer = true
            tvSensorStatus.text = "Step Counter not supported. Using Accelerometer instead."
        }
    }

    override fun onResume() {
        super.onResume()
        if (usingAccelerometer) {
            accelerometer?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        } else {
            stepSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialStepCount < 0) {
                initialStepCount = event.values[0]
            }
            val steps = (event.values[0] - initialStepCount).toInt()
            tvStepCount.text = steps.toString()

        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            val magnitude = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()

            if (magnitude - lastAccelMagnitude > 5f) {
                stepsFromAccelerometer++
                tvStepCount.text = stepsFromAccelerometer.toString()
            }
            lastAccelMagnitude = magnitude
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}