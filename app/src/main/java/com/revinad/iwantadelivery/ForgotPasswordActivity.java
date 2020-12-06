package com.revinad.iwantadelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout inputEmail;
    Button btnReset;
    FirebaseAuth mAuth;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //init Toolbar
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init Firebase
        mAuth = FirebaseAuth.getInstance();

        inputEmail = findViewById(R.id.inputEmailPasswordReset);
        btnReset = findViewById(R.id.btnReset);

        //retrieving text if screen rotates and putting it back to where it belongs
        if (savedInstanceState != null) {
            inputEmail.getEditText().setText(String.valueOf(savedInstanceState.getString("input_email_reset_key")));
        }

        //create onClick listeners
        btnReset.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //To enable back button on AppBar
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //When rotate save instance to reText fields onCreate
        outState.putString("input_email_reset_key", inputEmail.getEditText().getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnReset:
                String email = inputEmail.getEditText().getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(ForgotPasswordActivity.this, R.string.wrong_email_input, Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Reset E-mail successfully sended
                                Toast.makeText(ForgotPasswordActivity.this, R.string.please_check_your_email, Toast.LENGTH_SHORT).show();
                            } else {
                                //Reset E-mail NOT sended
                                Toast.makeText(ForgotPasswordActivity.this, R.string.reset_email_not_sended, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
        }
    }
}