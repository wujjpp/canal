
/**
 * Created by Wu Jian Ping on - 2021/09/15.
 */
package com.alibaba.otter.canal.client.adapter.grpc.support;

import java.util.Map;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.otter.canal.client.adapter.grpc.GrpcAdapter;

import java.util.concurrent.CompletableFuture;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class GrpcTemplate {
    private static Logger logger = LoggerFactory.getLogger(GrpcTemplate.class);

    private String serviceUrl;
    private String sign;

    public GrpcTemplate(String serviceUrl, String sign) {
        this.serviceUrl = serviceUrl;
        this.sign = sign;
    }

    public void insert(String database, String table, Map<String, Object> data) {
        this.runAsync(database, table, "insert", data);
    }

    public void update(String database, String table, Map<String, Object> data) {
        this.runAsync(database, table, "update", data);

    }

    public void delete(String database, String table, Map<String, Object> data) {
        this.runAsync(database, table, "delete", data);
    }

    public CompletableFuture<Boolean> runAsync(String database, String table, String action, Map<String, Object> data) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("database", database);
            body.put("table", table);
            body.put("action", action);
            body.put("data", data);
            body.put("sign", this.sign);

            if (logger.isDebugEnabled()) {
                logger.debug("{}", JSON.toJSONString(body, SerializerFeature.WriteMapNullValue));
            }
            return completedFuture(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return completedFuture(false);
        }
    }

    public int count() {
        return 0;
    }
}
