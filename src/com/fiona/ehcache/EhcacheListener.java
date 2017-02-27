package com.fiona.ehcache;

import static org.slf4j.LoggerFactory.getLogger;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.ehcache.event.EventType;
import org.slf4j.Logger;
/**
 * Ehcache的监听类
 * 
 * 要求Ehcache的key的命名是：alias.+……
 * 
 * @author wanderer
 *
 */
public class EhcacheListener<K, V> implements CacheEventListener<K, V> {

	private static final Logger LOGGER = getLogger(EhcacheUtil.class);
	private EhcacheManager ehcacheManager = new EhcacheManager();

	public void onEvent(CacheEvent<K, V> event) {

		LOGGER.debug("cacheEventType:" + event.getType().name());
		LOGGER.debug("cacheKey:" + event.getKey());
		LOGGER.debug("cacheNewValue:" + event.getNewValue());
		LOGGER.debug("cacheOldValue:" + event.getOldValue());

		String key = event.getKey().toString();
		String alias = key.substring(0, key.indexOf("."));
		// 如果不是按照命名规则的key，无法纳入管理
		if (null == alias || alias.equals("")) {
			return;
		}

		if (event.getType() == EventType.CREATED
				|| event.getType() == EventType.UPDATED) {
			// 新增缓存值
			ehcacheManager.putKeyCache(alias, key);
		} else {
			// 删除缓存值
			ehcacheManager.removeKeyCache(alias, key);
		}

		return;
	}

}
