package com.xuecheng.manage_cms_client.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class ConsumerPostPage {

    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    PageService pageService;

    //监听队列
    @RabbitListener(queues = {"${xuecheng.mq.queue}"})
    public void postPage(String msg){
        //将字符串转换为map
        Map map = JSON.parseObject(msg, Map.class);
        String pageId = (String)map.get("pageId");
        //查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
            return;
        }
        pageService.savePageToServerPath(pageId);
    }
}
