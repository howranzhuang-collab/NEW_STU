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
 * 宿舍分配实体类
 * 用于管理学生的宿舍分配信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dorm_allocations")
public class DormAllocation {
    
    /**
     * 分配ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID（关联学籍表）
     */
    private Long studentId;
    
    /**
     * 房间ID
     */
    private Long roomId;
    
    /**
     * 入住日期
     */
    private LocalDate checkInDate;
    
    /**
     * 退房日期
     */
    private LocalDate checkOutDate;
    
    /**
     * 分配状态：ACTIVE（在住）、COMPLETED（已退房）、CANCELLED（已取消）
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

