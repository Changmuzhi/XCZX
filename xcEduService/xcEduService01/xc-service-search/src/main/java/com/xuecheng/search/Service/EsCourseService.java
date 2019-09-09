package com.xuecheng.search.Service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    @Autowired
    RestHighLevelClient client;
    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index;
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type;
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;

    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam) {

        //创建查询对象
        SearchRequest request = new SearchRequest("xc_course");
        //设置查询类型
        request.types("doc");
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //分页查询
        if (page < 0) {
            page = 1;
        }
        if (size < 0) {
            size = 12;
        }
        int from = (page - 1) * size;
        builder.from(from);
        builder.size(size);
        //显示字段
        String[] strings = source_field.split(",");
        builder.fetchSource(strings, new String[]{});
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='eslight'>");
        highlightBuilder.postTags("</font>");
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));
        builder.highlighter(highlightBuilder);

        //关键字匹配
        if (StringUtils.isNotEmpty(courseSearchParam.getKeyword())) {
            //匹配域
            MultiMatchQueryBuilder matchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "teachplan", "description")
                    //占比
                    .minimumShouldMatch("70%")
                    //提升标题权重
                    .field("name", 10);
            //布尔查询中必须满足
            boolQueryBuilder.must(matchQueryBuilder);
        }
        //过虑条件，如果用户有选择，则必须满足
        //根据一级分类
        if (StringUtils.isNotEmpty(courseSearchParam.getMt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt", courseSearchParam.getMt()));
        }
        //根据二级分类
        if (StringUtils.isNotEmpty(courseSearchParam.getSt())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("st", courseSearchParam.getSt()));
        }
        //根据难度等级
        if (StringUtils.isNotEmpty(courseSearchParam.getGrade())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade", courseSearchParam.getGrade()));
        }

        //布尔查询
        builder.query(boolQueryBuilder);
        request.source(builder);

        SearchResponse response = null;

        try {
            //使用客户端查询
            response = client.search(request);

        } catch (Exception e) {
            e.printStackTrace();
            return new QueryResponseResult(CommonCode.SUCCESS, new QueryResult<CoursePub>());
        }
        //结果集处理
        SearchHits hits = response.getHits();
        //查询总记录数
        long totalHits = hits.getTotalHits();
        //得到查询数据
        SearchHit[] searchHits = hits.getHits();
        List<CoursePub> list = new ArrayList<>();
        for (SearchHit searchHit : searchHits) {
            //创建返回对象
            CoursePub coursePub = new CoursePub();
            //取出每条数据
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();

            //得到课程名称
            String name = (String) sourceAsMap.get("name");
            String id = (String) sourceAsMap.get("id");
            coursePub.setId(id);
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            if (highlightFields.get("name") != null) {
                HighlightField highlightField = highlightFields.get("name");
                Text[] texts = highlightField.fragments();
                StringBuffer stringBuffer = new StringBuffer();
                for (Text text : texts) {
                    stringBuffer.append(text);
                }
                name = stringBuffer.toString();
            }

            coursePub.setName(name);
            //得到图片信息
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //得到新价格
            Double price = null;
            //如果价格不为空，课程不免费
            try {
                if (sourceAsMap.get("price") != null) {
                    price = (Double) sourceAsMap.get("price");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);
            //得到老价格
            Double price_old = null;
            try {
                if (sourceAsMap.get("price_old") != null) {
                    price_old = (Double) sourceAsMap.get("price_old");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice_old(price_old);
            list.add(coursePub);
        }

        //构建返回值
        QueryResult<CoursePub> queryResult = new QueryResult();
        //设置查询条数
        queryResult.setTotal(totalHits);
        //设置具体对象
        queryResult.setList(list);
        QueryResponseResult<CoursePub> responseResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);
        return responseResult;
    }

    public Map<String, CoursePub> getall(String id) {
        //创建查询对象
        SearchRequest request = new SearchRequest(es_index);
        //设置查询类型
        request.types(es_type);
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //查询条件
        builder.query(QueryBuilders.termQuery("id", id));

        request.source(builder);

        SearchResponse searchResponse = null;
        HashMap<String, CoursePub> map = new HashMap<>();
        try {
            searchResponse = client.search(request);
            SearchHits responseHits = searchResponse.getHits();
            SearchHit[] hits = responseHits.getHits();


            for (SearchHit hit : hits) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                CoursePub coursePub = new CoursePub();
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setPic(pic);
                coursePub.setGrade(grade);
                coursePub.setTeachplan(teachplan);
                coursePub.setDescription(description);
                map.put(courseId, coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Value("${xuecheng.elasticsearch.media.index}")
    private String es_media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String es_media_type;
    @Value("${xuecheng.elasticsearch.media.source_field}")
    private String source_media_field;
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds) {
        //创建查询对象
        SearchRequest searchRequest = new SearchRequest(es_media_index);
        //设置类型
        searchRequest.types(es_media_type);
        //创建查询条件对象
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //源字段过滤
        String[] split = source_media_field.split(",");
        builder.fetchSource(split,new String[]{});
        //查询条件
        builder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //将查询条件赋值给查询对象
        searchRequest.source(builder);

        SearchResponse search =null;
        try {
            search = client.search(searchRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchHits hits = search.getHits();
        SearchHit[] hitsHits = hits.getHits();
        //创建返回对象
        List<TeachplanMediaPub> list= new ArrayList<>();
        QueryResult<TeachplanMediaPub> queryResult = new QueryResult();
        for (SearchHit hitsHit : hitsHits) {
            Map<String, Object> sourceAsMap = hitsHit.getSourceAsMap();
            //取出课程计划媒资信息
            String courseid = (String) sourceAsMap.get("courseid");
            String media_id = (String) sourceAsMap.get("media_id");
            String media_url = (String) sourceAsMap.get("media_url");
            String teachplan_id = (String) sourceAsMap.get("teachplan_id");
            String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            teachplanMediaPub.setCourseId(courseid);
            teachplanMediaPub.setMediaUrl(media_url);
            teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);
            teachplanMediaPub.setMediaId(media_id);
            teachplanMediaPub.setTeachplanId(teachplan_id);
            list.add(teachplanMediaPub);
        }
        queryResult.setList(list);
        queryResult.setTotal(hits.totalHits);

        return new QueryResponseResult(CommonCode.SUCCESS,queryResult);
    }
}
