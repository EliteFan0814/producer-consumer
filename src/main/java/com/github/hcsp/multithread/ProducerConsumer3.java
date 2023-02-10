package com.github.hcsp.multithread;

import java.util.Random;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ProducerConsumer3 {
    public static void main(String[] args) throws InterruptedException {
        BlockingDeque<Integer> queue = new LinkedBlockingDeque<>(1);
        BlockingDeque<Integer> signQueue = new LinkedBlockingDeque<>(1);
        Producer producer = new Producer(queue,signQueue);
        Consumer consumer = new Consumer(queue,signQueue);

        producer.start();
        consumer.start();

        producer.join();
        producer.join();
    }

    public static class Producer extends Thread {
        BlockingDeque<Integer> queue;
        BlockingDeque<Integer> signQueue;

        public Producer(BlockingDeque<Integer> queue, BlockingDeque<Integer> signQueue) {
            this.queue = queue;
            this.signQueue = signQueue;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                int r = new Random().nextInt();
                System.out.println("producer" + r);
                try {
                    queue.put(r);
                    signQueue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static class Consumer extends Thread {
        BlockingDeque<Integer> queue;
        BlockingDeque<Integer> signQueue;

        public Consumer(BlockingDeque<Integer> queue, BlockingDeque<Integer> signQueue) {
            this.queue = queue;
            this.signQueue = signQueue;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100; i++) {
                try {
                    System.out.println("consumer:" + queue.take());
                    signQueue.put(0);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
