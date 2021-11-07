package com.company.Ypoergasia_2.Concurrent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Ypoergasia_2_Concurrent {

    public static void main(String[] args) throws Exception {
        // csv data as list of lists
        List<String> lines = Ypoergasia_2_Concurrent.simpsonsScriptLines("\\externalFiles\\simpsons_script_lines.csv");
        System.out.println("Loaded " + lines.size() + " lines");
        long startTime;
        int THREADCOUNT; // αριθμος των THREAD
        int batchSize;
        int start;
        int end;
        ConcurrentHashMap<Integer, Integer> episodeWordCount;
        ConcurrentHashMap<String, Integer> locationDialogsCount;
        ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> characterMostUsedWord;

        // for loop για τις 4 περιπτωσεις χρησης Thread, 1,2,4,8
        for (int i = 0; i < 4; i++) {
            episodeWordCount = new ConcurrentHashMap<>();
            locationDialogsCount = new ConcurrentHashMap<>();
            characterMostUsedWord = new ConcurrentHashMap<>();

            // υπολογισμος αριθμου Thread
            THREADCOUNT = (int) Math.pow(2, i);  // 1, 2, 4, 8
            startTime = System.nanoTime();

            //
            batchSize = lines.size() / THREADCOUNT;
            start = 0;
            end = batchSize;
            ProcessThreadConcurrent[] ts = new ProcessThreadConcurrent[THREADCOUNT];
            // inner loop δημιουργια και εκκινηση των Thread
            for (int j = 0; j < THREADCOUNT; j++) {
                // Check whether the last batch should be extended to process the lines left (case of division with remainder).
                if (j == THREADCOUNT - 1 && end < lines.size()) {
                    end = lines.size();
                }
                // δημιουργια του Thread
                ts[j] = new ProcessThreadConcurrent(new ArrayList<>(lines.subList(start, end)), episodeWordCount, locationDialogsCount, characterMostUsedWord);
                // εκκινηση λειτουργιας του Thread
                ts[j].start();

                // επαναπροσδιορισμος οριων λιστας για το επομενο thread
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

            int[] arr = episodeIdWordCount(episodeWordCount);
            System.out.println("Episode ID with most words is " + arr[0] + ". Number of words is " + arr[1]);

            String[] arr2 = locationWithMostDialogs(locationDialogsCount);
            System.out.println("Most dialogs took place at \"" + arr2[0] + "\". Number of dialogs is " + arr2[1]);

            String[][] charartersMostUsedWords = charactersMostUsedWord(characterMostUsedWord);
            for (String[] charA : charartersMostUsedWords) {
                System.out.println(charA[0] + " used the word \"" + charA[1] + "\" " + charA[2] + " times.");
            }

            System.out.println("#" + THREADCOUNT + " Threads total elapsed time = " + (System.nanoTime() - startTime));
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
    // ψαχνει και επιστρεφει το επεισοδιο με το μεγαλυτερο πληθος λεξεων
    private static int[] episodeIdWordCount(ConcurrentHashMap<Integer, Integer> map) {
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

    // ψαχνει και επιστρεφει την τοποθεσια οπου ελαβαν χωρα οι περισσοτερο διαλογοι
    private static String[] locationWithMostDialogs(ConcurrentHashMap<String, Integer> map) {
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

    // ψαχνει και επιστρεφει για καθε χαρακτηρα εκ των 4 ζητουμενων, την πιο χρησιμοποιημενη λεξη τους καθως
    // και τον αριθμο των φορων που την χρησιμοποιησαν
    private static String[][] charactersMostUsedWord(ConcurrentHashMap<Integer, ConcurrentHashMap<String, Integer>> map) throws Exception {
        String[][] results = new String[4][3];
        String[] charNames = new String[]{"Marge", "Homer", "Bart", "Lisa"};
        int charNamesIndex = 0;
        int[] charIDs = {1, 2, 8, 9};

        for (int i : charIDs) {
            String popularWord = "";
            int wordCount = 0;

            if (map.containsKey(i)) {
                ConcurrentHashMap<String, Integer> wordsMap = map.get(i);

                for (String word : wordsMap.keySet()) {
                    if (wordsMap.get(word) >= wordCount) {
                        popularWord = word;
                        wordCount = wordsMap.get(word);
                    }
                }

            } else throw new Exception("Something went wrong BRO in charactersMostUsedWord method!");

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

