package com.atu.senior.ProducterAndConsumer;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author ��mark
 * @date ��Created in 2019/10/24 17:41
 * @description��https://www.cnblogs.com/hankzhouAndroid/p/8693278.html
 */
public class MainTest {
    public static void main(String[] args) {
        test();
    }

    private static final long waitTime = 3000;

    private static void test() {
        Queue<Integer> queue = new LinkedList<>();// ���ж���,��������ν�ġ�����
        int maxsize = 2;// �����е����Ԫ�ظ�������

        // ����4���̣߳�һ˲��ֻ����һ���̻߳�øö��������������ͬ�������
        Producer producer = new Producer(queue, maxsize, "Producer");
        Consumer consumer1 = new Consumer(queue, maxsize, "Consumer1");
        Consumer consumer2 = new Consumer(queue, maxsize, "Consumer2");
        Consumer consumer3 = new Consumer(queue, maxsize, "Consumer3");

        // ��ʵ����������ĸ�������ν����Ϊֻ��һ������ÿһ��ֻ����һ���߳��ܳ����������������queue
        producer.start();
        consumer2.start();
        consumer1.start();
        consumer3.start();
    }

    /**
     * �������߳�
     */
    public static class Producer extends Thread {
        Queue<Integer> queue;// queue,������
        int maxsize;// ò���Ƕ��е�������

        Producer(Queue<Integer> queue, int maxsize, String name) {
            this.queue = queue;
            this.maxsize = maxsize;
            this.setName(name);
        }

        @Override
        public void run() {
            while (true) {// ����ѭ��,��ͣ����Ԫ�أ�ֱ���ﵽ���ޣ�ֻҪ�ﵽ���ޣ��Ǿ�wait�ȴ���
                synchronized (queue) {// ͬ�������,ֻ�г���queue������Ķ�����ܷ�����������
                    try {
                        Thread.sleep(waitTime);
                        // sleep��wait������sleep���õ�ǰִ�е��߳�����һ��ʱ�䣬���ǲ����ͷ�����
                        // ����wait�������������һ��ͷ���
                    } catch (Exception e) {
                    }

                    System.out.println(this.getName() + "��ö��е���");// ֻ��������queue��������������ִ�е�����
                    // �������ж�һ��Ҫʹ��while������if
                    while (queue.size() == maxsize) {// �ж�������û�дﵽ����,����ﵽ�����ޣ����õ�ǰ�̵߳ȴ�
                        System.out.println("����������������" + this.getName() + "�ȴ�");
                        try {
                            queue.wait();// �õ�ǰ�̵߳ȴ���ֱ�������̵߳���notifyAll
                        } catch (Exception e) {
                        }
                    }

                    // ����д�ľ�����������
                    int num = (int) (Math.random() * 100);
                    queue.offer(num);// ��һ��int���ֲ��뵽������

                    System.out.println(this.getName() + "����һ��Ԫ�أ�" + num);
                    // ���������̣߳������ﰸ������ "�ȴ���"���������߳�
                    queue.notifyAll();// (ע��notifyAll��������
                    // �������г���queue�����������ڵȴ����߳�)

                    System.out.println(this.getName() + "�˳�һ���������̣�");
                }
            }
        }
    }

    public static class Consumer extends Thread {
        Queue<Integer> queue;
        int maxsize;

        Consumer(Queue<Integer> queue, int maxsize, String name) {
            this.queue = queue;
            this.maxsize = maxsize;
            this.setName(name);
        }

        @Override
        public void run() {
            while (true) {
                synchronized (queue) {// Ҫ���������Ĵ��룬�ͱ����Ȼ������
                    try {
                        Thread.sleep(waitTime);// sleep���õ�ǰ�߳�����ָ��ʱ�������ǲ������ͷ�queue��
                    } catch (Exception e) {
                    }

                    System.out.println(this.getName() + "��ö��е���");// �õ�����������ִ�е�����
                    // �������ж�һ��Ҫʹ��while������if,
                    while (queue.isEmpty()) {// while�ж϶����Ƿ�Ϊ�գ����Ϊ�գ���ǰ�������߳̾ͱ���wait����������������Ԫ��
                        // ����������ж������Ϊ�ж��consumer�̣߳���ÿһ����������������˶��п��ˣ��ͻ�wait��
                        System.out.println("����Ϊ�գ�������" + this.getName() + "�ȴ�");
                        try {
                            queue.wait();
                        } catch (Exception e) {
                        }
                    }

                    // ������в��ǿգ���ô�͵���һ��Ԫ��
                    int num = queue.poll();
                    System.out.println(this.getName() + "����һ��Ԫ�أ�" + num);
                    queue.notifyAll();// Ȼ���ٻ��������߳�,���Ѳ����ͷ��Լ�����

                    System.out.println(this.getName() + "�˳�һ�����ѹ��̣�");
                }
            }
        }
    }
}
