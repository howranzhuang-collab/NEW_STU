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
 * 校友实体类
 * 用于管理已毕业学生的校友信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("alumni")
public class Alumni {
    
    /**
     * 校友ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 学生ID（关联学籍表）
     */
    private Long studentId;
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 学号
     */
    private String studentNumber;
    
    /**
     * 专业
     */
    private String major;
    
    /**
     * 毕业日期
     */
    private LocalDate graduationDate;
    
    /**
     * 学位：BACHELOR（学士）、MASTER（硕士）、DOCTOR（博士）
     */
    private String degree;
    
    /**
     * 毕业去向：EMPLOYED（就业）、STUDYING（继续深造）、OTHER（其他）
     */
    private String status;
    
    /**
     * 工作单位/继续深造学校
     */
    private String organization;
    
    /**
     * 职位/专业
     */
    private String position;
    
    /**
     * 联系方式（邮箱/电话）
     */
    private String contact;
    
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

