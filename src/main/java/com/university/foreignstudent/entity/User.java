package com.university.foreignstudent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * 用于系统认证和授权
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {
    
    /**
     * 用户ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户名，唯一
     */
    private String username;
    
    /**
     * 密码，加密存储
     */
    private String password;
    
    /**
     * 用户角色：ADMIN（管理员）、STAFF（工作人员）、STUDENT（学生）
     */
    private String role;
    
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

