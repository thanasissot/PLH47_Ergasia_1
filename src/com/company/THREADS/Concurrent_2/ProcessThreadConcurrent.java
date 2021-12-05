package com.company.THREADS.Concurrent_2;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ProcessThreadConcurrent extends Thread {
    private final ArrayList<String> lines;
    private final ConcurrentHashMap<Integer, Integer> episodeWordCount;
    private final ConcurrentHashMap<String, Integer> locationDialogsCount;
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> characterMostUsedWord;
    private int linesProcessed = 0;

    public ProcessThreadConcurrent(ArrayList<String> lines, ConcurrentHashMap<Integer, Integer> episodeWordCount, ConcurrentHashMap<String, Integer> locationDialogsCount, ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> characterMostUsedWord) {
        this.lines = lines;
        this.episodeWordCount = episodeWordCount;
        this.locationDialogsCount = locationDialogsCount;
        this.characterMostUsedWord = characterMostUsedWord;
        System.out.println(this.getName() + " processing " + lines.size() + " lines");
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
        episodeWordCount.merge(episode_id, wordCount, Integer::sum);
    }

    private void processLocationDialogsCount(String rawLocationText) {
        locationDialogsCount.merge(rawLocationText, 1, Integer::sum);
    }

    private void processCharactersText(int charID, String text) {
        // split text to individual words
        String[] words = text.split(" ");
        // loop through the words Array
        for (String word : words) {
            if (word.length() >= 5) {
                if (!characterMostUsedWord.containsKey(charID)){
                    characterMostUsedWord.put(charID, new ConcurrentHashMap<>());
                }

                ConcurrentHashMap<String, Integer> tempList = characterMostUsedWord.get(charID);
                tempList.merge(word, 1, Integer::sum);
            }
        }
    }

}
