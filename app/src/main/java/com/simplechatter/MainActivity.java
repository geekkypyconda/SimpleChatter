package com.simplechatter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hbb20.CountryCodePicker;

public class MainActivity extends AppCompatActivity {
    private EditText editText;
    private Button button;
    private TextView t1,t2;
    private CountryCodePicker ccp;
    private FirebaseAuth mAuth;

    ArrayAdapter<CharSequence> arrayAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        ccp = (CountryCodePicker)findViewById(R.id.ccp);

        //FirebaseApp.initializeApp(this);
        setTitle("Log in");
        //Initialize all variables
        editText = (EditText)findViewById(R.id.MainActivity_phoneNumber_editText);
        button = (Button)findViewById(R.id.MainActivity_sendVerificationCode_Button);
        t1 = (TextView)findViewById(R.id.MainActivity_welcome_textView); t2 = (TextView)findViewById(R.id.MainActivity_phoneNumber_editText);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);


    }

    private void moveToSetUpUserActivity() {
        Intent intent = new Intent(this,SetUpUserActivity.class);
        startActivity(intent);

    }

    public void sendVerificationCode(View View)
    {
        String number = editText.getText().toString();
        ccp.registerCarrierNumberEditText(editText);
        Log.d("rock",ccp.getFullNumberWithPlus());
        if(number.length() != 10 || !checkNumber(number)){
            Toast.makeText(this, "Please Enter A Valid Number!", Toast.LENGTH_SHORT).show();
            Log.d("rock","Wrong number Entered By user! Number : " + number);
        }else
            moveToVerificationCodeActivity(ccp.getFullNumberWithPlus());
    }

    private boolean checkNumber(String number) {
        boolean onlyNumber = true;
        for(int x = 0;x < number.length();x++)
            if(Integer.valueOf(number.charAt(x)) < 48 || Integer.valueOf(number.charAt(x)) > 57)
                return false;
        return true;
    }

    public void moveToVerificationCodeActivity(String number)
    {
        Intent intent = new Intent(this,VerificationCodeActivity.class);
        intent.putExtra("number",number);
        startActivity(intent);
    }

}
