package com.example.lenovo.ronixtask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText userName;
    EditText password;
    FloatingActionButton loginButton;
    CheckBox rememberMe;
    TextView forgot_password;
    private String blockCharacterSet = "$~";
    DatabaseHelper helper;
    SharedPreferences sharedPreferences;
    public static final String REMEMBER_ME = "REMEMBERME";
    int userId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = (EditText) findViewById(R.id.login_username);
        password = (EditText) findViewById(R.id.login_password);
        loginButton = (FloatingActionButton) findViewById(R.id.login_button);
        rememberMe = (CheckBox) findViewById(R.id.rememberMe);
        forgot_password = (TextView) findViewById(R.id.forgot_password);
        helper = new DatabaseHelper(this);
        loginButton.setOnClickListener(this);
        forgot_password.setOnClickListener(this);
        sharedPreferences = getSharedPreferences("MYPREFERANCE", MODE_PRIVATE);
        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(REMEMBER_ME, isChecked);
                editor.apply();
            }
        });

        //Add filter to the Login edit text to prevent ^ $ ~ and set it to max 30 character
        InputFilter filter = new InputFilter() {

            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                if (charSequence != null && blockCharacterSet.contains(("" + charSequence))) {
                    return "";
                }
                return null;
            }
        };
        userName.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(30)});
        password.setFilters(new InputFilter[]{filter, new InputFilter.LengthFilter(30)});

        //Api response
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ronixtech.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UserClient service = retrofit.create(UserClient.class);
        retrofit2.Call<User> call = service.getUsers();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(retrofit2.Call<User> call, Response<User> response) {

                String ssid = response.body().getUserName();
                Log.d("=====", ssid + "");
            }

            @Override
            public void onFailure(retrofit2.Call<User> call, Throwable t) {
                Log.d("Response failed", "");
            }
        });

        restoreUserName();
     /*   //MQTT Receive Message
        PahoMqttClient pahoMqttClient = new PahoMqttClient();
        final MqttAndroidClient mqttAndroidClient = pahoMqttClient
                .getMqttClient(getApplicationContext(), "http://m13.cloudmqtt.com/", "11853");

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                userName.setText(s);

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                // setMessageNotification(s, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });*/
    }

    @Override
    public void onClick(View view) {
        if (view == loginButton) {
            performLogin();

        }
        if (view == forgot_password) {

        }
    }

    private void performLogin() {
        final String uName = userName.getText().toString();
        final String pass = password.getText().toString();


        if (uName.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "User Name and Password can't be empty", Toast.LENGTH_LONG).show();
            return;
        }
        storeUserName(uName, pass);
        validateCandidate();


    }

    private void storeUserName(String uName, String pass) {
        //Store the First Login attempt
        if (userId == 0) {
            Boolean result = helper.insertData(uName, pass);
            if (result == true) {
                Toast.makeText(this, "Data Inserted Successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Data Insertion Failed", Toast.LENGTH_SHORT).show();
            }
        }
        userId = 1;
    }

    private void restoreUserName() {

        Cursor res = helper.getAllData();
        Boolean rememberme = sharedPreferences.getBoolean(REMEMBER_ME, false);
        if (res != null && res.getCount() > 0) {
            while (res.moveToNext()) {
                if (userName != null) {
                    userName.setText(res.getString(1));
                    if (password != null)
                        password.setText(res.getString(2));
                }
                if (rememberme) {
                    rememberMe.setChecked(true);
                } else {
                    rememberMe.setChecked(false);
                }
            }

        }

    }

    private void validateCandidate() {
        helper = new DatabaseHelper(this);
        SQLiteDatabase database = helper.getReadableDatabase();
        String selection = DatabaseHelper.COL_2 + "=?";
        String[] selectionArgs = {userName.getText().toString().trim()};
        Cursor cursor = database
                .query(DatabaseHelper.TABLE_NAME, null, selection, selectionArgs, null, null, null);
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            String data = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_3));
            if (password.getText().toString().trim().equals(data)) {
                Toast.makeText(this, "Login successfully" + cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_2)), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid user name or password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid user name or password", Toast.LENGTH_SHORT).show();
        }
    }

}

