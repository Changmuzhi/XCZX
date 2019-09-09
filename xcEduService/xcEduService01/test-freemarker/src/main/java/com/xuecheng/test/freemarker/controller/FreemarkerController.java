package com.xuecheng.test.freemarker.controller;

import com.xuecheng.test.freemarker.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@RequestMapping("/freemarker")
@Controller
public class FreemarkerController {

    @RequestMapping("/test1")
    public String test1(Map<String, Object> map) {
        map.put("name", "黑马001");
        return "test1";
    }

    @RequestMapping("/test2")
    public String test2(Map<String, Object> map) {

        Student student = new Student();
        student.setName("小王");
        Student student1 = new Student();
        student1.setName("小李");

        ArrayList<Student> arrayList = new ArrayList<>();
        arrayList.add(student);
        arrayList.add(student1);

        map.put("students",arrayList);

        return "test1";
    }

    @RequestMapping("/test3")
    public String test3(Map<String, Object> map) {
        Student student = new Student();
        student.setName("小王");
        Student student1 = new Student();
        student1.setName("小李");
        HashMap<String,Student> hashMap = new HashMap<>();
        hashMap.put("sut1",student);
        hashMap.put("sut2",student1);
        map.put("stu",hashMap);
        return "test2";
    }

    @Autowired
    RestTemplate restTemplate;
    @RequestMapping("/banner")
    public String index_banner(Map<String,Object> map){
        ResponseEntity<Map> entity = restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f", Map.class);
        Map body = entity.getBody();
        map.putAll(body);
        return "index_banner";
    }
}
