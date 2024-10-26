package com.example.loginproject.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.loginproject.R;
import com.example.loginproject.UI.loginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;

public class loginActivity extends AppCompatActivity {

    TextView lblCrearCuenta;
    EditText txtInputEmail, txtInputPassword;
    int RC_SIGN_IN =1;
    Button btnLogin,btnGoogle;
    String TAG = "GoogleSignInLoginActivity";

    //Variable mAuthStateListener para controlar el estado del usuario:
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressDialog mProgressBar;
    //Variable para gestionar FirebaseAuth
    private FirebaseAuth mAuth;
    //Agregar cliente de inicio de sesión de Google
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_login);
        txtInputEmail = findViewById(R.id.inputEmail);
        txtInputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnlogin);
        lblCrearCuenta = findViewById(R.id.txtNoTieneCuenta);
        btnGoogle = findViewById(R.id.btnGoogle);


        lblCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(loginActivity.this,registerActivity.class));
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verificarCredenciales();
            }
        });
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        mProgressBar = new ProgressDialog(loginActivity.this);


        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Crear un GoogleSignInClient con las opciones especificadas por gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Controlar el estado del usuario
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null){ //si no es null redirigir
                    Intent intentDashboard = new Intent(getApplicationContext(), MainActivity.class);
                    intentDashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intentDashboard);
                }
            }
        };

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    // Google Sign In fallido, actualizar GUI
                    Log.w(TAG, "Google sign in failed", e);
                }
            } else {
                Log.d(TAG, "Error, login no exitoso:" + task.getException().toString());
                Toast.makeText(this, "Ocurrio un error. " + task.getException().toString(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Get the signed-in user's email address
                    String email = mAuth.getCurrentUser().getEmail();
                    checkIfEmailExists(email);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                }
            }
        });
    }
    private void checkIfEmailExists(String email) {
        mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if (task.isSuccessful()) {
                    SignInMethodQueryResult result = task.getResult();
                    if (result != null && result.getSignInMethods() != null && result.getSignInMethods().size() > 0) {
                        // Email is registered, proceed with the sign-in
                        Intent dashboardActivity = new Intent(loginActivity.this, MainActivity.class);
                        startActivity(dashboardActivity);
                        loginActivity.this.finish();
                    } else {
                        // Email is not registered, show an error message or handle accordingly
                        Toast.makeText(loginActivity.this, "Este correo no está registrado", Toast.LENGTH_SHORT).show();
                        // Optionally, sign out the user from Google to prevent automatic sign-in
                        signOutFromGoogle();
                    }
                } else {
                    // Error checking for existing email
                    Log.e(TAG, "Error checking if email exists", task.getException());
                }
            }
        });
    }

    private void signOutFromGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Sign out from Google successful");
                } else {
                    Log.e(TAG, "Sign out from Google failed", task.getException());
                }
            }
        });
    }
    public void verificarCredenciales() {
        String email = txtInputEmail.getText().toString();
        String password = txtInputPassword.getText().toString();

        if (email.isEmpty() || !email.contains("@")) {
            showError(txtInputEmail, "Email no válido");
        } else if (password.isEmpty() || password.length() < 7) {
            showError(txtInputPassword, "Contraseña inválida");
        } else {
            // Mostrar ProgressBar
            mProgressBar.setTitle("Login");
            mProgressBar.setMessage("Iniciando sesión, espere un momento..");
            mProgressBar.setCanceledOnTouchOutside(false);
            mProgressBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    mProgressBar.dismiss(); // Ocultar ProgressBar

                    if (task.isSuccessful()) {
                        // Redireccionar - Intent a MainActivity
                        Intent intent = new Intent(loginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else {
                        // Mostrar mensaje de error si no se pudo iniciar sesión
                        Toast.makeText(getApplicationContext(), "No se pudo iniciar Sesión, verifica el correo o contraseña", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                mProgressBar.dismiss(); // Ocultar ProgressBar

                if (task.isSuccessful()) {
                    // Redireccionar - Intent a MainActivity
                    Intent intent = new Intent(loginActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Mostrar mensaje de error si no se pudo iniciar sesión
                    Toast.makeText(getApplicationContext(), "No se pudo iniciar Sesión, verifica el correo o contraseña", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showError(EditText input, String s){
        input.setError(s);
        input.requestFocus();
    }

    @Override
    protected void onStart() {
        mAuth.addAuthStateListener(mAuthStateListener);
        super.onStart();
    }
}