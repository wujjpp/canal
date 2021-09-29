/**
 * Created by Wu Jian Ping on - 2021/09/28.
 */

package com.alibaba.otter.canal.client.adapter.tcp.monitor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.otter.canal.client.adapter.config.YmlConfigBinder;
import com.alibaba.otter.canal.client.adapter.tcp.TcpAdapter;
import com.alibaba.otter.canal.client.adapter.tcp.config.MappingConfig;
import com.alibaba.otter.canal.client.adapter.support.MappingConfigsLoader;
import com.alibaba.otter.canal.client.adapter.support.Util;
import com.alibaba.fastjson.JSON;

public class TcpConfigMonitor {

    private static final Logger logger = LoggerFactory.getLogger(TcpConfigMonitor.class);

    private static final String adapterName = "tcp";

    private TcpAdapter tcpAdapter;

    private Properties envProperties;

    private FileAlterationMonitor fileMonitor;

    public void init(TcpAdapter tcpAdapter, Properties envProperties) {
        this.tcpAdapter = tcpAdapter;
        this.envProperties = envProperties;
        File confDir = Util.getConfDirPath(adapterName);
        try {
            FileAlterationObserver observer = new FileAlterationObserver(confDir,
                    FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter("yml")));
            FileListener listener = new FileListener();
            observer.addListener(listener);
            fileMonitor = new FileAlterationMonitor(3000, observer);
            fileMonitor.start();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void destroy() {
        try {
            fileMonitor.stop();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private class FileListener extends FileAlterationListenerAdaptor {

        @Override
        public void onFileCreate(File file) {
            super.onFileCreate(file);
            try {
                // 加载新增的配置文件
                String configContent = MappingConfigsLoader.loadConfig(adapterName + File.separator + file.getName());
                MappingConfig config = YmlConfigBinder.bindYmlToObj(null, configContent, MappingConfig.class, null,
                        envProperties);
                if (config == null) {
                    return;
                }
                config.validate();
                addConfigToCache(file, config);

                logger.info("Add a new tcp mapping config: {} to canal adapter", file.getName());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void onFileChange(File file) {
            super.onFileChange(file);

            try {
                if (tcpAdapter.getTcpMapping().containsKey(file.getName())) {
                    // 加载配置文件
                    String configContent = MappingConfigsLoader
                            .loadConfig(adapterName + File.separator + file.getName());
                    if (configContent == null) {
                        onFileDelete(file);
                        return;
                    }
                    MappingConfig config = YmlConfigBinder.bindYmlToObj(null, configContent, MappingConfig.class, null,
                            envProperties);
                    if (config == null) {
                        return;
                    }
                    config.validate();
                    if (tcpAdapter.getTcpMapping().containsKey(file.getName())) {
                        deleteConfigFromCache(file);
                    }
                    addConfigToCache(file, config);

                    logger.info("Update a tcp mapping config: {} of canal adapter", file.getName());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        @Override
        public void onFileDelete(File file) {
            super.onFileDelete(file);

            try {
                if (tcpAdapter.getTcpMapping().containsKey(file.getName())) {
                    deleteConfigFromCache(file);

                    logger.info("Delete a tcp mapping config: {} of canal adapter", file.getName());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        private void addConfigToCache(File file, MappingConfig config) {
            tcpAdapter.getTcpMapping().put(file.getName(), config);
            Map<String, MappingConfig> configMap = tcpAdapter.getMappingConfigCache()
                    .computeIfAbsent(StringUtils.trimToEmpty(config.getDestination()), k1 -> new HashMap<>());
            configMap.put(file.getName(), config);

            if (logger.isDebugEnabled()) {
                logger.debug("mappingConfig:{}", JSON.toJSONString(tcpAdapter.getTcpMapping()));
                logger.debug("mappingConfigCache:{}", JSON.toJSONString(tcpAdapter.getMappingConfigCache()));
            }
        }

        private void deleteConfigFromCache(File file) {
            tcpAdapter.getTcpMapping().remove(file.getName());
            for (Map<String, MappingConfig> configMap : tcpAdapter.getMappingConfigCache().values()) {
                if (configMap != null) {
                    configMap.remove(file.getName());
                }
            }

        }
    }
}
