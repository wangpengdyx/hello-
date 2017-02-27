package com.fiona.ehcache;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;
/**
 * ehcache的缓存库alias、key值管理类，配合ehcache的监听类使用
 * @author wanderer
 *
 */
public class EhcacheManager {

	private static final Logger LOGGER = getLogger(EhcacheUtil.class);

	/**
	 * 写入监控缓存库
	 * 管理各个缓存库的key值写入到管理缓存库（keyCache）中
	 * @param alias 正在处理的缓存库alias值
	 * @param key 正在处理的缓存库的key值 ，此处要求key值转换为String类型传入
	 */
	protected void putKeyCache(String alias, String key) {
		String localAlias = "keyCache." + alias;
		String keyCacheValue = EhcacheUtil.get("keyCache", localAlias);
		if (null == keyCacheValue)
			keyCacheValue = "";

		if (keyCacheValue.indexOf(key + "&") == -1) 
			EhcacheUtil.put("keyCache", localAlias, keyCacheValue + key + "&");
		
		LOGGER.debug("写入监控缓存keyCach。alias:" + alias + " key:" + key);
		
		return;
	}
	/**
	 * 移除监控缓存库
	 * 管理各个缓存库的key值移除管理缓存库（keyCache）
	 * @param alias 正在处理的缓存库alias值
	 * @param key 正在处理的缓存库的key值 ，此处要求key值转换为String类型传入
	 */
	protected void removeKeyCache(String alias, String key) {
		String localAlias = "keyCache." + alias;
		String keyCacheValue = EhcacheUtil.get("keyCache", localAlias);
		keyCacheValue = keyCacheValue.replace(key + "&","");
		EhcacheUtil.put("keyCache", localAlias, keyCacheValue);
		
		LOGGER.debug("移除监控缓存keyCach。alias:" + alias + " key:" + key);
		
		return;
	}
	/**
	 * 清除监控缓存库
	 * 把各缓存库的key值全部清除出管理缓存库（keyCache）
	 * @param alias 需要清除缓存库alias值
	 */
	protected void clearKeyCache(String alias) {
		EhcacheUtil.remove("keyCache", "keyCache." + alias);
		
		LOGGER.debug("清除监控缓存keyCach。alias:" + alias);
		
		return;
	}
	/**
	 * 获得各缓存库的key值
	 * @param alias 需要操作的缓存库
	 * @return 缓存库的key值用"&"字符间隔合并。如果缓存库中无缓存，返回null。 此处已经把key值转换为String处理。
	 */
	public String getKeyCacheString(String alias) {
		String aliasKeys = EhcacheUtil.get("keyCache", "keyCache." + alias);
		LOGGER.debug("获取缓存库[" + alias + "]的缓存值:" + aliasKeys);
		return aliasKeys;
	}
	/**
	 * 获得各缓存库的key值
	 * @param alias 需要操作的缓存库
	 * @return 缓存库的key值数组。如果缓存库中无缓存，返回null。 此处已经把key值转换为String处理。
	 */
	public String[] getKeyCache(String alias) {
		return getKeyCacheString(alias).split("&");
	}

	/**
	 * 获得各缓存库名字
	 * @return 缓存库名字用"&"字符间隔合并。如果ehcache.xml中未配置任何缓存库，返回null。
	 */
	public String[] getAlias() {
		return getKeyCache("keyCache");
	}
	/**
	 * 获得各缓存库名字
	 * @param alias 需要操作的缓存库
	 * @return 缓存库的名字数组。如果缓存库中无缓存，返回null。
	 */
	public String getAliasString() {
		return getKeyCacheString("keyCache");
	}
	
}
