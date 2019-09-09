package com.xuecheng.api.cms;

import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(value="站点查询接口",description = "查询所有站点信息")
public interface CmsSiteControllerApi {
    public QueryResponseResult findSiteList();
}
