package com.simplechatter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerificationCodeActivity extends AppCompatActivity {
    private Button button;
    private TextView textView;
    private EditText editText;
    private String userNumber;
    private String verificationId;
    private boolean mVerificationInProgress = false;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //State Initialized
    public static final String TAG = "rock";
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_CODE_SENT = 2;
    private static final int STATE_VERIFY_FAILED = 3;
    private static final int STATE_VERIFY_SUCCESS = 4;
    private static final int STATE_SIGNIN_FAILED = 5;
    private static final int STATE_SIGNIN_SUCCESS = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        button = (Button)findViewById(R.id.VerificationCodeActivity_verify_button);
        textView = (TextView)findViewById(R.id.VerificationCodeActivity_verificationCode_textView);
        editText = (EditText)findViewById(R.id.VerificationCodeActivity_verificationCode_editText);


        Intent intent = getIntent();
        userNumber = intent.getStringExtra("number");

        Log.d(TAG,"User number is " + userNumber);
        

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                mVerificationInProgress = false;
                Log.d(TAG,"Verification Completed! Credentials:- " + phoneAuthCredential.toString());
                signInWithPhoneAuthCredential(phoneAuthCredential);
                editText.setText(phoneAuthCredential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                mVerificationInProgress = false;
                Toast.makeText(VerificationCodeActivity.this, "Verification failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Verification Failed! " + e.getMessage(),e);
            }

            @Override
            public void onCodeSent(@NonNull String mVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(mVerificationId, forceResendingToken);
                verificationId = mVerificationId;
                mResendToken = forceResendingToken;
                textView.setText("Code Sent!");
                Log.d(TAG,"verification Code send! Verification ID :-" + verificationId + " Token :- " + forceResendingToken.toString());
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(VerificationCodeActivity.this, "Time out! try Again!", Toast.LENGTH_SHORT).show();
            }

        };
        startPhoneNumberVerification();
    }
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (mVerificationInProgress && validatePhoneNumber()) {
//            startPhoneNumberVerification();
//        }
//
//    }
    private boolean isUserLoggedIn() {
        return false;
    }

    private void moveToSetUpUserActivity() {
        Intent intent = new Intent(this,SetUpUserActivity.class);
        startActivity(intent);
    }
    public void verifyUserCode(View view)
    {
        verifyPhoneNumberWithCode(verificationId, editText.getText().toString());
    }

    private void startPhoneNumberVerification()
    {
        if(PhoneAuthProvider.getInstance() != null){
            try{
                PhoneAuthProvider.getInstance().verifyPhoneNumber(userNumber,60,TimeUnit.SECONDS,this,mCallBacks);
                mVerificationInProgress = true;
            }catch (Exception e){
                Toast.makeText(this, "Exception Occred! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Exception Occred! " + e.getMessage());
            }

        }else {
            Toast.makeText(this, "Phone Auth Provider Null!", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Phone Auth Provider Null!");
        }

    }

    private void verifyPhoneNumberWithCode(String vID,String code)
    {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(vID, code);

        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(PhoneAuthProvider.ForceResendingToken token)                                      //Does not require User Number to be a part of arguments as user Number is Global
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(userNumber,60,TimeUnit.SECONDS,this,mCallBacks,token);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG,"Sign In With Credentials Success!");
                            Toast.makeText(VerificationCodeActivity.this, "Sign In Success!", Toast.LENGTH_SHORT).show();

                            FirebaseUser user = task.getResult().getUser();
                            moveToSetUpUserActivity();
                        }else{
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(VerificationCodeActivity.this, "Invalid Code!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"Wrong Code Entered!");
                            }

                        }

                    }
                });
    }
    private boolean validatePhoneNumber() {
        String phoneNumber = userNumber;
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Invalid Phone Number!1", Toast.LENGTH_SHORT).show();
            Log.d(TAG,"Invalid Phone Number!");
            return false;
        }

        return true;
    }

}
