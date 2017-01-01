//package com.example.zenbook.rtttest;
//
//import android.os.SystemClock;
//import android.widget.TextView;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//
///**
// * Created by ZENBOOK on 7/9/2016.
// */
//public class Ping implements Runnable {
//    private static int NUMBER_OF_PACKTETS = 1;
//    private static int PING_PACKET_SIZE = 32;
////    private static int PING_PACKET_SIZE = 56;
//    private static int NUMBER_OF_PING = 24;
//    private static int WAITING_TIME_MILLISECOND = 60000;
//
////    private static int WAITING_TIME_MILLISECOND = 1800000;
//
//    private boolean start = true;
//    private int count = 0;
//    private String url;
//    private ArrayList<Double> avg = new ArrayList<>();
//    private ArrayList<String> dateTime = new ArrayList<>();
//
//    public Ping(String url){
//        this.url = url;
//    }
//
//    @Override
//    public void run() {
//
//        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//
//        while (start) {
//            Date date = new Date();
//
//            count ++;
//            if(count < NUMBER_OF_PING+1) {
//                Double data = round(getLatency(url), 2);
//                avg.add(data);
//                System.out.println("Ping" + count + ": " + avg.get(count-1));
//                dateTime.add(dateFormat.format(date)+"");
//
////                String urlString = Global.Url_Port();
////                urlString += "&rtt="+data;
////                new RequestTask().execute(urlString);
//
//            }else start = false;
//
//            if(count < NUMBER_OF_PING){
//                SystemClock.sleep(WAITING_TIME_MILLISECOND);
//            }
//        }
////        System.out.println("ArrayList size: " + avg.size());
//    }
//
//    public double getLatency(String ipAddress){
////        String pingCommand = "/system/bin/ping -c " + NUMBER_OF_PACKTETS + " " + ipAddress;
//        String pingCommand = "/system/bin/ping -c " + NUMBER_OF_PACKTETS + " -s " + PING_PACKET_SIZE + " " + ipAddress;
////        String pingCommand = "/system/bin/ping" + " -s " + PING_PACKET_SIZE + " " + ipAddress;
//        String inputLine = "";
//        double avgRtt = 0;
//        Boolean found = true;
//
//        try {
//            // execute the command on the environment interface
//            Process process = Runtime.getRuntime().exec(pingCommand);
//            // gets the input stream to get the output of the executed command
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            inputLine = bufferedReader.readLine();
//            while ((inputLine != null)) {
//                System.out.println(inputLine);
//                if (inputLine.length() > 0 && inputLine.contains("avg")) {  // when we get to the last line of executed ping command
//                    found = true;
//                    break;
//                }else found = false;
//                inputLine = bufferedReader.readLine();
//            }
//        }
//        catch (IOException e){
////            Log.v(DEBUG_TAG, "getLatency: EXCEPTION");
//            e.printStackTrace();
//        }
//
//        // Extracting the average round trip time from the inputLine string
//        if(found) {
//            String afterEqual = inputLine.substring(inputLine.indexOf("="), inputLine.length()).trim();
//            String afterFirstSlash = afterEqual.substring(afterEqual.indexOf('/') + 1, afterEqual.length()).trim();
//            String strAvgRtt = afterFirstSlash.substring(0, afterFirstSlash.indexOf('/'));
//            avgRtt = Double.valueOf(strAvgRtt);
//        } else avgRtt = 9999.99;
//        return avgRtt;
//    }
//
//    public static double round(double value, int places) {
//        if (places < 0) throw new IllegalArgumentException();
//
//        long factor = (long) Math.pow(10, places);
//        value = value * factor;
//        long tmp = Math.round(value);
//        return (double) tmp / factor;
//    }
//
//    public ArrayList<Double> getArrayList(){
//        return avg;
//    }
//    public ArrayList<String> getDateTime() { return dateTime; }
//}
