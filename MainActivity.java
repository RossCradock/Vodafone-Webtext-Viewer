package com.example.user.sms;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    final private int CONTACT_PICKER_RESULT = 1;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button passwordButton = (Button)findViewById(R.id.button);
        passwordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("password", "user_password"); // copy the password to the clipboard to be pasted into the login webpage
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getBaseContext(), "password copied", Toast.LENGTH_SHORT).show();
            }
        });

        Button contactsButton = (Button)findViewById(R.id.contacts);
        contactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT); // start the contact picker
            }
        });

        Button websiteButton =(Button)findViewById(R.id.button2);
        websiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView = (WebView)findViewById(R.id.webView);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUseWideViewPort(true);
                webView.loadUrl("https://www.vodafone.ie/myv/services/login/index.jsp?redirect=/myv/messaging/webtext/index.jsp"); // change this webpage to whatever the login page is for your network. Note the redirect parameter in the url
                webView.setWebViewClient(new WebViewClient() {
                    public boolean shouldOverrideUrlLoading(WebView viewx, String urlx) {
                        viewx.loadUrl(urlx);
                        return false;
                    }
                });
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            Uri result = data.getData();
            Cursor cursor = getContentResolver().query(result, null, null, null, null);
            try{
                if(cursor.moveToFirst()){
                    String contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0){
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId,null, null);
                        if(phones.moveToFirst()){
                            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneNumber = phoneNumber.replaceAll("[^0-9]", ""); // remove all the '+'s and the '-' from the phone numbers
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("number", phoneNumber);
                            clipboard.setPrimaryClip(clip);
                            Toast.makeText(getApplicationContext(), "Got the phone Number",Toast.LENGTH_SHORT).show();
                        }
                        phones.close();
                    }
                }
            } catch (NullPointerException e){
                Toast.makeText(getApplicationContext(), "Didn't get the phone number",Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
    }

    // this will allow the user to go back in the website and not close the app if they press back.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webView.canGoBack()) {
                        webView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    public void onPause(){
        super.onPause();
    }
}
