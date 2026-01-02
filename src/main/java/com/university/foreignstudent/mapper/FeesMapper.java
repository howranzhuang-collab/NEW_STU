package com.university.foreignstudent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.university.foreignstudent.entity.Fees;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收费数据访问层接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 */
@Mapper
public interface FeesMapper extends BaseMapper<Fees> {
}

