package com.company.THREADS;
/**
 * Πολλαπλασιαζει γραμμες ενος πινακα με ενα διανυσμα
 * τα αποτελεσματα καθε γραμμης αποθηκευονται στον πινακα resultCols
 *
 * matrix ο πινακας που πολλαπλασιαζεται με το διανυσμα vector
 * startRow και endRow ειναι το μερος του πινακα που θα υπολογιστει απο το διανυσμα
 * resultCols ειναι ενας πινακας, μερος ή ολο του διανυσματος/αποτελεσμα
 */
public class RowByVectorMultiplicationThread extends Thread{

    private final int[][] matrix;
    private final int[] vector;
    private final int startRow;
    private final int endRow;
    private final int[] resultCols;

    public RowByVectorMultiplicationThread(String name, int[][] matrix, int[] vector, int startRow, int endRow) {
        super(name);

        // αποφυγη σφαλματων, το endRow πρεπει να ειναι μικροτερο η ίσο των γραμμων του πινακα - 1
        if (endRow > matrix.length) throw new ArrayIndexOutOfBoundsException(
                "endRow needs to be Matrix.Length - 1 or less"
        );

        this.matrix = matrix;
        this.vector = vector;
        this.startRow = startRow;
        this.endRow = endRow;

        // μετα την κληση του Constructor γνωριζουμε απο τις παραμετρους startRow, endRow ποσα στοιχεια θα περιεχει ο πινακας
        // resultCols και τον αρχικοποιουμε, τo startRow θεωρουμε οτι αρχιζει απο index 0 και οχι 1 για αυτο προσθετουμε + 1
        resultCols = new int[endRow - startRow];
    }

    @Override
    public void run() {
        int sum;
        int index = 0;

        // πολλαπλασιασμος καθε γραμμης με το διανυσμα
        for (int i = startRow; i < endRow; i++) {
            sum = 0;
            for (int j = 0; j < matrix[i].length; j++) {
                // πολλαπλασιασμος καθε στοιχειου του πινακα με το αντιστοιχο στοιχειο του
                // διανυσματος, συμφωνα με τον αλγοριθμο πολλαπλασιαμο πινακαων
                sum += (matrix[i][j] * vector[j]);
            }
            // αποθηκευση της τιμης καθε στοιχειου του αποτελεσματος
            resultCols[index++] = sum;
        }
    }

    public int[] getResultVector() {
        return resultCols;
    }
}

