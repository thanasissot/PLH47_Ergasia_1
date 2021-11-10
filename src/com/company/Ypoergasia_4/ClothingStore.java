package com.company.Ypoergasia_4;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class ClothingStore {
    enum SEX {MALE, FEMALE}
    static SEX[] SEX_INDEXED = SEX.values();
    static final int registerTime = (5 * (int) Math.pow(10, 9));  // 5 seconds needed at the Register
    static Semaphore totalCustomer = new Semaphore(40);
    static Semaphore maleFittingRoom = new Semaphore(5, true);
    static Semaphore femaleFittingRoom = new Semaphore(5, true);
    static Semaphore registerLine = new Semaphore(10, true);
    static Deque<Customer> customers = new LinkedList<>();

    public static void main(String[] args) throws InterruptedException {
        RegisterHandler registerHandler = new RegisterHandler(customers, totalCustomer);
        CustomerHandler customerHandler = new CustomerHandler(totalCustomer);

        customerHandler.start();
        registerHandler.start();

    }

    public static class RegisterHandler extends Thread {
        private final Deque<Customer> customers;
        private final Semaphore totalCustomers;

        public RegisterHandler(Deque<Customer> customers, Semaphore totalCustomers) {
            this.customers = customers;
            this.totalCustomers = totalCustomers;
        }

        @Override
        public void run() {
            while (true) {
                // if register line is not empty
                if (!customers.isEmpty()) {
                    // first in line is the one completing his shopping, 5 seconds and after release him
                    try {
                        Thread.sleep(5000); // 5 seconds to shopping completion
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // removing head of line
                    Customer cust = customers.pop();
                    System.out.println("Customer" + cust.getCustID() + " left the store, happily after shopping clothes.");
                    // releasins semaphore signal, the customer leaving the store and the register line
                    registerLine.release();
                    totalCustomers.release();
                } else {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static class CustomerHandler extends Thread {
        private final Semaphore totalCustomers;
        public static int customerID = 1;

        public CustomerHandler(Semaphore totalCustomers) {
            this.totalCustomers = totalCustomers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    totalCustomers.acquireUninterruptibly();
                    Customer customer = new Customer(customerID, maleFittingRoom, femaleFittingRoom, registerLine, customers);
                    customer.start();
                    customerID++;
                    Thread.sleep(ThreadLocalRandom.current().nextInt(2, 6) * 1000L); // every 2-5 seconds try to insert Customer in shop
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Customer extends Thread {
        private final int custID;
        private final SEX sex;
        private final Semaphore maleFittingRoom;
        private final Semaphore femaleFittingRoom;
        private final Semaphore registerLine;
        private final Deque<Customer> customers;

        public int getCustID() {
            return custID;
        }

        public Customer(int custID, Semaphore fittingRoom, Semaphore femaleFittingRoom, Semaphore registerLine, Deque<Customer> customers) {
            this.custID = custID;
            this.maleFittingRoom = fittingRoom;
            this.femaleFittingRoom = femaleFittingRoom;
            this.registerLine = registerLine;
            this.customers = customers;
//            this.fittingRoomTime = ThreadLocalRandom.current().nextInt(3, 11); // assign random fitting room time
            this.sex = SEX_INDEXED[ThreadLocalRandom.current().nextInt(0, 2)]; //  randomly assign SEX
        }

        @Override
        public void run() {
            System.out.println("Customer" + custID + " entered the store.");
            if (this.sex.equals(SEX.MALE)) {
                maleFittingRoom.acquireUninterruptibly();
            } else {
                femaleFittingRoom.acquireUninterruptibly();
            }
            System.out.println("Customer" + custID + " went into fitting room.");

            // introduce fitting room time delay 3-10 seconds
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(3, 11) * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // release Semaphore signal
            if (this.sex.equals(SEX.MALE)) {
                maleFittingRoom.release();
            } else {
                femaleFittingRoom.release();
            }
            System.out.println("Customer" + custID + " left the dressing room.");
            // customer finished trying clothes, heads into Register Line
            registerLine.acquireUninterruptibly();
            customers.add(this);
            System.out.println("Customer" + custID + " is in line for the register.");
        }
    }
}
