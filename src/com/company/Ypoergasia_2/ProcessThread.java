package com.company.Ypoergasia_2;

import java.util.HashMap;
import java.util.List;

public class ProcessThread extends Thread {
    private final List<List<String>> lines;
    private final HashMap<Integer, Integer> episodeWordCount = new HashMap<>();
    private final HashMap<String, Integer> locationDialogsCount = new HashMap<>();
    private final HashMap<Integer, HashMap<String, Integer>> characterMostUsedWord = new HashMap<>();
    private static final int[] charIDs = {1, 2, 8, 9};

    public static int[] getCharIDs() {
        return charIDs;
    }

    public HashMap<Integer, Integer> getEpisodeWordCount() {
        return episodeWordCount;
    }

    public HashMap<String, Integer> getLocationDialogsCount() {
        return locationDialogsCount;
    }

    public HashMap<Integer, HashMap<String, Integer>> getCharacterMostUsedWord() {
        return characterMostUsedWord;
    }

    public ProcessThread(List<List<String>> lines) {
        this.lines = lines;
        System.out.println(this.getName() + " processing " + lines.size() + " lines");
    }

    @Override
    public void run() {
        int episode_id;
        int wordCount;
        int charID;
        String raw_location_text;
        String text;

        // αρχικοποιηση των HashMap μεσα στο characterMostUsedWord
        createInnerListsOfcharacterMostUsedWord();

        for(List<String> list : lines) {
            // skip lines with errors
            if (list.size() != 9) continue;

            // skip corrupted lines
            try {
                charID = Integer.parseInt(list.get(3));
                episode_id = Integer.parseInt(list.get(1));
                wordCount = Integer.parseInt(list.get(8));
            }
            catch (NumberFormatException e) {
//                System.out.println("ParseInt failed on line " + lines.indexOf(list));
                continue;
            }

            raw_location_text = list.get(6);
            text = list.get(7);

            processWordCount(episode_id, wordCount);
            processLocationDialogsCount(raw_location_text);

            /*
             * Bart character id = 8
             * Lisa character id = 9
             * Marge character id = 1
             * Homer character id = 2
             */
            if (charID == 1 || charID == 2 || charID == 8 || charID == 9) {
                processCharactersText(charID, text);
            }

        }
    }

    private void processWordCount (int episode_id, int wordCount) {
        if (episodeWordCount.containsKey(episode_id)){
            if (episodeWordCount.get(episode_id) < wordCount){
                episodeWordCount.put(episode_id, wordCount);
            }
        }
        else {
            episodeWordCount.put(episode_id, wordCount);
        }
    }

    private void processLocationDialogsCount (String raw_location_text) {
        if (locationDialogsCount.containsKey(raw_location_text)){
            locationDialogsCount.put(raw_location_text, locationDialogsCount.get(raw_location_text) + 1);
        }
        else {
            locationDialogsCount.put(raw_location_text, 1);
        }
    }

    // αρχικοποιηση των HashMap, μας ενδιαφερουν μονο 4 ID, Homer, Marge, Bart, Lisa
    // εξοικονουμε ενα if ελεγχο σε καθε κληση της processCharactersText
    private void createInnerListsOfcharacterMostUsedWord () {
        for (int i : charIDs){
            characterMostUsedWord.put(i, new HashMap<>());
        }
    }

    private void processCharactersText (int charID, String text){
        // split text to individual words
        String[] words = text.split(" ");
        // loop through the words Array
        for (String word : words) {
            if (word.length() >= 5) {
                HashMap<String, Integer> tempList = characterMostUsedWord.get(charID);
                if (tempList.containsKey(word)) {
                    tempList.put(word, tempList.get(word) + 1);
                }
                else {
                    tempList.put(word, 1);
                }
            }
        }
    }

}
