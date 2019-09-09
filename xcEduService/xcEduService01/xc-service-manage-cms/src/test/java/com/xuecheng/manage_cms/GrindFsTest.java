package com.xuecheng.manage_cms;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GrindFsTest {

    @Autowired
    GridFsTemplate fsTemplate;
    //存文件
    @Test
    public void testGrindFsTest() throws FileNotFoundException {
        //要存储得文件
        File file = new File("C:\\Users\\David\\Desktop\\course.ftl");
        //定义输入流
        FileInputStream inputStream = new FileInputStream(file);
        //像GrindFS存储文件
        ObjectId objectId = fsTemplate.store(inputStream, "course.ftl");
        //得到文件ID
        String s = objectId.toString();
        System.out.println(s);
        //5d5dfdfe9d3f605b0cfc849c
    }

    @Autowired
    GridFSBucket gridFSBucket;
    //取文件
    @Test
    public void queryFile() throws IOException {
        //根据文件id查询文件
        GridFSFile gridFSFile = fsTemplate.findOne(Query.query(Criteria.where("_id").is("5d4d9feb0ef53a1ce8a19590")));
        //打开一个下载流
        GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建GridFsResource对象，获取流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, downloadStream);
        //从流中取数据
        String s = IOUtils.toString(gridFsResource.getInputStream(),"utf-8");
        System.out.println(s);
    }
}
