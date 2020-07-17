package com.example.phoneauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddDetails extends AppCompatActivity {

    //Create an instance of Firebase
    FirebaseAuth firebaseAuth;
    //Create an instance of Firestore
    FirebaseFirestore fStore;

    //Initialize the variable
    EditText mFirstName, mLastName, mEmailAdd;
    Button mSaveBtn;
    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_details);

        //Connect resources with the variable
        mFirstName = findViewById(R.id.firstName);
        mLastName = findViewById(R.id.lastName);
        mEmailAdd = findViewById(R.id.emailAddress);
        mSaveBtn = findViewById(R.id.saveBtn);

        //Initialize Firebase object and Firestore object
        firebaseAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        //Retrive the current user based on UserId
        userId = firebaseAuth.getCurrentUser().getUid();

        //Create a document Refference  for firebase firestore
        //Create an document based on the userId
        final DocumentReference docRef = fStore.collection("users").document(userId);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mFirstName.getText().toString().isEmpty() && !mLastName.getText().toString().isEmpty() &&
                        !mEmailAdd.getText().toString().isEmpty()) {
                    String fstName = mFirstName.getText().toString();
                    String lstName = mLastName.getText().toString();
                    String mailiD = mEmailAdd.getText().toString();

                    //Insert the data into to the FireStore database
                    Map<String, Object> user = new HashMap<>();
                    //firstName is the key and fstName is the value
                    user.put("firstName", fstName);
                    user.put("lastName", lstName);
                    user.put("mailId", mailiD);

                    //Use document reference to insert the data on the firestore database
                    docRef.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(AddDetails.this,"Data is not Inserted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(AddDetails.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }
}