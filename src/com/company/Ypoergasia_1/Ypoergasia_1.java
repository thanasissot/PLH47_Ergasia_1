package com.company.Ypoergasia_1;// package com.company.Ypoergasia_1;

import java.text.DecimalFormat;
import java.util.Random;

public class Ypoergasia_1 {
    // formats time string, uesd when printing to console
    static DecimalFormat formatter = new DecimalFormat("#,###");

    //  n, m, k are powers of 2 as instructed by SEP. also no need for Amyntikos Programmatismos, very minimum coded in
    public static void main(String[] args) throws Exception {
        int[][] matrix;
        int[] vector;
        int n , m;
        long[] totalTimes = new long[4];
        int[] result;

        if (args.length == 0){
            // if no input arguments give random power of 2 to variables of matrix and vector sizes
            Random rand = new Random();
            n = rand.nextInt(3) + 12;
            m = rand.nextInt(3) + 12;
        }
        else {
            // else take users inputs, needs to be power of 2
            n = Integer.parseInt(args[0]);
            m = Integer.parseInt(args[1]);
        }

        // create matrix and vector filled with random integers in instructed ranges
        vector = Ypoergasia_1.getVector((int) Math.pow(2, m));
        matrix = Ypoergasia_1.getMatrix((int) Math.pow(2, n), (int) Math.pow(2, m));
        // helper variabls
        int[][] arrs; // keeps track of results from each different thread and used to put em together at the end
        int THREADCOUNT; // number of threads
        // every threads starting and finishing index to calculate
        int lo; //
        int hi; //

        // 4 times loop each for each number of Threads 1,2,4,8
        for (int i = 0; i < 4; i++){
            long startTime = System.nanoTime();
            THREADCOUNT = (int) Math.pow(2,i);  // 1, 2, 4, 8
            RowByVectorMultiplicationThread[] ts = new RowByVectorMultiplicationThread[THREADCOUNT]; // initialize matrix for threds

            lo = 0;
            hi = matrix.length / THREADCOUNT;
            for (int j = 0; j < THREADCOUNT; j++){
                // create thread
                ts[j] = new RowByVectorMultiplicationThread("Thread" + (j + 1), matrix, vector, lo, hi);
                // start thread
                ts[j].start();

                // redefine starting and ending index
                lo = hi;
                hi += matrix.length / THREADCOUNT;
            }

            // initialize matrix of results
            arrs = new int[THREADCOUNT][];

            // wait for all threads to finish
            for (int j = 0; j < THREADCOUNT; j++) {
                ts[j].join();
                // save each result to its plce in results matrix arrs
                arrs[j] = ts[j].getResultVector();
            }

            // depending on number of threads used merge results
            switch (THREADCOUNT){
                case 1:
                    result = arrs[0];
                    break;
                case 2:
                    result = arrayConcat(arrs[0], arrs[1]);
                    break;
                case 4:
                case 8:
                    result = arrayConcat(arrs);
                    break;
                default:
                    throw new Exception("Default case in switch() reached. Something went wrong somehow.");
            }
            totalTimes[i] = (System.nanoTime() - startTime);
        }

        // print times
        for (int i = 0; i < 4; i++){
            System.out.println("Number of threads: " + (int) Math.pow(2,i) +". Elapsed time after putting together results in nanoseconds: " + formatter.format(totalTimes[i]));
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

    // concant 2 matrixes, one dimension each
    public static int[] arrayConcat(int[] arr1, int[] arr2) throws Exception {
        if (arr1.length != arr2.length) throw new Exception("Needs same length Arrays");

        int[] arr = new int[arr1.length * 2];
        // merge two matrixes and return
        for (int i = 0; i < arr1.length; i++) {
            arr[i] = arr1[i];
            arr[arr1.length + i] = arr2[i];
        }
        return arr;
    }

    // concant 4 matrixes, one dimension each, Overloaded method
    public static int[] arrayConcat(int[][] arr) throws Exception {
        if (arr.length == 4) {
            int[] arr12 = arrayConcat(arr[0], arr[1]);
            int[] arr34 = arrayConcat(arr[2], arr[3]);
            return arrayConcat(arr12, arr34);
        }
        else if (arr.length == 8) {
            int[] arr1234 = arrayConcat(new int[][]{arr[0], arr[1], arr[2], arr[3]});
            int[] arr5678 = arrayConcat(new int[][]{arr[4], arr[5], arr[6], arr[7]});
            return arrayConcat(arr1234, arr5678);
        }
        throw new Exception("Wrong size of int[][].");
    }


    public static class RowByVectorMultiplicationThread extends Thread{

        private final int[][] matrix;
        private final int[] vector;
        private final int startRow;
        private final int endRow;
        private final int[] resultCols;

        public RowByVectorMultiplicationThread(String name, int[][] matrix, int[] vector, int startRow, int endRow) {
            super(name);

            // endRow needs to be smaller or equal to matrix size - 1
            if (endRow > matrix.length) throw new ArrayIndexOutOfBoundsException(
                    "endRow needs to be Matrix.Length - 1 or less"
            );

            this.matrix = matrix;
            this.vector = vector;
            this.startRow = startRow;
            this.endRow = endRow;
            // while creating this Thread we now know the number of results this will carry in its resultCols[] so we initialize it here
            resultCols = new int[endRow - startRow];
        }

        @Override
        public void run() {
            int sum;
            int index = 0;

            // multiply each row with the vector
            for (int i = startRow; i < endRow; i++) {
                sum = 0;
                for (int j = 0; j < matrix[i].length; j++) {
                    // multiply each element with each respective to the vector element according to matrix multiply algorithm
                    sum += (matrix[i][j] * vector[j]);
                }
                // save each result
                resultCols[index++] = sum;
            }
        }

        public int[] getResultVector() {
            return resultCols;
        }
    }

}
