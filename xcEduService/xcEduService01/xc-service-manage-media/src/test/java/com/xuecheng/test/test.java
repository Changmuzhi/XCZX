package com.xuecheng.test;

import org.apache.commons.lang.exception.NestableError;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class test {


    @Test
    public void test1() throws IOException {
        //源文件路径
        File mergeFile = new File("C:\\Users\\David\\Videos\\a.mp4");
        //文件目标路径
        String s = new String("C:\\Users\\David\\Desktop\\asd\\");
        //定义块大小
        long chunkFileSize = 1*1024*1024;
        //块数
        long chunkFileNum = (long) Math.ceil(mergeFile.length() * 1.0 / chunkFileSize);
        //创建读文件的对象
        RandomAccessFile read = new RandomAccessFile(mergeFile,"r");
        byte[] b = new byte[1024];
        for (long i = 0; i < chunkFileNum; i++) {
            File file = new File(s + i);
            RandomAccessFile rw = new RandomAccessFile(file, "rw");
            int len=-1;
            while ((len=read.read(b))!=-1){

                rw.write(b,0,len);
                if (file.length()>=chunkFileSize){
                    break;
                }
            }
            rw.close();
        }
        read.close();
    }

    @Test
    public void testMerge() throws IOException {
        //块目录
        String s = new String("C:\\Users\\David\\Desktop\\asd\\");
        File chunkFolder = new File(s);
        //合并文件
        String s1 = new String("C:\\Users\\David\\Desktop\\a.mp4");
        //String s1 = new String("C:\\Users\\David\\Desktop\\chunks");
        File mergeFile = new File(s1);
        //块文件列表
        File[] files = chunkFolder.listFiles();
        List<File> list = Arrays.asList(files);
        Collections.sort(list, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName())>Integer.parseInt(o2.getName())){
                    return 1;
                }
                return -1;
            }
        });
        mergeFile.createNewFile();
        RandomAccessFile randomAccessFile = new RandomAccessFile(mergeFile,"rw");
        byte[] b = new byte[1024];
        for (File file : list) {
            RandomAccessFile r = new RandomAccessFile(file, "r");
            int len=-1;
            while ((len=r.read(b))!=-1){
                randomAccessFile.write(b,0,len);
            }
            r.close();
        }
        randomAccessFile.close();

    }
}
