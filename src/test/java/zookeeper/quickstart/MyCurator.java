package zookeeper.quickstart;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Curator基本使用quickstart
 * @author 程治玮
 * @since 2021/5/4 7:13 下午
 */
public class MyCurator {

    //Zookeeper地址
    private static final String CONNECT_STR = "localhost:2181";

    //使用Fluent风格的Api创建会话
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); //重试策略
    CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString(CONNECT_STR).retryPolicy(retryPolicy).build();

    //启动客户端
    @Before
    public void startClient() {
        client.start();
    }

    /**
     * 创建数据节点
     * PERSISTENT：持久化节点（默认）
     * PERSISTENT_SEQUENTIAL：持久化顺序节点
     * EPHEMERAL：临时节点
     * EPHEMERAL_SEQUENTIAL：临时顺序节点
     */

    //创建一个节点，初始内容为空
    @Test
    public void createPersistentNode() throws Exception {
        //创建一个节点，初始内容为空
        client.create().forPath("/cr7-path");
    }

    //创建一个节点，附带初始化内容
    @Test
    public void createPersistentNodeWithContent() throws Exception {
        client.create().forPath("/cr7-path-content", "mycontent".getBytes());
    }

    @Test
    //创建一个节点，指定创建模式（临时节点），附带初始化内容
    public void createEphemeralNode() throws Exception {
        client.create().withMode(CreateMode.EPHEMERAL).forPath("/cr7-path-ephemeral");
    }

    //创建一个节点，指定创建模式（临时节点），附带初始化内容，并且自动递归创建父节点
    @Test
    public void createEphemeralNodeRecursiveWithContent() throws Exception {
        client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/s1/cr7-path-ephemeral-parent", "mycontent".getBytes());
    }

    /**
     * 删除数据节点
     */
    //删除数据节点
    //注意，此方法只能删除叶子节点，否则会抛出异常。
    @Test
    public void deleteNode() throws Exception {
        client.delete().forPath("/cr7-path");
    }

    //删除一个节点，并且递归删除其所有的子节点
    @Test
    public void deleteNodeRecursive() throws Exception {
        client.delete().deletingChildrenIfNeeded().forPath("/s1");
    }

    //删除一个节点，强制指定版本进行删除
    //指定的version必须要等于节点的dataVersion才能删除成功，否则会抛出异常
    @Test
    public void deleteWithVersion() throws Exception {
        client.delete().withVersion(0).forPath("/cr7-path-content");
    }

    //删除一个节点，强制保证删除
    //guaranteed()接口是一个保障措施，只要客户端会话有效，那么Curator会在后台持续进行删除操作，直到删除节点成功。
    @Test
    public void deleteGuaranteed() throws Exception {
        client.delete().guaranteed().forPath("/cr7-path");
    }

    /**
     * 读取节点数据
     */
    @Test
    public void getNode() throws Exception {
        byte[] bytes = client.getData().forPath("/cr7-path-content");
        System.out.println(new String(bytes));
    }

    /**
     * 更新节点数据
     */
    //更新节点数据
    @Test
    public void updateNode() throws Exception {
        client.setData().forPath("/cr7-path-content", "newdata".getBytes());
    }

    //更新一个节点的数据内容，强制指定版本进行更新
//    [zk: localhost:2181(CONNECTED) 26] stat /cr7-path-content
//    cZxid = 0x37
//    ctime = Tue May 04 19:36:46 CST 2021
//    mZxid = 0x44
//    mtime = Tue May 04 19:42:53 CST 2021
//    pZxid = 0x37
//    cversion = 0
//    dataVersion = 1   //更新的版本要等于dataVersion才能成功
//    aclVersion = 0
//    ephemeralOwner = 0x0
//    dataLength = 7
//    numChildren = 0
    @Test
    public void updateNodeWithVersion() throws Exception {
        client.setData().withVersion(1).forPath("/cr7-path-content", "newdata2".getBytes());
    }

    /**
     * 检查节点是否存在
     */
    @Test
    public void checkNode() throws Exception {
        //该方法返回一个Stat实例，可以使用stat实例进一步获取节点的信息，如cZxid，ctime等
        //如果节点不存在则返回null
        Stat stat = client.checkExists().forPath("/cr7-path-content");
        if (stat != null) {
            System.out.println(stat.getCzxid());
        }
    }

    /**
     * 获取某个节点的所有子节点路径
     */
    @Test
    public void getChildNode() throws Exception {
        List<String> strings = client.getChildren().forPath("/s1");
        for (String s : strings) {
            System.out.println(s);
        }
    }

    /**
     * 事务
     */
    //CuratorFramework的实例包含inTransaction( )接口方法，调用此方法开启一个ZooKeeper事务.
    //可以复合create, setData, check, and/or delete 等操作然后调用commit()作为一个原子操作提交。
    //先创建一个节点/cr7-transaction，然后创建子节点/cr7-transaction/cr7-child，并赋值，最后给/cr7-transaction节点赋值
    @Test
    public void transaction() throws Exception {
        client.inTransaction().create().forPath("/cr7-transaction").and()
                .create().withMode(CreateMode.EPHEMERAL).forPath("/cr7-transaction/cr7-child","mydata".getBytes())
                .and().setData().withVersion(0).forPath("/cr7-transaction","mydata1".getBytes())
                .and().commit();
    }

    /**
     * 异步
     */
    //上面提到的创建、删除、更新、读取等方法都是同步的，Curator提供异步接口，
    // 引入了BackgroundCallback接口用于处理异步接口调用之后服务端返回的结果信息。
    // BackgroundCallback接口中一个重要的回调值为CuratorEvent，里面包含事件类型、响应吗和节点的详细信息。
    @Test
    public void asyncCreateNode() throws Exception {
        Executor executor = Executors.newFixedThreadPool(2);
        client.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground((curatorFramework, curatorEvent) -> { System.out.println(String.format("eventType:%s,resultCode:%s",curatorEvent.getType(),curatorEvent.getResultCode()));
                },executor)
                .forPath("/cr7-async");
        Thread.sleep(10000000);
    }

}
