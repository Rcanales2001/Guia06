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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    //Variable para gestionar FirebaseAuth
    private FirebaseAuth mAuth;
    Button btnCerraSesion, btnEliminarCuenta;
    private TextView userNombre,userEmail,userID;
    private CircleImageView userImg;

    //Variables opcionales para desloguear de google tambien
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;

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

        //set datos:
        userID.setText(currentUser.getUid());
        userNombre.setText(currentUser.getDisplayName());
        userEmail.setText(currentUser.getEmail());
        //cargar imágen con glide:
        Glide.with(this).load(currentUser.getPhotoUrl()).into(userImg);

        //Configurar las gso para google signIn con el fin de luego desloguear de google
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnCerraSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Cerrar session con Firebase
                mAuth.signOut();
                //Cerrar sesión con google tambien: Google sign out
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //Abrir MainActivity con SigIn button
                        if (task.isSuccessful()) {
                            Intent loginActivity = new Intent(getApplicationContext(), loginActivity.class);
                            startActivity(loginActivity);
                            MainActivity.this.finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "No se pudo cerrar sesión con google",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });

        btnEliminarCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Obtener el usuario actual
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    // Verificar si el usuario está autenticado con Google
                    GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

                    if (signInAccount != null) {
                        AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                        // Re-autenticar el usuario para eliminarlo
                        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Si la re-autenticación fue exitosa, elimina el usuario
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
                                } else {
                                    //ok
                                    Log.e("MainActivity", "Error al re-autenticar al usuario.", task.getException());
                                    Toast.makeText(getApplicationContext(), "Re-autenticación fallida. Intenta cerrar sesión e iniciar de nuevo.", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
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
    private void signOut() {
        //sign out de firebase
        FirebaseAuth.getInstance().signOut();
        //sign out de "google sign in"
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //regresar al login screen o MainActivity
                //Abrir mainActivity para que inicie sesión o sign in otra vez.
                Intent loginActivity = new Intent(getApplicationContext(), loginActivity.class);
                loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginActivity);
                MainActivity.this.finish();
            }
        });
    }



}