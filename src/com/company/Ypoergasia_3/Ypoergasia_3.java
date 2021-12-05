package com.company.Ypoergasia_3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ypoergasia_3 {
    private static final DecimalFormat df = new DecimalFormat("0.0000");
    // formats time string, uesd when printing to console
    static DecimalFormat formatter = new DecimalFormat("#,###");

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
        // for loop for each number of thread use case: Thread's 1,2,4,8
        for (int i = 0; i < 4; i++) {
            // calculate Thread Number
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
            System.out.println("Total time for " + THREADCOUNT + " number of Threads used is " + formatter.format(System.nanoTime() - startTime) + " nanoseconds");

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

    public static class RequestsThread extends Thread{
        private final HashMap<Character, Integer> characterMap = new HashMap<>();
        private final List<Integer> lengths =  new ArrayList<>();
        private final int apiCallsCount;
        private final URL url;

        public HashMap<Character, Integer> getCharacterMap() {
            return characterMap;
        }

        public List<Integer> getLengths() {
            return lengths;
        }

        public RequestsThread(int apiCallsCount, String url) throws MalformedURLException {
            this.apiCallsCount = apiCallsCount;
            this.url = new URL(url);
        }

        @Override
        public void run() {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader input;
            String inputline;
            String word;

            // K (= apiCallsCount) requests sent
            for (int i = 0; i < apiCallsCount; i++){
                // get String from Response body, append it to StringBuilder
                try {
                    // creating Connection
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    // set Https Request Method to GET
                    con.setRequestMethod("GET");
                    // if request was successfull response code is 200
                    if (con.getResponseCode() == 200){
                        // get the input stream and append it line by line to StringBuilder
                        input = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        while ((inputline = input.readLine()) != null) {
                            stringBuilder.append(inputline);
                        }
                        // close BufferedReader
                        input.close();
                    }
                    // ELSE null, wrong response code is like an empty text, so do nothing
                }
                catch (Exception e){
                    continue;
                }

                // use the StringBuilder to extract all words, while adding the lengts of each word
                // in an List<Integer> to calculate avg, and also mapping each word to characters
                // using Patter and Matcher, Regex
                Pattern pattern = Pattern.compile("(\\w+)");
                // getting all words from StringBuilder by matching the above Pattern
                Matcher matcher = pattern.matcher(stringBuilder);
                // for every word in Response
                while (matcher.find()){
                    word = matcher.group().toLowerCase();
                    // store word length
                    lengths.add(word.length());
                    // map words characters
                    for (char character : word.toCharArray()){
                        characterMap.put(character, characterMap.getOrDefault(character, 0) + 1);
                    }
                }
            }
        }
    }
}