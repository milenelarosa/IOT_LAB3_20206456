package com.example.lab3_20206456;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.lab3_20206456.Services.ApiService;
import com.example.lab3_20206456.Services.LoginRequest;
import com.example.lab3_20206456.Services.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button btnLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);

        // Inicializar los elementos de la interfaz
        usernameInput = findViewById(R.id.email);
        passwordInput = findViewById(R.id.password);
        btnLogin = findViewById(R.id.loginButton);

        // Asignar listener al botón de login
        btnLogin.setOnClickListener(view -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                authenticateUser(username, password);
            } else {
                Toast.makeText(MainActivity.this, "Campos vacíos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void authenticateUser(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);

        // Llamada al servicio API
        apiService.loginUser(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User loggedInUser = response.body();

                    // Verificar que los datos del usuario no sean nulos
                    if (loggedInUser.getFirstName() != null && loggedInUser.getEmail() != null) {
                        Toast.makeText(MainActivity.this, "Acceso concedido", Toast.LENGTH_SHORT).show();
                        launchTimerActivity(loggedInUser);
                    } else {
                        Toast.makeText(MainActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void launchTimerActivity(User user) {
        Intent intent = new Intent(MainActivity.this, TimerActivity.class);
        // Asegúrate de que los datos no son nulos al pasarlos
        intent.putExtra("userId", user.getId());
        intent.putExtra("username", user.getFirstName() + " " + user.getLastName());
        intent.putExtra("useremail", user.getEmail());
        intent.putExtra("gender", user.getGender());
        startActivity(intent);
        finish();
    }
}

