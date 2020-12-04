package countdownlatch;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * @author zhang.xu
 * email nagisaww.zhang@beibei.com
 * 2020/11/30 12:42 下午
 * info :
 */
public class CountDownLatchTest {

    static class PreTaskThread implements Runnable {
        private String task;
        private CountDownLatch countDownLatch;

        public PreTaskThread(String task, CountDownLatch countDownLatch) {
            this.task = task;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            try {
                Random random = new Random();
                Thread.sleep(random.nextInt(1000));
                System.out.println(task + "- finish");
                countDownLatch.countDown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(3);
        new Thread(() -> {
            try {
                System.out.println("数据加载");
                System.out.println(String.format("还有%d个前置任务", countDownLatch.getCount()));
                countDownLatch.await();
                System.out.println("加载完成， 开始");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(new PreTaskThread("地图", countDownLatch)).start();
        new Thread(new PreTaskThread("任务", countDownLatch)).start();
        new Thread(new PreTaskThread("音乐", countDownLatch)).start();
    }
}
