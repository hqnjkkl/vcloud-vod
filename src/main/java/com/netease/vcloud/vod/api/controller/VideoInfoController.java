package com.netease.vcloud.vod.api.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.vcloud.CommonService;
import com.netease.vcloud.common.AdminCommon;
import com.netease.vcloud.model.Msg;
import com.netease.vcloud.model.NewChannel;
import com.netease.vcloud.storage.dao.domain.ChannelType;
import com.netease.vcloud.storage.dao.domain.DBRelatedParams;
import com.netease.vcloud.utils.MD5Utils;
import com.netease.vcloud.vod.api.util.AdminUtil;
import com.netease.vcloud.vod.service.ChannelService;
import com.netease.vcloud.vod.service.UserService;



@Controller("videoInfoController")
public class VideoInfoController {

	public static final Logger logger = Logger.getLogger(VideoInfoController.class);
	
	@Autowired
	private UserService us;
	
	private CommonService commonService = null;
	
	@PostConstruct
	public void init() {
		try {
			commonService = CommonService.getInstance();
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	

	@RequestMapping(value = "/vod/video/get", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Map<String, Object> getVideo(@RequestHeader ("uid") Long uid,  
													  @RequestHeader ("token") String token,  
													  @RequestHeader ("accesskey") String accesskey,  
												      @RequestHeader ("timestamp") Long timestamp,
													  @RequestBody String body) {
			
		Map<String, Object> mapRet = new HashMap<String, Object>();	
		
		/** 错误信息  */
		String msg = null;
					
		try {			
			
			/** token验证中所需的参数检查 */
			Map<String, Object> mapResp = null;
			int code = commonService.getTokenAuthStorage().checkTokenParams(token, accesskey, timestamp);
			if ((mapResp = (getCheckTokenParamsResp(code))) != null) {
				return mapResp;
			}
			
			/** 授权逻辑 (token验证) */
		    if ((msg = tokenAuth(uid, token, accesskey, timestamp, body)) != null) {		
				return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_AUTH_FAILED, Msg.TOKEN_AUTH_FAILED);
		    }				
			
			
			/** 成功，则返回成功码和包含具体信息的json */
			return AdminUtil.genSuccessResponse1(mapRet, AdminCommon.ICode.CODE_SUCCESS);
			
		} catch (Exception e) {
			msg = e.getMessage();
			logger.error(msg, e);
		}
		
		/** 失败，则返回错误码和错误信息(可选) */
		return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_ADD_FAILED, msg);
	}
	

	@RequestMapping(value = "/vod/video/list", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
	public @ResponseBody Map<String, Object> updateChannel(@RequestHeader ("uid") Long uid,  
			  											   @RequestHeader ("token") String token,  
			  											   @RequestHeader ("accesskey") String accesskey,  
			  											   @RequestHeader ("timestamp") Long timestamp,
			  											   @RequestBody String body) {
				
		Map<String, Object> mapRet = new HashMap<String, Object>();
		
		/** 错误信息 */
		String msg = null;
		
		try {		
			
			/** token验证中所需的参数检查 */
			Map<String, Object> mapResp = null;
			int code = commonService.getTokenAuthStorage().checkTokenParams(token, accesskey, timestamp);
			if ((mapResp = (getCheckTokenParamsResp(code))) != null) {
				return mapResp;
			}
			
			/** 授权逻辑 (token验证) */
		    if ((msg = tokenAuth(uid, token, accesskey, timestamp, body)) != null) {		
				return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_AUTH_FAILED, Msg.TOKEN_AUTH_FAILED);
		    }			
			
			
			/** 成功，则返回成功码和包含具体信息的json */
			return AdminUtil.genSuccessResponse1(mapRet, AdminCommon.ICode.CODE_SUCCESS);
			
		} catch (Exception e) {
			msg = e.getMessage();
			logger.error(msg, e);
		}

		/** 失败，则返回错误码和错误信息(可选) */
		return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_UPDATE_FAILED, msg);
		
	}
	
	
	/**
	 * token验证
	 * @param uid
	 * @param token
	 * @param accesskey
	 * @param timestamp
	 * @param reqBody
	 * @return
	 * @throws Exception
	 */
	private String tokenAuth(Long uid, 
							 String token, 
							 String accesskey, 
							 Long timestamp,
							 String reqBody) throws Exception {

		/** 获取secretkey */
		String secretkey = us.getSecretKey(uid, accesskey);
		
		if (secretkey == null) {			
			return Msg.TOKEN_AUTH_FAILED;
		}
		
		String result = commonService.getTokenAuthStorage()
									 .tokenAuth(token, secretkey, timestamp, reqBody);
		
		return result;
		
	}
	
	private Map<String, Object> getCheckTokenParamsResp(int code) {
		
		if (code == AdminCommon.ICode.CODE_TOKEN_NOT_EXIST) {
			return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_TOKEN_NOT_EXIST, Msg.TOKEN_NOT_EXIST);
		}
		if (code == AdminCommon.ICode.CODE_ACCESSKEY_NOT_EXIST) {
			return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_ACCESSKEY_NOT_EXIST, Msg.ACCESSKEY_NOT_EXIST);
		}
		if (code == AdminCommon.ICode.CODE_TIMESTAMP_NOT_EXIST) {
			return AdminUtil.gen400ErrorResonse1(AdminCommon.ICode.CODE_TIMESTAMP_NOT_EXIST, Msg.TIMESTAMP_NOT_EXIST);
		}
		
		return null;
	}
}



















