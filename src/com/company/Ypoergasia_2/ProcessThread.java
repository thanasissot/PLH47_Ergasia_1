package com.company.Ypoergasia_2;

import java.util.ArrayList;
import java.util.HashMap;

public class ProcessThread extends Thread {
    private final ArrayList<String> lines;
    private final HashMap<Integer, Integer> episodeWordCount = new HashMap<>();
    private final HashMap<String, Integer> locationDialogsCount = new HashMap<>();
    private final HashMap<Integer, HashMap<String, Integer>> characterMostUsedWord = new HashMap<>();

    public ProcessThread(ArrayList<String> lines) {
        this.lines = lines;
        System.out.println(this.getName() + " processing " + lines.size() + " lines");
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

    @Override
    public void run() {
        int episode_id;
        int wordCount;
        int charID;
        String rawLocationText;
        String text;

        for (String inputLine : lines) {
            String[] columns = inputLine.split(",");

            // skip lines with errors
            if (columns.length != 9) {
                continue;
            }

            text = columns[7];
            // skip corrupted lines
            try {
                charID = Integer.parseInt(columns[3]);
                episode_id = Integer.parseInt(columns[1]);
                wordCount = text.split(" ").length;
            } catch (NumberFormatException e) {
                continue;
            }
            rawLocationText = columns[6];

            /*
             * Bart character id = 8
             * Lisa character id = 9
             * Marge character id = 1
             * Homer character id = 2
             */
            // εαν ο χαρακτηρας που εχει το διαλογο ειναι ενας εκ των 4 ζητουμενων κανουμε mapping τις λεξεις
            if (charID == 1 || charID == 2 || charID == 8 || charID == 9) {
                processCharactersText(charID, text);
            }
            processWordCount(episode_id, wordCount);
            processLocationDialogsCount(rawLocationText);
        }
    }

    // ΒΟΗΘΗΤΙΚΕΣ ΜΕΘΟΔΟΙ για το mapping των στοιχειων που θελουμε ανα ζητουμε
    private void processWordCount(int episode_id, int wordCount) {
        episodeWordCount.put(episode_id, episodeWordCount.getOrDefault(episode_id, 0) + wordCount);
    }

    private void processLocationDialogsCount(String rawLocationText) {
        locationDialogsCount.put(rawLocationText, locationDialogsCount.getOrDefault(rawLocationText, 0) + 1);
    }

    private void processCharactersText(int charID, String text) {
        // split text to individual words
        String[] words = text.split(" ");
        // loop through the words Array
        for (String word : words) {
            if (word.length() >= 5) {
                if (!characterMostUsedWord.containsKey(charID)){
                    characterMostUsedWord.put(charID, new HashMap<>());
                }

                HashMap<String, Integer> tempList = characterMostUsedWord.get(charID);
                tempList.put(word, tempList.getOrDefault(word, 0) + 1);
            }
        }
    }

}
