package com.example.nfceasyentry;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    public static IntentFilter[] intentFiltersArray;
    private String[][] techListsArray = new String[][]{new String[]{IsoDep.class.getName()},{ Ndef.class.getName() }, { NfcA.class.getName() },};
    private Tag tag;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //NfcUtils nfcUtils = new NfcUtils(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 绑定组件
        Button writeButton = findViewById(R.id.write_button);
        editText = findViewById(R.id.edit_text);

        // 设置监听事件
        writeButton.setOnClickListener(this);

        // 初始化 PendingIntent，当有 NFC 设备连接上时，就交给当前 Activity 处理
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 00/*PendingIntent.FLAG_CANCEL_CURRENT*/);
        // 处理新的 Intent
        onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            // 启用前台调度
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            // 禁用前台调度
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("nfceasyentry", intent.getAction());
        // 取出封装在 Intent 中的 TAG
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            Toast.makeText(this, "NFC tag detected", Toast.LENGTH_SHORT).show();
            // 获取 Ndef 对象
            Ndef ndef = Ndef.get(tag);
            for (String tech : tag.getTechList()) {
                Log.i("nfceasyentry", new String(tech)); // 显示设备支持技术
            }
            NdefMessage msg = null;
            if (ndef != null) {
                try {
                    msg = ndef.getCachedNdefMessage();
                    byte[] payload = msg.getRecords()[0].getPayload();
                    String encoding = ((payload[0] & 0x80) == 0) ? "utf-8" : "utf-16";
                    int languageCodeLength = payload[0] & 0x3f;
                    String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                    String text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, encoding);
                    if (text.isEmpty())
                        Toast.makeText(this, "Tag is empty!", Toast.LENGTH_SHORT).show();
                    editText.setText(text);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("nfceasyentry", "No raw message detected");
                editText.setText("");
                Toast.makeText(this, "Tag is uninitialized!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.write_button) {
            Log.i("nfceasyentry", "writeMsg");
            if (tag == null) {
                Toast.makeText(this, "Tag not detected!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 获取 Ndef 对象和 NdefFormatable 对象
            Ndef ndef = Ndef.get(tag);
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            // 创建 NdefRecord 对象
            NdefRecord ndefRecord = NdefRecord.createTextRecord(null, editText.getText().toString());
            NdefRecord[] records = {ndefRecord};
            NdefMessage ndefMessage = new NdefMessage(records);
            try {
                if (ndef != null) {
                    ndef.connect();
                    ndef.writeNdefMessage(ndefMessage);
                    ndef.close();
                } else if (ndefFormatable != null) {
                    ndefFormatable.connect();
                    ndefFormatable.format(ndefMessage);
                }
                Toast.makeText(this, "Write successful!", Toast.LENGTH_SHORT).show();
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "Write failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
