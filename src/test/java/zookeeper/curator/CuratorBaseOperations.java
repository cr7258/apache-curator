package zookeeper.curator;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.junit.Test;

import java.util.List;

/**
 * curator基本使用，zookeeper单机
 */
@Slf4j
public class CuratorBaseOperations extends CuratorBase {

    //创建节点
    @Test
    public void testCreate() throws Exception{
        CuratorFramework curatorFramework = getCuratorFramework();
        String path = curatorFramework.create().forPath("/test-node");
        log.info("curator create node :{}  successfully.", path);
    }

   // 递归创建子节点
    @Test
    public void testCreateWithParent() throws Exception {
        CuratorFramework curatorFramework = getCuratorFramework();

        String pathWithParent = "/node-parent/sub-node-1";
        String path = curatorFramework.create().creatingParentsIfNeeded().forPath(pathWithParent);
        log.info("curator create node :{}  successfully.", path);
    }



    // protection 模式，防止由于异常原因，导致僵尸节点
    @Test
    public void testCreateProtection() throws Exception {
        CuratorFramework curatorFramework = getCuratorFramework();
        String forPath = curatorFramework
                .create()
                .withProtection() //自动重试，创建节点的时候会生成一个唯一的UUID用于判断节点是否创建过  _c_75637698-5b06-407f-8d4e-8fd7f0fcb161-curator-node0000000016
                .withMode(CreateMode.EPHEMERAL_SEQUENTIAL).
                        forPath("/curator-node", "some-data".getBytes());
        log.info("curator create node :{}  successfully.", forPath);


    }



    //获取节点数据并注册监听，一次性
    @Test
    public void testGetData() throws Exception {
        CuratorFramework curatorFramework = getCuratorFramework();

        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getType() == Event.EventType.NodeDataChanged){
                    log.info("node {} data changed!",event.getPath());
                }
            }
        };
        byte[] bytes = curatorFramework.getData().usingWatcher(watcher).forPath("/curator-node");

    }


    //修改节点数据
    @Test
    public void testSetData() throws Exception {
        CuratorFramework curatorFramework = getCuratorFramework();
        curatorFramework.setData().forPath("/curator-node", "changed!".getBytes());
        byte[] bytes = curatorFramework.getData().forPath("/curator-node");
        log.info("get data from node /curator-node :{}  successfully.", new String(bytes));
    }

    //删除节点
    @Test
    public void testDelete() throws Exception {
        CuratorFramework curatorFramework = getCuratorFramework();
        String pathWithParent = "/node-parent";
        curatorFramework.delete().guaranteed().deletingChildrenIfNeeded().forPath(pathWithParent);
    }

    //列出节点下的所有子节点
    @Test
    public void testListChildren() throws Exception {
        CuratorFramework curatorFramework = getCuratorFramework();
        String pathWithParent = "/discovery/example";
        List<String> strings = curatorFramework.getChildren().forPath(pathWithParent);
        strings.forEach(System.out::println);
    }



}
