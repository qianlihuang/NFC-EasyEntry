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
import java.util.Arrays;

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
            "  \"permission\": \"Dormitory3-1018\",\n" +
            "  \"time\": \"2021-08-01 12:00:00\"\n" +
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
        // Implement door opening functionality here
        // For demonstration purposes, just show a toast message
        Toast.makeText(this, "Door opened", Toast.LENGTH_SHORT).show();
    }

    private void simulateNfcTagDetection() {
        // Hide the "Door not detected" message
        // Hide the local user info
        layoutLocalUserInfo.setVisibility(View.GONE);

        // Simulate door data
        String doorData = "Door: Dormitory3-1018\n" +
                "Users:\n" +
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
                "   Time: 2021-08-01 12:00:00\n" +
                "   Status: Success";

        // Display door data
        textDoorData.setText(doorData);
        layoutDoorInfo.setVisibility(View.VISIBLE);
    }

    private void showLocalUserInfo() {
        // Display the "Door not detected" message
        // Display local user info
        textLocalUserInfo.setText(LOCAL_USER_DATA);
        layoutLocalUserInfo.setVisibility(View.VISIBLE);
    }
}
