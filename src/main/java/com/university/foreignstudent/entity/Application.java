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
 * 申请实体类
 * 用于管理学生申请信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("applications")
public class Application {
    
    /**
     * 申请ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 招生项目ID
     */
    private Long projectId;
    
    /**
     * 学生ID（关联用户表）
     */
    private Long studentId;
    
    /**
     * 申请状态：PENDING（待审核）、APPROVED（已通过）、REJECTED（已拒绝）
     */
    private String status;
    
    /**
     * 申请日期
     */
    private LocalDate applicationDate;
    
    /**
     * 上传文件路径（如简历、成绩单等）
     */
    private String filePath;
    
    /**
     * 备注信息
     */
    private String remarks;
    
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

