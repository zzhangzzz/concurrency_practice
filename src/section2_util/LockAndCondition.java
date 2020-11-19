package section2_util;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zhang.xu
 * email nagisaww.zhang@beibei.com
 * 2020/11/19 11:10 下午
 * info :
 */
public class LockAndCondition {

    public class BlockedQueue<T> {
        final Lock lock = new ReentrantLock();
        // 条件变量 队列不满
        final Condition notFull = lock.newCondition();

        //条件变量 队列不空
        final Condition notEmpty = lock.newCondition();

        void enq(T x) throws InterruptedException {
            lock.lock();
            try {
                while (/* 队列已满 */ true) {
                    notFull.await();
                }

                // 入队操作
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        void deq() throws InterruptedException {
            lock.lock();
            try {
                while (/* 队列空 */ false) {
                    notEmpty.await();
                }
                // 出对操作
                notFull.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}
