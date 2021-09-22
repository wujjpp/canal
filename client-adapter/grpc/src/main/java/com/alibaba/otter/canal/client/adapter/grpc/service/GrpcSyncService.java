/**
 * Created by Wu Jian Ping on - 2021/09/15.
 */

package com.alibaba.otter.canal.client.adapter.grpc.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.client.adapter.grpc.config.MappingConfig;
import com.alibaba.otter.canal.client.adapter.grpc.support.GrpcTemplate;
import com.alibaba.otter.canal.client.adapter.support.Dml;

public class GrpcSyncService {

    private static Logger logger = LoggerFactory.getLogger(GrpcSyncService.class);

    private GrpcTemplate grpcTemplate;

    public GrpcSyncService(GrpcTemplate grpcTemplate) {
        this.grpcTemplate = grpcTemplate;
    }

    public void sync(MappingConfig config, Dml dml) {
        if (config != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("GrpcSyncService.sync: config: {}, dml: {}",
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
                    grpcTemplate.insert(database, table, data);
                } else if (type != null && type.equalsIgnoreCase("UPDATE")) {
                    grpcTemplate.update(database, table, data);
                } else if (type != null && type.equalsIgnoreCase("DELETE")) {
                    grpcTemplate.delete(database, table, data);
                }
            }
        }
    }
}
