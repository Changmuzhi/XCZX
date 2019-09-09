package com.xuecheng.search;


import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestSearch {
    @Autowired
    RestHighLevelClient client;

    @Autowired
    RestClient restClient;

    @Test
    //搜索type下的全部记录
    public void testSearchAll() throws IOException, ParseException {
        //指定请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //搜索方式
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        //source源字段过滤
        sourceBuilder.fetchSource(new String[]{"name", "description", "timestamp"}, new String[]{});
        //设置搜索源
        searchRequest.source(sourceBuilder);
        //执行搜索，向Es发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();

        long totalHits = hits.getTotalHits();
        System.out.println("共搜索到" + totalHits);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(timestamp);

        }
    }

    //分页搜索type下的记录
    @Test
    public void testSearchPage() throws IOException {
        //指定请求对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //指定类型
        searchRequest.types("doc");
        //搜索源构建对象
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //搜索方式
        sourceBuilder.query(QueryBuilders.matchAllQuery());
        //source源字段过滤
        sourceBuilder.fetchSource(new String[]{"name", "description", "timestamp"}, new String[]{});
        //设置分页条件
        //设置起始页数
        sourceBuilder.from(1);
        //设置每页显示条数
        sourceBuilder.size(1);
        //设置搜索源
        searchRequest.source(sourceBuilder);
        //执行搜索，向Es发起http请求
        SearchResponse searchResponse = client.search(searchRequest);
        //搜索结果
        SearchHits hits = searchResponse.getHits();

        long totalHits = hits.getTotalHits();
        System.out.println("共搜索到" + totalHits);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //得到匹配度高的文档
        SearchHit[] hitsHits = hits.getHits();
        for (SearchHit hit : hitsHits) {
            //文档主键
            String id = hit.getId();
            //源文档内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String timestamp = (String) sourceAsMap.get("timestamp");
            System.out.println(name);
            System.out.println(description);
            System.out.println(timestamp);

        }
    }

    //termQuery 精确查询 @Test
    @Test
    public void termQuery() throws IOException {
        //创建搜索对象
        SearchRequest searchRequest = new SearchRequest("xc_course");
        //设置搜索类型
        searchRequest.types("doc");
        //创建搜索条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置搜索条件
        builder.query(QueryBuilders.termQuery("name", "spring"));
        //设置搜索显示的字段
        builder.fetchSource(new String[]{"name", "description"}, new String[]{});
        //将搜索条件设置到搜索对象中
        searchRequest.source(builder);
        //通过客户端拿到搜索对象执行搜索操作
        SearchResponse search = client.search(searchRequest);
        //得到搜索结果
        SearchHits hits = search.getHits();
        //拿到搜索内容
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            System.out.println(name);
            System.out.println(description);

        }
    }

    //根据主页查询
    @Test
    public void termQuery_Id() throws IOException {
        //创建查询请求对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //创建查询id字段数组
        String[] arr = new String[]{"1", "2"};
        //设置查询条件
        builder.query(QueryBuilders.termsQuery("_id", arr));
        //设置查询显示的字段
        builder.fetchSource(new String[]{"name"}, new String[]{});
        //将查询条件设置给查询对象
        request.source(builder);
        //使用客户端查询
        SearchResponse searchResponse = client.search(request);
        //获取查询内容
        SearchHits responseHits = searchResponse.getHits();
        SearchHit[] hits = responseHits.getHits();
        for (SearchHit hit : hits) {
            String id = hit.getId();
            Map<String, Object> map = hit.getSourceAsMap();
            String name = (String) map.get("name");
            System.out.println(id + "_" + name);
        }
    }

    //根据关键字查询
    @Test
    public void matchQuery_operator() throws IOException {
        //创建查询请求对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询条件
        builder.query(QueryBuilders.matchQuery("description", "开发框架").operator(Operator.AND));
        //设置查询显示的字段
        builder.fetchSource(new String[]{"name", "description"}, new String[]{});
        //将查询条件设置给查询对象
        request.source(builder);
        //使用客户端查询
        SearchResponse searchResponse = client.search(request);
        //获取查询内容
        SearchHits responseHits = searchResponse.getHits();
        SearchHit[] hits = responseHits.getHits();
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            String name = (String) map.get("name");
            String description = (String) map.get("description");
            System.out.println(name + "_" + description);
        }
    }

    //根据匹配查询
    @Test
    public void matchQuery_minimumShouldMatch() throws IOException {
        //创建查询请求对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询条件
        builder.query(QueryBuilders.matchQuery("description", "java开发框架").minimumShouldMatch("80%"));
        //设置查询显示的字段
        builder.fetchSource(new String[]{"name", "description"}, new String[]{});
        //将查询条件设置给查询对象
        request.source(builder);
        //使用客户端查询
        SearchResponse searchResponse = client.search(request);
        //获取查询内容
        SearchHits responseHits = searchResponse.getHits();
        SearchHit[] hits = responseHits.getHits();
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            String name = (String) map.get("name");
            String description = (String) map.get("description");
            System.out.println(name + "_" + description);
        }
    }


    //根据多个域权重查询
    @Test
    public void multiMatchQuery() throws IOException {
        //创建查询请求对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询条件
        builder.query(QueryBuilders.multiMatchQuery("spring开发", "name", "name").minimumShouldMatch("60%").field("name", 10));
        //设置查询显示的字段
        builder.fetchSource(new String[]{"name", "description"}, new String[]{});
        //将查询条件设置给查询对象
        request.source(builder);
        //使用客户端查询
        SearchResponse searchResponse = client.search(request);
        //获取查询内容
        SearchHits responseHits = searchResponse.getHits();
        SearchHit[] hits = responseHits.getHits();
        for (SearchHit hit : hits) {
            Map<String, Object> map = hit.getSourceAsMap();
            String name = (String) map.get("name");
            String description = (String) map.get("description");
            System.out.println(name + "_" + description);
        }
    }

    @Test
    public void testBoolQuery() throws IOException {
        SearchRequest request = new SearchRequest("xc_course");
        request.types("doc");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询显示内容
        builder.fetchSource(new String[]{"name", "description", "studymodel"}, new String[]{});
        //精确查询
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("studymodel", 201001);
        //包含查询
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description").minimumShouldMatch("60%").field("name", 10);
        //布尔查询构建
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //必须满足的条件
        boolQueryBuilder.must(termQueryBuilder);
        //任意满足的条件
        boolQueryBuilder.must(multiMatchQueryBuilder);
        //设置给查询条件对象
        builder.query(boolQueryBuilder);
        //设置给查询对象
        request.source(builder);
        //设置查询对象给客户端
        SearchResponse response = client.search(request);
        //返回查询结果
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name + "\n" + description + "\n" + studymodel);
        }

    }

    //过滤器查询
    @Test
    public void testFilter() throws IOException {
        //创建查询对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询显示内容
        builder.fetchSource(new String[]{"name", "description", "studymodel"}, new String[]{});
        //匹配关键字
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description").minimumShouldMatch("50%").field("name", 10);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //必须满足关键字匹配
        boolQueryBuilder.must(matchQueryBuilder);
        //过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", 201001)).filter(
                QueryBuilders.rangeQuery("price").gte(80).lte(100)
        );
        //将布尔查询条件设置给查询条件对象
        builder.query(boolQueryBuilder);

        //设置给查询对象
        request.source(builder);
        //设置查询对象给客户端
        SearchResponse response = client.search(request);
        //返回查询结果
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            System.out.println(name + "\n" + description + "\n" + studymodel);
        }
    }

    //排序
    @Test
    public void testSort() throws IOException {
        //创建查询对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询显示内容
        builder.fetchSource(new String[]{"name", "description", "studymodel", "price"}, new String[]{});
        //匹配关键字
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description").minimumShouldMatch("50%").field("name", 10);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //必须满足关键字匹配
        boolQueryBuilder.must(matchQueryBuilder);
        //过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", 201001));
        //将布尔查询条件设置给查询条件对象
        builder.query(boolQueryBuilder);
        //排序
        builder.sort("price", SortOrder.ASC);

        //设置给查询对象
        request.source(builder);
        //设置查询对象给客户端
        SearchResponse response = client.search(request);
        //返回查询结果
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");
            String studymodel = (String) sourceAsMap.get("studymodel");
            double price = (double) sourceAsMap.get("price");
            System.out.println(name + "\n" + description + "\n" + studymodel + "\n" + price);
        }
    }

    //高亮
    @Test
    public void testHighlight() throws IOException {
        //创建查询对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //设置查询显示内容
        builder.fetchSource(new String[]{"name", "description", "studymodel", "price"}, new String[]{});
        //匹配关键字
        MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery("spring开发", "name", "description").minimumShouldMatch("50%").field("name", 10);
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //必须满足关键字匹配
        boolQueryBuilder.must(matchQueryBuilder);
        //过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("studymodel", 201001));
        //将布尔查询条件设置给查询条件对象
        builder.query(boolQueryBuilder);
        //排序
        builder.sort("price", SortOrder.ASC);
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<tag>");
        highlightBuilder.postTags("</tag>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        highlightBuilder.fields().add(new HighlightBuilder.Field("description"));
        builder.highlighter(highlightBuilder);

        //设置给查询对象
        request.source(builder);
        //设置查询对象给客户端
        SearchResponse response = client.search(request);
        //返回查询结果
        SearchHits hits = response.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

            //获取高亮显示的结果
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();

            String name = (String) sourceAsMap.get("name");
            String description = (String) sourceAsMap.get("description");


            //如果高亮显示的结果不为空证明有关键字匹配

            if (highlightFields.get("name") != null) {
                StringBuffer stringBufferName = new StringBuffer();
                HighlightField hname = highlightFields.get("name");
                Text[] textsname = hname.getFragments();
                for (Text text : textsname) {
                    stringBufferName.append(text);
                }
                name = stringBufferName.toString();
            }

            if (highlightFields.get("description") != null) {
                HighlightField hdescription = highlightFields.get("description");
                StringBuffer stringBufferDescription = new StringBuffer();
                Text[] textsdescription = hdescription.getFragments();
                for (Text text : textsdescription) {
                    stringBufferDescription.append(text);
                }
                description = stringBufferDescription.toString();
            }


            String studymodel = (String) sourceAsMap.get("studymodel");
            Double price = (double) sourceAsMap.get("price");
            System.out.println(name);
            System.out.println(description);
            System.out.println(studymodel);
            System.out.println(price);
        }
    }
}
