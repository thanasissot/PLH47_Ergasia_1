package com.company.Ypoergasia_1;

import java.util.Random;

public class Ypoergasia_1 {
    //  ΠΑΡΑΔΟΧΗ  n, m, k δυναμεις του 2, (ΟΔΗΓΙΑ ΣΕΠ => δεν χρειαζεται αμυντικος προγραμματισμος)
    public static void main(String[] args) throws Exception {
        int[][] matrix;
        int[] vector;
        int n , m;
        int[] result;

        if (args.length == 0){
            // εαν δεν υπαρχουν εισερχομενα δεδομενα δινουμε μια τυχαια δυναμη του 2 απο [12,14]
            // στις διαστασεις του πινακα και του διανυσματος
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
        // βοηθητικες μεταβλητες
        int[][] arrs; // κραταει τα αποτελεσματα απο καθε Thread και χρησιμοποιειται για τη συνενωση τους
        int THREADCOUNT; // αριθμος των THREAD
        // καθε Thread υπολογιζει απο lo εως hi στοιχειο του συνολικου αποτελεσματος
        int lo; //
        int hi; //

        // 4 επαναληψεις μια για καθε περιπτωση αριθμων Threads 1,2,4,8
        for (int i = 0; i < 4; i++){
            long startTime = System.nanoTime();
            THREADCOUNT = (int) Math.pow(2,i);  // 1, 2, 4, 8
            RowByVectorMultiplicationThread[] ts = new RowByVectorMultiplicationThread[THREADCOUNT]; // αρχικοποιηση πινακα RowByVectorMultiplicationThread

            // βο
            lo = 0;
            hi = matrix.length / THREADCOUNT;
            for (int j = 0; j < THREADCOUNT; j++){
                // δημιουργια των Thread
                ts[j] = new RowByVectorMultiplicationThread("Thread" + i, matrix, vector, lo, hi);
                // εκκινηση λειτουργιας του Thread
                ts[j].start();

                // επαναπροσδιορισμος ευρους υπολογισμου για το επομενο thread
                lo = hi;
                hi += matrix.length / THREADCOUNT;
            }

            // αρχικοποιηση πινακα πινακων
            arrs = new int[THREADCOUNT][];

            // wait for all threads to finish
            for (int j = 0; j < THREADCOUNT; j++) {
                ts[j].join();
                // αποθηκευουμε το αποτελεσμα απο καθε thread στον πινακα arrs
                arrs[j] = ts[j].getResultVector();
            }

            // αναλογα με τον αριθμο των Thread, γινεται και η καταλληλη συνενωση των αποτελεσματων σε 1 τελικο αποτελεσμα result
            switch (THREADCOUNT){
                case 1:
                    result = arrs[0];
                    break;
                case 2:
                    result = arrayConcat(arrs[0], arrs[1]);
                    break;
                case 4:
                    result = arrayConcat(arrs[0], arrs[1], arrs[2], arrs[3]);
                    break;
                case 8:
                    result = arrayConcat(arrs);
                    break;
                default:
                    throw new Exception("Default case in switch() reached. Something went wrong somehow.");
            }

            System.out.println("Number of threads: " +THREADCOUNT+". Elapsed time after putting together results in nanoseconds: " + (System.nanoTime() - startTime));
        }
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

    // ενωνει 2 πινακες
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

    // ενωνει 4 πινακες Overloaded method
    public static int[] arrayConcat(int[] arr1, int[] arr2, int[] arr3, int[] arr4) throws Exception {
        int[] arr12 = arrayConcat(arr1, arr2);
        int[] arr34 = arrayConcat(arr3, arr4);
        return arrayConcat(arr12, arr34);
    }

    // ενωνει 8 πινακες Overloaded method
    public static int[] arrayConcat(int[][] arr) throws Exception {
        if (arr.length != 8) throw new Exception("Wrong int[][] arr size. Needs to be 8.");
        int[] arr1234 = arrayConcat(arr[0], arr[1], arr[2], arr[3]);
        int[] arr5678 = arrayConcat(arr[4], arr[5], arr[6], arr[7]);
        return arrayConcat(arr1234, arr5678);
    }
}
