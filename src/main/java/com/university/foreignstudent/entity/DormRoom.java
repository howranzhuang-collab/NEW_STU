package com.university.foreignstudent.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 宿舍房间实体类
 * 用于管理宿舍房间信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dorm_rooms")
public class DormRoom {
    
    /**
     * 房间ID，主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 宿舍楼ID
     */
    private Long buildingId;
    
    /**
     * 房间号
     */
    private String roomNumber;
    
    /**
     * 楼层
     */
    private Integer floor;
    
    /**
     * 房间容量（可住人数）
     */
    private Integer capacity;
    
    /**
     * 当前入住人数
     */
    private Integer currentOccupancy;
    
    /**
     * 房间类型：SINGLE（单人间）、DOUBLE（双人间）、QUAD（四人间）
     */
    private String roomType;
    
    /**
     * 状态：AVAILABLE（可用）、FULL（已满）、MAINTENANCE（维护中）
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

