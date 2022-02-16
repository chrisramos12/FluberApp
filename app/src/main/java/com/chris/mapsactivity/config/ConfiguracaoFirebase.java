package com.chris.mapsactivity.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {
    private static DatabaseReference database;
    private static FirebaseAuth auth;

    //retorna a instancia do database
    public static DatabaseReference getFirebaseDatabase(){
        if(database==null){
            database= FirebaseDatabase.getInstance().getReference();

        }
        return database;
    }
    //retorna instancia do firebase auth
    public static FirebaseAuth getFirebaseAutenticacao(){
        if(auth == null){
            auth = FirebaseAuth.getInstance();
        }

        return auth;

    }
}
