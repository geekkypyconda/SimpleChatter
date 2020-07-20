package com.simplechatter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;
import com.simplechatter.numberemailpasssignin.LoginWithEmailActivity;
import com.simplechatter.numberemailpasssignin.VerificationCodeActivity;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private static final String TAG = "MainActivity";
    private boolean doubleBackToExitPressedOnce = false;
    private Button button;
    private TextView t1,t2;
    private CountryCodePicker ccp;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    ArrayAdapter<CharSequence> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        ccp = (CountryCodePicker)findViewById(R.id.ccp);
        FirebaseApp.initializeApp(this);
        setTitle("Log in");
        //Initialize all variables
        editText = (EditText)findViewById(R.id.MainActivity_phoneNumber_editText);
        button = (Button)findViewById(R.id.MainActivity_sendVerificationCode_Button);
        t1 = (TextView)findViewById(R.id.MainActivity_welcome_textView); t2 = (TextView)findViewById(R.id.MainActivity_phoneNumber_editText);
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.planets_array, android.R.layout.simple_spinner_item);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                Log.d(TAG, "onStart: Moving to setup user activity from 1");
                Intent intent = new Intent(new Intent(this,SetUpUserActivity.class));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("user_email",FirebaseAuth.getInstance().getCurrentUser().getEmail());
                startActivity(intent);
                finish();
            }else if(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null){
                Log.d(TAG, "onStart: Moving to setup user activity from 2 " + FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                Intent intent = new Intent(new Intent(this,SetUpUserActivity.class));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("user_number",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                startActivity(intent);
                finish();
            }else if(!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                Log.d(TAG, "onStart: Moving to email activity from 3");
                Intent intent = new Intent(new Intent(this,LoginWithEmailActivity.class));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("user_email",FirebaseAuth.getInstance().getCurrentUser().getEmail());
                startActivity(intent);
                finish();
            }
        }

    }

    private void moveToSetUpUserActivity() {
        Intent intent = new Intent(this,SetUpUserActivity.class);
        startActivity(intent);

    }
    public void moveToVerificationCodeActivity(String number)
    {
        Intent intent = new Intent(this, VerificationCodeActivity.class);
        intent.putExtra("number",number);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void sendVerificationCode(View View)
    {
        String number = editText.getText().toString();
        ccp.registerCarrierNumberEditText(editText);
        Log.d("rock",ccp.getFullNumberWithPlus());
        Log.d("rock","The Number For Checking is " + number);
        if(number.length() != 10 || !checkNumber(number)){
            Toast.makeText(this, "Please Enter A Valid Number!", Toast.LENGTH_SHORT).show();
            Log.d("rock","Wrong number Entered By user! Number : " + number);
        }else
            moveToVerificationCodeActivity(ccp.getFullNumberWithPlus());
    }

    private boolean checkNumber(String number) {
        boolean onlyNumber = true;
        Log.d("rock","Number Entered b user " + number);
        for(int x = 0;x < number.length();x++)
            if(Integer.valueOf(number.charAt(x)) < 48 || Integer.valueOf(number.charAt(x)) > 57)
                return false;
        return true;
    }


    public void goToLoginWithEmailActivity(View view) {
        startActivity(new Intent(this, LoginWithEmailActivity.class));
        finish();
    }



    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
