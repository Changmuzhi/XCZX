package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.PageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

@Controller
public class CmsPagePreviewController extends BaseController {
    @Autowired
    PageService pageService;
    @RequestMapping("/cms/preview/{pageId}")
    public void preview(@PathVariable("pageId") String pageId) throws IOException {
        String pageHtml = pageService.getPageHtml(pageId);
        if (StringUtils.isNoneEmpty(pageHtml)){
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("text/html;charset=utf-8");
            outputStream.write(pageHtml.getBytes("utf-8"));
        }
    }
}
