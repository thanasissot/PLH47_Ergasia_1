package com.company.THREADS.Concurrent_Synchronized_3;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Ypoergasia_3_v2 {
    private static final DecimalFormat df = new DecimalFormat("0.0000");

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
        String urlString; // API url to sent HttpRequests
        int k; // number of calls per thread
        // default values if no arguments passed during call
        if (args.length != 0){
            urlString = args[0];
            k = Integer.parseInt(args[1]);
        }
        else {
            urlString = "https://loripsum.net/api/10/plaintext";
            k = 100;
        }

        int THREADCOUNT;
        long startTime;
        List<Integer> lengths;
        ConcurrentHashMap<Character, Integer> characterIntegerConcurrentHashMap;
        // for loop για τις 4 περιπτωσεις χρησης Thread, 1,2,4,8
        for (int i = 0; i < 4; i++) {
            lengths =  Collections.synchronizedList(new ArrayList<>());
            characterIntegerConcurrentHashMap = new ConcurrentHashMap<>();
            // υπολογισμος αριθμου Thread
            THREADCOUNT = (int) Math.pow(2, i);  // 1, 2, 4, 8
            RequestsThread_v2[] ts = new RequestsThread_v2[THREADCOUNT];
            startTime = System.nanoTime();

            for (int j = 0; j < THREADCOUNT; j++){
                ts[j] = new RequestsThread_v2(characterIntegerConcurrentHashMap, lengths, k, urlString);
                ts[j].start();
            }

            // wait for all threads to finish
            for (int j = 0; j < THREADCOUNT; j++) {
                try {
                    ts[j].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            int sum = lengths.stream().reduce(0, Integer::sum);
            int count = lengths.size();

            System.out.println("Total time for " + THREADCOUNT + " number of Threads used is " + (System.nanoTime() - startTime));

            // souting avg length
            System.out.println("Average word length of " + "k calls by " + THREADCOUNT + " Threads, is " + sum/(double)count);

            // appearance percentage of each character is the the division of characters appearance by the total number of all characters
            System.out.println("Percentage appearance of each character:");
            SortedSet<Character> keys = new TreeSet<>(characterIntegerConcurrentHashMap.keySet());
            for (char character : keys) {
                System.out.println(character + " = " + df.format(100*(characterIntegerConcurrentHashMap.get(character) /(double) sum)) + "%");
            }

        }
    }
}
