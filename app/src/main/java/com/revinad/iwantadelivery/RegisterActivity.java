package com.revinad.iwantadelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputLayout inputEmail, inputPassword, inputConfirmPassword;
    Button btnRegister;
    TextView alreadyHaveAccount;

    FirebaseAuth mAuth;
    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        alreadyHaveAccount = findViewById(R.id.alreadyHaveAccount);

        //init FirebaseDatabase
        mAuth = FirebaseAuth.getInstance();

        //create LoadingBar
        mLoadingBar = new ProgressDialog(this);

        //retrieving text if screen rotates and putting it back to where it belongs
        if (savedInstanceState != null) {
            inputEmail.getEditText().setText(String.valueOf(savedInstanceState.getString("input_register_email_key")));
            inputPassword.getEditText().setText(String.valueOf(savedInstanceState.getString("input_register_password_key")));
            inputConfirmPassword.getEditText().setText(String.valueOf(savedInstanceState.getString("input_register_confirm_password_key")));
        }

        //create onClick listeners
        btnRegister.setOnClickListener(this);
        alreadyHaveAccount.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //When rotate save instance to reText fields onCreate
        outState.putString("input_register_email_key", inputEmail.getEditText().getText().toString());
        outState.putString("input_register_password_key", inputPassword.getEditText().getText().toString());
        outState.putString("input_register_confirm_password_key", inputConfirmPassword.getEditText().getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //if Click to
            case R.id.btnRegister:
                AttemptRegistration();
                break;
            case R.id.alreadyHaveAccount:
                //Go to loginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private void AttemptRegistration() {
        String email = Objects.requireNonNull(inputEmail.getEditText()).getText().toString();
        String password = Objects.requireNonNull(inputPassword.getEditText()).getText().toString();
        String confirmPassword = Objects.requireNonNull(inputConfirmPassword.getEditText()).getText().toString();

        if (email.isEmpty()) {
            showError(inputEmail, getString(R.string.email_is_not_valid));
        } else if (password.isEmpty() || password.length() < 8) {
            showError(inputPassword, getString(R.string.password_must_be_greater_than_8));
        } else if (!confirmPassword.equals(password)) {
            showError(inputConfirmPassword, getString(R.string.password_did_not_match));
        } else {
            mLoadingBar.setTitle(getString(R.string.register_label));
            mLoadingBar.setMessage(getString(R.string.please_wait_while_registration));
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Register Successful and go to SetupActivity
                        mLoadingBar.dismiss();
                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_is_successful), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, SetupActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        mLoadingBar.dismiss();
                        //Failed to Register
                        Toast.makeText(RegisterActivity.this, getString(R.string.registration_is_failed), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    private void showError(TextInputLayout field, String text) {
        field.setError(text);
        field.requestFocus();
    }

}