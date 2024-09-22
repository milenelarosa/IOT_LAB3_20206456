package com.example.lab3_20206456;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab3_20206456.Services.ApiService;
import com.example.lab3_20206456.Services.Task;
import com.example.lab3_20206456.Services.TaskResponse;
import com.example.lab3_20206456.Services.User;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TimerActivity extends AppCompatActivity {
    private TextView tvUserFullName, tvUserMail, tvTimerDisplay;
    private ImageView ivUserIcon, btnToggleTimer, btnLogout;
    private CountDownTimer workSessionTimer, breakSessionTimer;
    private boolean inWorkMode = true;
    private boolean timerActive = false;
    private long workDurationInMillis = 1500000; // 25 min en ms
    private long breakDurationInMillis = 300000; // 5 min en ms
    private long remainingTimeInMillis = workDurationInMillis;
    private User currentUser;
    private ApiService apiService;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // Configurar Retrofit (solo una vez)
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiService.class);

        // Asignar las vistas
        tvUserFullName = findViewById(R.id.userName);
        tvUserMail = findViewById(R.id.userEmail);
        tvTimerDisplay = findViewById(R.id.timerText);
        ivUserIcon = findViewById(R.id.userIcon);
        btnToggleTimer = findViewById(R.id.startButton);
        btnLogout = findViewById(R.id.btnLogout);


        // Obtener datos del usuario enviados a través de Intent
        Intent intent = getIntent();
        userId = getIntent().getIntExtra("userId", -1);

        // Verificar si userId es válido
        if (userId == -1) {
            // Mostrar mensaje de error y cerrar la actividad
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show();
            finish(); // Cerrar la actividad y regresar a la pantalla anterior
            return; // Salir de onCreate() si el ID no es válido
        }

        String firstName = intent.getStringExtra("username");
        String email = intent.getStringExtra("useremail");
        String gender = intent.getStringExtra("gender");

        // Verificar que los datos no sean nulos antes de asignarlos
        if (firstName != null && email != null) {
            // Mostrar información del usuario
            tvUserFullName.setText(firstName);
            tvUserMail.setText(email);

            // Seleccionar ícono según el género del usuario
            if ("male".equals(gender)) {
                ivUserIcon.setImageResource(R.drawable.male_icon_15);
            } else {
                ivUserIcon.setImageResource(R.drawable.female_icon_15);
            }
        } else {
            // Manejar el caso de datos faltantes, regresar a la pantalla de inicio
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show();
            finish(); // Cerrar la actividad y volver a la anterior
        }

        // Botón para iniciar y alternar el temporizador
        btnToggleTimer.setOnClickListener(v -> {
            if (timerActive) {
                // Detener y reiniciar temporizador si está activo
                stopCurrentTimer();
            } else {
                // Iniciar temporizador si está detenido
                startTimer();
            }
        });

        // Cerrar sesión y volver a MainActivity
        btnLogout.setOnClickListener(v -> {
            clearUserSession();
            Intent logoutIntent = new Intent(TimerActivity.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish(); // Finalizar la actividad actual
        });
    }

    // Método para iniciar temporizador de trabajo o descanso
    private void startTimer() {
        if (inWorkMode) {
            startWorkSession();
        } else {
            startBreakSession();
        }
        btnToggleTimer.setImageResource(R.drawable.restart);
        timerActive = true;
    }

    // Método para detener y resetear el temporizador actual
    private void stopCurrentTimer() {
        if (workSessionTimer != null) {
            workSessionTimer.cancel();
        }
        if (breakSessionTimer != null) {
            breakSessionTimer.cancel();
        }
        remainingTimeInMillis = workDurationInMillis;
        tvTimerDisplay.setText("25:00"); // Reiniciar el texto del temporizador
        btnToggleTimer.setImageResource(R.drawable.play);
        timerActive = false;
    }

    // Iniciar la sesión de trabajo
    private void startWorkSession() {
        workSessionTimer = new CountDownTimer(remainingTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeInMillis = millisUntilFinished;
                updateTimerUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                inWorkMode = false;
                remainingTimeInMillis = breakDurationInMillis;
                showWorkFinishedDialog();
            }
        }.start();
    }

    // Iniciar la sesión de descanso
    private void startBreakSession() {
        breakSessionTimer = new CountDownTimer(remainingTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeInMillis = millisUntilFinished;
                updateTimerUI(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                inWorkMode = true;
                remainingTimeInMillis = workDurationInMillis;
                showBreakFinishedDialog();
            }
        }.start();
    }

    // Actualizar la UI del temporizador con los minutos y segundos restantes
    private void updateTimerUI(long millisUntilFinished) {
        int minutes = (int) (millisUntilFinished / 1000) / 60;
        int seconds = (int) (millisUntilFinished / 1000) % 60;
        tvTimerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }

    // Mostrar diálogo cuando la sesión de trabajo termina
    private void showWorkFinishedDialog() {
        verTasks();
    }

    // Mostrar diálogo cuando la sesión de descanso termina
    private void showBreakFinishedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Descanso terminado")
                .setMessage("Tu descanso ha terminado. Es hora de volver a trabajar.")
                .setPositiveButton("Empezar de nuevo", (dialog, which) -> startWorkSession())
                .setCancelable(false)
                .show();
    }

    // Método que limpia la sesión del usuario actual
    private void clearUserSession() {
        SharedPreferences preferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    private void verTasks() {
        apiService.getUserTasks(userId).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Task> tasks = response.body().getTodos();
                    Log.d("TASK_LIST", "Tasks: " + tasks.toString());
                    if (tasks.isEmpty()) {
                        showNoTasksDialog(); // Mostrar un diálogo si no tiene tareas
                    } else {
                        // Abrir la actividad con las tareas si tiene al menos una tarea
                        Intent intent = new Intent(TimerActivity.this, TaskActivity.class);
                        intent.putExtra("tasks", (ArrayList<Task>) tasks);
                        startBreakSession();
                        startActivity(intent);
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.code() + " - " + response.message());
                    Toast.makeText(TimerActivity.this, "Error al cargar las tareas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error en la llamada a la API", t);
                Toast.makeText(TimerActivity.this, "Error al cargar las tareas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Mostrar un dialog si no tiene tareas
    private void showNoTasksDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Descanso!")
                .setMessage("Inició el tiempo de descanso. No tienes tareas.")
                .setPositiveButton("Entendido", (dialogInterface, which) -> {
                    startBreakSession();
                })
                .setCancelable(false)
                .show();
    }
}

