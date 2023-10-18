package lab5b;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main_B {
    private static final int NUMBER_OF_THREADS = 4;

    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(NUMBER_OF_THREADS);
        ThreadManager threadManager = new ThreadManager(NUMBER_OF_THREADS);

        Thread[] threads = new Thread[NUMBER_OF_THREADS];
        String[] initialStrings = { "BACCDDAABCD", "BAACBBCABAC", "ADCCBCDACCD", "DDABBABCDCBA" };

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            threads[i] = new Thread(new ThreadModifier(initialStrings[i], barrier, threadManager, i + 1));
            threads[i].start();
        }

        System.out.println("Initial threads started.");
    }
}

class ThreadManager {
    private boolean isRunning = true;
    private int threadCounter = 0;
    private final int numberOfThreads;
    private final int[] threadInfo;
    private boolean allThreadsArrived = false;

    public ThreadManager(int threadNum) {
        numberOfThreads = threadNum;
        threadInfo = new int[threadNum];
    }

    public boolean isRunning() {
        return isRunning;
    }

    public synchronized void checkEquality() {
        boolean isEqual = true;
        Arrays.sort(threadInfo);
        for (int i = 1; i < threadInfo.length - 2; i++) {
            if (threadInfo[i] != threadInfo[i + 1]) {
                isEqual = false;
                break;
            }
        }
        if (isEqual && (threadInfo[0] == threadInfo[1] || threadInfo[threadInfo.length - 1] == threadInfo[1])) {
            isRunning = false;
        }
    }

    public synchronized void updateInfo(int data) {
        threadInfo[threadCounter] = data;
        threadCounter++;
        if (threadCounter == numberOfThreads) {
            notifyAll();
            allThreadsArrived = true;
        }
        while (!allThreadsArrived) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threadCounter--;
        if (threadCounter == 0) {
            checkEquality();
            allThreadsArrived = false;
        }
    }
}

class ThreadModifier implements Runnable {
    private final Random random = new Random();
    private String currentString;
    private final CyclicBarrier barrier;
    private final ThreadManager threadManager;
    private boolean isRunning = true;
    private int abCount;
    private final int indexOfThread;

    public ThreadModifier(String str, CyclicBarrier barrier, ThreadManager threadManager, int index) {
        this.currentString = str;
        this.barrier = barrier;
        this.threadManager = threadManager;
        this.abCount = countAbMentioning(str);
        this.indexOfThread = index;
    }

    @Override
    public void run() {
        while (isRunning) {
            int randIndex = random.nextInt(currentString.length());
            char selectedChar = currentString.charAt(randIndex);

            switch (selectedChar) {
                case 'A':
                    currentString = replaceChar(currentString, randIndex, 'C', 'A', 'B');
                    abCount--;
                    break;
                case 'B':
                    currentString = replaceChar(currentString, randIndex, 'D', 'A', 'B');
                    abCount--;
                    break;
                case 'C':
                    currentString = replaceChar(currentString, randIndex, 'A', 'C', 'D');
                    abCount++;
                    break;
                case 'D':
                    currentString = replaceChar(currentString, randIndex, 'B', 'C', 'D');
                    abCount++;
                    break;
            }

            threadManager.updateInfo(abCount);
            System.out.println("Thread #" + indexOfThread + " " + currentString + " " + abCount);
            try {
                barrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
            System.out.println();
            isRunning = threadManager.isRunning();
        }
    }

    private int countAbMentioning(String str) {
        int count = 0;
        for (char c : str.toCharArray()) {
            if (c == 'A' || c == 'B') {
                count++;
            }
        }
        return count;
    }

    private String replaceChar(String input, int index, char newChar, char... oldChars) {
        char[] chars = input.toCharArray();
        for (char oldChar : oldChars) {
            if (chars[index] == oldChar) {
                chars[index] = newChar;
                break;
            }
        }
        return new String(chars);
    }
}
