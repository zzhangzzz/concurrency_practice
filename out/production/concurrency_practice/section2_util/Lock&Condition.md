# 1 Lock  

JDK 通过Lock & Condition两个接口实现管程  其中Lock主要是处理互斥问题，Condition解决同步问题。  

#### 为什么需要？  
synchronized 申请资源时，如果申请不到，线程进入阻塞，就无法释放资源。  
但是很多情况下， 期待的情况是：  
对于占用部分资源的线程如果进一步申请其他资源时，如果失败，可以主动释放占有的资源。  

有3中方法可以实现上述期望：  
1、能够相应中断。
2、支持超时
3、非阻塞的获取锁

看一下Lock的三个方法
```java
// 支持中断的API
    void lockInterruptibly() 
    throws InterruptedException;
    // 支持超时的API
    boolean tryLock(long time, TimeUnit unit) 
    throws InterruptedException;
// 支持非阻塞获取锁的API
    boolean tryLock();


    // 使用范例
class X {
    private final Lock ttl = new ReentrantLock();
    int value;
    public void addOne() {
        rtl.lock(); // 获取锁
        try {
            value += 1;
        } finally {
            rtl.unlock(); // 释放锁
        }
    }
}
```


###### 可重入锁  
可重入锁概念： 线程可以重复获取同一把锁。 (多个线程可以同时调用该函数)
如下范例所示
```java
    class X {
        private final Lock rtl = new ReentrantLock();
        int value;
        public int get() {
            rtl.lock();    // 2
            try {
                return value;
            } finally{
                rtl.unlock();
            }
        }       
        public void addOne() {
            rtl.lock();   
            try {
                value = 1 + get();  // 1
            } finally{
                rtl.unlock();
            }
        }   
    }
```
线程A执行到1处时， 已经获得了rtl锁，1调用get（），会再对rtl加锁，如果是可重入锁可以加成功，否则会失败。  


###### 可重入锁     
ReentrantLock 可以通过入参，实现公平或非公平锁  

公平锁：   一个线程没有获得锁，就会进入等待队列，有线程释放锁时，就从等待队列中唤醒一个等待的线程  
非公平锁： 不提供这个公平保证，有可能等待时间短的线程先醒。 
```java
public ReentrantLock() {
    sync = new NonfairSync();
}

public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```



// 小测验 下面这段代码是否有死锁的问题
```java
class Account {
    private int balance;
    private final Lock lock = new ReentrantLock();
    
    void transfer(Account tar, int amt) {
        // 有可能产生活锁
        // A、B两个账户互相转账，各自持有自己的Lock ，一直在等待获得对方的lock，形成活锁
        while (true) {
            if (this.lock.tryLock()) {
                try {
                    if (tar.lock.tryLock()) {
                        try {
                            this.balance -= amt;
                            tar.balance += amt;
                        } finally{
                            tar.lock.unlock();
                        }
                    }
                } finally{
                   this.lock.unlock();
                }
            }
        }
    }   
}
```


#2 Condition
java 内置的管程synchronized只有一个条件变量，而Lock & Condition 支持多个变量，这是一个重要区别

案例1 通过两个条件变量快速实现阻塞队列：  
section2_util.LockAndCondition.BlockedQueue  

需要注意，Lock 和 Condition 实现的管程，线程等待和通知需要调用 await()、signal()、signalAll()

wait()、notify()、notifyAll() 只有在 synchronized 实现的管程里才能使用  

再用dubbo 异步转同步的方式来看一下作用  
首先是这里发送RPC请求，通过get() 等待RPC返回结果
```java
public class DubboInvoker{
  Result doInvoke(Invocation inv){
    // 下面这行就是源码中108行
    // 为了便于展示，做了修改
    return currentClient 
      .request(inv, timeout)
      .get();
  }
}
```  
下面是Dubbo中通知等待机制的实现
```java
    //  创建锁 & 条件变量
    private final Lock lock = new ReentrantLock();
    private final Condition done = lock.newCondition();

    // 调用方法等待结果
    Object get(int timeout) {
        long start = System.nanoTime();
        lock.lock();
        try {
            while (!isDone()) {
                done.await(timeout);
                long cur = System.nanoTime();
                if (isDone() || cur - start > timeout) {
                    break;
                }   
            }   
        } finally {
            lock.unlock();
        }
        if (!isDone()) {
            throw new TimeoutException();
        }               
        return returnFromResponse();
    }   

    // Rpc结果是否返回
    boolean isDone() {
        return response != null;    
    }

    // RPC结果返回时调用此方法
    private void doReceived(Response res) {
        lock.lock();
        try {
            response = res;
            if (done != null) {
                done.signal();
            }        
        } finally {
            lock.unlock();
        }              
    }
```