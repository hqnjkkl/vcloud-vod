package com.netease.vcloud.vod.service.impl;

import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.netease.vcloud.common.ErrorCode;
import com.netease.vcloud.model.UpPwd;
import com.netease.vcloud.storage.cache.redis.UserRedisUtil;
import com.netease.vcloud.storage.dao.ASKeyDao;
import com.netease.vcloud.storage.dao.AuthTokenDao;
import com.netease.vcloud.storage.dao.UserDao;
import com.netease.vcloud.storage.dao.domain.ASKey;
import com.netease.vcloud.storage.dao.domain.AuthToken;
import com.netease.vcloud.storage.dao.domain.User;
import com.netease.vcloud.utils.Constants;
import com.netease.vcloud.utils.EmailAuthentication;
import com.netease.vcloud.utils.GenerateASKey;
import com.netease.vcloud.utils.MD5Utils;
import com.netease.vcloud.utils.RandomPassword;
import com.netease.vcloud.vod.service.UserService;

/**
 * @author hzgaochao
 * @version 创建时间：Sep 22, 2015 类说明
 */

@Service("userServiceBean")
public class UserServiceImpl implements UserService {
	private static Logger logger = Logger.getLogger(UserServiceImpl.class);

	@Autowired
	UserRedisUtil redisUtil;

	@Autowired
	UserDao userDao;

	@Autowired
	AuthTokenDao authTokenDao;

	@Autowired
	ASKeyDao aSKeyDao;

	/*
	 * 注册用户帐号
	 * 
	 * @pre NewUser.valid(), 校验注册验证码
	 * 
	 * @param User
	 * 
	 * @param JsonObject(装配返回结果,ret:{uid, ctime})
	 * 
	 * @throw Exception 数据库/发邮件操作异常
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#register(com.netease.vcloud.dao.
	 * domain.User, com.google.gson.JsonObject)
	 */
	@Override
	@Transactional
	public void register(User user, JsonObject jo) throws Exception {
		String nce_password = RandomPassword.genRandomNum(15);
		String nce_tenantId = UUID.randomUUID().toString().replaceAll("-", "");

		// 持久化UserInfo到DB
		user.setNce_password(nce_password);
		user.setNce_tenantId(nce_tenantId);
		user.setCreatetime(System.currentTimeMillis() / 1000);
		userDao.addUser(user);
		user = userDao.getUser(user.getEmail());

		// 持久化Token到DB
		String token = MD5Utils.encoderByMd5With32Bit(user.getNce_tenantId() + user.getCreatetime());
		AuthToken authToken = new AuthToken();
		authToken.setUid(user.getUid());
		authToken.setEmail(user.getEmail());
		authToken.setToken(token);
		authTokenDao.addToken(authToken);

		// 发送邮件
		EmailAuthentication.sendAuthMail(user.getEmail(), token);

		// 装配返回结果
		JsonObject ret = new JsonObject();
		ret.addProperty("uid", user.getUid());
		ret.addProperty("ctime", user.getCreatetime());
		jo.add("ret", ret);
	}

	/*
	 * 发送激活token到用户邮箱
	 * 
	 * @pre email格式
	 * 
	 * @param email(String)
	 * 
	 * @param JsonObject(装配返回结果,code)
	 * 
	 * @throw Exception 数据库/redis操作异常
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#sendTokenMail(java.lang.String)
	 */
	@Override
	public void sendTokenMail(String email, JsonObject jo) throws Exception {
		User user = findUserByEmail(email);
		if (user == null) {
			jo.addProperty("code", ErrorCode.USER_NOTEXIST);
			jo.addProperty("msg", "用户不存在");
			return;
		}

		if (user.getStats() == 1) {
			jo.addProperty("code", ErrorCode.USER_ALREADY_ACTIVE);
			jo.addProperty("msg", "用户已激活");
			return;
		}

		AuthToken authToken = authTokenDao.getTokenByUid(user.getUid());
		// 发送邮件
		EmailAuthentication.sendAuthMail(authToken.getEmail(), authToken.getToken());
		jo.addProperty("code", 200);
	}

