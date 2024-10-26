package com.example.loginproject.UI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.loginproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.SignInMethodQueryResult;

public class registerActivity extends AppCompatActivity {

    TextView tieneCuenta;
    Button btnRegistrar;
    EditText txtInputUsername, txtInputEmail, txtInputPassword, txtInputConfirmPassword;
    private ProgressDialog mProgressBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        txtInputUsername = findViewById(R.id.inputUsername);
        txtInputEmail = findViewById(R.id.inputEmail);
        txtInputPassword = findViewById(R.id.inputPassword);
        txtInputConfirmPassword = findViewById(R.id.inputConfirmPassword);

        btnRegistrar = findViewById(R.id.btnRegister);
        tieneCuenta =findViewById(R.id.alreadyHaveAccount);

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarCredenciales();
            }
        });

        tieneCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(registerActivity.this,loginActivity.class));
            }
        });
        mAuth = FirebaseAuth.getInstance();
        mProgressBar = new ProgressDialog(registerActivity.this);

    }

    public void verificarCredenciales(){
        String username = txtInputUsername.getText().toString();
        String email = txtInputEmail.getText().toString();
        String password = txtInputPassword.getText().toString();
        String confirmPass = txtInputConfirmPassword.getText().toString();
        if(username.isEmpty() || username.length() < 5){
            showError(txtInputUsername,"Username no valido");
        }else if (email.isEmpty() || !email.contains("@")){
            showError(txtInputEmail,"Email no valido");
        }else if (password.isEmpty() || password.length() < 7){
            showError(txtInputPassword,"Clave no valida minimo 7 caracteres");
        }else if (confirmPass.isEmpty() || !confirmPass.equals(password)){
            showError(txtInputConfirmPassword,"Clave no valida, no coincide.");
        }else{

            //Registrar usuario
            // Check if the email is already registered
            mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                @Override
                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                    if (task.isSuccessful()) {
                        SignInMethodQueryResult result = task.getResult();
                        if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                            // Email is already registered
                            showError(txtInputEmail, "Este correo ya está registrado");
                        } else {
                            // Email is not registered, proceed with user registration
                            registerUser(email, password);
                        }
                    } else {
                        // Error checking for existing email
                        Toast.makeText(getApplicationContext(), "Error al verificar el correo", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    private void registerUser(String email, String password) {
        //Mostrar ProgressBar
        mProgressBar.setTitle("Proceso de Registro");
        mProgressBar.setMessage("Registrando usuario, espere un momento");
        mProgressBar.setCanceledOnTouchOutside(false);
        mProgressBar.show();

        // Registrar usuario
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mProgressBar.dismiss(); // Dismiss the progress bar

                if (task.isSuccessful()) {
                    // Redireccionar - intent a login
                    Intent intent = new Intent(registerActivity.this, loginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "No se pudo registrar, el correo ya existe", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }
}
