package com.netease.vcloud.vod.service.impl;

import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;

@Service
public class AdminService {

	public void init() {
		// TODO:
	}

	/**
	 * 创建频道
	 * 
	 * @param uid
	 * @param name
	 * @param cid
	 * @param pid
	 * @param jo
	 * @throws Exception
	 */
	public void addChannel(String uid, String name, Long cid, Long pid, JsonObject jo) throws Exception {
		long ctime = System.currentTimeMillis();
		String accessurl = null;

		// TODO: 添加频道实现

		jo.addProperty("ctime", ctime);
		jo.addProperty("accessurl", accessurl);
	}
}
