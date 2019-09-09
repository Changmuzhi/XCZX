package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@Api(value = "/media/upload",tags = "文件上传")
public interface MediaUploadControllerApi {

    @ApiOperation("文件上传注册")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileMd5", value = "md5校验码"),
            @ApiImplicitParam(name = "fileName", value = "文件名称", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "fileSize", value = "文件大小", required = true, paramType = "path", dataType = "Long"),
            @ApiImplicitParam(name = "mimetype", value = "文件类型", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "fileExt", value = "文件扩展名", required = true, paramType = "path", dataType = "String")
    })
    public ResponseResult register(
            String fileMd5,
            String fileName,
            Long fileSize,
            String mimetype,
            String fileExt

    );
    @ApiOperation("分块检查")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileMd5", value = "md5校验码"),
            @ApiImplicitParam(name = "chunk", value = "文件分块下标"),
            @ApiImplicitParam(name = "chunkSize", value = "文件分块大小")
    })
    public CheckChunkResult checkchunk(
            String fileMd5,
            Integer chunk,
            Integer chunkSize
    );

    @ApiOperation("上传分块")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "MultipartFile", value = "文件"),
            @ApiImplicitParam(name = "chunk", value = "文件分块下标"),
            @ApiImplicitParam(name = "fileMd5", value = "md5校验码")
    })
    public ResponseResult uploadchunk(MultipartFile file,
                                      Integer chunk,
                                      String fileMd5);

    @ApiOperation("合并文件")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileMd5", value = "md5校验码"),
            @ApiImplicitParam(name = "fileName", value = "文件名称"),
            @ApiImplicitParam(name = "fileSize", value = "文件大小"),
            @ApiImplicitParam(name = "mimetype", value = "文件类型"),
            @ApiImplicitParam(name = "fileExt", value = "文件扩展名")
    })
    public ResponseResult mergechunks(String fileMd5,
                                      String fileName,
                                      Long fileSize,
                                      String mimetype,
                                      String fileExt);
}


