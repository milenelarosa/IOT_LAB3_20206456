package com.example.lab3_20206456;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab3_20206456.Services.ApiService;
import com.example.lab3_20206456.Services.Task;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TaskActivity extends AppCompatActivity {

    private Spinner taskDropdown;
    private Button updateStatusButton;
    private List<Task> taskList;
    private ApiService apiService;
    private ArrayAdapter<String> dropdownAdapter;
    private ImageView logoutButton;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        // Inicializar Retrofit para el PUT request
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/") // Dirección base
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Asignar vistas a las variables
        taskDropdown = findViewById(R.id.taskSpinner);
        updateStatusButton = findViewById(R.id.btnChangeStatus);
        logoutButton = findViewById(R.id.btnLogout);
        backButton = findViewById(R.id.btnBack);

        // Obtener la lista de tareas del Intent
        taskList = (List<Task>) getIntent().getSerializableExtra("tasks");
        Log.d("TASK_LIST", "Received tasks: " + taskList); // Log aquí

        if (taskList == null || taskList.isEmpty()) {
            Log.e("TASK_LIST", "La lista de tareas es nula o vacía");
            Toast.makeText(this, "Error al cargar las tareas", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una lista con el formato "nombre - estado" para el Spinner
        ArrayList<String> taskDescriptions = new ArrayList<>();
        for (Task task : taskList) {
            Log.d("TASK_INFO", "Task title: " + task.getTitle()); // Log para verificar el título
            String status = task.isCompleted() ? "Completado" : "No completado";
            taskDescriptions.add(task.getTitle() + " - " + status);
        }

        // Configurar el adaptador del Spinner
        dropdownAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, taskDescriptions);
        dropdownAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskDropdown.setAdapter(dropdownAdapter);

        // Botón para cambiar el estado de la tarea seleccionada
        updateStatusButton.setOnClickListener(v -> {
            int selectedTaskPosition = taskDropdown.getSelectedItemPosition();
            Task selectedTask = taskList.get(selectedTaskPosition);

            // Alternar el estado de completado
            boolean updatedStatus = !selectedTask.isCompleted();
            selectedTask.setCompleted(updatedStatus);

            // Realizar solicitud PUT para actualizar el estado en la API
            apiService.updateTaskStatus(selectedTask.getId(), selectedTask).enqueue(new Callback<Task>() {
                @Override
                public void onResponse(Call<Task> call, Response<Task> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // Actualizar la lista en el Spinner
                        String newStatus = updatedStatus ? "Completado" : "No completado";
                        taskDescriptions.set(selectedTaskPosition, selectedTask.getTitle() + " - " + newStatus);
                        dropdownAdapter.notifyDataSetChanged();

                        Toast.makeText(TaskActivity.this, "Estado actualizado correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TaskActivity.this, "No se pudo actualizar la tarea", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onFailure(Call<Task> call, Throwable t) {
                    Toast.makeText(TaskActivity.this, "Error", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Acción para el botón de retroceso
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, TimerActivity.class);
            startActivity(intent);
            finish(); // Finaliza la actividad actual
        });

        // Acción para el botón de logout
        logoutButton.setOnClickListener(v -> {
            clearSession(); // Limpiar la sesión del usuario
            // Ir a la pantalla principal
            Intent logoutIntent = new Intent(TaskActivity.this, MainActivity.class);
            logoutIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(logoutIntent);
            finish();
        });
    }

    private void clearSession() {
        SharedPreferences sharedPreferences = getSharedPreferences("userSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}



