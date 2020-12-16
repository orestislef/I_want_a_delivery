package com.revinad.iwantadelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    CircleImageView profileImageView;
    private TextInputLayout inputUsername, inputStreet, inputArea;
    Button btnUpdate, getLocationBtn;

    DatabaseReference mRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference storageRef;
    Uri imageUri;
    String profileImageUrlV, usernameV, streetV, areaV, professionV;

    ProgressDialog mLoadingBar;
    Toolbar toolbar;

    private static final int REQUEST_CODE = 101;
    public static final String TAG = "ProfileActivity";
    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //init AppBarToolBar
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.setup_profile_title));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImageView = findViewById(R.id.circleImageView);
        inputUsername = findViewById(R.id.inputUsername);
        inputStreet = findViewById(R.id.inputStreet);
        inputArea = findViewById(R.id.inputArea);
        btnUpdate = findViewById(R.id.btnUpdate);
        getLocationBtn = findViewById(R.id.getLocationBtn);


        //init loadingBar
        mLoadingBar = new ProgressDialog(this);

        //init Firebase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users));
        storageRef = FirebaseStorage.getInstance().getReference().child(getString(R.string.ref_profileImage));

        //init fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        //Fetch data for current user and putting it in the right fields
        mRef.child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "onDataChange: " + snapshot.getValue());
                    //fetching data for user
                    String profileImageUrl = snapshot.child(getString(R.string.ref_users_profileImage)).getValue().toString();
                    String username = snapshot.child(getString(R.string.ref_users_username)).getValue().toString();
                    String street = snapshot.child(getString(R.string.ref_users_street)).getValue().toString();
                    String area = snapshot.child(getString(R.string.ref_users_area)).getValue().toString();

                    //Putting it into fields
                    Picasso.get().load(profileImageUrl).placeholder(R.drawable.profile_image).resize(250, 250).centerInside().into(profileImageView);
                    inputStreet.getEditText().setText(street);
                    inputArea.getEditText().setText(area);
                    inputUsername.getEditText().setText(username);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: fetch data of current user " + error.toString());
                Toast.makeText(ProfileActivity.this, "error: " + error.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //click on Button save
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateData();
            }
        });
    }

    private void getCurrentLocation() {
        //check permission for location
        if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            //When permission granted
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    //init location
                    Location location = task.getResult();
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(ProfileActivity.this, Locale.getDefault());
                        //init address list
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            //set locality to inputStreet
                            Objects.requireNonNull(inputStreet.getEditText()).setText(addresses.get(0).getAddressLine(0));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

        } else {
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

    }

    private void updateData() {
        //taking data from EditTexts into local variables
        String username = Objects.requireNonNull(inputUsername.getEditText()).getText().toString();
        String street = Objects.requireNonNull(inputStreet.getEditText()).getText().toString();
        String area = Objects.requireNonNull(inputArea.getEditText()).getText().toString();

        //Check if fields is empty or NOT acceptable
        if (username.isEmpty() || username.length() < 3) {
            showError(inputUsername, getString(R.string.username_is_not_valid));
        } else if (street.isEmpty() || street.length() < 3) {
            showError(inputUsername, getString(R.string.street_is_not_valid));
        } else if (area.isEmpty() || area.length() < 3) {
            showError(inputUsername, getString(R.string.area_is_not_valid));
        } else {
            mLoadingBar.setTitle(getString(R.string.adding_setup_profile));
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put(getString(R.string.ref_users_username), username);
            hashMap.put(getString(R.string.ref_users_street), street);
            hashMap.put(getString(R.string.ref_users_area), area);

            //Updating data of current User
            mRef.child(mUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    //data successfully updated
                    Log.d(TAG, "onSuccess: Data updated for User: " + mAuth.getUid());
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    mLoadingBar.dismiss();
                    Toast.makeText(ProfileActivity.this, getString(R.string.setup_complete_msg), Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //data did NOT updated
                    Log.d(TAG, "onFailure: Data did NOT updated for User: " + mAuth.getUid());
                    mLoadingBar.dismiss();
                    Toast.makeText(ProfileActivity.this, "error: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void showError(TextInputLayout input, String s) {
        input.setError(s);
        input.requestFocus();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}