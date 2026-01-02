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
 * 签证实体类
 * 用于管理学生的签证信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("visas")
public class Visas {
    
    /**
     * 签证ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID（关联学籍表）
     */
    private Long studentId;
    
    /**
     * 签证类型：X1（长期学习）、X2（短期学习）、F（访问）、L（旅游）等
     */
    private String visaType;
    
    /**
     * 签证号码
     */
    private String visaNumber;
    
    /**
     * 签发日期
     */
    private LocalDate issueDate;
    
    /**
     * 到期日期
     */
    private LocalDate expiryDate;
    
    /**
     * 入境日期
     */
    private LocalDate entryDate;
    
    /**
     * 签证状态：VALID（有效）、EXPIRED（已过期）、EXPIRING_SOON（即将过期）、CANCELLED（已取消）
     */
    private String status;
    
    /**
     * 签发机构
     */
    private String issuingAuthority;
    
    /**
     * 备注
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

