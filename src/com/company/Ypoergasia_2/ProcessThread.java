package com.company.Ypoergasia_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessThread extends Thread {
    private final ArrayList<String> lines;
    private final HashMap<Integer, Integer> episodeWordCount = new HashMap<>();
    private final HashMap<String, Integer> locationDialogsCount = new HashMap<>();
    private final HashMap<Integer, HashMap<String, Integer>> characterMostUsedWord = new HashMap<>();
    private int linesProcessed = 0;

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

            // skip corrupted lines
            try {
                charID = Integer.parseInt(columns[3]);
                episode_id = Integer.parseInt(columns[1]);
                wordCount = Integer.parseInt(columns[8]);
            } catch (NumberFormatException e) {
//                System.out.println("ParseInt failed on line " + lines.indexOf(list));
                continue;
            }
            linesProcessed++;

            rawLocationText = columns[6];
            text = columns[7];

            processWordCount(episode_id, wordCount);
            processLocationDialogsCount(rawLocationText);

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
        System.out.println(this.getName() + " processed = " + linesProcessed + " lines.");
    }

    private void processWordCount(int episode_id, int wordCount) {
        if (episodeWordCount.containsKey(episode_id)) {
            if (episodeWordCount.get(episode_id) < wordCount) {
                episodeWordCount.put(episode_id, wordCount);
            }
        } else {
            episodeWordCount.put(episode_id, wordCount);
        }
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
                if (tempList.containsKey(word)) {
                    tempList.put(word, tempList.get(word) + 1);
                } else {
                    tempList.put(word, 1);
                }
            }
        }
    }

}