package com.example.phoneauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {

    public static final String TAG = "TAG";
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    EditText mPhoneNo,mCodeEnter;
    Button mNextBtn;
    ProgressBar mProgressBar;
    TextView mState;
    CountryCodePicker mCodePicker;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken token;
    //Use flag to do multiple task by one button
    Boolean verificationInProgress = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Create the object for Firebase authentication
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        mPhoneNo = findViewById(R.id.phone);
        mCodeEnter = findViewById(R.id.codeEnter);
        mNextBtn = findViewById(R.id.nextBtn);
        mProgressBar = findViewById(R.id.progressBar);
        mState = findViewById(R.id.state);
        mCodePicker = findViewById(R.id.ccp);


        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!verificationInProgress) {
                    if (!mPhoneNo.getText().toString().isEmpty() && mPhoneNo.getText().toString().length() == 10) {
                        //Grab the countrycode and append it with the phone number entered by the user
                        // "+" is require for firebase phone number authentication
                        String phoneNum = "+" + mCodePicker.getSelectedCountryCode() + mPhoneNo.getText().toString();
                        Log.d(TAG, "Onclick: phone number -> " + phoneNum);

                        // When the user wait for the otp user see the progress bar
                        mProgressBar.setVisibility(View.VISIBLE);
                        mState.setText("Sending OTP..");
                        mState.setVisibility(View.VISIBLE);
                        requestOTP(phoneNum);

                    } else {
                        mPhoneNo.setError("Phone number not valid");
                    }
                } else {
                    String userOTP = mCodeEnter.getText().toString();
                    if(!userOTP.isEmpty() && userOTP.length() == 6) {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, userOTP);
                        //create a method to varify the user
                        verifyAuth(credential);

                    } else {
                        mCodeEnter.setError("Valid OTP is required");
                    }

                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mAuth.getCurrentUser() != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mState.setText("Checking..");
            mState.setVisibility(View.VISIBLE);
        }
          
    }

    private void verifyAuth(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(Register.this,"Authentication is Successful",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Register.this,"Authentication is Failed",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void requestOTP(String phoneNum) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNum, 60L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                // this method is called when the verification code is sent to the user
                super.onCodeSent(s, forceResendingToken);
                //As soon as the user get the OTP progress bar and State need to be invisible
                mProgressBar.setVisibility(View.GONE);
                mState.setVisibility(View.GONE);
                // and the code enter field need to visible
                mCodeEnter.setVisibility(View.VISIBLE);
                mVerificationId = s;
                token = forceResendingToken;
                // Change the text of the button to Varify
                mNextBtn.setText("Verify");
                //Action of the button also not required now
                //mNextBtn.setEnabled(false);
                verificationInProgress = true;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //When the user unable to get the OTP
                Toast.makeText(Register.this, "Cannot Create Account" + e.getMessage(),Toast.LENGTH_SHORT).show();

            }
        });
    }
}