package com.company.Ypoergasia_4;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class ClothingStore {
    public enum SEX {MALE, FEMALE};
    public static SEX[] SEX_INDEXED = SEX.values();
    public static final int registerTime = (5 * (int) Math.pow(10,9));  // 5 seconds needed at the Register

    Semaphore maleFittingRoom = new Semaphore(5, true);
    Semaphore femaleFittingRoom = new Semaphore(5, true);
    Semaphore registerLine = new Semaphore(10, true);
    Semaphore fittingLine = new Semaphore(10, true);
    Semaphore afterFittingRoomLine = new Semaphore(10, true);

    public class Customer extends Thread{
        private SEX sex;
        private int fittingRoomTime;  // 3-10 seconds needed in fitting room
        private final int registerTime; // 2-5 sexonds needed in register to complete purchase
        private boolean completedShoppingFlag = false;

        public SEX getSex() {
            return sex;
        }

        public int getFittingRoomTime() {
            return fittingRoomTime;
        }

        public int getRegisterTime() {
            return registerTime;
        }

        public boolean isCompletedShoppingFlag() {
            return completedShoppingFlag;
        }

        public Customer(int registerTime) {
            this.registerTime = registerTime;
            this.fittingRoomTime = ThreadLocalRandom.current().nextInt(3, 11); // assign random fitting room time
            this.sex = SEX_INDEXED[ThreadLocalRandom.current().nextInt(0,2)];
        }

        @Override
        public void run() {

        }
    }

    public static class FittingRoom extends Thread {


        @Override
        public void run() {
        }
    }
}
