package com.university.foreignstudent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宿舍楼实体类
 * 用于管理宿舍楼信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dorm_buildings")
public class DormBuilding {
    
    /**
     * 宿舍楼ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宿舍楼名称
     */
    private String name;
    
    /**
     * 宿舍楼地址
     */
    private String address;
    
    /**
     * 总楼层数
     */
    private Integer totalFloors;
    
    /**
     * 总房间数
     */
    private Integer totalRooms;
    
    /**
     * 状态：ACTIVE（使用中）、MAINTENANCE（维护中）、CLOSED（已关闭）
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

