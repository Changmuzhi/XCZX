package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class MediaFileService {
    @Autowired
    MediaFileRepository mediaFileRepository;

    public QueryResponseResult findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {

        //如果说查询条件为空，重新创建
        if (queryMediaFileRequest == null) {
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        MediaFile mediaFile = new MediaFile();
        //如果查询条件不为空就设置给查询对象
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getTag())) {
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())) {
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        if (StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())) {
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }


        //查询条件匹配器
        ExampleMatcher matching = ExampleMatcher.matching()
                //tag标签模糊匹配
                .withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains())
                //文件原始名称匹配
                .withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains())
                //文件状态精确匹配
                .withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.exact());



        //定义example实例(查询对象，匹配条件)
        Example<MediaFile> ex = Example.of(mediaFile, matching);

        if (page <= 0) {
            page = 1;
        }
        page -= 1;
        if (size <= 0) {
            size = 10;
        }
        //设置分页参数
        PageRequest pageRequest = new PageRequest(page,size);
        //jpa查询(查询实例、分页参数)
        Page<MediaFile> all = mediaFileRepository.findAll(ex, pageRequest);
        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<MediaFile>();
        mediaFileQueryResult.setList(all.getContent());
        mediaFileQueryResult.setTotal(all.getTotalElements());
        return new QueryResponseResult(CommonCode.SUCCESS,mediaFileQueryResult);

    }
}

