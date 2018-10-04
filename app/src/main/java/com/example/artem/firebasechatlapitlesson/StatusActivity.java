package com.example.artem.firebasechatlapitlesson;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private TextInputLayout textInputLayout;
    private Button btn_save;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        textInputLayout = (TextInputLayout) findViewById(R.id.cng_sts);
        btn_save = (Button) findViewById(R.id.btn_save_sts);

        getSupportActionBar().setTitle("Account Status");

        progressDialog = new ProgressDialog(this);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = firebaseUser.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("ChatUsers").child(current_uid);

        String status_value = getIntent().getStringExtra("status_value");

        textInputLayout.getEditText().setText(status_value);

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.setTitle("Please wait...");
                progressDialog.show();

                String status = textInputLayout.getEditText().getText().toString();

                databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            Intent saveIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(saveIntent);
                            finish();
                        } else {
                            Toast.makeText(StatusActivity.this, "Error saving changes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

    }
}
