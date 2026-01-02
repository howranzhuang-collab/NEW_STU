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
 * 收费实体类
 * 用于管理学生的缴费信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("fees")
public class Fees {
    
    /**
     * 费用ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID（关联学籍表）
     */
    private Long studentId;
    
    /**
     * 费用类型：TUITION（学费）、ACCOMMODATION（住宿费）、OTHER（其他）
     */
    private String feeType;
    
    /**
     * 费用金额
     */
    private BigDecimal amount;
    
    /**
     * 应缴日期
     */
    private LocalDate dueDate;
    
    /**
     * 实缴金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 实缴日期
     */
    private LocalDate paidDate;
    
    /**
     * 缴费状态：UNPAID（未缴）、PAID（已缴）、PARTIAL（部分缴费）、OVERDUE（逾期）
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

