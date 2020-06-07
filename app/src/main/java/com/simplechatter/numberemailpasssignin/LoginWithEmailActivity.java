package com.simplechatter.numberemailpasssignin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.simplechatter.MainActivity;
import com.simplechatter.R;
import com.simplechatter.SetUpUserActivity;

public class LoginWithEmailActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;
    private String userEmail,userPassword,deviceToken;
    private EditText userEmail_editText,userPassword_editText;
    private TextView textView;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootRef;
    private Button loginButton,signupButton;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_email);
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        userEmail_editText = (EditText)findViewById(R.id.LoginWithEmailActivity_email_editText);
        userPassword_editText = (EditText)findViewById(R.id.LoginWithEmailActivity_password_editText);
        userEmail = userEmail_editText.getText().toString();userPassword = userPassword_editText.getText().toString();
        textView = (TextView)findViewById(R.id.LoginWithEmailActivity_login_textView);
        loginButton = (Button)findViewById(R.id.LoginWithEmailActivity_login_button);signupButton = (Button)findViewById(R.id.LoginWithEmailActivity_signup_button);
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        if(mUser != null && mUser.isEmailVerified() && mUser.getEmail().contains("@"))
            moveToSetupUserActivity();
        progressDialog = new ProgressDialog(this);
    }
    private boolean checkEmailAndPassword() {
        if(isEmailOk() && isPasswordOk())
            return true;
        else
            return false;
    }
    public boolean isEmailOk()
    {
        userEmail = userEmail_editText.getText().toString();
        Log.d("rock","Mail is " + userEmail);
        if(userEmail.contains("@") && userEmail.substring(userEmail.length() - 4,userEmail.length()).equalsIgnoreCase(".com"))
            return true;
        else{
            Toast.makeText(this, "Invalid email", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    public boolean isPasswordOk()
    {
        userPassword = userPassword_editText.getText().toString();
        Log.d("rock","Password is " + userPassword);
        if(userPassword.length() < 8){
            Toast.makeText(this, "Password must be 8 characters long and must contain at least 1 alphabet and 1 number", Toast.LENGTH_LONG).show();
            return false;
        }
        String stopWords[] = {"12345","abcde","pqrs"},alphabets[] = "a b c d e f g h i j k l m n o p q r s t u v w x y z".split(" "),number[] = "1 2 3 4 5 6 7 8 9 0".split(" ");
        for(int x = 0;x < 3;x++)
            if(userPassword.equalsIgnoreCase(stopWords[x])){
                Toast.makeText(this, "Password is not Strong!", Toast.LENGTH_LONG).show();
                return false;
            }
        boolean ap = false,num = false;
        for(int x = 0;x < 26;x++)
            if(userPassword.contains(alphabets[x]))
                ap = true;
        for(int x = 0;x < 10;x++)
            if(userPassword.contains(number[x]))
                num = true;
        if(ap && num)
            return true;
        else {
            Toast.makeText(this, "Password must be 8 characters long and must contain at least 1 alphabet and 1 number", Toast.LENGTH_LONG).show();
            return false;
        }
    }
    public void loginButtonClicked(View view)
    {
        if(!checkEmailAndPassword())
            return;
        showProgressDialog("logging in");
        disableButtons();
        userEmail = userEmail_editText.getText().toString();userPassword = userPassword_editText.getText().toString();
        mAuth.signInWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(mUser != null){
                        Log.d("rock","User is Not Null!");
                        if(mUser.isEmailVerified()) {
                            progressDialog.dismiss();
                            String currentUserId = mAuth.getCurrentUser().getUid();

                            deviceToken = FirebaseInstanceId.getInstance().getToken();
                            moveToSetupUserActivity();
                            finish();
                        }
                    }
                    else if(mUser == null){
                            Toast.makeText(LoginWithEmailActivity.this, "Please try again!", Toast.LENGTH_LONG).show();
                            Log.d("rock","Please try again!");
                            mUser = FirebaseAuth.getInstance().getCurrentUser();
                            enableButtons();
                            progressDialog.dismiss();
                        }
                    else{
                        Toast.makeText(LoginWithEmailActivity.this, "Email not Verified! Please Verify Your Email and then try again!", Toast.LENGTH_LONG).show();
                        Log.d("rock","Email not Verified! Please Verify Your Email and then try again!");
                        textView.setText("Verification link send To " + userEmail + " Please Verify! and click login");
                        sendVerificationMail();
                        progressDialog.dismiss();
                        enableButtons();

                    }
                }else{
                    Toast.makeText(LoginWithEmailActivity.this, "Login Failed! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("rock","Login Failed! " + task.getException().getMessage());
                    textView.setText("Login With Email and Password");
                    progressDialog.dismiss();
                    enableButtons();
                }
            }
        });
    }

    private void enableButtons()
    {
        loginButton.setEnabled(true);
        signupButton.setEnabled(true);
        userEmail_editText.setEnabled(true);
        userPassword_editText.setEnabled(true);
    }

    private void disableButtons()
    {
        loginButton.setEnabled(false);
        signupButton.setEnabled(false);
        userEmail_editText.setEnabled(false);
        userPassword_editText.setEnabled(false);
    }
    private void moveToSetupUserActivity() {
        Intent intent = new Intent(this, SetUpUserActivity.class);
        progressDialog.dismiss();
        intent.putExtra("user_email",userEmail);
        startActivity(intent);
        finish();
    }

    public void signupButtonClicked(View view)
    {
        if(!checkEmailAndPassword())
            return;
        disableButtons();
        showProgressDialog("Signing up");
        userEmail = userEmail_editText.getText().toString();userPassword = userPassword_editText.getText().toString();
        mAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mUser = FirebaseAuth.getInstance().getCurrentUser();
                    mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(LoginWithEmailActivity.this, "Email Verification send To " + userEmail + " Please Verify! Before Continuing", Toast.LENGTH_LONG).show();
                                Log.d("rock","Email Verification send To " + userEmail + " Please Verify!");
                                textView.setText("Email Verification link send to " + userEmail + " Please Verify! and click login");
                                String userId = mAuth.getCurrentUser().getUid();
                                rootRef.child("Users").child(userId).setValue("");
                                progressDialog.dismiss();
                                enableButtons();
                            }else{
                                Toast.makeText(LoginWithEmailActivity.this, "Sending Email Verification Failed!. Please Check Your Email Address!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                Log.d("rock","Sending Email Verification Failed!. Please Check Your Email Address!" + task.getException().getMessage());
                                textView.setText("Login With Email and Password");
                                progressDialog.dismiss();
                                enableButtons();
                            }
                        }
                    });
                }else{
                    Toast.makeText(LoginWithEmailActivity.this, "Sign Up Failed!. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("rock","Sign Up Failed!. " + task.getException().getMessage());
                    textView.setText("Login With Email and Password");
                    progressDialog.dismiss();
                    enableButtons();
                }
            }
        });
    }



    public void showProgressDialog(String ghf)
    {
        progressDialog.setTitle(ghf);
        progressDialog.setMessage("Please Wait.....");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    public void goToLoginWthNumber(View view)
    {
        startActivity(new Intent(this, MainActivity.class));
        progressDialog.dismiss();
        finish();
    }
    public void sendVerificationMail()
    {
        mUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(LoginWithEmailActivity.this, "Email Verification send To " + userEmail + " Please Verify! Before Continuing", Toast.LENGTH_LONG).show();
                    Log.d("rock","Email Verification send To " + userEmail + " Please Verify");
                }else{
                    Toast.makeText(LoginWithEmailActivity.this, "Sending Email Verification Failed!. Please Check Your Email Address!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("rock","Sending Email Verification Failed!. Please Check Your Email Address!" + task.getException().getMessage());
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_LONG).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
