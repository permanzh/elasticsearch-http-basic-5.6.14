package com.asquera.elasticsearch.plugins.http.search;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;

public class ESTestMain {


    public static void main(String...args){

        ESClient.setEsServerIp("192.168.31.71");

        ESClient.setPort("9300");
        ESClient.setHttpPort("8090");

        ESClient.setClusterName("esearch");
        ESClient.setUserName("");
        ESClient.setPassword("");

        TransportClient client = ESClient.getInstance();

        IndicesAdminClient indices = client.admin().indices();
        GetMappingsRequest getMappingsRequest = new GetMappingsRequest();
        getMappingsRequest.indices("doc_0");
        ActionFuture<GetMappingsResponse> mappings = indices.getMappings(getMappingsRequest);
        GetMappingsResponse getMappingsResponse = mappings.actionGet();

        System.out.println(JSONObject.toJSONString(getMappingsResponse));


    }

}
