package com.example.nfcattendease;

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
        //绑定组件
        Button writeButton = findViewById(R.id.write_button);
        editText = findViewById(R.id.edit_text);
        //设置监听事件
        writeButton.setOnClickListener(this);

        // 初始化PendingIntent，当有NFC设备连接上的时候，就交给当前Activity处理
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),  0/*PendingIntent.FLAG_CANCEL_CURRENT*/);
        onNewIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("nfcattendease", intent.getAction());
        //取出封装在intent中的TAG
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()))
        {
            Toast.makeText(this, "检测到NFC标签", Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);
            for (String tech : tag.getTechList()) {
                Log.i("nfcattendease",new String(tech));;// 显示设备支持技术
            }
            NdefMessage msg = null;
            if (ndef !=null){
                try {
                    msg = ndef.getCachedNdefMessage();
                    byte[] payload = msg.getRecords()[0].getPayload();
                    String encoding = ((payload[0] & 0x80) == 0) ? "utf-8" : "utf-16";
                    int languageCodeLength = payload[0] & 0x3f;
                    String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                    String text = new String(payload, languageCodeLength+1, payload.length - languageCodeLength - 1, encoding);
                    if(text.isEmpty())
                        Toast.makeText(this, "标签为空！", Toast.LENGTH_SHORT).show();
                    editText.setText(text);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("nfcattendease","No raw message detected");
                editText.setText("");
                Toast.makeText(this, "标签未初始化！", Toast.LENGTH_SHORT).show();
            }
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.write_button) {
            Log.i("nfcattendease","writeMsg");
            if(tag==null)
            {
                Toast.makeText(this, "未检测到标签！", Toast.LENGTH_SHORT).show();
                return;
            }
            Ndef ndef = Ndef.get(tag);
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            NdefRecord ndefRecord = NdefRecord.createTextRecord(null, editText.getText().toString());
            NdefRecord[] records = {ndefRecord};
            NdefMessage ndefMessage = new NdefMessage(records);
            try {
                if (ndef != null) {
                    ndef.connect();
                    ndef.writeNdefMessage(ndefMessage);
                    ndef.close();
                } else if(ndefFormatable != null) {
                    ndefFormatable.connect();
                    ndefFormatable.format(ndefMessage);
                }
                Toast.makeText(this, "写入成功！", Toast.LENGTH_SHORT).show();
            } catch (IOException | FormatException e) {
                e.printStackTrace();
                Toast.makeText(this, "写入失败！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}




