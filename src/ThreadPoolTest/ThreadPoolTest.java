package ThreadPoolTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhang.xu
 * email nagisaww.zhang@beibei.com
 * 2020/11/30 11:36 上午
 * info : 模拟实现一个线程池
 */
public class ThreadPoolTest {

    public interface ThreadPools<Job extends Runnable> {

        /**
         * 执行任务 实现Runable
         * @param job
         */
        void execute(Job job);

        // 关闭线程池
        void shutdown();

        // 增加工作线程 用来执行任务的
        void addWorker(int num);

        // 减少工作线程
        void removeWorker(int num);

        // 只在执行的任务数
        int getJobSize();
    }

    public class DefaultThreadPool <Job extends Runnable> implements ThreadPools<Job> {
        // 工作线程参数
        private static final int MAX_WORKER_NUMS = 30;
        private static final int DEFAULT_WORKER_NUMS = 5;
        private static final int MIN_WORKER_NUMS = 1;

        // 工作列表
        private final LinkedList<Job> jobs = new LinkedList<Job>();

        // 工作线程列表
        private final List<Worker> workers = Collections.synchronizedList(new ArrayList<>());

        // 工作者数量
        private int workerNum;
        // 工作者编号
        private AtomicLong threadNum = new AtomicLong();

        public DefaultThreadPool(int workerNum) {
            if (workerNum > MAX_WORKER_NUMS) {
                this.workerNum = MAX_WORKER_NUMS;
            } else {
                this.workerNum = workerNum;
            }
            initlizeWorkers(workerNum);
        }

        // 初始化所有工作线程
        private void initlizeWorkers(int num) {
            for (int i = 0; i < num; i++) {
                Worker worker = new Worker();
                workers.add(worker);
                Thread thread = new Thread(worker);
                thread.start();
            }
        }

        /**
         * 执行任务 实现Runable
         *
         * @param job
         */
        @Override
        public void execute(Job job) {
            if (null == job) {
                throw new NullPointerException();
            }
            synchronized (jobs) {
                jobs.add(job);
                jobs.notify();
            }
        }

        @Override
        public void shutdown() {
            for (Worker worker : workers) {
                worker.shutdown();
            }
        }

        @Override
        public void addWorker(int num) {
            synchronized (jobs) {
                if (num + this.workerNum > MAX_WORKER_NUMS) {
                    num = MAX_WORKER_NUMS - this.workerNum;
                }
                initlizeWorkers(num);
                this.workerNum += num;
            }
        }

        @Override
        public void removeWorker(int num) {
            synchronized (jobs) {
                if (num >= this.workerNum) {
                    throw new IllegalArgumentException("线程数超过目前上限");
                }
                for (int i = 0; i < num; i ++) {
                    Worker worker = workers.get(i);
                    if (worker != null) {
                        worker.shutdown();
                        workers.remove(i);
                    }
                }
                this.workerNum -= num;
            }
        }

        @Override
        public int getJobSize() {
            return workers.size();
        }


        class Worker implements Runnable {
            private volatile boolean running = true;

            @Override
            public void run() {
                while (running) {
                    Job job = null;
                    synchronized (jobs) {
                        if (jobs.isEmpty()) {
                            try {
                                jobs.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }

                        job = jobs.removeFirst();
                    }

                    if (job != null) {
                        job.run();
                    }
                }
            }

            public void shutdown() {
                running = false;
            }
        }

    }


}
