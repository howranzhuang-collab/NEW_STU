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
 * 学籍记录实体类
 * 用于管理学生的学籍信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("student_records")
public class StudentRecord {
    
    /**
     * 学籍ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID（关联用户表）
     */
    private Long studentId;
    
    /**
     * 学号
     */
    private String studentNumber;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 性别：MALE、FEMALE
     */
    private String gender;
    
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    
    /**
     * 国籍
     */
    private String nationality;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 年级
     */
    private String grade;
    
    /**
     * 入学日期
     */
    private LocalDate enrollmentDate;
    
    /**
     * 学籍状态：ACTIVE（在读）、GRADUATED（已毕业）、SUSPENDED（休学）、DROPPED（退学）
     */
    private String status;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 电子邮箱
     */
    private String email;
    
    /**
     * 地址
     */
    private String address;
    
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

