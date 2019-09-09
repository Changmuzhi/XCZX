package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//从TeachplanMedia查询课程计划媒资信息
public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {
    List<TeachplanMedia> findByCourseId(String courseId);
}
