package com.company.Ypoergasia_1;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Ypoergasia_1 {
    //  ΠΑΡΑΔΟΧΗ  n, m, k δυναμεις του 2, (ΟΔΗΓΙΑ ΣΕΠ => δεν χρειαζεται αμυντικος προγραμματισμος)
    public static void main(String[] args) throws Exception {
        int[][] matrix;
        int[] vector;
        int n , m;

        if (args.length == 0){
            // εαν δεν υπαρχουν εισερχομενα δεδομενα δινουμε μια τυχαια δυναμη του 2 απο [12,14]
            // στις διαστασει του πινακα και του διανυσματος
            Random rand = new Random();
            n = rand.nextInt(3) + 12;
            m = rand.nextInt(3) + 12;

        }
        else {
            // αλλιως οτι τιμες εισαγει στο προγραμμα ο χρηστης
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
        }

        // δημιουργια του τυχαιου πινακα και διανυσματος
        vector = Ypoergasia_1.getVector((int) Math.pow(2, m));
        matrix = Ypoergasia_1.getMatrix((int) Math.pow(2, n), (int) Math.pow(2, m));

        // αποθηκευση τιμων χρονου
        long[] times = new long[8];

        // υπολογισμος με 1 THREAD
        times[0] = System.nanoTime();
        int[] result1 = oneThreadMult(matrix, vector, 0, matrix.length);
        times[1] = System.nanoTime();

        // υπολογισμος με 2 THREAD
        times[2] = System.nanoTime();
        int[] result2 = twoThreadMult(matrix, vector, 0, matrix.length);
        times[3] = System.nanoTime();

        // υπολογισμος με 3 THREAD
        times[4] = System.nanoTime();
        int[] result3 = fourThreadMult(matrix, vector, 0, matrix.length);
        times[5] = System.nanoTime();

        // υπολογισμος με 4 THREAD
        times[6] = System.nanoTime();
        int[] result4 = eightThreadMult(matrix, vector, 0, matrix.length);
        times[7] = System.nanoTime();

        // ελεγχος ισοτητας αποτελεσματων
        if (Ypoergasia_1.equalityCheck(result1, result2, result3, result4)) {
            for (int i = 0; i < times.length; i+=2) {
                System.out.println("Total Execution time with " + (int) Math.pow(2,i/2) + " Threads = "  + (times[i + 1] - times[i]));
            }
            System.out.println("Matrix size = " + matrix.length + ", " + matrix[0].length);
            System.out.println("Vector size = " + vector.length);
        }
        else {
            System.out.println("Καποια μεθοδος δεν λειτουργει σωστα, δεν επιστρεφει σωστο αποτελεσμα του πολλαπλασιασμου!");
            print1D(result1);
            print1D(result2);
            print1D(result3);
            print1D(result4);
        }

    }

    // χρηση ενος THREAD για υπολογισμο ολοκληρου του πινακα
    private static int[] oneThreadMult(int[][] matrix, int[] vector, int startRow, int endRow) throws InterruptedException {
        RowByVectorMultiplicationThread thread1 = new RowByVectorMultiplicationThread("Thread_1", matrix, vector, startRow, endRow);
        // ξεκιναει η εκτελεση του thread
        thread1.start();
        // περιμενουμε το Thread να "πεθανει"/τελειωσει την εκτελεση του
        thread1.join();
        // επιστρεφει το αποτελεσμα
        return thread1.getResultVector();
    }

    /*
    * χρηση 2 THREAD για υπολογισμο του πινακα
    * o πινακας χωριζεται σε 2 μερη και καθε Thread υπολογιζει το κομματι που του αναλογει
    * η υλοποιηση ειναι ακριβως ιδια με το 1 Thread, δεν ακολουθει DRY Principle
    * η περιπτωση των 2 Thread ειναι πολυ απλη για να δημιουργησουμε γενικη περιπτωση
    * */
    private static int[] twoThreadMult(int[][] matrix, int[] vector, int startRow, int endRow) throws Exception {
        RowByVectorMultiplicationThread thread1 = new RowByVectorMultiplicationThread("Thread_1", matrix, vector, startRow, endRow / 2);
        RowByVectorMultiplicationThread thread2 = new RowByVectorMultiplicationThread("Thread_1", matrix, vector, (endRow / 2), endRow);

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        int[] arr1 = thread1.getResultVector();
        int[] arr2 = thread2.getResultVector();

        // ενωση των 2 πινακων/μερων του αποτελεσματος και επιστροφη
        return Ypoergasia_1.arrayConcat(arr1, arr2);
    }

    // μεθοδος που δημιουργει 4 Thread και υπολογιζει τις σειρες του πινακα απο StartRow εως EndRow
    /* generic μεθοδος, χρησιμοποιειται και απο την eightThreadMult μεθοδο */
    private static int[] fourThreadMult(int[][] matrix, int[] vector, int startRow, int endRow) throws Exception {
        // συνολικο μηκος γραμμων που θα υπολογισει
        int len = endRow - startRow;
        // δηλωση και αρχικοποιηση του πινακα/διανυσμα που θα επιστραφει
        int[] result = new int[len];
        // πινακας 4 Thread
        RowByVectorMultiplicationThread[] ts = new RowByVectorMultiplicationThread[4];

        // start, end = αρχη και τελος καθε τμηματος του πινακα που θα ανατεθει σε Thread για τον υπολογισμο
        int start = startRow;
        int end = (startRow + (endRow - startRow + 1)/4);
        for (int i = 0; i < 4; i++) {
            // δημιουργια του Thread
            ts[i] = new RowByVectorMultiplicationThread("Thread" + i, matrix, vector, start, end);
            // επαναπροσδιορισμος των start, end
            start = end;
            end += (endRow - startRow + 1)/4;
            // εκκινηση λειτουργιας του Thread
            ts[i].start();
        }

        // wait for all threads to finish
        for (int i = 0; i < 4; i++) {
            ts[i].join();
        }

        // concat all resutls into 1
        int[] arr1, arr2, arr3, arr4;
        arr1 = ts[0].getResultVector();
        arr2 = ts[1].getResultVector();
        arr3 = ts[2].getResultVector();
        arr4 = ts[3].getResultVector();

        return Ypoergasia_1.arrayConcat(Ypoergasia_1.arrayConcat(arr1, arr2), Ypoergasia_1.arrayConcat(arr3, arr4));
    }

    // χρηση 8 Thread για τον υπολογισμο, χωριζει τον πινακα σε 2 μισα και χρησιμοποιει την fourThreadMult μεθοδο για τον υπολογισμο
    private static int[] eightThreadMult(int[][] matrix, int[] vector, int startRow, int endRow) throws Exception {
        int[] arr1 = fourThreadMult(matrix, vector, startRow, endRow / 2);
        int[] arr2 = fourThreadMult(matrix, vector, (endRow / 2), endRow);

        // ενωση των 2 πινακων/μερων του αποτελεσματος και επιστροφη
        return Ypoergasia_1.arrayConcat(arr1, arr2);
    }

    // HELPER METHODS
    // int[][], n * m matrix filled with random 0-10 integers (inclusive - inclusive)
    private static int[][] getMatrix(int n, int m) {
        Random rand = new Random();
        // n, m must be powers of 2,
        int[][] matrix = new int[n][m];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                // assign pseudo random integer in range [0,10]
                matrix[i][j] = rand.nextInt(11);
            }
        }
        return matrix;
    }

    // int[], m vector filled with random 0-10 integers (inclusive - inclusive)
    private static int[] getVector(int m) {
        Random rand = new Random();
        int[] vector = new int[m];

        for (int i = 0; i < m; i++) {
            // assign pseudo random integer in range [0,10]
            vector[i] = rand.nextInt(11);
        }
        return vector;
    }

    // print 1D Matrix
    public static void print1D(int[] vector) {
        System.out.println("START");
        for (int i : vector) {
            System.out.print(i + " ");
        }
        System.out.println("\nEND");
    }

    // print 2D Matrix
    // source : https://www.geeksforgeeks.org/print-2-d-array-matrix-java/
    public static void print2D(int[][] mat) {
        // Loop through all rows
        for (int i = 0; i < mat.length; i++)
            // Loop through all elements of current row
            for (int j = 0; j < mat[i].length; j++)
                System.out.print(mat[i][j] + " ");
    }

    // ελεγχει αν οι 4 int arrays περιεχουν ακριβως τα ιδια στοιχεια, με την ιδια σειρα και αρα ειναι ισες!
    public static boolean equalityCheck (int[] arr1, int[] arr2, int[] arr3, int[] arr4) {
        return Arrays.equals(arr1, arr2) && Arrays.equals(arr1, arr3) && Arrays.equals(arr1, arr4);
    }

    // ενωνει 2 πινακες και επιστρεφει 1,
    public static int[] arrayConcat(int[] arr1, int[] arr2) throws Exception {
        if (arr1.length != arr2.length) throw new Exception("Needs same length Arrays");

        int[] arr = new int[arr1.length * 2];
        // ενωση των 2 πινακων/μερων του αποτελεσματος και επιστροφη
        for (int i = 0; i < arr1.length; i++) {
            arr[i] = arr1[i];
            arr[arr1.length + i] = arr2[i];
        }

        return arr;
    }

}
