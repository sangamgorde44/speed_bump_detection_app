package com.example.speedbumpdetectionapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {
    EditText Mail;
    EditText Password;
    Button Login;
    SignInButton LoginWithGoogle;
    TextView Register;
    TextView ForgotPassword;
    ProgressBar progressBar;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    String userName = "";
    String userPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        Mail = findViewById(R.id.editTextEmailAddressLogin);
        Password = findViewById(R.id.editTextPasswordLogin);
        Login = findViewById(R.id.buttonLogin);
        LoginWithGoogle = findViewById(R.id.ButtonGoogleLogin);
        Register = findViewById(R.id.goToRegister);
        ForgotPassword = findViewById(R.id.textViewForgotPassLogin);
        progressBar = findViewById(R.id.progressBarLogin);
        progressBar.setVisibility(View.INVISIBLE);

        //------------------------------------------------------------------------------------------
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userName = Mail.getText().toString();
                userPassword = Password.getText().toString();

                if(!userName.equals("") && !userPassword.equals("")){
                    Login.setClickable(false);
                    progressBar.setVisibility(View.VISIBLE);
                    signInFirebase(userName,userPassword);
                }
                else{
                    Toast.makeText(LoginPage.this, "Please enter complete information !! ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //------------------------------------------------------------------------------------------
        Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginPage.this , RegisterPage.class);
                startActivity(i);
                finish();
            }
        });
        //------------------------------------------------------------------------------------------
        ForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginPage.this , ForgotPassPage.class);
                startActivity(i);
            }
        });
    }

    //----------------------------------------- ** METHODS ** ------------------------------------------
    public void signInFirebase(String userName, String userPassword){

        auth.signInWithEmailAndPassword(userName,userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginPage.this, "Login Successfull", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent i = new Intent(LoginPage.this , MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                        else{
                            Toast.makeText(LoginPage.this, "Either you don't have an account or Check your internet", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.INVISIBLE);
                            Login.setClickable(true);
                            Mail.setText("");
                            Password.setText("");
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if(user != null){
            Intent directHome = new Intent(LoginPage.this, MainActivity.class);
            startActivity(directHome);
            finish();
        }
    }
}

//---------------------------------------------- end -----------------------------------------------