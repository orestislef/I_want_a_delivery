package com.revinad.iwantadelivery;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;

import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {

    private final String TAG = "MyViewHolder";

    CircleImageView profileImage;
    TextView username, timeAgo, postDesc;
    CardView cardView;
    ConstraintLayout singleViewPostConstraint;
    CheckBox completeCB, onMyWayCB;
    Button mapBtn;


    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        singleViewPostConstraint = itemView.findViewById(R.id.single_view_post_constraint);
        profileImage = itemView.findViewById(R.id.profileImagePost);
        username = itemView.findViewById(R.id.profileUsernamePost);
        timeAgo = itemView.findViewById(R.id.timeAgo);
        postDesc = itemView.findViewById(R.id.postDescription);
        cardView = itemView.findViewById(R.id.singlePostCardView);
        completeCB = itemView.findViewById(R.id.completedCB);
        onMyWayCB = itemView.findViewById(R.id.onMyWayCB);
        mapBtn = itemView.findViewById(R.id.mapBtn);
    }

    public void initCB(String postKey, DatabaseReference postRef, Context context, String mUsername, String mDate) {
        //TODO: if onMyWayCB.isChecked(false) -> completeCB.setClickable(false) *need to +id the view
        //      if onMyWayCB.isChecked(true) -> completeCB.setClickable(true)

        onMyWayCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(context.getString(R.string.ref_posts_on_my_way), isChecked);
                postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (isChecked) {
                                onMyWayCB.setClickable(false);
                                //Adding Username of onTheWay to onTheWayUsername <- Database
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(context.getString(R.string.ref_post_username_of_on_my_way),mUsername);

                                postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d(TAG, "onComplete: Successful onMyWay username added: "+mUsername);
                                        }
                                    }
                                });

                            }else onMyWayCB.setClickable(true);
                            Log.d(TAG, "onComplete: onMyWayCB: " + isChecked);
                        }
                    }
                });
            }
        });

        //TODO: if completeCB.isChecked(true) -> show btn to delete Post(postKey) from Database
        completeCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(context.getString(R.string.ref_posts_completed), isChecked);
                postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (isChecked) {
                                completeCB.setClickable(false);

                                //adding Date of complete delivery
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(context.getString(R.string.ref_post_completed_date),mDate);
                                postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Log.d(TAG, "onComplete: Successful complete date added: "+mDate);
                                        }
                                    }
                                });
                            }else completeCB.setClickable(true);
                            Log.d(TAG, "onComplete: completeCB: " + isChecked);
                        }
                    }
                });
            }
        });
    }

    public void initCBText(String postKey, String uid, DatabaseReference postRef, Context context) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put(context.getString(R.string.ref_post_completed_date), new Date().toString());
        hashMap.put(context.getString(R.string.ref_post_username_of_on_my_way), uid);
        postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: ");
                }
            }
        });
    }
}
