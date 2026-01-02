package com.university.foreignstudent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.foreignstudent.entity.AdmissionProject;
import org.apache.ibatis.annotations.Mapper;

/**
 * 招生项目数据访问层接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface AdmissionProjectMapper extends BaseMapper<AdmissionProject> {
}

