package com.netease.vcloud.vod.api.util;

import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.netease.vcloud.framework.ISysConfigLoader;
import com.netease.vcloud.framework.SysConfig;

/**
 * 配置加载类
 * @author sf
 *
 */
public class AdminWebConfigLoader extends ISysConfigLoader {
    private static Logger m_logger = Logger.getLogger(AdminWebConfigLoader.class);

    public void loadSysConfig() throws Exception {
        m_logger.info("reloading api-web config...");
        ClassPathResource cpr = new ClassPathResource("api-web.xml");
        // xml
        SysConfig.load(new FileInputStream(cpr.getFile()));

        // TODO: 读取配置信息
        
        /*DeviceEnv.isCacheEnabled = SysConfig.getBoolean("CacheEnabled", true);*/
        
        
        // 打印log
        m_logger.info("configs:" + SysConfig.getProperties());

        
    }
}

