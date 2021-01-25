package zookeeper.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 创建节点添加数据并循环注册监听
 * 2021-01-05 21:53:10,101 [myid:] - INFO  [main-EventThread:ConfigCenter$1@32] - 连接已建立
 * 2021-01-05 21:53:10,298 [myid:] - INFO  [main:ConfigCenter@71] - 原始数据: MyConfig(key=anykey, name=anyName)
 * 2021-01-05 21:55:01,854 [myid:] - INFO  [main-EventThread:ConfigCenter$2@55] -  PATH:/myconfig  发生了数据变化
 * 2021-01-05 21:55:01,857 [myid:] - INFO  [main-EventThread:ConfigCenter$2@60] - 数据发生变化: MyConfig(key=anykey, name=chengzw)
 */

@Slf4j
public class ConfigCenter {

    private final static String CONNECT_STR = "192.168.1.82:2181";

    private final static Integer SESSION_TIMEOUT = 30 * 1000;

    private static ZooKeeper zooKeeper = null;


    private static CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {


        zooKeeper = new ZooKeeper(CONNECT_STR, SESSION_TIMEOUT, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.None
                        && event.getState() == Event.KeeperState.SyncConnected) {
                    log.info("连接已建立");
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();

        //创建一个节点并添加数据
        MyConfig myConfig = new MyConfig();
        myConfig.setKey("anykey");
        myConfig.setName("anyName");

        ObjectMapper objectMapper = new ObjectMapper();  //json序列化
        byte[] bytes = objectMapper.writeValueAsBytes(myConfig);
        String s = zooKeeper.create("/myconfig", bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

        //为节点添加监听
        Watcher watcher = new Watcher() {
            @SneakyThrows
            @Override
            public void process(WatchedEvent event) {
                if (event.getType() == Event.EventType.NodeDataChanged
                        && event.getPath() != null && event.getPath().equals("/myconfig")) {
                    log.info(" PATH:{}  发生了数据变化", event.getPath());

                    //循环注册，一旦发生变化，拿到数据后重新注册
                    byte[] data = zooKeeper.getData("/myconfig", this, null);
                    MyConfig newConfig = objectMapper.readValue(new String(data), MyConfig.class);
                    log.info("数据发生变化: {}", newConfig);

                }


            }
        };

        //获取监听触发的事件
        byte[] data = zooKeeper.getData("/myconfig", watcher, null);
        MyConfig originalMyConfig = objectMapper.readValue(new String(data), MyConfig.class);
        log.info("原始数据: {}", originalMyConfig);

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }


}
