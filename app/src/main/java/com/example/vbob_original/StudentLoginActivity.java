package com.example.vbob_original;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class StudentLoginActivity extends AppCompatActivity {

    EditText reg,pass;
    Button login;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_login);

        reg=findViewById(R.id.reg_input_loginpage);
        pass=findViewById(R.id.pass_input_loginpage);
        login=findViewById(R.id.login_button_loginpage);
        auth=FirebaseAuth.getInstance();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reg_number=reg.getText().toString();
                String password=pass.getText().toString();
                loginStudent(reg_number,password);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void loginStudent(String regNum, String password) {

        regNum=regNum+"@student.com";
        auth.signInWithEmailAndPassword(regNum,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    Toast.makeText(StudentLoginActivity.this, "Succesfully login", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(StudentLoginActivity.this, StudentTrackingActivity.class));
                }
                else
                {
                    Toast.makeText(StudentLoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



}