	/*
	 * 激活用户帐号
	 * 
	 * @param token(String)
	 * 
	 * @throw Exception 数据库/redis操作异常
	 * 
	 * @return AuthToken
	 * 
	 * @see com.netease.vcloud.service.UserService#activate(java.lang.String)
	 */
	@Override
	@Transactional
	public AuthToken activate(String token) throws Exception {
		// 验证token是否有效
		AuthToken authToken = authTokenDao.getToken(token);
		if (authToken == null) {
			return null;
		}

		// 获取accessKey和secretKey
		ASKey askey = GenerateASKey.getPairKey();
		askey.setUid(authToken.getUid());

		// 更新User表激活用户
		userDao.activeUser(authToken.getUid());
		// 更新Auth表添加accessKey和secretKey
		aSKeyDao.addAuth(askey);
		// 删除Redis中的相应User
		redisUtil.delUser(authToken.getEmail(), authToken.getUid());
		// 删除Token
		authTokenDao.deleteToken(authToken.getUid());

		return authToken;
	}

	/*
	 * 用户登录
	 * 
	 * @pre 用户存在
	 * 
	 * @param User(String)
	 * 
	 * @param JsonObject(装配返回结果，code)
	 * 
	 * @throw Exception Redis操作异常
	 * 
	 * @see com.netease.vcloud.service.UserService#login(com.netease.vcloud.dao.
	 * domain.User)
	 */
	@Override
	public void login(User user, JsonObject jo) throws Exception {
		// 构造随机Session串sid并存入redis
		String sid = RandomPassword.genRandomNum(5) + System.currentTimeMillis();
		redisUtil.userLogin(sid, user.getUid());

		JsonObject ret = new JsonObject();
		ret.addProperty("uid", user.getUid());
		ret.addProperty("sid", sid);
		jo.add("ret", ret);
	}

	/*
	 * 用户登出
	 * 
	 * @pre sid不为空
	 * 
	 * @param sid(String)
	 * 
	 * @throw Exception Redis异常, 发送邮件异常
	 * 
	 * @return 退出成功，返回true; 用户未登录，返回false
	 */
	@Override
	public boolean logout(String sid) throws Exception {
		// 检查用户是否已登录
		if (!redisUtil.isUserLogin(sid)) {
			return false;
		} else {
			redisUtil.delSession(sid);
			return true;
		}
	}

	/*
	 * 发起密码重置，发送验证码到邮件
	 * 
	 * @pre 用户是否存在
	 * 
	 * @param email(String)
	 * 
	 * @throw Exception Redis异常, 发送邮件异常
	 * 
	 * @see com.netease.vcloud.service.UserService#upPwdMail(java.lang.String)
	 */
	@Override
	public void upPwdMail(String email) throws Exception {
		// 生成四位验证码
		String captcha = RandomPassword.genRandomNum(4);

		// 将验证码存入redis缓存, 30分钟有效
		redisUtil.updateCached(Constants.UPPWD_CAPTCHA + email, captcha, 1800);

		// 发送邮件
		EmailAuthentication.sendCaptchaMail(email, captcha);
	}

