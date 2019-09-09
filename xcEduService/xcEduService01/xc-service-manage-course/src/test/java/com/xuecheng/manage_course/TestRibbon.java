package com.xuecheng.manage_course;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {
    @Autowired
    RestTemplate restTemplate;
    @Test
    public void TestRibbon(){
        String service="XC-SERVICE-MANAGE-CLIENT-CMS";
        ResponseEntity<Map> entity = restTemplate.getForEntity("http://"+service+"/cms/page/get/5a795ac7dd573c04508f3a56", Map.class);
        Map map = entity.getBody();
        System.out.println(map);
    }

    @Autowired
    CmsPageClient cmsPageClient;
    @Test
    public void testRibbon(){
        CmsPage cmsPage = cmsPageClient.findById("5a795ac7dd573c04508f3a56");
        System.out.println(cmsPage);
    }
}
