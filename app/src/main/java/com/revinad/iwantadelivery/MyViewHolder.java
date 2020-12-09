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
    TextView username, timeAgo, postDesc, onMyWayUsername, completeDate;
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
        onMyWayUsername = itemView.findViewById(R.id.onMyWayUsername);
        completeDate = itemView.findViewById(R.id.completeDate);

        completeCB.setClickable(false);
    }

    public void initCB(String postKey, DatabaseReference postRef, Context context, String mUsername, String mDate) {

        onMyWayCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(context.getString(R.string.ref_posts_on_my_way), isChecked);
                hashMap.put(context.getString(R.string.ref_post_username_of_on_my_way), mUsername);

                onMyWayUsername.setText(mUsername);

                postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            onMyWayCB.setClickable(!isChecked);
                            completeCB.setClickable(isChecked);

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
                hashMap.put(context.getString(R.string.ref_post_completed_date), mDate);

                completeDate.setText(mDate);

                postRef.child(postKey).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            completeCB.setClickable(!isChecked);

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
