package com.xuecheng.manage_media_process.mq;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.MediaFileProcess_m3u8;
import com.xuecheng.framework.utils.HlsVideoUtil;
import com.xuecheng.framework.utils.Mp4VideoUtil;
import com.xuecheng.manage_media_process.dao.MediaFileRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Administrator
 * @version 1.0
 **/
@Component
public class MediaProcessTask {

    //ffmpeg绝对路径
    @Value("${xc-service-manage-media.ffmpeg-path}")
    String ffmpeg_path;

    //上传文件根目录
    @Value("${xc-service-manage-media.video-location}")
    String serverPath;



    @Autowired
    MediaFileRepository mediaFileRepository;

    //视频处理监听
    @RabbitListener(queues="${xc-service-manage-media.mq.queue-media-video-processor}",containerFactory = "customContainerFactory")
    public void receiveMediaProcessTask(String msg) {
        //将消息解析为map集合
        Map map = JSON.parseObject(msg, Map.class);
        //得到数据主键
        String md5 = (String) map.get("mediaId");
        //得到了文件对象
        Optional<MediaFile> optional = mediaFileRepository.findById(md5);
        //如果信息为空结束方法
        if (!optional.isPresent()) {
            return;
        }
        MediaFile mediaFile = optional.get();
        //得到文件类型
        String fileType = mediaFile.getFileType();
        //如果文件不是avi类型视频，或者类型等于空
        if (!fileType.equals("avi") || fileType == null) {
            //处理状态为无需处理
            mediaFile.setProcessStatus("303004");
            //更新数据库信息
            mediaFileRepository.save(mediaFile);
            //结束方法
            return;
        } else {
            //处理状态为未处理
            mediaFile.setProcessStatus("303001");
            //更新数据库信息
            mediaFileRepository.save(mediaFile);
        }
        //mp4转码
        //将被转码视频的路径=文件根目录+视频文件目录+视频全名
        String video_path = serverPath + mediaFile.getFilePath() + mediaFile.getFileName();
        //转码文件名称=文件id+"mp4后缀名"
        String mp4_name = mediaFile.getFileId() + ".mp4";
        //转码后视频存放路径=文件根目录+视频文件目录
        String mp4folder_path = serverPath + mediaFile.getFilePath();
        //转格式工具类 第一个参数(ffmpeg路径，目标文件路径，被转码名称，被转码后存放的目录)
        Mp4VideoUtil mp4VideoUtil = new Mp4VideoUtil(ffmpeg_path, video_path, mp4_name, mp4folder_path);
        //返回转码响应结果
        String result = mp4VideoUtil.generateMp4();
        //判断响应结果
        if (result == null || !result.equals("success")) {
            //操作失败写入处理日志
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            return;
        }
        //生成m3u8
        //mp4文件存放全路径(文件根目录+视频文件目录+视频名称)
        String video_path_mp4 = serverPath + mp4folder_path + mp4_name;
        //m3u8文件名称
        String m3u8_name = mediaFile.getFileId() + ".m3u8";
        //m3u8文件存放路径
        String m3u8folder_path = serverPath + mediaFile.getFilePath() + "hls/";
        //使用工具类操作m3u8文件(ffmpeg_path文件路径,mp4文件路径,m3u8文件名称,m3u8文件存放路径)
        HlsVideoUtil hlsVideoUtil = new HlsVideoUtil(ffmpeg_path, video_path, m3u8_name, m3u8folder_path);
        String m3u8Result = hlsVideoUtil.generateM3u8();
        //判断响应结果
        if (m3u8Result == null || !m3u8Result.equals("success")) {
            //操作失败写入处理日志
            mediaFile.setProcessStatus("303003");//处理状态为处理失败
            MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
            mediaFileProcess_m3u8.setErrormsg(m3u8Result);
            mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
            return;
        }
        //获取m3u8列表
        List<String> ts_list = hlsVideoUtil.get_ts_list();
        //更新视频状态码
        mediaFile.setProcessStatus("303002");
        //将m3u8文件列表写入文件对象
        MediaFileProcess_m3u8 mediaFileProcess_m3u8 = new MediaFileProcess_m3u8();
        mediaFileProcess_m3u8.setTslist(ts_list);
        mediaFile.setMediaFileProcess_m3u8(mediaFileProcess_m3u8);
        //m3u8文件url
        mediaFile.setFileUrl(mediaFile.getFilePath() + "hls/" + m3u8_name);
        mediaFileRepository.save(mediaFile);
    }
}