	/*
	 * 重置密码
	 * 
	 * @pre UpPwd.valid()
	 * 
	 * @param UpPwd(String)
	 * 
	 * @param JsonObject(装配返回结果)
	 * 
	 * @throw Exception Redis/数据库操作异常
	 * 
	 * @see com.netease.vcloud.service.UserService#updatePwd(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void updatePwd(UpPwd upPwd, JsonObject jo) throws Exception {
		// 验证用户是否存在
		User user = findUserByEmail(upPwd.getEmail());
		if (user == null) {
			logger.info("用户不存在>>" + upPwd.getEmail());
			jo.addProperty("code", ErrorCode.USER_NOTEXIST);
			jo.addProperty("msg", "用户不存在");
			return;
		}
		// 根据email获取校验码
		String captcha = redisUtil.getCached((Constants.UPPWD_CAPTCHA + upPwd.getEmail()));
		if (captcha != null && captcha.equals(upPwd.getCaptcha())) {
			// 更新密码
			userDao.upPwd(user.getUid(), upPwd.getPwd());
			// 删除redis相应校验码
			redisUtil.delCached((Constants.UPPWD_CAPTCHA + upPwd.getEmail()));
			// 删除redis相应User
			redisUtil.delUser(user.getEmail(), user.getUid());
			jo.addProperty("code", 200);
			return;
		} else {
			logger.info("验证码错误");
			jo.addProperty("code", ErrorCode.CAPTCHA_ERROR);
			jo.addProperty("msg", "验证码错误");
			return;
		}

	}

	/*
	 * 根据Email判断用户是否存在
	 * 
	 * @pre email格式
	 * 
	 * @param email(String)
	 * 
	 * @throw Exception Redis/数据库操作异常
	 * 
	 * @return User
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#findUserByEmail(java.lang.String)
	 */
	@Override
	public User findUserByEmail(String email) throws Exception {
		// 先去redis查找
		User userObj = null;
		userObj = redisUtil.getUser(email);
		if (userObj != null) {
			return userObj;
		}

		// 去数据库查找
		userObj = userDao.getUser(email);
		if (userObj != null) {
			// 存redis
			redisUtil.addUser(userObj);
		}
		return userObj;
	}

	/*
	 * 根据uid判断用户是否存在
	 * 
	 * @param uid(Long, User ID 5位)
	 * 
	 * @throw Exception Redis/数据库操作异常
	 * 
	 * @return User
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#findUserByEmail(java.lang.String)
	 */
	@Override
	public User findUserByID(Long uid) throws Exception {
		// 先去redis查找
		User userObj = null;
		userObj = redisUtil.getUser(uid.toString());
		if (userObj != null) {
			return userObj;
		}

		// 去数据库查找
		userObj = userDao.getUser(uid);
		if (userObj != null) {
			// 存redis
			redisUtil.addUser(userObj);
		}
		return userObj;
	}

	/*
	 * 根据uid, sid判断用户有无登录
	 * 
	 * @param uid(String, User ID 5位)
	 * 
	 * @param sid(String, Session ID)
	 * 
	 * @throw Exception Redis操作异常
	 * 
	 * @return 用户已登录，返回true；用户未登录，返回false
	 * 
	 * @see com.netease.vcloud.service.UserService#login_auth(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public boolean login_auth(String uid, String sid) throws Exception {
		// 去redis查找相应sid
		String value = redisUtil.getCached(Constants.SESSION_LOGIN_NAME + sid);
		if (value == null || !value.equals(uid)) {
			return false;
		} else {
			// 更新用户登录有效期
			redisUtil.userLogin(sid, Long.parseLong(uid));
			return true;
		}
	}

	/*
	 * 更新用户帐号信息（name， phone，industryId）
	 * 
	 * @pre 用户已登录, 请求信息完整
	 * 
	 * @param User(uid(必需), industryId(可选), name(可选), phone(可选))
	 * 
	 * @param JsonObject(装配返回结果)
	 * 
	 * @throw Exception 数据库操作异常
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#upUserInfo(com.netease.vcloud.dao.
	 * domain.User, com.google.gson.JsonObject)
	 */
	@Override
	public void upUserInfo(User user, JsonObject jo) throws Exception {
		User upUser = findUserByID(user.getUid());
		if (upUser == null) {
			logger.info("用户不存在");
			jo.addProperty("code", ErrorCode.USER_NOTEXIST);
			jo.addProperty("msg", "用户不存在");
			return;
		}
		if (user.getName() != null) {
			upUser.setName(user.getName());
		}
		if (user.getPhone() != null) {
			upUser.setPhone(user.getPhone());
		}
		if (user.getIndustryId() != null) {
			upUser.setIndustryId(user.getIndustryId());
		}
		// 更新DB
		userDao.upUserInfo(upUser);
		// 删除redis
		redisUtil.delUser(upUser.getEmail(), upUser.getUid());
		jo.addProperty("code", 200);
	}

