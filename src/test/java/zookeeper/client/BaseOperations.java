package zookeeper.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

/**
 * 创建，获取，修改，删除节点
 */
@Slf4j
public class BaseOperations extends StandaloneBase {

    private String first_node = "/first-node";

    //创建节点
    @Test
    public void testCreate() throws KeeperException, InterruptedException {
        ZooKeeper zooKeeper = getZooKeeper();

        String s = zooKeeper.create(first_node, "first".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        log.info("Create:{}", s);
    }


    //获取节点数据，并循环注册监听
    @Test
    public void testGetData() {

        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if (event.getPath() != null && event.getPath().equals(first_node)
                        && event.getType() == Event.EventType.NodeDataChanged) {
                    log.info(" PATH: {}  发现变化", first_node);
                    try {
                        byte[] data = getZooKeeper().getData(first_node, this, null);
                        log.info(" data: {}", new String(data));
                    } catch (KeeperException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        try {
            byte[] data = getZooKeeper().getData(first_node, watcher, null);
            log.info(" data: {}", new String(data));
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    //修改节点数据
    @Test
    public void testSetData() throws KeeperException, InterruptedException {
        ZooKeeper zooKeeper = getZooKeeper();
        Stat stat = new Stat();
        byte[] data = zooKeeper.getData(first_node, false, stat);
        // int version = stat.getVersion();
        zooKeeper.setData(first_node, "third".getBytes(), 1); //乐观锁，version要是当前的版本才能修改成功


    }


    //删除节点
    @Test
    public void testDelete() throws KeeperException, InterruptedException {
        // -1 代表匹配所有版本，直接删除
        // 任意大于 -1 的代表可以指定数据版本删除
        getZooKeeper().delete("/config", -1);

    }



}
