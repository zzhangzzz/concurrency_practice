package section1_base_theory;

/**
 * @author zhang.xu
 * email nagisaww.zhang@beibei.com
 * 2020/11/19 3:43 下午
 * info :
 */
public class Class01 {

    private static long count = 0;
    private void add10k() {
        int idx = 0;
        while (idx ++ < 100000) {
            count += 1;
        }
    }

    public static long cal_not_thread_safe() throws InterruptedException {
        final Class01 test =  new Class01();
        // 创建两个线程 执行add操作
        Thread th1 = new Thread(() -> {
            test.add10k();
        });

        Thread th2 = new Thread(() -> {
            test.add10k();
        });
        th1.start();
        th2.start();
        th1.join();
        th2.join();
        return count;
    }


    /**
     * 饿汉式
     */
    public static class Singleton {
        private static Singleton instance = new Singleton();

        public static Singleton getInstance() {
            return instance;
        }

    }

    /**
     * 懒汉式
     */
    public static class Singleton_lazy {
        private static Singleton_lazy instance;
        public static Singleton_lazy getInstance() {
            if (null == instance) {
                instance = new Singleton_lazy();
            }
            return instance;
        }
    }

    /**
     * 双重锁
     */
    public class SingletonLock {
        private SingletonLock instance;
        public synchronized SingletonLock getInstance() {
            if (instance == null) {
                instance = new SingletonLock();
            }
            return instance;
        }
    }

    /**
     * DCL 解决后
     */
    public class SingletonDCL {
        private SingletonDCL instance;
        public SingletonDCL getInstance() {
            if (instance == null) {
                synchronized (SingletonDCL.class) {
                    instance = new SingletonDCL();
                }
            }
            return instance;
        }

    }

    /**
     * volatile解决if判断问题
     */
    public class SingletonDCLAndVolatile {
        private volatile SingletonDCLAndVolatile instance;
        public SingletonDCLAndVolatile  getInstance() {
            if (instance == null) {
                synchronized (SingletonDCLAndVolatile.class) {
                    if (instance == null) {
                        instance = new SingletonDCLAndVolatile();
                    }
                }
            }
            return instance;
        }
    }
}
