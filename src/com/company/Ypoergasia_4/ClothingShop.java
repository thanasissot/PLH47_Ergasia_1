package com.company.Ypoergasia_4;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;

public class ClothingShop {
    // change this variable to speed up times, its used as a denominator like 5000millis / universalTimeReduce = 500 if universalTimeReduce = 10
    static int universalTimeReduce = 20;
    // used to assign SEX to customere
    enum SEX {MEN, WOMEN}
    static SEX[] SEX_INDEXED = SEX.values();
    // Semaphores and their permits int, all initialized with
    static Semaphore totalCustomers = new Semaphore(40);
    static Semaphore mensFittingRoom = new Semaphore(5, true);
    static Semaphore womensFittingRoom = new Semaphore(5, true);
    static Semaphore registerLine = new Semaphore(10, true);
    // used as a Queue for the register Line
    static Deque<Customer> customers = new LinkedList<>();

    public static void main(String[] args) {
        // initialize inner classes
        RegisterLineController registerHandler = new RegisterLineController(customers, totalCustomers);
        CustomerEntryController customerHandler = new CustomerEntryController(totalCustomers);
        // start classes
        customerHandler.start();
        registerHandler.start();
    }


    // used to release 1st customer in line for register, every 5 secons (εκφωνηση ασκησης)
    // and the permits he has consumed
    public static class RegisterLineController extends Thread {
        private final Deque<Customer> customers;
        private final Semaphore totalCustomers;

        public RegisterLineController(Deque<Customer> customers, Semaphore totalCustomers) {
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
                        Thread.sleep((5000/universalTimeReduce)); // 5 seconds to shopping completion
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // removing head of line
                    Customer cust = customers.pop();
                    System.out.println("Customer" + cust.getCustID() + " left the store, happily after shopping his favorites clothes.");
                    // releasing semaphore permits, the customer leaving the store and the register line
                    registerLine.release();
                    totalCustomers.release();
                }
                // used this to let the thread sleep or else it always finds Customers Queue as empty
                else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // used to create a customer every 2-5 seconds and .start()
    // every 100 Customers print Semaphores status within ClothingShop context
    public static class CustomerEntryController extends Thread {
        private final Semaphore totalCustomers;
        public static int customerID = 1;
        public static int printCounter = 1;

        public CustomerEntryController(Semaphore totalCustomers) {
            this.totalCustomers = totalCustomers;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // create Customer
                    Customer customer = new Customer(customerID, mensFittingRoom, womensFittingRoom, registerLine, customers);
                    // start Customer
                    customer.start();
                    customerID++;
                    // every 2-5 seconds try to insert Customer in shop
                    Thread.sleep((ThreadLocalRandom.current().nextInt(2, 6) * 1000L)/universalTimeReduce);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // every 100 customers created print the status of ClothingShop, aka Semaphores permits
                if(customerID % 100 == 0) {
                    printCounter = 0;
                    System.out.print("\nThere are " + (40 - totalCustomers.availablePermits()) + " customers currently inside the store.\n");
                    System.out.print("Mens fitting room have " + mensFittingRoom.availablePermits() + " empty fitting stalls. "
                            + "Currently " + mensFittingRoom.getQueueLength() +" men customers are waiting to try on some clothes.\n");
                    System.out.print("Womens fitting room have " + womensFittingRoom.availablePermits() + " empty fitting stalls. "
                            + "Currently " + womensFittingRoom.getQueueLength() +" women customers are waiting to try on some clothes.\n");
                    System.out.print("There are " + (10 - registerLine.availablePermits()) + " customers on the register line and "
                            + registerLine.getQueueLength() + " are waiting to get in line\n");
                    System.out.println("Currently " + totalCustomers.getQueueLength() + " are waiting to enter the store.\n");
                }
            }
        }
    }

    // Customer, main class,
    public static class Customer extends Thread {
        private final int custID;
        private final SEX sex;
        private final Semaphore mensFittingRoom;
        private final Semaphore womensFittingRoom;
        private final Semaphore registerLine;
        // Dequeue used as a Queue for the register line
        private final Deque<Customer> customers;

        public int getCustID() {
            return custID;
        }

        public Customer(int custID, Semaphore mensFittingRoom, Semaphore womensFittingRoom, Semaphore registerLine, Deque<Customer> customers) {
            this.custID = custID;
            this.mensFittingRoom = mensFittingRoom;
            this.womensFittingRoom = womensFittingRoom;
            this.registerLine = registerLine;
            this.customers = customers;
            this.sex = SEX_INDEXED[ThreadLocalRandom.current().nextInt(0, 2)]; //  randomly assign SEX
        }

        @Override
        public void run() {
            // επιβαλει την υπαρξη 40 ατομων στο καταστημα, πρεπει να ο 1ος απο την ουρα του ταμειου για να συνεχισει ο επομενος
            totalCustomers.acquireUninterruptibly();
            System.out.println("Customer" + custID + " entered the store.");
            // αναλογα με το φυλο του Customer παιρνει permit στο αντιστοιχο δοκιμαστηριο
            if (this.sex.equals(SEX.MEN)) {
                mensFittingRoom.acquireUninterruptibly();
            } else {
                womensFittingRoom.acquireUninterruptibly();
            }
            System.out.println("Customer" + custID + " is in the " + this.sex + "'s fitting room.");

            // introduce fitting room time delay 3-10 seconds
            try {
                Thread.sleep((ThreadLocalRandom.current().nextInt(3, 11) * 1000L)/universalTimeReduce);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // release Semaphore signal
            if (this.sex.equals(SEX.MEN)) {
                mensFittingRoom.release();
            } else {
                womensFittingRoom.release();
            }
            System.out.println("Customer" + custID + " left the dressing room and is waiting to get in line for the register.");

            // customer finished trying clothes, heads into Register Line
            // waits for registerLine semaphore permit to become available
            registerLine.acquireUninterruptibly();
            // addded to Dequeue
            customers.add(this);
            System.out.println("Customer" + custID + " is in line for the register.");
        }
    }
}
