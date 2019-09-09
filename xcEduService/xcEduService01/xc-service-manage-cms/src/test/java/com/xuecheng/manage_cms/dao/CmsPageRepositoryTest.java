package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 * @create 2018-09-12 18:11
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageRepositoryTest {

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Test
    public void testFindAll() {
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    //分页查询
    @Test
    public void testFindPage() {
        //分页参数
        int page = 2;//从0开始
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<CmsPage> all = cmsPageRepository.findAll(pageable);
        System.out.println(all);
    }

    //修改
    @Test
    public void testUpdate() {
        //查询对象
        Optional<CmsPage> optional = cmsPageRepository.findById("5a754adf6abb500ad0568811");
        if (optional.isPresent()) {
            CmsPage cmsPage = optional.get();
            //设置要修改值
            cmsPage.setPageAliase("test01");
            //...
            //修改
            CmsPage save = cmsPageRepository.save(cmsPage);
            System.out.println(save);
        }
    }

    //根据页面名称查询
    @Test
    public void testfindByPageName() {
        CmsPage cmsPage = cmsPageRepository.findByPageName("测试页面");
        System.out.println(cmsPage);
    }

    //条件查询
    @Test
    public void testFindAllByExample() {
        int page = 0;
        int size = 2;
        //分页
        PageRequest pageRequest = PageRequest.of(page, size);
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageAliase("课");
        //创建条件匹配器
        ExampleMatcher matching = ExampleMatcher.matching();
        matching = matching.withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //pageAliase进行匹配的字段
        // ExampleMatcher.GenericPropertyMatchers.contains():查询模式
        //Example
        Example<CmsPage> example = Example.of(cmsPage, matching);
        Page<CmsPage> all = cmsPageRepository.findAll(example, pageRequest);
        List<CmsPage> content = all.getContent();
        System.out.println(content);
    }

    @Autowired
    CmsSiteRepository cmsSiteListRepository;

    @Test
    public void findSiteList() {
        List<CmsPage> all = cmsPageRepository.findAll();
        System.out.println(all);
    }

    @Autowired
    RestTemplate restTemplate;

    @Test
    public void testRestTemplate() {
        ResponseEntity<Map> forEntity =
                restTemplate.getForEntity("http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f",
                        Map.class);
        System.out.println(forEntity);
    }

}
