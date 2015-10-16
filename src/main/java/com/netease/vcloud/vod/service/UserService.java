package com.netease.vcloud.vod.service;

import com.google.gson.JsonObject;
import com.netease.vcloud.model.UpPwd;
import com.netease.vcloud.storage.dao.domain.ASKey;
import com.netease.vcloud.storage.dao.domain.AuthToken;
import com.netease.vcloud.storage.dao.domain.User;

/**
 * @author hzgaochao
 * @version 创建时间：Sep 6, 2015 类说明
 */
public interface UserService {
	// 注册新用户
	public void register(User user, JsonObject jo) throws Exception;

	// 发送用户激活码
	public void sendTokenMail(String email, JsonObject jo) throws Exception;

	// 激活用户
	public AuthToken activate(String token) throws Exception;

	// 发送更新密码验证码
	public void upPwdMail(String email) throws Exception;

	// 更新密码
	public void updatePwd(UpPwd upPwd, JsonObject jo) throws Exception;

	// 根据email查找用户
	public User findUserByEmail(String email) throws Exception;

	// 根据uid查找用户
	public User findUserByID(Long uid) throws Exception;

	// 登录认证
	public boolean login_auth(String uid, String sid) throws Exception;

	// 用户登录
	public void login(User user, JsonObject jo) throws Exception;

	// 用户登出
	public boolean logout(String sid) throws Exception;

	// 更新用户帐号信息
	public void upUserInfo(User user, JsonObject jo) throws Exception;

	// 获取安全信息
	public void getAuthInfo(ASKey askey, JsonObject jo) throws Exception;

	// 获取某对key
	public String getSecretKey(Long uid, String accessKey) throws Exception;

	// 添加一对key
	public void addAuthInfo(ASKey askey, JsonObject jo) throws Exception;

	// 删除一对key
	public void delAuthInfo(ASKey askey, JsonObject jo) throws Exception;

}
