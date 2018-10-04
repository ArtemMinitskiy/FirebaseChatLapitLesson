package com.example.artem.firebasechatlapitlesson;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout logEmail, logPassword;
    private Button btnLogin;
    private ProgressDialog loginProgDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        logEmail = (TextInputLayout) findViewById(R.id.log_email);
        logPassword = (TextInputLayout) findViewById(R.id.log_password);
        btnLogin = (Button) findViewById(R.id.btn_login);

        loginProgDialog = new ProgressDialog(this);

        getSupportActionBar().setTitle("Login Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strLogEmail = logEmail.getEditText().getText().toString();
                String strLogPassword = logPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(strLogEmail) || !TextUtils.isEmpty(strLogPassword)){

                    loginUser(strLogEmail, strLogPassword);
                    loginProgDialog.setTitle("Logging In");
                    loginProgDialog.setMessage("Please wait...");
                    loginProgDialog.show();

                }
            }
        });
    }

    private void loginUser(String strLogEmail, String strLogPassword) {

        firebaseAuth.signInWithEmailAndPassword(strLogEmail, strLogPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    loginProgDialog.dismiss();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }else {
                    loginProgDialog.hide();
                    // If sign in fails, display a message to the user.
                    Log.w("Log", "createUserWithEmail:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
