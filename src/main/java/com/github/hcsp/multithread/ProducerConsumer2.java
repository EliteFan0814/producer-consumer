package com.github.hcsp.multithread;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ProducerConsumer2 {
        public static void main(String[] args) throws InterruptedException {
            ReentrantLock lock = new ReentrantLock();
            Container container = new Container(lock);

            Producer producer = new Producer(container,lock);
            Consumer consumer = new Consumer(container,lock);

            producer.start();
            consumer.start();

            producer.join();
            producer.join();
        }
    public static class Container {
        private Condition notConsumedYet;
        private Condition notProducedYet;
        private Optional<Integer> value = Optional.empty();

        public Container(ReentrantLock lock) {
            notConsumedYet = lock.newCondition();
            notProducedYet = lock.newCondition();
        }

        public Optional<Integer> getValue() {
            return value;
        }

        public void setValue(Optional<Integer> value) {
            this.value = value;
        }

        public Condition getNotConsumedYet() {
            return notConsumedYet;
        }

        public Condition getNotProducedYet() {
            return notProducedYet;
        }

    }

    public static class Producer extends Thread {
        Container container;
        ReentrantLock lock;

        public Producer(Container container, ReentrantLock lock) {
            this.container = container;
            this.lock = lock;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    lock.lock();
                    while (container.getValue().isPresent()) {
                        try {
                            container.getNotConsumedYet().await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Integer value = new Random().nextInt();
                    container.setValue(Optional.of(value));
                    System.out.println("productor:" + value);
                    container.getNotProducedYet().signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    public static class Consumer extends Thread {
        Container container;
        ReentrantLock lock;

        public Consumer(Container container, ReentrantLock lock) {
            this.container = container;
            this.lock = lock;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                try {
                    lock.lock();
                    while (!container.getValue().isPresent()) {
                        try {
                            container.getNotProducedYet().await();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    Integer value = container.getValue().get();
                    container.setValue(Optional.empty());
                    System.out.println("consumer:" + value);
                    container.getNotConsumedYet().signal();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
