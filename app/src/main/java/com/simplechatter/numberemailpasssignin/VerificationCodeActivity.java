package com.simplechatter.numberemailpasssignin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.simplechatter.MainActivity;
import com.simplechatter.R;
import com.simplechatter.SetUpUserActivity;

import java.util.concurrent.TimeUnit;

public class VerificationCodeActivity extends AppCompatActivity {
    private Button button;
    private TextView textView;
    private EditText editText;
    private String userNumber;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
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
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        button = (Button)findViewById(R.id.VerificationCodeActivity_verify_button);
        textView = (TextView)findViewById(R.id.VerificationCodeActivity_verificationCode_textView);
        editText = (EditText)findViewById(R.id.VerificationCodeActivity_verificationCode_editText);
        progressDialog = new ProgressDialog(this);

        Intent intent = getIntent();
        userNumber = intent.getStringExtra("number");
        if(userNumber == null || userNumber.equalsIgnoreCase("")){
            moveToMainActivity();
        }
        Log.d(TAG,"User number is " + userNumber);
        

        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                mVerificationInProgress = false;
                Log.d(TAG,"Verification Completed! Credentials:- " + phoneAuthCredential.toString());
                signInWithPhoneAuthCredential(phoneAuthCredential);
                editText.setText(phoneAuthCredential.getSmsCode());
                showProgressDialog();
                disableButtons();
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                mVerificationInProgress = false;
                Toast.makeText(VerificationCodeActivity.this, "Verification failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG,"Verification Failed! " + e.getMessage(),e);
                progressDialog.dismiss();
                enableButtons();
            }

            @Override
            public void onCodeSent(@NonNull String mVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(mVerificationId, forceResendingToken);
                verificationId = mVerificationId;
                mResendToken = forceResendingToken;
                textView.setText("Code Sent!");
                Log.d(TAG,"verification Code send! Verification ID :-" + verificationId + " Token :- " + forceResendingToken.toString());
                progressDialog.dismiss();
                enableButtons();
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(VerificationCodeActivity.this, "Time out! try Again!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
                enableButtons();
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

    public void moveToMainActivity()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void moveToSetUpUserActivity() {
        Intent intent = new Intent(this, SetUpUserActivity.class);
        intent.putExtra("user_number",userNumber);
        Log.d("rock","Sending User Number " + userNumber);
        startActivity(intent);
    }
    public void goToEmailPasswordActivity(View view) {
        Intent intent = new Intent(this, LoginWithEmailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    public void goToMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void disableButtons()
    {
        editText.setEnabled(false);
        button.setEnabled(false);
    }
    private void enableButtons()
    {
        editText.setEnabled(true);
        button.setEnabled(true);
    }

    public void verifyUserCode(View view)
    {
        verifyPhoneNumberWithCode(verificationId, editText.getText().toString());
        showProgressDialog();
        disableButtons();
    }

    public void showProgressDialog()
    {
        progressDialog.setTitle("Verifying");
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
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
                            final String userId = mAuth.getCurrentUser().getUid();
                            rootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists())
                                        rootRef.child("Users").child(userId).setValue("");
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            FirebaseUser user = task.getResult().getUser();
                            progressDialog.dismiss();
                            enableButtons();

                            moveToSetUpUserActivity();
                            finish();
                        }else{
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                Toast.makeText(VerificationCodeActivity.this, "Invalid Code!", Toast.LENGTH_SHORT).show();
                                Log.d(TAG,"Wrong Code Entered!");
                                progressDialog.dismiss();
                                enableButtons();
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
