package com.example.zenbook.rtttest;


/**
 * Created by Fafaform on 8/27/15 AD.
 */
public class Global {

    private static String URlmango = "nbtc.ee.psu.ac.th";
    private static String port = "8080";

    public static String Url_Port() {
        String url_port = "http://" + URlmango + ":" + port + "/httpds?__device=User01";
        return url_port;
    }
}
