package com.revinad.iwantadelivery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ekalips.fancybuttonproj.FancyButton;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.revinad.iwantadelivery.ApiFcmNotification.ApiClient;
import com.revinad.iwantadelivery.ApiFcmNotification.ApiInterface;
import com.revinad.iwantadelivery.ApiFcmNotification.RequestNotification;
import com.revinad.iwantadelivery.ApiFcmNotification.SendNotificationModel;
import com.revinad.iwantadelivery.Utills.Posts;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mDatabaseRef, mUserRef, mPostRef, mTokenRef;
    String profileImageUrlV, usernameV, streetV, areaV, professionV, tokenV;
    boolean statusV;
    CircleImageView profileImageViewHeader;
    TextView usernameHeader;
    FancyButton addPostBtn;
    ProgressDialog mLoadingBar;
    FirebaseRecyclerAdapter<Posts, MyViewHolder> adapter;
    FirebaseRecyclerOptions<Posts> options;
    RecyclerView recyclerView;
    int onlineDeliveryCount;

    ImageButton onlineCountBtn;
    TextView onlineCountTv;

    private final String TAG = "MainActivity";

    ArrayList<String> tokenDeliveryBoyList, tokenShopList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init AppToolbar
        toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24);

        //init FirebaseDatabase
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_users));
        mPostRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_posts));
        mTokenRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.ref_tokens));

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navView);
        addPostBtn = findViewById(R.id.addPost);
        recyclerView = findViewById(R.id.recyclerView);

        //init header in NavigationView
        View view = navigationView.inflateHeaderView(R.layout.drawer_header);
        profileImageViewHeader = view.findViewById(R.id.profileImageHeader);
        usernameHeader = view.findViewById(R.id.username_header);
        onlineCountBtn = view.findViewById(R.id.online_count_btn);
        onlineCountTv = view.findViewById(R.id.online_count_tv);
        navigationView.setNavigationItemSelectedListener(this);

        //init loadingBar
        mLoadingBar = new ProgressDialog(this);

        //click listener for add post button
        addPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: addPostBtn PRESSED");
                //TODO: add alert dialog for when you want the delivery to come

                if (v instanceof FancyButton) {
                    if (((FancyButton) v).isExpanded())
                        ((FancyButton) v).collapse();
                    else
                        ((FancyButton) v).expand();
                }
                //TODO: ask in how much time delivery boy to arrive (maybe with a custom alertDialog)
                if (onlineDeliveryCount > 0) {
                    addPost(v);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(getString(R.string.there_is_no_online_delivery));
                    builder.setPositiveButton(getString(R.string.dialog_no_online_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addPost(v);
                        }
                    }).setNegativeButton(getString(R.string.dialog_no_online_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((FancyButton) v).expand();
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            }
        });

        //init TokenArrayLists
        tokenDeliveryBoyList = new ArrayList<>();
        tokenShopList = new ArrayList<>();

        //init LineaLayoutManager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, true);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //Listener for new post to scroll to top recyclerView.smoothScrollToPosition((int) snapshot.getChildrenCount());
        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recyclerView.smoothScrollToPosition((int) snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //get online Users keys
        mUserRef.orderByChild(getString(R.string.ref_users_status)).equalTo(true).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Log.d(TAG, "onDataChange: online Users: " + snapshot.getChildrenCount() + "\n");
                    Log.d(TAG, "onDataChange: online Username : " + snapshot);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Log.d(TAG, "onDataChange: online key: " + dataSnapshot.getKey());

                        //save the online delivery boy token
                        mTokenRef.child(getString(R.string.profession_delivery_boy)).child(Objects.requireNonNull(dataSnapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    Log.d(TAG, "onDataChange: online save the token: " + snapshot.getValue());
                                    tokenDeliveryBoyList.add((String) snapshot.getValue());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(MainActivity.this, "there is no online deliverades", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addPost(View v) {
        //TODO: add check if no delivery boys online

        //Create Description
        String postDesc = getString(R.string.ask_for_delivery_boy) + "\n"
                + getString(R.string.ask_for_delivery_boy_at_street) + " " + streetV + "\n"
                + getString(R.string.ask_for_delivery_boy_at_area) + " " + areaV;

        //Create Date
        Date date = new Date();
        //SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = formatter.format(date);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(getString(R.string.ref_posts_postdate), strDate);
        hashMap.put(getString(R.string.ref_posts_user_profile_image_url), profileImageUrlV);
        hashMap.put(getString(R.string.ref_posts_post_desc), postDesc);
        hashMap.put(getString(R.string.ref_posts_username), usernameV);
        hashMap.put(getString(R.string.ref_posts_id_of_user), mAuth.getUid());
        hashMap.put(getString(R.string.ref_posts_on_my_way), false);
        hashMap.put(getString(R.string.ref_posts_completed), false);
        hashMap.put(getString(R.string.ref_post_username_of_on_my_way), "");
        hashMap.put(getString(R.string.ref_post_completed_date), "");
        hashMap.put(getString(R.string.ref_post_street), streetV);

        mPostRef.child(strDate + " " + mUser.getUid()).updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            //Post Posted
                            ((FancyButton) v).expand();
                            Toast.makeText(MainActivity.this, getString(R.string.post_added_successful), Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onComplete: tokenList.size=" + tokenDeliveryBoyList.size());
                            for (int i = 0; i < tokenDeliveryBoyList.size(); i++) {
                                Log.d(TAG, "onComplete: token" + i + ": " + tokenDeliveryBoyList.get(i));
                                sendNotificationToToken(tokenDeliveryBoyList.get(i), postDesc, usernameV);
                                Log.d(TAG, "onDataChange: post scroll to Top");
                            }
                        } else {
                            ((FancyButton) v).expand();
                            //Post did NOT posted
                            Log.d(TAG, "onComplete: post did NOT posted");
                            Toast.makeText(MainActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadPost() {

        if (professionV.equals(getString(R.string.profession_shop))) {
            options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(mPostRef.orderByChild(getString(R.string.ref_posts_id_of_user)).equalTo(mAuth.getUid()), Posts.class).build();

        } else if (professionV.equals(getString(R.string.profession_delivery_boy))) {
            options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(mPostRef, Posts.class).build();

        }
        adapter = new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Posts model) {
                String postKey;
                postKey = getRef(position).getKey();
                Log.d(TAG, "onBindViewHolder: PostKey = " + postKey);
                holder.description.setText(model.getPostDesc());
                String timeAgo = calculateTimeAgo(model.getPostDate());
                holder.timeAgo.setText(timeAgo);
                holder.username.setText(model.getUsername());
                Picasso.get().load(model.getUserProfileImageUrl()).placeholder(R.drawable.profile_image).resize(200, 200).centerInside().into(holder.profileImage);

                //holder for the check Buttons
                //sets isChecked state from local completeCB, onMyWayCB to firebaseDatabase
                holder.initCB(postKey, mPostRef, getApplicationContext());
                //sets check from value of firebaseDatabase
                holder.completeCB.setChecked(model.getCompleted());
                holder.onMyWayCB.setChecked(model.getOnMyWay());

                //mapButton go to Intent google maps onClick
                holder.mapBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.co.in/maps?q=" + model.getStreet().replace(" ", "+")));
                        startActivity(intent);
                    }
                });

                //if shop can't click onMyWay
                if (professionV.equals(getString(R.string.profession_shop))) {
                    holder.onMyWayCB.setClickable(false);
                }

                //show sendNotificationToShopBtn if onMyWay is Checked
                if (holder.onMyWayCB.isChecked()) {
                    if (professionV.equals(getString(R.string.profession_delivery_boy))) {
                        holder.sendNotificationToShopBtn.setVisibility(View.VISIBLE);
                    }
                } else holder.sendNotificationToShopBtn.setVisibility(View.INVISIBLE);

                //show deleteBtn if completeCB is checked
                if (holder.completeCB.isChecked()) {
                    holder.deletePostBtn.setVisibility(View.VISIBLE);
                    holder.sendNotificationToShopBtn.setVisibility(View.INVISIBLE);
                } else {
                    holder.deletePostBtn.setVisibility(View.INVISIBLE);
                }

                holder.deletePostBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //alert dialog for confirm delete
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.are_you_sure_to_delete_post_label));
                        builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //delete the post
                                mPostRef.child(postKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "onComplete: post: " + postKey + " deleted");
                                    }
                                });
                            }
                        }).setNegativeButton(getString(R.string.dialog_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Log.d(TAG, "onClick: dialogDelete: Canceled");
                            }
                        });
                        builder.show();
                    }
                });

                holder.sendNotificationToShopBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //alert dialog for sendNotification to Token shop
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(getString(R.string.send_notification_label) + " " + model.getUsername());

                        //adding view text input in alertDialog
                        final EditText comment = new EditText(getApplicationContext());
                        comment.setHint(getString(R.string.send_notification_comment_text_hint));
                        LinearLayout layout = new LinearLayout(getApplicationContext());
                        layout.setOrientation(LinearLayout.VERTICAL);
                        layout.setPadding(48, 24, 32, 24);
                        comment.setText(getString(R.string.notification_send_to_shop));
                        comment.setTextColor(getResources().getColor(R.color.app_bar_color));
                        layout.addView(comment);
                        builder.setView(layout);

                        builder.setPositiveButton(getString(R.string.send_notification_yes_label), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //sendNotificationToToken
                                mTokenRef.child(getString(R.string.profession_shop)).child(model.getIdOfUser()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            Log.d(TAG, "onDataChange: sendNotificationTo: " + snapshot.getValue());
                                            sendNotificationToToken(Objects.requireNonNull(snapshot.getValue()).toString(), comment.getText().toString(), usernameV);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.d(TAG, "onCancelled: " + error.toString());
                                    }
                                });

                            }
                        }).setNegativeButton(getString(R.string.send_notification_cancel_label), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                Log.d(TAG, "onClick: dialogSendNotificationToShop: Canceled");
                            }
                        });
                        builder.show();
                    }
                });

            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post, parent, false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "onDataChange: adapterStartsListening");
    }

    private void sendNotificationToToken(String token, String content, String usernameV) {

        SendNotificationModel sendNotificationModel = new SendNotificationModel(content, usernameV);
        RequestNotification requestNotification = new RequestNotification();
        requestNotification.setSendNotificationModel(sendNotificationModel);
        //token is the key to send notification ,
        requestNotification.setToken(token);

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        retrofit2.Call<ResponseBody> responseBodyCall = apiService.sendChatNotification(requestNotification);

        responseBodyCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                Log.d(TAG, "sendNotificationToTokenCall: " + response.toString());
                Toast.makeText(MainActivity.this, getString(R.string.notification_sended_to_token), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "onFailure: sendNotificationToTokenCall" + t.toString());
                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String calculateTimeAgo(String postDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long time = sdf.parse(postDate).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.SECOND_IN_MILLIS);
            return ago + "";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        MenuItem item = menu.findItem(R.id.app_bar_switch);
        item.setActionView(R.layout.switch_item);
        Switch mStatusSwitch = item.getActionView().findViewById(R.id.status_switch_item);

        //update firebase with user status online/offline -> true/false
        mStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(getString(R.string.ref_users_status), isChecked);
                mUserRef.child(mAuth.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isComplete()) {
                            if (mStatusSwitch.isChecked())
                                mStatusSwitch.setText(getString(R.string.status_online));
                            else
                                mStatusSwitch.setText(getString(R.string.status_offline));
                        }
                    }
                });
            }
        });
        //if data changed toggle switch on or off
        mUserRef.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mStatusSwitch.setChecked((Boolean) snapshot.child(getString(R.string.ref_users_status)).getValue());
                    if (professionV.equals(getString(R.string.profession_shop))) {
                        mStatusSwitch.setChecked(false);
                        mStatusSwitch.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "onCancelled: " + error.getMessage());
            }
        });
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                //just closes the Navigation Drawer
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case R.id.profile:
                //starting ProfileActivity to edit users profile
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            case R.id.logout:
                //log-out
                HashMap hashMap = new HashMap();
                hashMap.put(getString(R.string.ref_users_status), false);
                mUserRef.child(mAuth.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        if (professionV.equals(getString(R.string.profession_delivery_boy)))
                            Toast.makeText(MainActivity.this, getString(R.string.the_user) + " " + usernameV + " " + getString(R.string.is_currently) + " " + getString(R.string.status_offline), Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUser == null) {
            //Send user to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            //fetching local variables profileImageUrlV, usernameV, streetV, areaV, professionV, tokenV, statusV
            mUserRef.child(Objects.requireNonNull(mAuth.getUid())).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        profileImageUrlV = Objects.requireNonNull(snapshot.child(getString(R.string.ref_users_profileImage)).getValue()).toString();
                        usernameV = Objects.requireNonNull(snapshot.child(getString(R.string.ref_users_username)).getValue()).toString();
                        streetV = Objects.requireNonNull(snapshot.child(getString(R.string.ref_users_street)).getValue()).toString();
                        areaV = Objects.requireNonNull(snapshot.child(getString(R.string.ref_users_area)).getValue()).toString();
                        professionV = Objects.requireNonNull(snapshot.child(getString(R.string.ref_users_profession)).getValue()).toString();
                        statusV = (boolean) snapshot.child(getString(R.string.ref_users_status)).getValue();

                        //header init Name and Picture
                        usernameHeader.setText(usernameV);
                        Picasso.get().load(profileImageUrlV).placeholder(R.drawable.profile_image).resize(250, 250).centerInside().into(profileImageViewHeader);

                        //hide newPostBtn from profession=delivery_boy
                        if (professionV.equals(getString(R.string.profession_delivery_boy))) {
                            addPostBtn.setVisibility(View.GONE);
                        }

                        //Get Tokens
                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                if (task.isSuccessful()) {
                                    tokenV = task.getResult();
                                    Log.d(TAG, "onComplete: Token of User: " + mAuth.getUid() + " : " + tokenV);

                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put(mAuth.getUid(), tokenV);

                                    //save token to database
                                    mTokenRef.child(professionV).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "onComplete: Token saved");
                                            }
                                        }
                                    });
                                }
                            }
                        });

                        //set online users in drawer text View
                        mUserRef.orderByChild(getString(R.string.ref_users_status)).equalTo(true).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    onlineDeliveryCount = (int) snapshot.getChildrenCount();
                                    onlineCountTv.setText(String.valueOf((int) snapshot.getChildrenCount()));
                                    onlineCountBtn.setImageResource(R.drawable.online_green);
                                } else {
                                    onlineDeliveryCount = 0;
                                    onlineCountBtn.setImageResource(R.drawable.offline_red);
                                    onlineCountTv.setText("0");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        loadPost();

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: " + error.toString());
                }
            });
        }
    }
}