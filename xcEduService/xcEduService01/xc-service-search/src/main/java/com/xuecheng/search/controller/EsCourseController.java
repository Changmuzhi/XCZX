package com.xuecheng.search.controller;

import com.xuecheng.api.search.EsCourseControllerApi;
import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.search.Service.EsCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search/course")
public class EsCourseController implements EsCourseControllerApi {

    @Autowired
    EsCourseService esCourseService;

    @Override
    @GetMapping(value = "/list/{page}/{size}")
    public QueryResponseResult<CoursePub> list(@PathVariable("page") int page, @PathVariable("size") int size, CourseSearchParam courseSearchParam) {
        QueryResponseResult<CoursePub> queryResponseResult = esCourseService.list(page, size, courseSearchParam);
        return queryResponseResult;
    }

    @Override
    @GetMapping("/getall/{id}")
    public Map<String, CoursePub> getall(@PathVariable("id") String id) {
        return esCourseService.getall(id);
    }

    @Override
    @GetMapping(value="/getmedia/{teachplanId}")
    public TeachplanMediaPub getmedia(@PathVariable("teachplanId")String teachplanId) {
        //将课程计划id放在数组中，为调用service作准备
        String[]teachplanIds=new String[]{teachplanId};
        //调用service,根据计划ID查询，得到返回结果
        QueryResponseResult<TeachplanMediaPub> mediaPubQueryResponseResult = esCourseService.getmedia(teachplanIds);
        //拿到返回结果，将结果中的视频计划媒资信息拿出来，返回给前台
        QueryResult<TeachplanMediaPub> queryResult = mediaPubQueryResponseResult.getQueryResult();
        if (queryResult!=null){
            if (queryResult.getList().size()>0&&queryResult.getList()!=null) {
                List<TeachplanMediaPub> list = queryResult.getList();
                for (TeachplanMediaPub teachplanMediaPub : list) {
                    return teachplanMediaPub;
                }
            }
        }
        return null;
    }
}
