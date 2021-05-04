package zookeeper.curator.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.junit.Test;
import zookeeper.curator.CuratorBase;

/**
 * NodeCache是用来监听节点数据的变化，包括增删查改。但是NodeCache并不能获取是哪种事件类型。
 * @author 程治玮
 * @since 2021/1/25 8:35 下午
 */

//循环监听节点数据变化
public class registryNodeCache extends CuratorBase {
    @Test
    public void registryNodeCache() {
        CuratorFramework curatorFramework = getCuratorFramework();
        final NodeCache nodeCache = new NodeCache(curatorFramework, "/myinfo");
        try {
            // 如果设置为true,则会缓存当前节点信息，否则只有在节点改变时才会获取节点数据
            nodeCache.start(false);
            nodeCache.getListenable().addListener(new NodeCacheListener() {
                // 这里监听的是节点的增删查改，而不是event事件
                @Override
                public void nodeChanged() throws Exception {
                    if (nodeCache.getCurrentData().getData() != null) {
                        System.out.println("当前节点：" + new String(nodeCache.getCurrentData().getData()));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
