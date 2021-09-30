
/**
 * Created by Wu Jian Ping on - 2021/09/15.
 */
package com.alibaba.otter.canal.client.adapter.http.support;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.kevinsawicki.http.HttpRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.ArrayList;

public class HttpTemplate {
    private static Logger logger = LoggerFactory.getLogger(HttpTemplate.class);

    private String serviceUrl;
    private String sign;

    public HttpTemplate(String serviceUrl, String sign) {
        this.serviceUrl = serviceUrl;
        this.sign = sign;
    }

    public void insert(String database, String table, Map<String, Object> data) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(data);
        this.runAsync(database, table, "insert", items, null);
    }

    public void update(String database, String table, Map<String, Object> data) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(data);
        this.runAsync(database, table, "update", items, null);

    }

    public void delete(String database, String table, Map<String, Object> data) {
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(data);
        this.runAsync(database, table, "delete", items, null);
    }

    public CompletableFuture<Boolean> runAsync(String database, String table, String action,
            List<Map<String, Object>> data, AtomicLong impCount) {
        return completedFuture(execute(database, table, action, data, impCount, "sync"));
    }

    public boolean execute(String database, String table, String action, List<Map<String, Object>> data,
            AtomicLong impCount, String mode) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("mode", mode);
            body.put("database", database);
            body.put("table", table);
            body.put("action", action);
            body.put("data", data);
            body.put("sign", this.sign);

            if (logger.isDebugEnabled()) {
                logger.debug("{}", JSON.toJSONString(body, SerializerFeature.WriteMapNullValue));
            }

            HttpRequest.post(this.serviceUrl).contentType("application/json;charset=UTF-8")
                    .send(JSON.toJSONString(body, SerializerFeature.WriteMapNullValue)).code();

            if (impCount != null) {
                impCount.addAndGet(data.size());
            }

            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public int count() {
        String endpoint = this.serviceUrl;
        if (!endpoint.endsWith("/")) {
            endpoint = endpoint + "/";
        }
        endpoint = endpoint + "count";
        String response = HttpRequest.get(endpoint).body();

        if (logger.isDebugEnabled()) {
            logger.debug("count response: {}", response);
        }

        JSONObject result = JSON.parseObject(response);
        Object obj = result.get("count");

        if (obj != null) {
            return (int) obj;
        }
        return 0;
    }
}
