package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    //将页面html保存到页面物理路径
    public void savePageToServerPath(String pageId) {
        //根据页面ID查询出页面对象
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        //如果页面id为空抛出非法参数异常
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //取出页面信息
        CmsPage cmsPage = optional.get();
        //根据页面信息取出站点ID，再根据站点ID查询出站点信息
        CmsSite cmsSite = this.getCmsSiteById(cmsPage.getSiteId());
        //页面的物理路径
        String pagePath = cmsSite.getSitePhysicalPath() + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();
        //查询页面静态文件ID
        String cmsPageHtmlFileId = cmsPage.getHtmlFileId();
        InputStream inputStream = this.getFileById(cmsPageHtmlFileId);
        if (inputStream ==null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        FileOutputStream fileOutputStream =null;
        try {
             fileOutputStream = new FileOutputStream(new File(pagePath));
             //将文件内容保存到服务物理路径
            IOUtils.copy(inputStream,fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //根据静态页面的id，获取文件的内容
    private InputStream getFileById(String cmsPageHtmlFileId) {
        //根据id得到文件对象
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(cmsPageHtmlFileId)));
        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义GridFsResource
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //根据从页面对象中取出的站点信息，查询其站点的对象
    private CmsSite getCmsSiteById(String siteId) {
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if (!optional.isPresent()) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        CmsSite cmsSite = optional.get();
        return cmsSite;
    }
}
