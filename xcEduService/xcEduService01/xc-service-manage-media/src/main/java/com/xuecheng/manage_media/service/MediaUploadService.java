package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Value("${xc-service-manage-media.upload-location}")
    String uploadlocation;

    @Autowired
    MediaFileRepository mediaFileRepository;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 根据文件md5得到文件路径
     * 规则：
     * 一级目录：md5的第一个字符
     * 二级目录：md5的第二个字符
     * 三级目录：md5
     * 文件名：md5+文件扩展名
     *
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    //注册服务
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {

        //得到文件路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);

        //查询数据库，查看文件id是否存在
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);

        //检查路径文件是否存在，和数据库中是否有相同的md5码文件
        if (optional.isPresent() && file.exists()) {
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }

        //文件不存在时做准备工资
        //得到文件目录
        String fileFolderRelativePath = this.getFileFolderRelativePath(fileMd5);
        File file1 = new File(fileFolderRelativePath);
        if (!file1.exists()) {
            file1.mkdirs();
        }

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //得到文件路径
    private String getFilePath(String fileMd5, String fileExt) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uploadlocation).append(fileMd5.substring(0, 1)).append("/").append(fileMd5.substring(1, 2)).append("/")
                .append(fileMd5).append("/").append(fileMd5).append(".").append(fileExt);
        String FilePath = stringBuilder.toString();
        return FilePath;
    }

    //得到文件目录
    private String getFileFolderRelativePath(String fileMd5) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(uploadlocation).append(fileMd5.substring(0, 1)).append("/").append(fileMd5.substring(1, 2)).append("/").append(fileMd5).append("/");
        String fileFolderPath = stringBuilder.toString();
        return fileFolderPath;
    }

    //得到分块目录
    public String getFileFolderPath(String fileMd5) {
        String fileChunkFolderPath = this.getFileFolderRelativePath(fileMd5) + "chunks/";
        return fileChunkFolderPath;
    }

    //分块检查
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //得到块文件目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //得到块文件的路径
        File file = new File(fileFolderPath + chunk);
        //如果块文件存在
        if (file.exists()) {
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }
        //如果不存在
        else {
            return new CheckChunkResult(MediaCode.FILE_NO_EXIST_CHECK, false);
        }
    }

    //分块上传
    public ResponseResult uploadchunk(MultipartFile file, Integer chunk, String fileMd5) {

        //如果块文件目录不存在就创建
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        File file1 = new File(fileFolderPath);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        //得到块文件路径
        File file2 = new File(fileFolderPath + chunk);
        InputStream reader = null;
        FileOutputStream fileOutputStream = null;
        try {
            reader = file.getInputStream();
            fileOutputStream = new FileOutputStream(file2);
            IOUtils.copy(reader, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }


    //合并块文件，并记录信息到文件系统
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //合并所有分块
        //得到分块文件得目录
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        File file = new File(fileFolderPath);
        //得到了文件列表
        File[] files = file.listFiles();
        //创建合并文件对象
        String filePath = this.getFilePath(fileMd5, fileExt);
        //合并文件
        File mergechunks = this.mergechunks(files, filePath);
        if (mergechunks == null) {
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }
        //校验md5
        boolean md5 = this.checkFileMd5(mergechunks, fileMd5);
        if (!md5) {
            ExceptionCast.cast(MediaCode.FILE_CHECK_FAILED);
        }
        //将信息写入数据库
        MediaFile mediaFile = new MediaFile();
        //数据主键
        mediaFile.setFileId(fileMd5);
        //文件存储名称
        mediaFile.setFileName(fileMd5 + "." + fileExt);
        //文件原始名称
        mediaFile.setFileOriginalName(fileName);
        //文件保存路径
        StringBuilder sb = new StringBuilder();
        sb.append(fileMd5.substring(0, 1)).append("/").append(fileMd5.substring(1, 2)).append("/").append(fileMd5)
                .append("/");
        mediaFile.setFilePath(sb.toString());
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");

        mediaFileRepository.save(mediaFile);

        ResponseResult responseResult = this.sendProcessVideoMsg(mediaFile.getFileId());

        return responseResult;

    }

    //合并文件
    private File mergechunks(File[] files, String filePath) {
        //校验合并文件
        File file1 = new File(filePath);
        //如果文件存在得话就删除
        if (file1.exists()) {
            file1.delete();
        }
        //不存在得话就创建
        try {
            file1.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //对块文件列表排序
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())) {
                    return 1;
                }
                return -1;
            }
        });

        //写入文件
        RandomAccessFile randomAccessFile = null;
        RandomAccessFile r = null;
        try {
            randomAccessFile = new RandomAccessFile(file1, "rw");
            byte[] b = new byte[1024];
            for (File file : list) {
                r = new RandomAccessFile(file, "r");
                int len = -1;
                while ((len = r.read(b)) != -1) {
                    randomAccessFile.write(b, 0, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                r.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file1;
    }

    //校验md5
    private boolean checkFileMd5(File mergechunks, String fileMd5) {
        //进行校验
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(mergechunks);
            //得到文件得md5
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            //校验md5
            if (md5Hex.equals(fileMd5)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    String routingkey_media_video;

    //向MQ发送视频处理消息
    public ResponseResult sendProcessVideoMsg(String fileMd5) {
        //查询出当前上传文件的信息
        Optional<MediaFile> optional = mediaFileRepository.findById(fileMd5);
        //如果查询不到抛出异常
        if (!optional.isPresent()) {
            return new ResponseResult(CommonCode.FAIL);
        }
        try {
            //发送视频处理消息
            Map<Object, Object> map = new HashMap<>();
            map.put("mediaId", fileMd5);
            String msg = JSON.toJSONString(map);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, msg);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
