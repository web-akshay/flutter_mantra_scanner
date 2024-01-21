package com.example.flutter_mantra;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import java.io.ByteArrayOutputStream;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity implements MFS100Event {
    private static final String CHANNEL = "native-channel";
//-----
MFS100 mfs100 = null;
int timeout = 10000;
CheckBox cbFastDetection;
private long mLastAttTime = 0;
private static long Threshold = 1500;
long mLastDttTime = 0;
    private MFS100Receiver receiver;



    private class MFS100Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle the intent here
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the receiver
        unregisterReceiver(receiver);
    }
@Override
    public void onStart() {
    Log.e("log", "call onStart--->");
    receiver = new MFS100Receiver();
    IntentFilter filter = new IntentFilter("com.mantra.mfs100.ACTION_USB_PERMISSION");
    registerReceiver(receiver, filter);

    try {
            if (this.mfs100 == null) {
                this.mfs100 = new MFS100(this);
                this.mfs100.SetApplicationContext(this);
            } else {
                InitScanner();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
            e.printStackTrace();
        }
        super.onStart();
    }


    /* access modifiers changed from: protected */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("log", "call onCreate--->");
//        setContentView(R.layout.activity_mfs100_sample);
//        FindFormControls();
        try {
            getWindow().setSoftInputMode(3);
        } catch (Exception e) {
            Log.e("log", "error  getWindow onCreate--->");

            Log.e("Error", e.toString());
        }
        try {
            this.mfs100 = new MFS100(this);
            this.mfs100.SetApplicationContext(this);
        } catch (Exception e2) {
            e2.printStackTrace();
            Log.e("log", "error  mfs100 onCreate--->");
            Log.e("Error", e2.toString());


        }
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);


        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
//                            if (call.method.equals("log-message")) {
//                                String data = printMessage();
//                                result.success(data);
//                            Log.d("success", "in side the log-message function");
//
//
//                            }else
                                if (call.method.equals("initScanner")) {
                                InitScanner();
//                                result.success(data);
                            Log.d("success", "in side the initScanner");


                            }
                            else if (call.method.equals("mantra-start-scan")) {
                           StartSyncCapture(result);
//                                result.success(data);
                                Log.d("success", "in side the on start");


                            }
//                            else if (call.method.equals("get-future")) {
//                                myMethod(result);
//                            }


                            else {
                            Log.d("fail", "function not found");
                                result.notImplemented();
                            }


                        }
                );
    }


//    public String printMessage() {
//        Log.d("log", "printMessage--->");
//
//        String message = "Hello from native code";
//        return message;
//    }

