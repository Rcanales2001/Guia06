package com.example.loginproject;
import static com.example.loginproject.TODOApplication.firebaseFirestore;
import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
public class TODOApplication extends Application {
    public static FirebaseFirestore firebaseFirestore;
    @Override
    public void onCreate() {
        super.onCreate();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }
}
