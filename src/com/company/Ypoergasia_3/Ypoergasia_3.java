package com.company.Ypoergasia_3;

import java.net.MalformedURLException;
import java.net.URL;

public class Ypoergasia_3 {
    /**
     * args[0] = API url to sent GET REQUEST
     * args[1] = number of requests to send per Thread
     * args[2] = number of Threads to use
     *
     * default settings when no arguments passed to call
     * URL = https://loripsum.net/api/10/plaintext
     * K = 5
     * THREADSCOUNT = 8
     */
    public static void main(String[] args) throws Exception {
        if (!(args.length == 0 || args.length == 3)) throw new Exception("0 or 3 arguments needed!");

        String urlString;
        int k;
        int THREADCOUNT;

        if (args.length != 0){
            urlString = args[0];
            k = Integer.parseInt(args[1]);
            THREADCOUNT = Integer.parseInt(args[2]);
        }
        else {
            urlString = "https://loripsum.net/api/10/plaintext";
            k = 5;
            THREADCOUNT = 8;
        }
        URL url = new URL(urlString);

    }
}
