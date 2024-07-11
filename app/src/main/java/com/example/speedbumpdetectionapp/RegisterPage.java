package com.example.speedbumpdetectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class RegisterPage extends AppCompatActivity {
    EditText userName;
    EditText email;
    EditText password;
    //SignInButton btnGoogleSignIn;
    Button register;
    TextView login;
    ProgressBar progressBar;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    String userEmail = "";
    String userPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        userName = findViewById(R.id.editTextUserName);
        email = findViewById(R.id.editTextEmailAddress);
        password = findViewById(R.id.editTextPassword);
        //btnGoogleSignIn = findViewById(R.id.ButtonGoogleSignIn);
        register = findViewById(R.id.buttonRegister);
        login = findViewById(R.id.goToLogin);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        //------------------------------------------------------------------------------------------
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail = email.getText().toString();
                userPassword = password.getText().toString();
                if(!userEmail.equals("") && !userPassword.equals("")){
                    register.setClickable(false);
                    signUpFirebase(userEmail,userPassword);
                }
                else {
                    Toast.makeText(RegisterPage.this, "Please enter complete information !! ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterPage.this , LoginPage.class);
                startActivity(i);
                finish();
            }
        });
    }

    //-----------------------------------------*** METHODS ***--------------------------------------
    public void signUpFirebase(String userEmail, String userPassword){

        progressBar.setVisibility(View.VISIBLE);
        auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterPage.this, "Registration Successfull", Toast.LENGTH_SHORT).show();
                            //Add new user to real time database
                            addNewUserInDatabase();
                            progressBar.setVisibility(View.INVISIBLE);
                            //After registration user should login first
                            auth.signOut();
                            Intent i = new Intent(RegisterPage.this , LoginPage.class);
                            startActivity(i);
                            finish();
                        }
                        else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(RegisterPage.this, "Try Again Some Time !!!", Toast.LENGTH_SHORT).show();
                            register.setClickable(true);
                            email.setText("");
                            password.setText("");
                        }
                    }
                });
    }

    public void addNewUserInDatabase(){
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference dbReference = database.getReference("User_accounts");
            String userId = user.getUid();
            dbReference.child(userId).child("Total_speed_bump_added").setValue(0).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("DBerror","Not added in Database" );
                }
            });
        } else {
            Toast.makeText(this, "DB error!!", Toast.LENGTH_SHORT).show();
        }
    }
}

//---------------------------------------------- end -----------------------------------------------