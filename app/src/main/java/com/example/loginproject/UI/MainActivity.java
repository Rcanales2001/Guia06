package com.example.loginproject.UI;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.loginproject.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    // Variables para gestionar FirebaseAuth y Google SignIn
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

    Button btnCerraSesion, btnEliminarCuenta;
    private TextView userNombre, userEmail, userID;
    private CircleImageView userImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userNombre = findViewById(R.id.userNombre);
        userEmail = findViewById(R.id.userEmail);
        userID = findViewById(R.id.userId);
        userImg = findViewById(R.id.userImagen);
        btnCerraSesion = findViewById(R.id.btnLogout);
        btnEliminarCuenta = findViewById(R.id.btnEliminarCta);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Mostrar datos del usuario
            userID.setText(currentUser.getUid());
            userNombre.setText(currentUser.getDisplayName());
            userEmail.setText(currentUser.getEmail());
            Glide.with(this).load(currentUser.getPhotoUrl()).into(userImg);
        }

        // Configurar opciones de Google SignIn
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Configurar botón de cerrar sesión
        btnCerraSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        // Configurar botón de eliminar cuenta
        btnEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                    if (signInAccount != null) {
                        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                        if (credential != null) {
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        deleteUser(user);
                                    } else {
                                        Log.e("MainActivity", "Error al re-autenticar al usuario.", task.getException());
                                        Toast.makeText(getApplicationContext(), "Re-autenticación fallida. Intenta cerrar sesión e iniciar de nuevo.", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } else {
                            Log.e("MainActivity", "Error: No se pudo obtener las credenciales.");
                        }
                    } else {
                        Log.d("MainActivity", "Error: cuenta de usuario no encontrada.");
                        Toast.makeText(getApplicationContext(), "No se encontró cuenta de Google vinculada.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d("MainActivity", "Error: usuario no autenticado.");
                    Toast.makeText(getApplicationContext(), "No hay usuario autenticado.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Método para eliminar usuario
    private void deleteUser(final FirebaseUser user) {
        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("MainActivity", "Usuario eliminado exitosamente.");
                    signOut();
                } else {
                    Log.e("MainActivity", "Error al eliminar el usuario.", task.getException());
                    Toast.makeText(getApplicationContext(), "Error al eliminar la cuenta. Inténtalo de nuevo.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Método para cerrar sesión
    private void signOut() {
        mAuth.signOut();
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Intent loginActivity = new Intent(getApplicationContext(), loginActivity.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginActivity);
                MainActivity.this.finish();
            }
        });
    }
}
