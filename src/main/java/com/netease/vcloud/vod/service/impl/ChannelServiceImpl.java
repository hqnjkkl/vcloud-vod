package com.netease.vcloud.vod.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netease.vcloud.framework.SysConfig;
import com.netease.vcloud.model.Msg;
import com.netease.vcloud.model.NewChannel;
import com.netease.vcloud.storage.cache.redis.RedisDevice;
import com.netease.vcloud.storage.dao.ChannelDao;
import com.netease.vcloud.storage.dao.domain.Channel;
import com.netease.vcloud.storage.dao.domain.ChannelStatus;
import com.netease.vcloud.utils.MD5Utils;
import com.netease.vcloud.vod.service.ChannelService;




/**
 * @Title: ChannelServiceImpl.java
 * @Package com.netease.vcloud.service.impl
 * @Description: Channel Controller
 * @Company Netease
 * @author hzxuechen@corp.netease.com
 * @date 2015-9-14 18:00:00
 */
@Service
public class ChannelServiceImpl implements ChannelService {
	
	private static Logger logger = Logger.getLogger(ChannelServiceImpl.class);
	
	/** redis操作类 */
	private static RedisDevice rd;
	
	/** 静态字段  */
	private static final String PUSH_PREFIX;
	private static final String HTTP_PUll_PREFIX;
	private static final String HLS_PULL_PREFIX;
	private static final String RTMP_PULL_PREFIX;
	private static final String CDN_SECRETKEY;
	private static final String CDN_VALIDITY;
	
	/** 初始化 */
	static {
		
		rd = new RedisDevice();
		
		/**
		 * 从配置文件中获取配置项
		 */
		PUSH_PREFIX = SysConfig.getString("push_prefix");
		HTTP_PUll_PREFIX = SysConfig.getString("http_pull_prefix");
		HLS_PULL_PREFIX = SysConfig.getString("hls_pull_prefix");
		RTMP_PULL_PREFIX = SysConfig.getString("rtmp_pull_prefix");
		CDN_SECRETKEY = SysConfig.getString("cdn_secretkey");
		CDN_VALIDITY = SysConfig.getString("cdn_validity");
	}
	
	@Autowired
	ChannelDao channelDao;

	@Override
	public void init() {
	
	}

	/**
	 * 创建频道
	 */
	@Override
	public void addChannel(NewChannel newChannel, Map<String, Object> mapRet) throws Exception {
		
		/** 获取需要持久化字段 */
		long uid = newChannel.getUid();
		String name = newChannel.getName();
		int type = newChannel.getType();

		/** 判断频道名称是否已经存在，若存在，则抛出异常 */
		if (isChannelNameExist(uid, name, null)) {
			throw new Exception(Msg.CHANNEL_NAME_ALREADY_EXIST);
		}
		
		/** 获取频道ID */
		String cid = allocId();		
		
		/** 获取当前时间戳 */
		long ctime = System.currentTimeMillis();	
		
		mapRet.put("ctime", ctime);
		
		
		
		Channel channel = new Channel();

		channel.setCid(cid);
		channel.setUid(uid);
		channel.setName(name);
		channel.setStatus(ChannelStatus.NORMAL.ordinal());
		channel.setType(type);
		channel.setCtime(ctime);
		
		/** 添加数据库记录 */
		channelDao.addChannel(channel);
		
		logger.info("********ChannelService addChannel*******");
		
		/** 获取直播地址 */
		getLiveAddress(uid, cid, mapRet);

	}

	/**
	 * 编辑频道
	 */
	@Override
	public void updateChannel(NewChannel newChannel, Map<String, Object> mapRet) throws Exception {
		
		long uid = newChannel.getUid();
		String cid = newChannel.getCid();
		String name = newChannel.getName();
		int type = newChannel.getType();	
		
		Channel channel = new Channel();
		channel.setUid(uid);
		channel.setCid(cid);
		channel.setName(name);
		channel.setType(type);
		
		/** 更新数据库记录 */
		channelDao.updateChannel(channel);
		
		
		/** 删除redis中相应缓存 */
		rd.delChannel(uid + cid);
		
	}

	/**
	 * 删除频道
	 */
	@Override
	public void deleteChannel(long uid, String cid, Map<String, Object> mapRet) throws Exception {
		
		/** 删除数据库记录 */
	    channelDao.deleteChannel(uid, cid);
		
	    /** 删除redis中相应缓存 */
	    rd.delChannel(uid + cid);
	}

	/**
	 * 获取频道列表
	 */
	@Override
	public boolean getChannelList(NewChannel newChannel, Map<String, Object> mapRet) throws Exception {
		
	    /** 从数据库中查找, 频道列表不缓存到redis */
		List<Channel> channelList = channelDao.getChannelList(newChannel);
		
		if (channelList == null) {
			return false;
		}
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		/** 将得到的Channel一个一个放入ArrayList */
		for(Channel channel : channelList) {
			Map<String, Object> mapChannel = new HashMap<String, Object>();
			mapChannel = mapResp(channel, mapChannel);
			list.add(mapChannel);
		}
		
		mapRet.put("list", list);
		
		return true;
	}
	

