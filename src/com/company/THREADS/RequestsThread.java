package com.company.THREADS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestsThread extends Thread{
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
