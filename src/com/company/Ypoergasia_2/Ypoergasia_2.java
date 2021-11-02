package com.company.Ypoergasia_2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class Ypoergasia_2 {

    public static void main(String[] args) throws Exception {
        //parsing a CSV file into Scanner class constructor

        // csv data as list of lists
        List<List<String>> lines = Ypoergasia_2.simpsonsScriptLines("\\externalFiles\\simpsons_script_lines.csv");
        System.out.println("Loaded " + lines.size() + " lines");

        int THREADCOUNT; // αριθμος των THREAD
        int batchSize;
        int start;
        int end;

        // for loop για τις 4 περιπτωσεις χρησης Thread, 1,2,4,8
        for (int i = 0; i < 4; i++) {
            THREADCOUNT = (int) Math.pow(2,i);  // 1, 2, 4, 8

            batchSize = lines.size() / THREADCOUNT;
            start = 0;
            end = batchSize;
            ProcessThread[] ts = new ProcessThread[THREADCOUNT];
            // inner loop δημιουργια και εκκινηση των Thread
            for (int j = 0; j < THREADCOUNT; j++){
                // Check whether the last batch should be extended to process the lines left (case of division with remainder).
                if (j == THREADCOUNT - 1 && end < lines.size()){
                    end = lines.size();
                }
                // δημιουργια του Thread
                ts[j] = new ProcessThread(lines.subList(start, end));
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
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }



        }


        // OΝΕ Thread
        ProcessThread thread = new ProcessThread(lines);
        thread.start();
        thread.join();

        int[] arr = episodeIdWordCount(thread.getEpisodeWordCount());
        System.out.println("Episode ID with most words is " + arr[0] + ". Number of words is " + arr[1]);

        String[] arr2 = locationWithMostDialogs(thread.getLocationDialogsCount());
        System.out.println("Most dialogs took place at \"" + arr2[0] + "\". Number of dialogs is " + arr2[1]);

        String[][] charartersMostUsedWords = charactersMostUsedWord(thread.getCharacterMostUsedWord());
        for (String[] charA : charartersMostUsedWords) {
            System.out.println(charA[0] + " used the word \"" + charA[1] +"\" " + charA[2] + " times.");
        }




    }

    // relativePathToCSVFile including .csv e.g. fileFullPath = System.getProperty("user.dir") + "\\externalFiles\\simpsons_script_lines.csv";
    static List<List<String>> simpsonsScriptLines (String relativePathToCSVFile){
        String fileFullPath = System.getProperty("user.dir") + relativePathToCSVFile;
        List<List<String>> simpsonsScriptLineslist = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileFullPath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                simpsonsScriptLineslist.add(Arrays.asList(values));
            }
        }
        catch (Exception e){
            System.err.println(e.getMessage());
        }

        return simpsonsScriptLineslist;
    }

    // helper methods
    // ψαχνει και επιστρεφει το επεισοδιο με το μεγαλυτερο πληθος λεξεων
    private static int[] episodeIdWordCount (HashMap<Integer, Integer> map) {
        int episodeID = -1;
        int max = 0;
        for (int key : map.keySet()){
            if (map.get(key) >= max) {
                episodeID = key;
                max = map.get(key);
            }
        }

        return new int[]{episodeID, max};
    }

    // ψαχνει και επιστρεφει την τοποθεσια οπου ελαβαν χωρα οι περισσοτερο διαλογοι
    private static String[] locationWithMostDialogs (HashMap<String, Integer> map) {
        String location = "";
        int max = 0;

        for (String locat : map.keySet()) {
            if (map.get(locat) >= max) {
                location = locat;
                max = map.get(locat);
            }
        }

        return new String[]{location, String.valueOf(max)};
    }

    private static String[][] charactersMostUsedWord (HashMap<Integer, HashMap<String, Integer>> map) throws Exception {
        String[][] results = new String[4][3];
        String[] charNames = new String[]{"Marge", "Homer", "Bart", "Lisa"};
        int charNamesIndex = 0;
        int[] charIDs = {1, 2, 8, 9};

        for (int i : charIDs) {
            String popularWord = "";
            int wordCount = 0;

            if (map.containsKey(i)) {
                HashMap<String, Integer> wordsMap = map.get(i);

                for (String word : wordsMap.keySet()){
                    if (wordsMap.get(word) >= wordCount) {
                        popularWord = word;
                        wordCount = wordsMap.get(word);
                    }
                }

            }
            else throw new Exception("Something went wrong BRO in charactersMostUsedWord method!");

            // character name
            results[charNamesIndex][0] = charNames[charNamesIndex];
            // most used word
            results[charNamesIndex][1] = popularWord;
            // times word used and increment indexing
            results[charNamesIndex++][2] = String.valueOf(wordCount);
        }

        return results;
    }



    // ONE thread implementtion
    private static void singleThread (List<List<String>> lines) {
        ProcessThread thread = new ProcessThread(lines);

    }

}

