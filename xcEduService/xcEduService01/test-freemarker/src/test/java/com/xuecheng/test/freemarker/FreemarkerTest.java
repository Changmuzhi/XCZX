package com.xuecheng.test.freemarker;

import com.xuecheng.test.freemarker.model.Student;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FreemarkerTest {
    @Autowired
    RestTemplate restTemplate;

    //测试静态化，基于ftl模板生成html
    @Test
    public void testGenerateHtml() throws IOException, TemplateException {
        //定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //定义模板
        //得到classpath得路径
        String classpath = this.getClass().getResource("/").getPath();
        //定义模板路径
        configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
        //获取模板文件得内容
        Template template = configuration.getTemplate("course.ftl");
        //定义数据类型
        Map map = getMap();
        //静态化
        String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        //生成h5页面
        InputStream inputStream = IOUtils.toInputStream(content);
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\David\\Desktop\\com.xuecheng.test.html"));
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
    }

    @Test
    public void testGenerateHtmlByString() throws IOException, TemplateException {
        //定义配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //模板内容
        String templateString = "" +
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                " 名称：${name}\n" +
                " </body>\n" +
                "</html>";
        //模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        stringTemplateLoader.putTemplate("template", templateString);
        configuration.setTemplateLoader(stringTemplateLoader);
        //得到模板
        Template template = configuration.getTemplate("template", "utf-8");
        //数据模型
        Map<String, String> map = new HashMap();
        map.put("name", "小张");
        //静态化
        String s = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        InputStream inputStream = IOUtils.toInputStream(s);
        FileOutputStream outputStream = new FileOutputStream(new File("C:\\Users\\David\\Desktop\\com.xuecheng.test.html"));
        IOUtils.copy(inputStream, outputStream);

    }

    //获取数据模型
    public Map getMap() {
//        Student student = new Student();
//        student.setName("小明");
//        Student student1 = new Student();
//        student1.setName("小李");
//        ArrayList<Student> arrayList = new ArrayList<>();
//        arrayList.add(student);
//        arrayList.add(student1);
//        Map<String, Object> map = new HashMap<>();
//        map.put("list", arrayList);
        ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://localhost:31200/course/courseview/297e7c7c62b888f00162b8a7dec20000", Map.class);
        Map map = forEntity.getBody();
        return map;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate(new OkHttp3ClientHttpRequestFactory());
    }

}
