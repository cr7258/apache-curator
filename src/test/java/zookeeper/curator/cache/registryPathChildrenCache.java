package zookeeper.curator.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.junit.Test;
import zookeeper.curator.CuratorBase;

/**
 * PathChildrenCache是对指定路径的一级子节点进行监听（如果是指定目录的子节点的子节点修改，就监听不到了）
 * 事件类型：CONNECTION_RECONNECTED
 * 事件类型：CHILD_ADDED
 * 事件类型：CHILD_UPDATED
 *
 * @author 程治玮
 * @since 2021/1/25 8:45 下午
 */
public class registryPathChildrenCache extends CuratorBase {
    @Test
    public void registryPathChildrenCache() {
        CuratorFramework curatorFramework = getCuratorFramework();
        // cacheData表示是否将数据缓存到本地
        final PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, "/myinfo2", true);
        try {
            pathChildrenCache.start(PathChildrenCache.StartMode.NORMAL);
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {

                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    System.out.println("事件类型：" + event.getType());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
