/**
 * Created by Wu Jian Ping on - 2021/09/28.
 */

package com.alibaba.otter.canal.client.adapter.tcp.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.client.adapter.tcp.config.MappingConfig;
import com.alibaba.otter.canal.client.adapter.tcp.support.TcpTemplate;
import com.alibaba.otter.canal.client.adapter.support.Dml;

public class TcpSyncService {

    private static Logger logger = LoggerFactory.getLogger(TcpSyncService.class);

    private TcpTemplate tcpTemplate;

    public TcpSyncService(TcpTemplate tcpTemplate) {
        this.tcpTemplate = tcpTemplate;
    }

    public void sync(MappingConfig config, Dml dml) {
        if (config != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("TcpSyncService.sync: config: {}, dml: {}",
                        JSON.toJSONString(config, SerializerFeature.WriteMapNullValue),
                        JSON.toJSONString(dml, SerializerFeature.WriteMapNullValue));
            }

            String type = dml.getType();
            String database = dml.getDatabase();
            String table = dml.getTable();
            List<Map<String, Object>> list = dml.getData();

            if (list.size() > 0) {
                Map<String, Object> data = list.get(0);
                if (type != null && type.equalsIgnoreCase("INSERT")) {
                    tcpTemplate.insert(database, table, data);
                } else if (type != null && type.equalsIgnoreCase("UPDATE")) {
                    tcpTemplate.update(database, table, data);
                } else if (type != null && type.equalsIgnoreCase("DELETE")) {
                    tcpTemplate.delete(database, table, data);
                }
            }
        }
    }
}
