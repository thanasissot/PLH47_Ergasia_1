package com.company.Ypoergasia_3;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class Ypoergasia_3 {
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
        // for loop για τις 4 περιπτωσεις χρησης Thread, 1,2,4,8
        for (int i = 0; i < 4; i++) {
            // υπολογισμος αριθμου Thread
            THREADCOUNT = (int) Math.pow(2, i);  // 1, 2, 4, 8
            RequestsThread[] ts = new RequestsThread[THREADCOUNT];
            startTime = System.nanoTime();

            for (int j = 0; j < THREADCOUNT; j++){
                ts[j] = new RequestsThread(k, urlString);
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

            // combine all results to one
            int sum = 0;
            int count = 0;
            HashMap<Character, Integer> characterStringHashMap = new HashMap<>();
            for (RequestsThread rts : ts) {
                // sum all integers from the Threads returned Lists
                // while counting number of entries so we can calculate the avg length of all
                // words we received from the API
                List<Integer> tempList = rts.getLengths();
                count += tempList.size();
                sum += tempList.stream().reduce(0, Integer::sum);
                // sum all the mappings from the Threads in a whole one so we can provide percentage
                HashMap<Character, Integer> tempMap = rts.getCharacterMap();
                for (char character : tempMap.keySet()) {
                    if (characterStringHashMap.containsKey(character)){
                        characterStringHashMap.put(character, characterStringHashMap.get(character) + tempMap.get(character));
                    }
                    else {
                        characterStringHashMap.put(character, tempMap.get(character));
                    }

                }
            }
            System.out.println("Total time for " + THREADCOUNT + " number of Threads used is " + (System.nanoTime() - startTime));

            // souting avg length
            System.out.println("Average word length of " + "k calls by " + THREADCOUNT + " Threads, is " + sum/(double)count);

            // appearance percentage of each character is the the division of characters appearance by the total number of all characters
            System.out.println("Percentage appearance of each character:");
            // using a SortedSet to sort Character Keys for printing in correct order
            SortedSet<Character> keys = new TreeSet<>(characterStringHashMap.keySet());
            for (char character : keys) {
                System.out.println(character + " = " + df.format(100*(characterStringHashMap.get(character) /(double) sum)) + "%");
            }

        }
    }
}
