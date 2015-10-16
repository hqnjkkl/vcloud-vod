package com.netease.vcloud.vod.api.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.netease.vcloud.param.ContentParam;

public class ControllerUtil {
	
	public static final Logger logger = Logger.getLogger(ControllerUtil.class);

	public static ContentParam getContentMeta(HttpServletRequest request,
			boolean withoutContent) throws Exception {
		int len = 0;
		InputStream is = null;
		if (!withoutContent) {
			len = request.getContentLength();
			if (len == 0)
				throw new Exception("content length is 0");
			is = request.getInputStream();
		}

		try {
			if (len == -1)
				throw new IOException("read content length is -1!");
			ContentParam cp = new ContentParam();
			cp.setUid(Long.parseLong(request.getHeader("uid")));
			/*String ktype = request.getHeader("k");
			if (ktype != null) {
				try {
					cp.setKtype(Integer.parseInt(ktype));
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}

			try {
				String strCt = request.getHeader("ct");
				if (strCt != null) {
					cp.setClienttype(Integer.parseInt(strCt));
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}*/

			if (!withoutContent) {
				byte[] buffer = new byte[len];
				if (is != null)
					IOUtils.readFully(is, buffer);
				cp.setContent(buffer);
			}
			return cp;
		} catch (Exception ex) {
			logger.error(ex);
			throw ex;
		} finally {
			if (!withoutContent)
				IOUtils.closeQuietly(is);
		}
	}
}
