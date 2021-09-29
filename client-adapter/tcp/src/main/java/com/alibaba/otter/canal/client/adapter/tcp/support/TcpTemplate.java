
/**
 * Created by Wu Jian Ping on - 2021/09/28.
 */

package com.alibaba.otter.canal.client.adapter.tcp.support;

import java.util.Map;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class TcpTemplate {
    private static Logger logger = LoggerFactory.getLogger(TcpTemplate.class);

    private String sign;
    private String serviceUrl;

    private Connections connections;

    public TcpTemplate(String hosts, int poolSize, String serviceUrl, String sign) {
        this.serviceUrl = serviceUrl;
        this.sign = sign;
        this.connections = new Connections(hosts, poolSize);
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
            body.put("serviceUrl", this.serviceUrl);
            body.put("sign", this.sign);

            if (logger.isDebugEnabled()) {
                logger.debug("{}", JSON.toJSONString(body, SerializerFeature.WriteMapNullValue));
            }

            return completedFuture(this.connections.send(JSON.toJSONString(body)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return completedFuture(false);
        }
    }

    public int count() {
        // String endpoint = this.serviceUrl;
        // if (!endpoint.endsWith("/")) {
        // endpoint = endpoint + "/";
        // }
        // endpoint = endpoint + "count";
        // String response = TcpRequest.get(endpoint).body();

        // if (logger.isDebugEnabled()) {
        // logger.debug("count response: {}", response);
        // }

        // JSONObject result = JSON.parseObject(response);
        // Object obj = result.get("count");

        // if (obj != null) {
        // return (int) obj;
        // }
        return 0;
    }
}
