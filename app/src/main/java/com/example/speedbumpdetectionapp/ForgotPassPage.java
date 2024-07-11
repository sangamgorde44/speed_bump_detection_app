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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassPage extends AppCompatActivity {
    EditText emailId;
    Button forgotPass;
    TextView mailLink;
    ProgressBar progressBar;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String userEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass_page);

        emailId = findViewById(R.id.editTextEmailAddressPassReset);
        forgotPass = findViewById(R.id.buttonResetPassword);
        mailLink = findViewById(R.id.checkMailText);
        progressBar = findViewById(R.id.progressBarForgotPass);

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userEmail = emailId.getText().toString();

                if(!userEmail.equals("")){
                    forgotPass.setClickable(false);
                    progressBar.setVisibility(View.VISIBLE);
                    resetPassword(userEmail);
                }
                else{
                    Toast.makeText(ForgotPassPage.this, "Please enter your Email..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void resetPassword(String userEmail){
        auth.sendPasswordResetEmail(userEmail)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mailLink.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.INVISIBLE);
                            finish();
                        }
                        else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(ForgotPassPage.this, "Someting went wrong !! ", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }
}

//---------------------------------------------- end -----------------------------------------------