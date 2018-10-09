package com.example.artem.firebasechatlapitlesson;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout regName, regEmail, regPassword;
    private Button btnCreate;
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private DatabaseReference databaseReference, tokenReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        tokenReference = FirebaseDatabase.getInstance().getReference().child("ChatUsers");
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        regName = (TextInputLayout) findViewById(R.id.reg_name);
        regEmail = (TextInputLayout) findViewById(R.id.reg_email);
        regPassword = (TextInputLayout) findViewById(R.id.reg_password);
        btnCreate = (Button) findViewById(R.id.btn_create_account);

        progressDialog = new ProgressDialog(this);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dsplName = regName.getEditText().getText().toString();
                String dsplEmail = regEmail.getEditText().getText().toString();
                String dsplPassword = regPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(dsplName) || !TextUtils.isEmpty(dsplEmail) || !TextUtils.isEmpty(dsplPassword)){
                    progressDialog.setTitle("Rigister User");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    registerUser(dsplName, dsplEmail, dsplPassword);
                }

            }
        });
    }

    private void registerUser(final String dsplName, String dsplEmail, String dsplPassword) {

        mAuth.createUserWithEmailAndPassword(dsplEmail, dsplPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();
//                            String current_user_id = mAuth.getCurrentUser().getUid();
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("ChatUsers").child(uid);

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", dsplName);
                            userMap.put("status", "Here is Johnny!");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", deviceToken);

                            databaseReference.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                         progressDialog.dismiss();
                                         Log.d("Log", "createUserWithEmail:success");
                                         Toast.makeText(RegisterActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                                         Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                         mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                         startActivity(mainIntent);
                                         finish();
                                    }
                                }
                            });
//
                        } else {
                            progressDialog.hide();
                            // If sign in fails, display a message to the user.
                            Log.w("Log", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
