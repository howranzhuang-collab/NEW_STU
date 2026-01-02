package com.university.foreignstudent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 招生项目实体类
 * 用于管理招生项目信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("admission_projects")
public class AdmissionProject {
    
    /**
     * 项目ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 项目标题
     */
    private String title;
    
    /**
     * 项目描述
     */
    private String description;
    
    /**
     * 招生名额
     */
    private Integer quota;
    
    /**
     * 已报名人数
     */
    private Integer appliedCount;
    
    /**
     * 申请截止日期
     */
    private LocalDate deadline;
    
    /**
     * 项目状态：OPEN（开放）、CLOSED（关闭）、ENDED（已结束）
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    private Integer deleted;
}

