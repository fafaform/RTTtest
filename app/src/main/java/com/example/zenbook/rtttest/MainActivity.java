package com.example.zenbook.rtttest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static int NUMBER_OF_PACKTETS = 10;
    private static int PING_PACKET_SIZE = 32;

    private static int NUMBER_OF_PING = 10;
//    private static int NUMBER_OF_PING = 1440;

//    private static int WAITING_TIME_MILLISECOND = 6000;
    private static int WAITING_TIME_MILLISECOND = 60000;
    private boolean start = true;
    private boolean run = true;
    private int count = 0;

    //todo ------------------------------------------------------
    private EditText url;
    private TextView error;
    private TextView serverStatus;
    private Double min;
    private Double avg;
    private Double max;
    private Double rss;
    private String networkType;
    private String cid;
    private int battery;
    public static Activity activity;
    private Button button;
    private LinearLayout linearLayout;
    private FileOutputStream outputStream;
    private String filename;
    private String thingspeakKey = "";
    private boolean Send2Server;

    private int LTEsignalStrength;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        linearLayout = (LinearLayout) findViewById(R.id.listLayout);

        url = (EditText)findViewById(R.id.url);
        error = (TextView)findViewById(R.id.errorText);
        button = (Button)findViewById(R.id.button);
        serverStatus = (TextView)findViewById(R.id.serverst);
        activity = this;

        //// TODO: 1/6/2017 Send to server enable
        Send2Server = true;
        if(Send2Server)serverStatus.setText("ON"); serverStatus.setTextColor(Color.GREEN);
        //// TODO: 1/6/2017 end Send to server enable

        //// TODO: 9/18/2016 start initial rss
        myPhoneStateListener psListener = new myPhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(psListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        //// TODO: 9/18/2016 finish initial rss

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //// TODO: 10/31/2016 Screen brightness
                android.provider.Settings.System.putInt(getBaseContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 1);
                //// TODO: 10/31/2016 End screen brightness
                if(button.getText().equals("START")) {
                    button.setText("STARTING");
                    button.setClickable(false);
                    run = true;

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            while(run) {
//                            for (int i = 0; i < 10; i++) {

                                try {
                                    filename = "test.csv";
                                    File file = new File(getBaseContext().getFilesDir(), filename);
                                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);

                                    //// TODO: 9/18/2016 Table head
                                    String string = "";
//                                    String string = "DateTime,min,avg,max,rss,networkType,cid,battery\n";
                                    outputStream.write(string.getBytes());
                                    //// TODO: 9/18/2016 End table head

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                start = true;
                                count = 0;
                                while (start) {
                                    count++;
                                    if (count < NUMBER_OF_PING + 1) {
                                        ////TODO: text to ping
                                        try {
                                            mHandler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    // TODO Auto-generated method stub
                                                    error.setText("Pinging: "+ count);
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        // TODO: 11/11/2016 end Text to ping
                                        double[] data = getLatency(url.getText().toString());
                                        min = round(data[0], 2);
                                        avg = round(data[1], 2);
                                        max = round(data[2], 2);
                                        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                                        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
                                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                            cid = "";
                                            rss = Double.parseDouble(wifiManager.getConnectionInfo().getRssi() + "");
                                            networkType = "Wi-Fi";
                                        } else if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()) {

                                            // TODO: 9/18/2016 For API 17+
                                            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                                            System.out.println("DataNetworkType: "+telephonyManager.getNetworkType()+"\n"+"Network_Type_LTE: "
//                                                    +telephonyManager.NETWORK_TYPE_LTE+"\n"+"Network_Type_WCDMA: "+telephonyManager.NETWORK_TYPE_UMTS);
                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                                if(telephonyManager.getNetworkType() == telephonyManager.NETWORK_TYPE_LTE){
                                                    System.out.println("LTE");
                                                    CellInfoLte cellinfogsm = (CellInfoLte) telephonyManager.getAllCellInfo().get(0);
                                                    CellSignalStrengthLte cellSignalStrengthLte = cellinfogsm.getCellSignalStrength();
                                                    CellIdentityLte cellIdentityLte = cellinfogsm.getCellIdentity();
                                                    cid = cellIdentityLte.getCi()+"";
                                                    rss = Double.parseDouble(cellSignalStrengthLte.getDbm() + "");
                                                    networkType = "LTE";
                                                }else if(telephonyManager.getNetworkType() == telephonyManager.NETWORK_TYPE_UMTS){
                                                    System.out.println("UMTS");
                                                    CellInfoWcdma cellinfogsm = (CellInfoWcdma) telephonyManager.getAllCellInfo().get(0);
                                                    CellSignalStrengthWcdma cellSignalStrengthLte = cellinfogsm.getCellSignalStrength();
                                                    CellIdentityWcdma cellIdentityLte = cellinfogsm.getCellIdentity();
                                                    cid = cellIdentityLte.getCid()+"";
                                                    rss = Double.parseDouble(cellSignalStrengthLte.getDbm() + "");
                                                    networkType = "UMTS";
                                                }else if(telephonyManager.getNetworkType() == telephonyManager.NETWORK_TYPE_CDMA){
                                                    System.out.println("CDMA");
                                                    CellInfoCdma cellinfogsm = (CellInfoCdma) telephonyManager.getAllCellInfo().get(0);
                                                    CellSignalStrengthCdma cellSignalStrengthLte = cellinfogsm.getCellSignalStrength();
                                                    CellIdentityCdma cellIdentityLte = cellinfogsm.getCellIdentity();
                                                    cid = cellIdentityLte.getBasestationId()+"";
                                                    rss = Double.parseDouble(cellSignalStrengthLte.getDbm() + "");
                                                    networkType = "CDMA";
                                                }else if(telephonyManager.getNetworkType() == telephonyManager.NETWORK_TYPE_EDGE){
                                                    System.out.println("EDGE");
                                                    CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                                                    CellSignalStrengthGsm cellSignalStrengthLte = cellinfogsm.getCellSignalStrength();
                                                    CellIdentityGsm cellIdentityLte = cellinfogsm.getCellIdentity();
                                                    cid = cellIdentityLte.getCid()+"";
                                                    rss = Double.parseDouble(cellSignalStrengthLte.getDbm() + "");
                                                    networkType = "EDGE";
                                                }else{
                                                    System.out.println("UNKNOWN");
                                                    CellInfoGsm cellinfogsm = (CellInfoGsm) telephonyManager.getAllCellInfo().get(0);
                                                    CellSignalStrengthGsm cellSignalStrengthLte = cellinfogsm.getCellSignalStrength();
                                                    CellIdentityGsm cellIdentityLte = cellinfogsm.getCellIdentity();
                                                    cid = cellIdentityLte.getCid()+"";
                                                    rss = Double.parseDouble(cellSignalStrengthLte.getDbm() + "");
                                                    networkType = "UNKNOWN";
                                                }

                                            }
                                            //// TODO: 9/18/2016 End for API 17+
                                            //// TODO: 10/5/2016 for API 17-
//                                rss = Double.parseDouble(LTEsignalStrength+"");
                                            //// TODO: 10/5/2016 end for API 17-
                                        }
                                        //// TODO: 12/17/2016 battery manager 
//                                        BatteryManager batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
//                                        battery = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
//                                        battery = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER);
//                                        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                                        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
                                        battery = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                                        System.out.println(battery);
                                        //// TODO: 12/17/2016 end battery manager

                                        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                        Date date = new Date();
                                        System.out.println("DateTime: " + dateFormat.format(date) + ", Min: " + min + ", AVG: " + avg + ", Max: " + max + ", rss: " + rss + ", technique: " + networkType + ", cid: " + cid + ", battery: " + battery);
                                        try {
                                            String string = dateFormat.format(date) + "," + min + "," + avg + "," + max + "," + rss + "," + networkType + "," + cid + "," + battery +"\n";
                                            outputStream.write(string.getBytes());

                                            //// TODO: 9/18/2016 sending to thingspeak

                                            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                                            if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
                                                thingspeakKey = "1CC7UK5QBF1MAN40";
                                                System.out.println("Wi-Fi");
                                            }else if(manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
                                                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                                                String carrierName = telephonyManager.getNetworkOperatorName();
                                                System.out.println(carrierName);
                                                if(carrierName.equals("AIS")){
                                                    thingspeakKey = "13TDUBSYN2YOWH8T";
                                                }else if(carrierName.equals("dtac")){
                                                    thingspeakKey = "NB52MGLUQW2AZ33Q";
                                                }else if(carrierName.equals("TRUE 3G+")){
                                                    thingspeakKey = "9YA8GORIH3NWQZB1";
                                                }
                                            }
                                            //new RequestTask().execute("https://api.thingspeak.com/update?api_key="+thingspeakKey+"&field1="+min+"&field2="+avg+ "&field3="+max+"&field4="+rss);

                                            //// TODO: 11/5/2016 send data

//                                            StringBuilder result = new StringBuilder();
//                                            URL url = null;
//                                            try {
//                                                url = new URL("https://api.thingspeak.com/update?api_key="+thingspeakKey+"&field1="+min+"&field2="+avg+ "&field3="+max+"&field4="+rss);
//                                                System.out.println(url);
//                                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                                                conn.setRequestMethod("GET");
//                                                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                                                String line;
//                                                while ((line = rd.readLine()) != null) {
//                                                    result.append(line);
//                                                }
//                                                rd.close();
//                                            } catch (MalformedURLException e) {
//                                                e.printStackTrace();
//                                            } catch (ProtocolException e) {
//                                                e.printStackTrace();
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }

                                            //// TODO: 9/18/2016 end sending to thingspeak

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        start = false;
                                        try {
                                            outputStream.close();
                                            Thread thread = new Thread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    //// TODO: 9/18/2016 server url
                                                    String strUrlServer = "http://172.31.16.5/FileServer/index.php";
//                                                    String strUrlServer = "http://202.29.148.77/FileServer/index.php";
//                                                    String strUrlServer = "http://192.168.1.16/FileServer/index.php";
                                                    ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                                                    if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
                                                        System.out.println("Wi-Fi");
                                                        strUrlServer = "http://172.31.16.5/FileServer/index.php";
                                                        if(url.getText().equals(R.string.nbtc))
                                                            try {
                                                                mHandler.post(new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        // TODO Auto-generated method stub
                                                                        // Write your code here to update the UI.
                                                                        url.setText(R.string.nbtc_local);
                                                                    }
                                                                });
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                            }
//                                                            url.setText(R.string.nbtc_local);
                                                    }

                                                    //// TODO: 9/18/2016 end server url

                                                    DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
                                                    Date date = new Date();
                                                    String filname = dateFormat.format(date) + ".csv";
                                                    File d = new File(getBaseContext().getFilesDir().getPath());
                                                    File f = new File(d, "test.csv");
                                                    File t = new File(d, filname);
                                                    f.renameTo(t);
                                                    String strSDPath = getBaseContext().getFilesDir().getPath() + "/" + filname;

                                                    //// TODO Upload file to server
                                                    if(Send2Server) {
                                                        String resServer = uploadFiletoServer(strSDPath, strUrlServer);
                                                    }
                                                    //// TODO Finished upload file to server

                                                    String strStatusID = "0";
                                                    String strError = "Unknow Status!";
//
                                                    File externalD = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                                                    if (!externalD.exists()) {
                                                        System.out.println("Created Folder");
                                                        File dir = new File(Environment.getExternalStorageDirectory() + "/Download/");
                                                        dir.mkdirs();
                                                    }
                                                    try {
                                                        copyFile(new File(getBaseContext().getFilesDir().getPath() + "/" + filname), new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + filname));
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            thread.start();
                                            thread.join();
                                            File f = new File(getBaseContext().getFilesDir().getPath() + "/test.csv");
                                            if (!f.exists()) {
                                                System.out.println("File deleted");
                                            } else {
                                                getBaseContext().deleteFile(filename);
                                            }

                                            //// TODO: 11/11/2016 Text to ping
                                            try {
                                                mHandler.post(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        // TODO Auto-generated method stub
                                                        // Write your code here to update the UI.
                                                        error.setText("");
                                                    }
                                                });
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            //// TODO: 11/11/2016 end Text to ping

                                            try{
                                                synchronized (this) {
                                                    wait(WAITING_TIME_MILLISECOND);
                                                }
                                            }catch(InterruptedException ex){
                                                ex.printStackTrace();
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (count < NUMBER_OF_PING) {
                                        //// TODO: 11/11/2016 Text to ping
                                        try {
                                            mHandler.post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    // TODO Auto-generated method stub
                                                    // Write your code here to update the UI.
//                                                    error.setText("");
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        //// TODO: 11/11/2016 end Text to ping

                                        try{
                                            synchronized (this) {
                                                wait(WAITING_TIME_MILLISECOND);
                                            }
                                        }catch(InterruptedException ex){
                                            ex.printStackTrace();
                                        }
                                    }
//                                System.out.println(count);
                                }
                            }
                        }
                    });
                }else {
                    button.setText("START");
                    run = false;
                }
            }
        });
    }

    public String uploadFiletoServer(String strSDPath, String strUrlServer){
        int byteRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1*1024*1024;
        int resCode = 0;
        String resMessage = "";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        try{
            File file = new File(strSDPath);
            if(!file.exists()){
                return "{\"StatusID\":\"0\",\"Error\":\"Please check path on SD Card\"}";
            }
            FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));
            URL url = new URL(strUrlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alice");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);

            //// folder of server

            ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if(manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()){
                outputStream.writeBytes("Content-Disposition: form-data;name=\"filUpload\";filename=\"" + strSDPath + "\"" + lineEnd);
            }else if(manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected()){
                TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String carrierName = telephonyManager.getNetworkOperatorName();
                if(carrierName.equals("AIS")){
                    outputStream.writeBytes("Content-Disposition: form-data;name=\"ais\";filename=\"" + strSDPath + "\"" + lineEnd);
                }else if(carrierName.equals("dtac")){
                    outputStream.writeBytes("Content-Disposition: form-data;name=\"dtac\";filename=\"" + strSDPath + "\"" + lineEnd);
                }else if(carrierName.equals("TRUE 3G+")){
                    outputStream.writeBytes("Content-Disposition: form-data;name=\"true\";filename=\"" + strSDPath + "\"" + lineEnd);
                }
            }

            //// end folder of server

            outputStream.writeBytes(lineEnd);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            byteRead = fileInputStream.read(buffer, 0, bufferSize);
            while (byteRead > 0){
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                byteRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            resCode = conn.getResponseCode();
            if(resCode == HttpURLConnection.HTTP_OK){
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while((read = is.read()) != -1){
                    bos.write(read);
                }
                byte[] result = bos.toByteArray();
                bos.close();
                resMessage = new String(result);
            }
            Log.d("resCode=",Integer.toString(resCode));
            Log.d("resMessage=",resMessage.toString());
            fileInputStream.close();
            outputStream.flush();
            outputStream.close();
            return resMessage.toString();
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public double[] getLatency(String ipAddress){
//        String pingCommand = "/system/bin/ping -s " + PING_PACKET_SIZE + " " + ipAddress;
        String pingCommand = "/system/bin/ping -c " + NUMBER_OF_PACKTETS + " -s " + PING_PACKET_SIZE + " " + ipAddress;
        String inputLine = "";
        double[] Rtt = new double[3];
        Boolean found = true;
        try {
            // execute the command on the environment interface
            Process process = Runtime.getRuntime().exec(pingCommand);
            // gets the input stream to get the output of the executed command
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            inputLine = bufferedReader.readLine();
            while ((inputLine != null)) {
                System.out.println(inputLine);
                if (inputLine.length() > 0 && inputLine.contains("avg")) {  // when we get to the last line of executed ping command
                    found = true;
                    break;
                }else found = false;
                inputLine = bufferedReader.readLine();
            }
        }
        catch (IOException e){
//            Log.v(DEBUG_TAG, "getLatency: EXCEPTION");
            e.printStackTrace();
        }
        // Extracting the average round trip time from the inputLine string
        if(found) {
            try {
                String afterEqual = inputLine.substring(inputLine.indexOf("="), inputLine.length()).trim();
                String strMinRtt = afterEqual.substring(1, afterEqual.indexOf('/'));
                String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1, afterEqual.length()).trim();
                String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
                String afterSecondSlash = afterFirstSlash.substring(afterFirstSlash.indexOf('/') + 1, afterFirstSlash.length()).trim();
                String strMaxRtt = afterSecondSlash.substring(0, afterSecondSlash.indexOf('/'));

                Rtt[0] = Double.valueOf(strMinRtt);
                Rtt[1] = Double.valueOf(strAvgRtt);
                Rtt[2] = Double.valueOf(strMaxRtt);
            }catch (NullPointerException e){
                Rtt = timeout();
            }
        } else{
            Rtt = timeout();
        }
        System.out.println("Min: " + Rtt[0] + ", AVG: " + Rtt[1] + ", Max: " + Rtt[2]);
        return Rtt;
    }

    public double[] timeout(){
        double[] Rtt = new double[3];
        for(int i = 0; i < 3; i++){
            Rtt[i] = 9999.99;
        }
        return Rtt;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    public class myPhoneStateListener extends PhoneStateListener {
        public int signalStrengthValue;
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength() * 2 - 113;
                else
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
            } else {
                signalStrengthValue = signalStrength.getCdmaDbm();
            }
            LTEsignalStrength = signalStrengthValue;
        }
    }

}
