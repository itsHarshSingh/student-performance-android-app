package com.example.result_ai;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    // UI Elements declare kar rahe hain
    EditText editStudyHours, editAttendance, editPrevScore;
    Button btnPredict;
    TextView txtResult;

    // Tumhare Python Server ka address
    String API_URL = "https://student-performance-api-9zsd.onrender.com/predict";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // XML se connect kiya

        // Views ko id ke through dhoondhna
        editStudyHours = findViewById(R.id.editStudyHours);
        editAttendance = findViewById(R.id.editAttendance);
        editPrevScore = findViewById(R.id.editPrevScore);
        btnPredict = findViewById(R.id.btnPredict);
        txtResult = findViewById(R.id.txtResult);

        // Jab Button par click ho tab kya karna hai
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // User ke daale hue numbers uthana
                String hoursStr = editStudyHours.getText().toString();
                String attStr = editAttendance.getText().toString();
                String prevStr = editPrevScore.getText().toString();

                // 1. Check karna ki koi dabba khali toh nahi hai
                if (hoursStr.isEmpty() || attStr.isEmpty() || prevStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter all values", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    // String ko numbers me convert karna
                    double hours = Double.parseDouble(hoursStr);
                    double att = Double.parseDouble(attStr);
                    double prev = Double.parseDouble(prevStr);

                    // 2. EXTRA CONDITIONS (Input Validation)
                    if (hours < 0 || hours > 24) {
                        editStudyHours.setError("Study hours must be between 0 and 24");
                        return; // Code yahi ruk jayega
                    }

                    if (att < 0 || att > 100) {
                        editAttendance.setError("Attendance must be between 0 and 100%");
                        return;
                    }

                    if (prev < 0 || prev > 100) {
                        editPrevScore.setError("Previous score must be between 0 and 100");
                        return;
                    }

                    // Agar sab sahi hai, toh Data ko JSON object me pack karna
                    JSONObject studentData = new JSONObject();
                    studentData.put("study_hours", hours);
                    studentData.put("attendance", att);
                    studentData.put("prev_score", prev);

                    // Server ko request bhejna
                    sendDataToServer(studentData);

                } catch (NumberFormatException e) {
                    // Agar user ne text/special characters daal diye numbers ki jagah
                    Toast.makeText(MainActivity.this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void sendDataToServer(JSONObject studentData) {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, studentData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            double predictedMarks = response.getDouble("predicted_marks");
                            // Result ko 2 decimal places tak limit karne ke liye format lagaya
                            String formattedResult = String.format("%.2f", predictedMarks);
                            txtResult.setText("Predicted Final Score: " + formattedResult);
                        } catch (JSONException e) {
                            txtResult.setText("Error reading response");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtResult.setText("Server Error: Check connection or IP");
                error.printStackTrace();
            }
        });

        queue.add(jsonObjectRequest);
    }
}