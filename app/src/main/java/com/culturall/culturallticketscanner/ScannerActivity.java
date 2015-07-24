package com.culturall.culturallticketscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;


public class ScannerActivity extends Activity {

    EditText editText;
    Button scanButton;
    static String currentH1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                EditText editText = (EditText) findViewById(R.id.editText);
                editText.setText(intent.getStringExtra("SCAN_RESULT")); // This will contain your scan result
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);


        editText = (EditText) findViewById(R.id.editText);
        scanButton = (Button) findViewById(R.id.scan_button);
        WebView webView = (WebView) findViewById(R.id.webView);

        //disable button
        scanButton.setEnabled(false);

        //add JavaScriptInterface to the WebView
        final MyJavacriptInterface myJsInterface = new MyJavacriptInterface(this);

        webView.addJavascriptInterface(myJsInterface, "AndroidJsInterface");

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(
                        "com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
            }
        });

        editText.setText("bla-bla");

        startWebView(webView, "http://dev.culturall.com/ticket/hmk/entrance_check/?ec_l=ticketscanner&ec_p=wand.schnalle");
    }

    //defines the WebView behaviour
    private void startWebView(final WebView webView, String url) {

        //set the webView client
        webView.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog;
            boolean initialLoad = false;

            //if we omit this method url links are open in the new browser, not in this webView
            public boolean shouldOverrideUrlLoading(WebView  view, String url) {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(ScannerActivity.this);
                    progressDialog.setMessage("Loading, please wait!");
                    progressDialog.show();
                }
                view.loadUrl(url);
                return true;
            }

            //show loader on url load
            @Override
            public void onLoadResource(WebView view, String url) {
                if (initialLoad) {
                    return;
                }

                initialLoad = true;
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(ScannerActivity.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                try {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //check whether we're on the right page, so that we can enable the "Scan" button
                webView.loadUrl("javascript:AndroidJsInterface.getCurrentH1tag(document.getElementsByTagName('h1')[0].innerHTML)");
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scanner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyJavacriptInterface {
        Context mContext;

        MyJavacriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void openAndroidDialog() {
            AlertDialog.Builder myDialog = new AlertDialog.Builder(ScannerActivity.this);
            myDialog.setTitle("Dialog Title");
            myDialog.setMessage("Dialog Message");
            myDialog.setPositiveButton("ON", null);
            myDialog.show();
        }

        @JavascriptInterface
        public void getCurrentH1tag(String h1) {
            currentH1 = h1;

            //it must be this way, cause otherwise the views don't get updated
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final EditText editText = (EditText) findViewById(R.id.editText);
                    final Button scanButton = (Button) findViewById(R.id.scan_button);

                    Log.d("Scanner", "int the \"getCurrentH1tag\": " + currentH1);
                    boolean isRightPage = currentH1.equalsIgnoreCase("Eintrittskontrolle");
                    if (isRightPage) {
                        scanButton.setEnabled(true);
                    }
                    boolean isScanEnabled = scanButton.isEnabled();
                    //this doesn't work
                    editText.setText(currentH1);
                    scanButton.setText(currentH1);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TicketScanner", "onStart");
    }

    @Override
    protected void onResume() {
        super.onPostResume();
        Log.d("TicketScanner", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("TicketScanner", "onPause");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("TicketScanner", "onRestart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("TicketScanner", "onStop");
    }
}