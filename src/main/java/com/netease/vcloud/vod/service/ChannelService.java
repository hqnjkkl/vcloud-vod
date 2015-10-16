package com.netease.vcloud.vod.service;

import java.util.Map;

import com.netease.vcloud.model.NewChannel;



/**
 * @Title: ChannelService.java
 * @Package com.netease.vcloud.service
 * @Description: Channel Service
 * @Company Netease
 * @author hzxuechen@corp.netease.com
 * @date 2015-9-14 18:00:00
 */
public interface ChannelService {

	/**
	 * initialization
	 */
	public void init();
	
	/**
	 * create channel
	 * @param uid
	 * @param name
	 * @param cid
	 * @param map
	 */
	public void addChannel(NewChannel newChannel, Map<String, Object> map) throws Exception;
	
	/**
	 * update channel
	 * @param name
	 * @param cid
	 * @param map
	 */
	public void updateChannel(NewChannel newChannel, Map<String, Object> map) throws Exception;
	
	/**
	 * delete channel
	 * @param uid
	 * @param cid
	 * @param map
	 */
	public void deleteChannel(long uid, String cid, Map<String, Object> map) throws Exception;
	
	/**
	 * get channel list
	 * @param newChannel
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public boolean getChannelList(NewChannel newChannel, Map<String, Object> map) throws Exception;
	
	/**
	 * get channel status
	 * @param uid
	 * @param name
	 * @param cid
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public boolean getChannelStats(long uid, String name, String cid, Map<String, Object> map) throws Exception;
	
	/**
	 * get live address (one push, three pull)
	 * @param uid
	 * @param cid
	 * @param map
	 * @return
	 * @throws Exception
	 */
	public boolean getLiveAddress(long uid, String cid, Map<String, Object> map) throws Exception;
	
}






