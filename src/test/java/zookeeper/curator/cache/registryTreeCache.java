package zookeeper.curator.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.junit.Test;
import zookeeper.curator.CuratorBase;

/**
 * TreeCache是NodeCache和PathChildrenCache的结合，可以对整个目录进行监听。TreeCache可以设置监听的深度，默认应该是Integer.MAX_VALUE。
 * 事件类型：NODE_ADDED, 路径：/myinfo3
 * 事件类型：NODE_UPDATED, 路径：/myinfo3
 * 事件类型：NODE_ADDED, 路径：/myinfo3/child
 * @author 程治玮
 * @since 2021/1/25 8:47 下午
 */
public class registryTreeCache extends CuratorBase {

    @Test
    public void registryTreeCache() {
        CuratorFramework curatorFramework = getCuratorFramework();
        TreeCache treeCache = TreeCache.newBuilder(curatorFramework, "/myinfo3").setCacheData(true).setMaxDepth(10).build();
        try {
            treeCache.start();
            // 添加错误监听
            treeCache.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {

                @Override
                public void unhandledError(String message, Throwable e) {
                    System.out.println("错误信息：" + e.getMessage());
                }
            });
            // 添加节点变化监听器
            treeCache.getListenable().addListener(new TreeCacheListener() {

                @Override
                public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                    System.out.println("事件类型："+event.getType() + ", 路径：" + event.getData().getPath() );
                }
            });
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
