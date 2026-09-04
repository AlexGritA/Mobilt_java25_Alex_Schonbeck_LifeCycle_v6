package com.alex.lifecyclev6;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "profile_prefs";

    private EditText etName, etHeight, etWeight;
    private Button btnPickDate, btnSave;
    private TextView tvSelectedDate, tvBmiResult;
    private RadioGroup rgGender;
    private CheckBox cbNotifications;
    private Spinner spinnerColor;
    private Switch switchDarkMode;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        etName = findViewById(R.id.etName);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        rgGender = findViewById(R.id.rgGender);
        cbNotifications = findViewById(R.id.cbNotifications);
        spinnerColor = findViewById(R.id.spinnerColor);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        btnSave = findViewById(R.id.btnSave);
        tvBmiResult = findViewById(R.id.tvBmiResult);

        Button btnGoToSteps = findViewById(R.id.btnGoToSteps);
        btnGoToSteps.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, StepActivity.class));
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            SharedPreferences loginPrefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            loginPrefs.edit().putBoolean("remember_me", false).apply();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });

        // Spinner: populate with color options
        String[] colors = {"Red", "Blue", "Green", "Yellow", "Purple"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colors);
        spinnerColor.setAdapter(adapter);

        // DatePicker button
        btnPickDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                tvSelectedDate.setText(selectedDate);
            }, 2000, 0, 1);
            datePickerDialog.show();
        });

        // Load previously saved data
        loadData();

        // Save button: calculate BMI + persist data
        btnSave.setOnClickListener(v -> {
            String heightStr = etHeight.getText().toString();
            String weightStr = etWeight.getText().toString();

            if (!heightStr.matches("\\d{2,3}(\\.\\d+)?") || !weightStr.matches("\\d{2,3}(\\.\\d+)?")) {
                Toast.makeText(this, "Enter valid height/weight (numbers only, e.g. 175 or 68.5)", Toast.LENGTH_LONG).show();
                return;
            }

            float heightCm = Float.parseFloat(heightStr);
            float weightKg = Float.parseFloat(weightStr);
            float heightM = heightCm / 100f;
            float bmi = weightKg / (heightM * heightM);

            tvBmiResult.setText(String.format("BMI: %.1f", bmi));

            saveData(bmi);
            Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveData(float bmi) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", etName.getText().toString());
        editor.putString("height", etHeight.getText().toString());
        editor.putString("weight", etWeight.getText().toString());
        editor.putString("birthdate", selectedDate);
        editor.putBoolean("notifications", cbNotifications.isChecked());
        editor.putBoolean("darkMode", switchDarkMode.isChecked());
        editor.putInt("colorIndex", spinnerColor.getSelectedItemPosition());
        editor.putFloat("bmi", bmi);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etName.setText(prefs.getString("name", ""));
        etHeight.setText(prefs.getString("height", ""));
        etWeight.setText(prefs.getString("weight", ""));
        selectedDate = prefs.getString("birthdate", "");
        if (!selectedDate.isEmpty()) {
            tvSelectedDate.setText(selectedDate);
        }
        cbNotifications.setChecked(prefs.getBoolean("notifications", false));
        switchDarkMode.setChecked(prefs.getBoolean("darkMode", false));
        spinnerColor.setSelection(prefs.getInt("colorIndex", 0));
        float savedBmi = prefs.getFloat("bmi", 0f);
        if (savedBmi > 0) {
            tvBmiResult.setText(String.format("BMI: %.1f", savedBmi));
        }
    }
}