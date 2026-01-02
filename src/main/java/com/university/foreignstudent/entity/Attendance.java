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
 * 考勤实体类
 * 用于记录学生的考勤信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("attendances")
public class Attendance {
    
    /**
     * 考勤ID，主键，自增
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
     * 考勤日期
     */
    private LocalDate attendanceDate;
    
    /**
     * 考勤状态：PRESENT（出席）、ABSENT（缺席）、LATE（迟到）、LEAVE（请假）
     */
    private String status;
    
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

