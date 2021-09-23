/**
 * Created by Wu Jian Ping on - 2021/09/22.
 */

package com.alibaba.otter.canal.client.adapter.grpc.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.client.adapter.config.YmlConfigBinder;
import com.alibaba.otter.canal.client.adapter.support.MappingConfigsLoader;

public class MappingConfigLoader {

    private static Logger logger = LoggerFactory.getLogger(MappingConfigLoader.class);

    public static Map<String, MappingConfig> load(Properties envProperties) {
        logger.info("## Start loading grpc mapping config ... ");

        Map<String, MappingConfig> result = new LinkedHashMap<>();

        Map<String, String> configContentMap = MappingConfigsLoader.loadConfigs("grpc");
        configContentMap.forEach((fileName, content) -> {
            MappingConfig config = YmlConfigBinder.bindYmlToObj(null, content, MappingConfig.class, null,
                    envProperties);
            if (config == null) {
                return;
            }
            try {
                config.validate();
            } catch (Exception e) {
                throw new RuntimeException("ERROR load Config: " + fileName + " " + e.getMessage(), e);
            }
            result.put(fileName, config);
        });

        logger.info("## grpc mapping config loaded");
        return result;
    }
}
