package com.company.Ypoergasia_4;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class ClothingStore {
    public enum SEX {MALE, FEMALE};
    public static SEX[] SEX_INDEXED = SEX.values();
    public static final int registerTime = (5 * (int) Math.pow(10,9));  // 5 seconds needed at the Register


    public static void main(String[] args) throws InterruptedException {
        Semaphore totalCustomer = new Semaphore(40);
        Semaphore maleFittingRoom = new Semaphore(5, true);
        Semaphore femaleFittingRoom = new Semaphore(5, true);
        Semaphore registerLine = new Semaphore(10, true);
        Semaphore fittingLine = new Semaphore(10, true);
        Semaphore afterFittingRoomLine = new Semaphore(10, true);
        ArrayList<Customer> customers = new ArrayList<>(40);

    }

    public class CustomerHandler extends Thread {
        private final Semaphore totalCustomers;
        private final Customer[] customers;
        public int customerID = 1;

        public CustomerHandler(Semaphore totalCustomers, Customer[] customers) {
            this.totalCustomers = totalCustomers;
            this.customers = customers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    totalCustomers.acquire();
                    Customer customer = new Customer(customerID++, registerTime);
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2, 6) * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                for (Customer customer : customers){

                }
            }


        }
    }

    public class Customer extends Thread{
        private SEX sex;
        private int fittingRoomTime;  // 3-10 seconds needed in fitting room
        private final int registerTime; // 2-5 sexonds needed in register to complete purchase
        private boolean completedShoppingFlag = false;
        private int id;

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

        public Customer(int id, int registerTime) {
            this.id = id;
            this.registerTime = registerTime;
            this.fittingRoomTime = ThreadLocalRandom.current().nextInt(3, 11); // assign random fitting room time
            this.sex = SEX_INDEXED[ThreadLocalRandom.current().nextInt(0,2)];
        }

        @Override
        public void run() {

        }
    }

}
