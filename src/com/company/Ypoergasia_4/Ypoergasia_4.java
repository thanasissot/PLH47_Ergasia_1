package com.company.Ypoergasia_4;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;

public class Ypoergasia_4 {
    private static final int N = 10;
    private static final int MAXCUSTOMERS = 40;
    private static final int MAXDRESSINGROOMCUSTOMERS = 5;
    private static final int MAXLINE = 10;
    private static final int payTime = (5 * (int) Math.pow(10,9)) / N;
    private final Semaphore counter = new Semaphore(1);
    private final Semaphore mensRoom = new Semaphore(5);
    private final Semaphore womensRoom = new Semaphore(5);

    //private static int tryingOutfitTime = ThreadLocalRandom.current().nextInt(3, 11);
    //private static int nextCustomerTime = ThreadLocalRandom.current().nextInt(2, 6);



    public static void main(String[] args) {
        for (int i = 0; i < 10; i++){
        }
    }
}