	/*
	 * 根据用户的uid获取所有acessKey和secretKey对
	 * 
	 * @pre 用户已登录
	 * 
	 * @param ASKey(uid 必需)
	 * 
	 * @param JsonObject(装配返回结果, 包括code，ret[{accessKey, secretKey},{accessKey,
	 * secretKey}...])
	 * 
	 * @throw Exception 数据库操作异常
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#getAuthInfo(com.netease.vcloud.dao
	 * .domain.ASKey, com.google.gson.JsonObject)
	 */
	@Override
	public void getAuthInfo(ASKey askey, JsonObject jo) throws Exception {
		if (askey.getUid() == null) {
			jo.addProperty("code", ErrorCode.USER_NOTEXIST);
			jo.addProperty("msg", "用户不存在");
			return;
		}
		List<ASKey> keyList = aSKeyDao.getSecretKeys(askey.getUid());
		jo.addProperty("code", 200);
		JsonObject ret = new JsonObject();
		if (keyList == null) {
			jo.add("ret", ret);
		} else {
			JsonArray jsonArr = new JsonArray();
			for (int i = 0; i < keyList.size(); i++) {
				JsonObject tmp = new JsonObject();
				tmp.addProperty("accessKey", keyList.get(i).getAccessKey());
				tmp.addProperty("secretKey", keyList.get(i).getSecretKey());
				jsonArr.add(tmp);
			}
			jo.add("ret", jsonArr);
		}
	}

	/*
	 * 根据uid， accessKey获取secretKey
	 * 
	 * @pre 用户已登录
	 * 
	 * @param uid(Long)
	 * 
	 * @param accessKey(String)
	 * 
	 * @throw Exception 数据库操作异常
	 * 
	 * @return secretKey(String) 无相应数据返回null
	 * 
	 * @see com.netease.vcloud.service.UserService#getSecretKey(java.lang.Long,
	 * java.lang.String)
	 */
	@Override
	public String getSecretKey(Long uid, String accessKey) throws Exception {
		if (uid == null || accessKey == null)
			return null;
		ASKey askey = new ASKey();
		askey.setUid(uid);
		askey.setAccessKey(accessKey);
		return aSKeyDao.getSecretKey(askey);
	}

	/*
	 * 添加一对key
	 * 
	 * @pre 用户已登录
	 * 
	 * @param ASKey(必需：uid)
	 * 
	 * @param JsonObject(装配返回结果, 包括code，ret[accessKey, secretKey])
	 * 
	 * @throw Exception 数据库操作异常
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#addAuthInfo(com.netease.vcloud.dao
	 * .domain.ASKey, com.google.gson.JsonObject)
	 */
	@Override
	public void addAuthInfo(ASKey askey, JsonObject jo) throws Exception {
		if (askey.getUid() == null) {
			jo.addProperty("code", ErrorCode.USER_NOTEXIST);
			jo.addProperty("msg", "用户不存在");
			return;
		}
		ASKey newASKey = GenerateASKey.getPairKey();
		newASKey.setUid(askey.getUid());
		// 持久化到数据库
		aSKeyDao.addAuth(newASKey);
		// 装配返回结果
		jo.addProperty("code", 200);
		JsonObject ret = new JsonObject();
		ret.addProperty("accessKey", newASKey.getAccessKey());
		ret.addProperty("secretKey", newASKey.getSecretKey());
		jo.add("ret", ret);
	}

	/*
	 * 删除一对key
	 * 
	 * @pre 用户已登录
	 * 
	 * @param ASKey(必需：uid, accessKey)
	 * 
	 * @param JsonObject(装配返回结果)
	 * 
	 * @throw Exception 数据库操作异常
	 * 
	 * @see
	 * com.netease.vcloud.service.UserService#delAuthInfo(com.netease.vcloud.dao
	 * .domain.ASKey, com.google.gson.JsonObject)
	 */
	@Override
	public void delAuthInfo(ASKey askey, JsonObject jo) throws Exception {
		if (askey.getUid() == null || askey.getAccessKey() == null) {
			jo.addProperty("code", ErrorCode.USER_NOTEXIST);
			jo.addProperty("msg", "用户不存在");
			return;
		}

		// 更新数据库
		aSKeyDao.deleteAuth(askey);
		// 装配返回结果
		jo.addProperty("code", 200);
	}

}
