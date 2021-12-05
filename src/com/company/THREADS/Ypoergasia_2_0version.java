package com.company.THREADS;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Ypoergasia_2_0version {

    public static void main(String[] args) throws Exception {
        // csv data as list of lists
        String relativePath;
        if (args.length != 0){
            relativePath = args[0];
        }else {
            relativePath = "\\externalFiles\\simpsons_script_lines.csv";
        }
        List<String> lines = Ypoergasia_2_0version.simpsonsScriptLines(relativePath);
        if (lines.size() != 0) {
            System.out.println("Loaded " + lines.size() + " lines\n");
        }
        else {
            System.out.println("No file loaded. End!");
            System.exit(-1);
        }
        // variables
        long startTime;
        int THREADCOUNT; // number of THREADs
        int batchSize;
        int start;
        int end;

        // for loop for each number of thread use case: Thread's 1,2,4,8
        for (int i = 0; i < 4; i++) {
            // calculate number of threads
            THREADCOUNT = (int) Math.pow(2, i);  // 1, 2, 4, 8
            startTime = System.nanoTime();

            //
            batchSize = lines.size() / THREADCOUNT;
            start = 0;
            end = batchSize;
            ProcessThread[] ts = new ProcessThread[THREADCOUNT];
            // inner loop create and start thread
            for (int j = 0; j < THREADCOUNT; j++) {
                // Check whether the last batch should be extended to process the lines left (case of division with remainder).
                if (j == THREADCOUNT - 1 && end < lines.size()) {
                    end = lines.size();
                }
                // create Thread
                ts[j] = new ProcessThread(new ArrayList<>(lines.subList(start, end)));
                // start Thread
                ts[j].start();

                // redefine list start and end indexes for next thread calls
                start = end;
                end += batchSize;
            }

            // wait for all threads to finish
            for (int j = 0; j < THREADCOUNT; j++) {
                try {
                    ts[j].join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final HashMap<Integer, Integer> episodeWordCount = new HashMap<>();
            final HashMap<String, Integer>locationDialogsCount = new HashMap<>();
            final HashMap<Integer, HashMap<String, Integer>> characterMostUsedWord = new HashMap<>();
            // merge different thread results
            for (ProcessThread thread : ts){
                //
                thread.getEpisodeWordCount().forEach((episodeID, wordCount) -> {
                    episodeWordCount.put(episodeID, episodeWordCount.getOrDefault(episodeID, 0) + wordCount);
                });
                //
                thread.getLocationDialogsCount().forEach((rawLocationText, dialogsCount) -> {
                    locationDialogsCount.put(rawLocationText, locationDialogsCount.getOrDefault(rawLocationText, 0) + dialogsCount);
                });
                //
                thread.getCharacterMostUsedWord().forEach((charID, map) -> {
                    if (!characterMostUsedWord.containsKey(charID)){
                        characterMostUsedWord.put(charID, new HashMap<>());
                    }
                    map.forEach((word, count) -> {
                        characterMostUsedWord.get(charID).put(word, characterMostUsedWord.get(charID).getOrDefault(word, 0) + count);
                    });
                });
            }

            int[] arr = episodeIdWordCount(episodeWordCount);
            System.out.println("Episode ID with most words is " + arr[0] + ". Number of words is " + arr[1]);

            String[] arr2 = locationWithMostDialogs(locationDialogsCount);
            System.out.println("Most dialogs took place at \"" + arr2[0] + "\". Number of dialogs is " + arr2[1]);

            String[][] charartersMostUsedWords = charactersMostUsedWord(characterMostUsedWord);
            for (String[] charA : charartersMostUsedWords) {
                System.out.println(charA[0] + " used the word \"" + charA[1] + "\" " + charA[2] + " times.");
            }

            System.out.println("#" + THREADCOUNT + " Threads Total Run time = " + (System.nanoTime() - startTime));
            System.out.println();
        }

    }

    // relativePathToCSVFile including .csv e.g. fileFullPath = System.getProperty("user.dir") + "\\externalFiles\\simpsons_script_lines.csv";
    static ArrayList<String> simpsonsScriptLines(String relativePathToCSVFile) {
        String fileFullPath = System.getProperty("user.dir") + relativePathToCSVFile;
        ArrayList<String> simpsonsScriptLineslist = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileFullPath));
            String line;
            while ((line = br.readLine()) != null) {
                simpsonsScriptLineslist.add(line);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        return simpsonsScriptLineslist;
    }

    // helper methods
    // returns episode with most words
    private static int[] episodeIdWordCount(HashMap<Integer, Integer> map) {
        int episodeID = -1;
        int max = 0;
        for (int key : map.keySet()) {
            if (map.get(key) >= max) {
                episodeID = key;
                max = map.get(key);
            }
        }

        return new int[]{episodeID, max};
    }

    // returns location of most dialogs took place
    private static String[] locationWithMostDialogs(HashMap<String, Integer> map) {
        String rawLocationText = "";
        int max = 0;

        for (String locat : map.keySet()) {
            if (map.get(locat) >= max) {
                rawLocationText = locat;
                max = map.get(locat);
            }
        }

        return new String[]{rawLocationText, String.valueOf(max)};
    }

    // for each character which is "Marge", "Homer", "Bart", "Lisa", returns their most frequent used word
    private static String[][] charactersMostUsedWord(HashMap<Integer, HashMap<String, Integer>> map) throws Exception {
        String[][] results = new String[4][3];
        String[] charNames = new String[]{"Marge", "Homer", "Bart", "Lisa"};
        int charNamesIndex = 0;
        int[] charIDs = {1, 2, 8, 9};

        for (int i : charIDs) {
            String popularWord = "";
            int wordCount = 0;

            if (map.containsKey(i)) {
                HashMap<String, Integer> wordsMap = map.get(i);

                for (String word : wordsMap.keySet()) {
                    if (wordsMap.get(word) >= wordCount) {
                        popularWord = word;
                        wordCount = wordsMap.get(word);
                    }
                }

            } else throw new Exception("Something went wrong in charactersMostUsedWord method!");

            // character name
            results[charNamesIndex][0] = charNames[charNamesIndex];
            // most used word
            results[charNamesIndex][1] = popularWord;
            // times word used and increment indexing
            results[charNamesIndex++][2] = String.valueOf(wordCount);
        }

        return results;
    }
}

