package com.example.nfceasyentry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.tech.Ndef;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.method.ScrollingMovementMethod;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutDoorInfo;
    private LinearLayout layoutLocalUserInfo;
    private TextView textLocalUserInfo;
    private TextView textDoorInfo;
    private TextView textDoorData;
    private TextView textDoorNotDetected;
    private Button buttonOpenDoor;

    // Mocked local user data
    private static final String LOCAL_USER_DATA = "{\n" +
            "  \"username\": \"admin\",\n" +
            "  \"phone\": \"13812345678\",\n" +
            "  \"device\": \"Redmi Note\",\n" +
            "  \"password\": \"123456\",\n" +
            "  \"permission\": \"Dormitory3-1018\"\n" +
            "}";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        layoutDoorInfo = findViewById(R.id.layout_door_info);
        layoutLocalUserInfo = findViewById(R.id.layout_local_user_info);
        textLocalUserInfo = findViewById(R.id.text_local_user_info);
        textDoorNotDetected = findViewById(R.id.text_door_not_detected);
        textDoorInfo = findViewById(R.id.text_door_info);
        textDoorData = findViewById(R.id.text_door_data);
        textDoorData.setMovementMethod(new ScrollingMovementMethod());
        buttonOpenDoor = findViewById(R.id.button_open_door);

        // Set click listener for the open door button
        buttonOpenDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform open door action
                openDoor();
            }
        });

        // Delayed display of simulated door open after 5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Simulate NFC tag detection (replace this with actual NFC tag detection logic)
                simulateNfcTagDetection();
            }
        }, 5000); // 5000 milliseconds = 5 seconds
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check for NFC tag detection
        checkNfcTagDetection();
    }

    private void checkNfcTagDetection() {
        Intent intent = getIntent();
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            // NFC tag detected, show door info
            showDoorInfo(intent);
        } else {
            // NFC tag not detected, show local user info
            showLocalUserInfo();
        }
    }

    private void showDoorInfo(Intent intent) {
        // Hide the "Door not detected" message
        // Hide the local user info
        layoutLocalUserInfo.setVisibility(View.GONE);
        
        // Extract tag from intent
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        // Get Ndef object from the tag
        Ndef ndef = Ndef.get(tag);

        if (ndef != null) {
            // Ndef is available, read Ndef message
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            if (ndefMessage != null) {
                // Ndef message found, extract data
                NdefRecord[] records = ndefMessage.getRecords();
                if (records != null && records.length > 0) {
                    // Extract text from the first record
                    String text = getTextFromNdefRecord(records[0]);
                    // Display door data
                    textDoorData.setText(text);
                    layoutDoorInfo.setVisibility(View.VISIBLE);
                }
            }
        } else {
            // Ndef is not available, show error message
            Toast.makeText(this, "NFC tag is not compatible", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTextFromNdefRecord(NdefRecord record) {
        try {
            // Get payload bytes
            byte[] payload = record.getPayload();
            // Get encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
            // Get language code length
            int languageCodeLength = payload[0] & 0063;
            // Get text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("NFC", "Unsupported Encoding", e);
        }
        return null;
    }

    private void openDoor() {
        // Check if local user matches any valid user
        if (isLocalUserValid()) {
            Toast.makeText(this, "Door opened", Toast.LENGTH_SHORT).show();
        
            // Write log entry
            writeLogEntry("Yiliu", getCurrentTime(), "Success");
        } else {
            // Local user does not match any valid user
            // Display an error message
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();

            writeLogEntry("Zhikai", getCurrentTime(), "Fail");
        }
    }

    private boolean isLocalUserValid() {
        // Parse the local user data
        try {
            JSONObject localUserData = new JSONObject(LOCAL_USER_DATA);
            String localUsername = localUserData.getString("username");
            
            // Check if the local user is valid

            // For demonstration purposes, the local user is considered valid if the username is "admin"
            if (localUsername.equals("admin") || localUsername.equals("Yiliu")) {
                return true; // Return true if the local user is valid
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return false; // Return false if there is an error or the local user is invalid
    }

    private void writeLogEntry(String username, String time, String status) {
        // Implement code to write log entry to a log file or database
        // For demonstration purposes, just print the log entry
        String logEntry = "User: " + username + "\n" +
                          "Time: " + time + "\n" +
                          "Status: " + status;
        Log.d("DoorLog", logEntry);
    }

    private void simulateNfcTagDetection() {
        // Hide the "Door not detected" message
        // Hide the local user info
        layoutLocalUserInfo.setVisibility(View.GONE);

        // Simulate door data
        String doorData = "Door: Dormitory3-1018\n" +
                "Valid Users:\n" +
                " - Name: admin\n" +
                "   Phone: 13812345678\n" +
                "   Device: Redmi Note\n" +
                " - Name: Yiliu\n" +
                "   Phone: 13812345679\n" +
                "   Device: Redmi Note\n" +
                "Logs:\n" +
                " - User: admin\n" +
                "   Time: 2021-08-01 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-02 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-03 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-04 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-05 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-06 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-07 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Yiliu\n" +
                "   Time: 2021-08-08 12:00:00\n" +
                "   Status: Success\n" +
                " - User: Zhikai\n" +
                "   Time: 2021-08-09 12:00:00\n" +
                "   Status: Fail\n";   

        // Display door data
        textDoorData.setText(doorData);
        layoutDoorInfo.setVisibility(View.VISIBLE);
    }

    private void showLocalUserInfo() {
        try {
            JSONObject userData = new JSONObject(LOCAL_USER_DATA);
            String username = userData.getString("username");
            String phone = userData.getString("phone");
            String device = userData.getString("device");
            String password = userData.getString("password");
            String permission = userData.getString("permission");

            String userInfo = "Username: " + username + "\n" +
                    "Phone: " + phone + "\n" +
                    "Device: " + device + "\n" +
                    "Password: " + password + "\n" +
                    "Permission: " + permission + "\n" +
                    "Time: " + getCurrentTime();

            // Display the "Door not detected" message
            // Display local user info
            textLocalUserInfo.setText(userInfo);
            layoutLocalUserInfo.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
