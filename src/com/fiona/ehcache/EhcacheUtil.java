package com.fiona.ehcache;


import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
/**
 * Ehcache工具类
 * @author wanderer
 *
 */
public class EhcacheUtil  {

	private static CacheManager cacheManager;
	private static final Logger LOGGER = getLogger(EhcacheUtil.class);
	private static EhcacheManager ehcacheManager = new EhcacheManager();

	/**
	 * 静态代码块，根据ehcache.xml初始化cacheManager，并且把所有缓存库初始化完成。
	 */
	static {
		URL url = EhcacheUtil.class.getResource("/ehcache.xml");
		Configuration xmlConfig = new XmlConfiguration(url);
		cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
		cacheManager.init();
		LOGGER.info("cacheManager初始化完毕！");
	}

	/**
	 * @return the cacheManager
	 */
	public static CacheManager getCacheManager() {
		return cacheManager;
	}

	/**
	 * 清除缓存库
	 * 
	 * @param alias
	 *            缓存库的名字
	 * @param keyType
	 *            key的数据类型
	 * @param valueType
	 *            value的数据类型
	 */
	public static void clear(String alias, java.lang.Class<?> keyType,
			java.lang.Class<?> valueType) {
		Cache<?, ?> cache = getCacheManager().getCache(alias, keyType,
				valueType);
		cache.clear();
		ehcacheManager.clearKeyCache(alias);
		LOGGER.info("缓存库：[" + alias + "]已经清除完毕！");
	}

	/**
	 * 清除缓存库，默认缓存库的key和value的数据类型都是java.lang.String
	 * 
	 * @param alias
	 *            缓存库的名字
	 */
	public static void clear(String alias) {
		clear(alias, String.class, String.class);
	}

	/**
	 * 写入缓存
	 * 
	 * @param alias
	 *            缓存库的名字
	 * @param keyType
	 *            key的数据类型
	 * @param key
	 *            具体缓存标识
	 * @param valueType
	 *            value的数据类型
	 * @param value
	 *            具体的缓存值
	 */
	@SuppressWarnings("unchecked")
	public static <K extends java.lang.Object, V extends java.lang.Object> void put(
			String alias, java.lang.Class<?> keyType, K key,
			java.lang.Class<?> valueType, V value) {
		Cache<K, V> cache = (Cache<K, V>) getCacheManager().getCache(alias,
				keyType, valueType);
		cache.put(key, value);
		LOGGER.info("缓存库：[" + alias + "]已经加载完毕！ key：" + key.toString()
				+ " value：" + value.toString());
	}

	/**
	 * 写入缓存，默认key和value的数据类型都是java.lang.String
	 * 
	 * @param alias
	 *            缓存库的名字
	 * @param key
	 *            具体缓存标识
	 * @param value
	 *            具体的缓存值
	 */
	public static void put(String alias, String key, String value) {
		put(alias, String.class, key, String.class, value);
	}

	/**
	 * 取得缓存数据
	 * 
	 * @param alias
	 *            缓存库的名字
	 * @param keyType
	 *            key的数据类型
	 * @param key
	 *            具体缓存标识
	 * @param valueType
	 *            value的数据类型
	 * @return 缓存值，如果出现任何异常返回null
	 */
	@SuppressWarnings("unchecked")
	public static <K extends java.lang.Object, V extends java.lang.Object> V get(
			String alias, java.lang.Class<?> keyType, K key,
			java.lang.Class<?> valueType) {

		try {
			Cache<K, V> cache = (Cache<K, V>) getCacheManager().getCache(alias,
					keyType, valueType);
			LOGGER.info("缓存库：[" + alias + "]已经获取完毕！ key：" + key.toString());
			return cache.get(key);
		} catch (Exception e) {// 出现异常反馈null
			return null;
		}
	}

	/**
	 * 取得已经缓存的数据，默认key和value的数据类型都是java.lang.String
	 * 
	 * @param alias
	 *            缓存库的名字
	 * @param key
	 *            具体缓存标识
	 * @return 缓存值，如果出现任何异常返回null
	 */
	public static String get(String alias, String key) {
		return get(alias, String.class, key, String.class);
	}

	@SuppressWarnings("unchecked")
	public static <K extends java.lang.Object, V extends java.lang.Object> void remove(String alias, java.lang.Class<?> keyType,
			java.lang.Class<?> valueType,K key) {
		Cache<K, V> cache = (Cache<K, V>) getCacheManager().getCache(alias, keyType,
				valueType);
		cache.remove(key);
		LOGGER.info("缓存库：[" + alias + "]的缓存值已经移除完毕！key：" + key.toString());
	}
	
	public static void remove(String alias, String key) {
		remove(alias,String.class,String.class,key);
	}

}

