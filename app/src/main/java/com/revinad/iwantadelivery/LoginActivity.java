package com.revinad.iwantadelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private TextInputLayout inputEmail, inputPassword;
    Button btnLogin;
    TextView forgotPassword, createNewAccount;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef;
    ProgressDialog mLoadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        btnLogin = findViewById(R.id.btnLogin);
        forgotPassword = findViewById(R.id.forgotPassword);
        createNewAccount = findViewById(R.id.createNewAccount);

        //Retrieving text if screen rotates and putting it back to where it belongs
        if (savedInstanceState != null) {
            inputEmail.getEditText().setText(String.valueOf(savedInstanceState.getString("input_login_email_key")));
            inputPassword.getEditText().setText(String.valueOf(savedInstanceState.getString("input_login_password_key")));
        }

        //init FirebaseDatabase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users));

        //create LoadingBar
        mLoadingBar = new ProgressDialog(this);

        //create onClick listeners
        createNewAccount.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //When rotate save instance to reText fields onCreate
        outState.putString("input_login_email_key", inputEmail.getEditText().getText().toString());
        outState.putString("input_login_password_key", inputPassword.getEditText().getText().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.createNewAccount:
                //if click on createNewAccount go to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btnLogin:
                //attempt login
                attemptLogin();
                break;
            case R.id.forgotPassword:
                //if click on forgotPassword go to ForgotPasswordActivity
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
                break;
        }
    }

    private void attemptLogin() {
        String email = Objects.requireNonNull(inputEmail.getEditText()).getText().toString();
        String password = Objects.requireNonNull(inputPassword.getEditText()).getText().toString();

        if (email.isEmpty()) {
            showError(inputEmail, getString(R.string.email_is_not_valid));
        } else if (password.isEmpty() || password.length() < 8) {
            showError(inputPassword, getString(R.string.password_must_be_greater_than_8));
        } else {
            mLoadingBar.setTitle(getString(R.string.login_label));
            mLoadingBar.setMessage(getString(R.string.please_wait_while_login));
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        mLoadingBar.dismiss();
                        Toast.makeText(LoginActivity.this, R.string.connection_is_successful, Toast.LENGTH_SHORT).show();

                        //check if in database User is already setup
                        mUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() == null) {
                                    //if user is NOT already setup go to SetupActivity
                                    Intent intent = new Intent(LoginActivity.this, SetupActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    //if user is already setup go to MainActivity
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.d(TAG, "onCancelled: " + error.toString());
                            }
                        });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onFailure: "+e.getMessage());
                    mLoadingBar.dismiss();
                }
            });
        }

    }

    private void showError(TextInputLayout field, String text) {
        field.setError(text);
        field.requestFocus();
    }

}