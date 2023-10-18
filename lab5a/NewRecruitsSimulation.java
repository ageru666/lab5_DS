package lab5a;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;

public class NewRecruitsSimulation {
    private static final int NUM_RECRUITS = 100;
    private static final int GROUP_SIZE = 50;

    private final ReentrantLock lock = new ReentrantLock();
    private final CyclicBarrier startBarrier = new CyclicBarrier(NUM_RECRUITS / GROUP_SIZE + 1);
    private final CyclicBarrier endBarrier = new CyclicBarrier(NUM_RECRUITS / GROUP_SIZE + 1);

    private boolean[] recruits;

    public NewRecruitsSimulation() {
        recruits = new boolean[NUM_RECRUITS];
    }

    public void runSimulation() {
        Thread[] threads = new Thread[NUM_RECRUITS / GROUP_SIZE];
        for (int i = 0; i < threads.length; i++) {
            final int startIndex = i * GROUP_SIZE;
            threads[i] = new Thread(() -> {
                try {
                    startBarrier.await();
                    for (int j = startIndex; j < startIndex + GROUP_SIZE; j++) {
                        moveRecruit(j);
                    }
                    endBarrier.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        try {
            startBarrier.await();
            endBarrier.await();
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Simulation completed.");
    }


    private void moveRecruit(int recruitIndex) {
        while (true) {
            lock.lock();
            try {
                int neighborIndex = recruitIndex + 1;
                if (neighborIndex >= NUM_RECRUITS) {
                    neighborIndex = 0;
                }

                boolean recruitFacingLeft = recruits[recruitIndex];
                boolean neighborFacingLeft = recruits[neighborIndex];

                if (recruitFacingLeft == neighborFacingLeft) {
                    return;
                } else {
                    recruits[recruitIndex] = !recruitFacingLeft;
                    recruits[neighborIndex] = !neighborFacingLeft;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public void printRecruitState() {
        StringBuilder state = new StringBuilder();
        for (boolean facingLeft : recruits) {
            state.append(facingLeft ? "R" : "L");
        }
        System.out.println(state.toString());
    }

    public static void main(String[] args) {
        NewRecruitsSimulation simulation = new NewRecruitsSimulation();
        simulation.runSimulation();
        simulation.printRecruitState();
    }
}
