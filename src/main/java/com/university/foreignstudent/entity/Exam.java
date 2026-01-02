package com.university.foreignstudent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考试实体类
 * 用于记录学生的考试成绩
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("exams")
public class Exam {
    
    /**
     * 考试ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID（关联学籍表）
     */
    private Long studentId;
    
    /**
     * 课程名称
     */
    private String courseName;
    
    /**
     * 考试类型：MIDTERM（期中）、FINAL（期末）、QUIZ（测验）、ASSIGNMENT（作业）
     */
    private String examType;
    
    /**
     * 考试日期
     */
    private LocalDate examDate;
    
    /**
     * 成绩
     */
    private BigDecimal score;
    
    /**
     * 满分
     */
    private BigDecimal fullScore;
    
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