//    public void myMethod( MethodChannel.Result result) {
//
//                                Handler hndler= new Handler();
//                                hndler.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//
//                                        Log.d("success", "log from android---->print after 5 sec");
//                                        result.success("native future---> print after 5 sec");
//
//                                    }
//                                },5000);
//                                Log.d("success", "in side the get-future");
//    }


    private void InitScanner() {
        Log.d("log", "initscanner----->");
        try {
            int ret = this.mfs100.Init();
            if (ret != 0) {
//                SetTextOnUIThread(this.mfs100.GetErrorMsg(ret));
                Log.e("success", "initialize failed");
                return;
            }else{
                Log.d("failed", "initialize successfully");

            }
//            SetTextOnUIThread("Init success");
//            SetLogOnUIThread("Serial: " + this.mfs100.GetDeviceInfo().SerialNo() + " Make: " + this.mfs100.GetDeviceInfo().Make() + " Model: " + this.mfs100.GetDeviceInfo().Model() + "\nCertificate: " + this.mfs100.GetCertification());
        } catch (Exception e) {
              Log.e("error", "Init failed, unhandled exception");
            Log.e("error",  e.toString());
//            Toast.makeText(getApplicationContext(), "Init failed, unhandled exception", 1).show();
//            SetTextOnUIThread("Init failed, unhandled exception");
        }
    }

    private FingerData lastCapFingerData = null;
    ImageView imgFinger;
    private boolean isCaptureRunning = false;


    private void StartSyncCapture(MethodChannel.Result result) {
        Log.d("log", "StartSyncCapture--->");
        String encodedImage = "";
        new Thread(new Runnable() {
            /* class MFS100Test.AnonymousClass1 */

            public void run() {
//                MFS100CodeHubs.this.SetTextOnUIThread("");
                MainActivity.this.isCaptureRunning = true;
                try {
                    FingerData fingerData = new FingerData();
                    int ret = MainActivity.this.mfs100.AutoCapture(fingerData, MainActivity.this.timeout, true);
                    Log.e("StartSyncCapture.RET", "" + ret);
                    if (ret != 0) {
//                        MainActivity.this.SetTextOnUIThread(MainActivity.this.mfs100.GetErrorMsg(ret));
                    } else {
                        MainActivity.this.lastCapFingerData = fingerData;
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0, fingerData.FingerImage().length);

                       //send image to flutter

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();

                        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
                        result.success(encodedImage);
//                        Log.d("ImageString",encodedImage);

                        // end send image

                        Log.e("log", "WSQInfo--->"+fingerData.FingerImage());
                        Log.e("log", "WSQInfo--->"+fingerData.WSQInfo());
                        Log.e("log", "WSQInfo--->"+fingerData.ISOTemplate().toString());

//                        MainActivity.this.SetTextOnUIThread("Capture Success");
//                        MainActivity.this.SetLogOnUIThread("\nQuality: " + fingerData.Quality() + "\nNFIQ: " + fingerData.Nfiq() + "\nWSQ Compress Ratio: " + fingerData.WSQCompressRatio() + "\nImage Dimensions (inch): " + fingerData.InWidth() + "\" X " + fingerData.InHeight() + "\"\nImage Area (inch): " + fingerData.InArea() + "\"\nResolution (dpi/ppi): " + fingerData.Resolution() + "\nGray Scale: " + fingerData.GrayScale() + "\nBits Per Pixal: " + fingerData.Bpp() + "\nWSQ Info: " + fingerData.WSQInfo());
//                        MainActivity.this.SetData2(fingerData);
                    }
                } catch (Exception e) {
                    Log.e("log", "UnInitScanner--->"+e);
                } catch (Throwable th) {
                    MainActivity.this.isCaptureRunning = false;
                    throw th;
                }
                MainActivity.this.isCaptureRunning = false;
            }
        }).start();

    }


    private void UnInitScanner() {
        Log.e("log", "UnInitScanner--->");


        try {
            int ret = this.mfs100.UnInit();
            if (ret != 0) {
//                SetTextOnUIThread(this.mfs100.GetErrorMsg(ret));
                Log.e("error", "--->this.mfs100.GetErrorMsg(ret)");

                return;
            }
//            SetLogOnUIThread("Uninit Success");
//            SetTextOnUIThread("Uninit Success");
            Log.d("Success", "Uninit Success");

            this.lastCapFingerData = null;
        } catch (Exception e) {
            Log.e("UnInitScanner.EX", e.toString());
        }
    }


    public void OnDeviceAttached(int vid, int pid, boolean hasPermission) {
        Log.e("log", "--->OnDeviceAttached");

        if (SystemClock.elapsedRealtime() - this.mLastAttTime >= Threshold) {
            this.mLastAttTime = SystemClock.elapsedRealtime();
            if (!hasPermission) {
//                SetTextOnUIThread("Permission denied");
                Log.e("error", "Permission denied");
            } else if (vid != 1204 && vid != 11279) {
            } else {
                if (pid == 34323) {
                    try {
                        int ret = this.mfs100.LoadFirmware();
                        if (ret != 0) {
//                            SetTextOnUIThread(this.mfs100.GetErrorMsg(ret));
                            Log.e("error", "OnDeviceAttached ---> ret != 0");

                        } else {
//                            SetTextOnUIThread("Load firmware success");
                            Log.e("error", "Load firmware success");

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (pid == 4101) {
                    int ret2 = this.mfs100.Init();
                    if (ret2 == 0) {
//                        showSuccessLog("Without Key");
                        Log.e("error", "Without Key");

                    } else {
//                        SetTextOnUIThread(this.mfs100.GetErrorMsg(ret2));
                        Log.e("error", "this.mfs100.GetErrorMsg(ret2)");

                    }
                }
            }
        }
    }

    @Override
    public void OnDeviceDetached() {
        Log.e("log", "--->OnDeviceDetached");

        try {
            if (SystemClock.elapsedRealtime() - this.mLastDttTime >= Threshold) {
                this.mLastDttTime = SystemClock.elapsedRealtime();
                UnInitScanner();
//                SetTextOnUIThread("Device removed");
                Log.d("success", "--->Device removed");

            }
        } catch (Exception e) {
        }


    }

    @Override
    public void OnHostCheckFailed(String s) {
        Log.e("log", "--->OnHostCheckFailed");
        try {
//            SetLogOnUIThread(err);
//            Toast.makeText(getApplicationContext(), 'err', 1).show();
            Log.e("error", "--->OnHostCheckFailed");

        } catch (Exception e) {
        }

    }






}