	/**
	 * 获取频道信息
	 */
	@Override
	public boolean getChannelStats(long uid, String name, String cid, Map<String, Object> mapRet) throws Exception {
		
		/** 先从缓存中查找相应的频道信息 */
		Channel channel = rd.getChannel(uid + cid);
		
		if (channel != null) {
			mapRet = mapResp(channel, mapRet);
			return true;
		}
		
		/** 如果缓存中没有相应的频道信息，则从数据库中查找 */
		channel = channelDao.getChannelStatsById(uid, cid);
		
		/** 判断获取结果是否为空 */
		if (channel == null) {
			return false;
		}
		
		mapRet = mapResp(channel, mapRet);
		
		/** 将数据库中查到的信息存入redis */
		rd.setChannel(uid + cid, channel);
		
		return true;		
	} 

	
	/**
	 * 先判断cid获取直播地址
	 */
	@Override
	public boolean getLiveAddress(long uid, String cid, Map<String, Object> mapRet) throws Exception {
			
		if (!isChannelIdExist(uid, cid)) {
			return false;
		}
		
		makeLiveAddress(uid, cid, mapRet);
		
		return true;
	}
	
	/**
	 * 构造推拉流地址
	 */
	private void makeLiveAddress(long uid, String cid, Map<String, Object> mapRet) throws Exception {
		
		/** 获取当前时间 */
		long currenttime = System.currentTimeMillis();
		
		/** 推流地址失效时间 */
 		long endtime = currenttime + Long.parseLong(CDN_VALIDITY);
		
		/** 推拉流的url前缀 */
		StringBuffer sbPush = new StringBuffer(PUSH_PREFIX);
		StringBuffer sbHttpPull = new StringBuffer(HTTP_PUll_PREFIX);
		StringBuffer sbHlsPull = new StringBuffer(HLS_PULL_PREFIX);
		StringBuffer sbRtmpPull = new StringBuffer(RTMP_PULL_PREFIX);

		/** 构造推流的Url */
		sbPush.append("/")
			  .append(uid)
			  .append("/")
			  .append(cid);
						
		/** 构造拉流地址(http) */
		sbHttpPull.append("/")
			  	  .append(uid)
			      .append("/")
			      .append(cid)
			      .append(".flv");
		
		/** 构造拉流地址(hls) */
		sbHlsPull.append("/")
				 .append(uid)
		         .append("/")
		         .append(cid)
		         .append("/playlist.m3u8");
		
		/** 构造拉流地址(rtmp) */
		sbRtmpPull.append("/")
				  .append(uid)
				  .append("/")
				  .append(cid);
		
		/** 构造推流防盗链 */
		String WsSecret= getWsSecret(uid, cid, CDN_SECRETKEY, endtime);
		sbPush.append("?")
			  .append("wsSecret=")
			  .append(WsSecret)
			  .append("&wsTime=")
			  .append(endtime);
		
		mapRet.put("pushUrl", sbPush.toString());
		mapRet.put("httpPullUrl", sbHttpPull.toString());
		mapRet.put("hlsPullUrl", sbHlsPull.toString());
		mapRet.put("rtmpPullUrl", sbRtmpPull.toString());
		
	}
	
	/**
	 * 构造推流防盗链
	 */
	private String getWsSecret(long uid, String cid, String secret, long endtime) throws Exception {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(secret)
			  .append("/live/")
			  .append(uid)
			  .append("/")
			  .append(cid)
			  .append(endtime);
		
		String secretStr = MD5Utils.encoderByMd5With32Bit(buffer.toString());
		
		return secretStr;
	}
	
	/**
	 * 生成频道ID
	 */
	private String allocId() throws Exception {
		
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString();
		uuidStr = uuidStr.replace("-", "");
		
		return uuidStr;

	}	
	
	/**
	 * 将频道信息装入json
	 * @param channel
	 * @param mapRet
	 * @return
	 */
	private Map<String, Object> mapResp(Channel channel, Map<String, Object> map) throws Exception {		

		map.put("cid", channel.getCid());
		map.put("name", channel.getName());
		map.put("status", channel.getStatus());
		map.put("type", channel.getType()); 
		map.put("ctime", channel.getCtime());
		
		return map;
	}
	
	/**
	 * 判断频道名称是否已经存在
	 * @param uid
	 * @param name
	 * @param cid
	 * @return
	 * @throws Exception
	 */
	private boolean isChannelNameExist(long uid, String name, String cid) throws Exception {
		
		/** 若缓存和数据库中都不存在该频道名称，则返回false */
		if ( (rd.getChannel(uid + cid) == null) && (channelDao.getChannelStatsByName(uid, name) == null) ) {
			return false;
		}
		/** 否则，返回true */
		return true;
	}
	
	private boolean isChannelIdExist(long uid, String cid) throws Exception {
		
		/** 若缓存和数据库中都不存在该频道Id，则返回false */
		if ( (rd.getChannel(uid + cid) == null) && (channelDao.getChannelStatsById(uid, cid) == null) ) {
			return false;
		}
		/** 否则，返回true */
		return true;
	}

	
}
