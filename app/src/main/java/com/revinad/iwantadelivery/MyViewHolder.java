package com.revinad.iwantadelivery;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.revinad.iwantadelivery.ApiFcmNotification.ApiClient;
import com.revinad.iwantadelivery.ApiFcmNotification.ApiInterface;
import com.revinad.iwantadelivery.ApiFcmNotification.RequestNotification;
import com.revinad.iwantadelivery.ApiFcmNotification.SendNotificationModel;
import com.revinad.iwantadelivery.Utills.Posts;

import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.ResponseBody;
import retrofit2.Callback;

public class MyViewHolder extends RecyclerView.ViewHolder {

    private final String TAG = "MyViewHolder";

    CircleImageView profileImage;
    TextView username, timeAgo, postDesc;
    CardView cardView;
    ConstraintLayout singleViewPostConstraint;
    CheckBox completeCB, onMyWayCB;
    Button mapBtn, deletePostBtn, sendNotificationToShopBtn;


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
        deletePostBtn = itemView.findViewById(R.id.deletePostBtn);
        sendNotificationToShopBtn = itemView.findViewById(R.id.sendNotificationToShopBtn);

    }

    public void initCB(String postKey, DatabaseReference postRef, Context context) {

        onMyWayCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(context.getString(R.string.ref_posts_on_my_way), isChecked);

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

        completeCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put(context.getString(R.string.ref_posts_completed), isChecked);
//                hashMap.put(context.getString(R.string.ref_post_completed_date), mDate);

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
}
