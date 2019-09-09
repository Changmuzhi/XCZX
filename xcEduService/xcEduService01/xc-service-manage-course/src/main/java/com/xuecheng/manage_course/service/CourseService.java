package com.xuecheng.manage_course.service;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    @Autowired
    TeachplanMapper teachplanMapper;

    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId) {
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        return teachplanNode;
    }

    //添加课程计划
    public ResponseResult addTeachplan(Teachplan teachplan) {
        //校验课程id和课程计划名称
        if (teachplan == null ||
                StringUtils.isEmpty(teachplan.getCourseid()) ||
                StringUtils.isEmpty(teachplan.getPname())) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //课程计划
        String courseid = teachplan.getCourseid();
        //节点信息
        String parentid = teachplan.getParentid();
        //如果节点为空 ||未选择节点信息
        if (StringUtils.isEmpty(parentid)) {
            //根据课程信息取出课程根节点
            parentid = this.getTeachplanRoot(courseid);
        }
        Optional<Teachplan> optional = teachplanRepository.findById(parentid);
        Teachplan parentNode = optional.get();
        String grade = parentNode.getGrade();
        //新节点
        Teachplan teachplanNew = new Teachplan();
        BeanUtils.copyProperties(teachplan, teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setCourseid(courseid);
        if (grade.equals("1")) {
            teachplanNew.setGrade("2");
        } else {
            teachplanNew.setGrade("3");
        }
        teachplanRepository.save(teachplanNew);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    @Autowired
    TeachplanRepository teachplanRepository;
    @Autowired
    CourseBaseRepository courseBaseRepository;
    @Autowired
    CoursePicRepository coursePicRepository;

    @Transactional
    //查询课程的根节点，如果查询不到自动添加根节点
    public String getTeachplanRoot(String courseId) {
        //根据课程信息ID查询出   队形课程的对象信息
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if (!optional.isPresent()) {
            //如果课程ID为空则返回空值
            return null;
        }
        //如果不为空，取出课程信息对象
        CourseBase courseBase = optional.get();

        //根据课程信息ID取出课程根节点
        List<Teachplan> teachplanList = teachplanRepository.findByCourseidAndParentid(courseId, "0");
        //如果信息对象为空，说名该课程顶级父节点不存在
        if (teachplanList == null || teachplanList.size() == 0) {
            //新增一个节点
            Teachplan teachplan = new Teachplan();
            //设置课程信息ID
            teachplan.setCourseid(courseId);
            //设置课程信息名称，根据课程id查询出的  课程信息对象取出的课程名称赋值
            teachplan.setPname(courseBase.getName());
            //设置节点层级
            teachplan.setParentid("0");
            teachplan.setGrade("1");//1级
            teachplan.setStatus("0");//未发布
            teachplanRepository.save(teachplan);
            return teachplan.getId();
        }
        Teachplan teachplan = teachplanList.get(0);
        return teachplan.getId();
    }


    public ResponseResult saveCoursePic(String courseId, String pic) {
        //查询课程图片
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()) {
            coursePic = optional.get();
        }
        //没有图片则新建对象
        if (coursePic == null) {
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程图片
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optional = coursePicRepository.findById(courseId);
        CoursePic coursePic = null;
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long l = coursePicRepository.deleteByCourseid(courseId);
        if (l < 1) {
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);

    }

    /**
     * @param id
     * @return CourseBase courseBase;//基础信息
     * CourseMarket courseMarket;//课程营销
     * CoursePic coursePic;//课程图片
     * TeachplanNode TeachplanNode;//教学计划
     * @Autowired TeachplanRepository teachplanRepository;
     * @Autowired CourseBaseRepository courseBaseRepository;
     * @Autowired CoursePicRepository coursePicRepository;
     */
    @Autowired
    CourseMarketRepository courseMarketRepository;

    //查询课程的视图
    public CourseView getCoruseView(String id) {
        CourseView courseView = new CourseView();

        //基础信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            courseView.setCourseBase(courseBase);
        }
        //课程图片
        Optional<CoursePic> optional1 = coursePicRepository.findById(id);
        if (optional1.isPresent()) {
            CoursePic coursePic = optional1.get();
            courseView.setCoursePic(coursePic);
        }
        //课程营销
        Optional<CourseMarket> optional2 = courseMarketRepository.findById(id);
        if (optional2.isPresent()) {
            CourseMarket courseMarket = optional2.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }


    //根据id查询课程基本信息
    public CourseBase findCourseBaseById(String id) {
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            return courseBase;
        }
        ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        return null;
    }

    @Value("${course‐publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course‐publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course‐publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course‐publish.siteId}")
    private String publish_siteId;
    @Value("${course‐publish.templateId}")
    private String publish_templateId;
    @Value("${course‐publish.previewUrl}")
    private String previewUrl;

    @Autowired
    CmsPageClient cmsPageClient;

    public CoursePublishResult preview(String id) {
        CourseBase one = findCourseBaseById(id);
        //发布课程预览页面
        CmsPage cmsPage = new CmsPage();
        //站点
        cmsPage.setSiteId(publish_siteId);//课程预 览站点
        //模板
        cmsPage.setTemplateId(publish_templateId);
        //页面名称
        cmsPage.setPageName(id + ".html");
        //页面别名
        cmsPage.setPageAliase(one.getName());
        //页面访问路径
        cmsPage.setPageWebPath(publish_page_webpath);
        //页面存储路径
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        //数据url
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        //远程请求cms保存页面信息
        CmsPageResult cmsPageResult = cmsPageClient.save(cmsPage);
        if (!cmsPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        //页面id
        String pageId = cmsPageResult.getCmsPage().getPageId();
        //页面url
        String pageUrl = previewUrl + pageId;
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //课程发布
    @Transactional
    public CoursePublishResult publish(String id) {
        //调用cms一键发布接口将课程详情页面发布到服务器
        CourseBase courseBaseById = findCourseBaseById(id);

        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);
        cmsPage.setDataUrl(publish_dataUrlPre + id);
        cmsPage.setPageName(id + ".html");
        cmsPage.setPageAliase(courseBaseById.getName());
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);
        cmsPage.setPageWebPath(publish_page_webpath);
        cmsPage.setTemplateId(publish_templateId);
        //保存课程的发布状态为"已发布"
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        CourseBase courseBase = saveCoursePubState(id);

        if (courseBase == null) {
            return new CoursePublishResult(CommonCode.FAIL, null);
        }

        //创建出四张表信息组成表对象，便于es查询
        CoursePub coursePub = createCoursePub(id);
        //将这个对象保存到数据库
        CoursePub pub = saveCoursePub(id, coursePub);
        if (pub == null) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_VIEWERROR);
        }


        //缓存课程的信息
        //保存课程计划媒资信息到待索引表
        this.saveTeachplanMediaPub(id);
        //得到页面的url
        String pageUrl = cmsPostPageResult.getPageUrl();
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    //操作索引表
    @Autowired
    TeachplanMediaPubRepository  teachplanMediaPubRepository;
    private void saveTeachplanMediaPub(String courseId){
        //将课程计划从媒资信息表中查询出来
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        //将课程计划媒资信息存储待索引表
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        List<TeachplanMediaPub>array=new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            BeanUtils.copyProperties(teachplanMedia,teachplanMediaPub);
            teachplanMediaPub.setTimestamp(new Date());
            array.add(teachplanMediaPub);
        }
        teachplanMediaPubRepository.saveAll(array);
    }

    //更改课程状态为已发布 202002
    private CourseBase saveCoursePubState(String courseId) {
        CourseBase courseBaseById = this.findCourseBaseById(courseId);
        courseBaseById.setStatus("202002");
        courseBaseRepository.save(courseBaseById);
        return courseBaseById;
    }

    //保存coursePub对象
    public CoursePub saveCoursePub(String id, CoursePub coursePub) {
        CoursePub coursePubNew = null;
        //判断课程ID是否为空
        if (StringUtils.isEmpty(id)) {
            ExceptionCast.cast(CourseCode.COURSE_PUBLISH_COURSEIDISNULL);
        }
        //先从数据库查询Id，辨别更新/创建操作
        Optional<CoursePub> optional = coursePubRepository.findById(id);
        //得到数据库中存在的对象
        if (optional.isPresent()) {
            coursePubNew = optional.get();
        } else {
            //创建新的对象
            coursePubNew = new CoursePub();
        }
        //将从四张表查询的数据对象克隆到新的数据库对象
        BeanUtils.copyProperties(coursePub, coursePubNew);
        //设置主键
        coursePubNew.setId(id);
        //设置时间戳
        coursePubNew.setTimestamp(new Date());
        //设置发布时间  2018-07-08 12:12:42
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = dateFormat.format(new Date());
        coursePubNew.setPubTime(format);
        //写入数据库
        CoursePub coursePub1 = coursePubRepository.save(coursePubNew);
        return coursePub1;

    }

    //创建coursePub对象，便于让es检索
    @Autowired
    CoursePubRepository coursePubRepository;

    public CoursePub createCoursePub(String id) {


        CoursePub coursePub = new CoursePub();
        coursePub.setId(id);

        //设置基础信息
        Optional<CourseBase> optional = courseBaseRepository.findById(id);
        if (optional.isPresent()) {
            CourseBase courseBase = optional.get();
            BeanUtils.copyProperties(courseBase, coursePub);
        }

        //设置课程图片
        Optional<CoursePic> optional1 = coursePicRepository.findById(id);
        if (optional1.isPresent()) {
            CoursePic coursePic = optional1.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }

        //设置营销信息
        Optional<CourseMarket> optional2 = courseMarketRepository.findById(id);
        if (optional2.isPresent()) {
            CourseMarket courseMarket = optional2.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }

        //查询课程计划
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        String jsonString = JSON.toJSONString(teachplanNode);
        coursePub.setTeachplan(jsonString);


        return coursePub;
    }


    /**
     * @param teachplanMedia
     * teachplan_id 课程计划id
     * media_id 媒资文件id
     * media_fileoriginalname 媒资文件的原始名称
     * media_url 媒资文件访问地址
     * @return
     */


    @Autowired
    TeachplanMediaRepository teachplanMediaRepository;

    //保存媒资信息
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        //如果传递过来的参数为空，抛异常
        if (teachplanMedia == null) {
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //得到课程计划
        String teachplanId = teachplanMedia.getTeachplanId();
        //从数据库中查询到课程信息对象
        Optional<Teachplan> optional = teachplanRepository.findById(teachplanId);
        //如果从课程计划表中查询的信息为空，证明参数有误
        if (!optional.isPresent()) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_ISNULL);
        }
        //查询出课程计划信息
        Teachplan teachplan = optional.get();
        //拿到课程计划节点计划
        String grade = teachplan.getGrade();
        //判断课程节点等级
        if (StringUtils.isEmpty(grade) || !grade.equals("3")) {
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }

        //拿到课程视频保存信息
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        //创建课程视频保存计划对象
        TeachplanMedia one = null;
        //如果该课程没有保存信息，则创建一个
        if (!teachplanMediaOptional.isPresent()) {
            one = new TeachplanMedia();
        }
        //如果有保存信息
        else {
            one = teachplanMediaOptional.get();
        }
        //保存媒资信息与课程计划信息
        //设置课程计划ID
        one.setTeachplanId(teachplanId);
        //设置课程id
        one.setCourseId(teachplanMedia.getCourseId());
        //设置课程原始名称
        one.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());
        //设置媒资文件id
        one.setMediaId(teachplanMedia.getMediaId());
        //设置媒资文件访问地址
        one.setMediaUrl(teachplanMedia.getMediaUrl());
        //写入数据库
        teachplanMediaRepository.save(one);
        return new ResponseResult(CommonCode.SUCCESS);

    }



}
