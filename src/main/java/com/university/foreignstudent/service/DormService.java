package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.DormAllocation;
import com.university.foreignstudent.entity.DormBuilding;
import com.university.foreignstudent.entity.DormRoom;
import com.university.foreignstudent.mapper.DormAllocationMapper;
import com.university.foreignstudent.mapper.DormBuildingMapper;
import com.university.foreignstudent.mapper.DormRoomMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住宿管理服务类
 * 提供宿舍楼、房间、分配的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DormService {
    
    private final DormBuildingMapper buildingMapper;
    private final DormRoomMapper roomMapper;
    private final DormAllocationMapper allocationMapper;
    
    // ==================== 宿舍楼管理 ====================
    
    /**
     * 创建宿舍楼
     * 
     * @param building 宿舍楼对象
     * @return 创建的宿舍楼
     */
    @Transactional
    public DormBuilding createBuilding(DormBuilding building) {
        log.info("创建宿舍楼: {}", building.getName());
        
        if (building.getName() == null || building.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("宿舍楼名称不能为空");
        }
        
        if (building.getStatus() == null || building.getStatus().isEmpty()) {
            building.setStatus("ACTIVE");
        }
        if (building.getTotalRooms() == null) {
            building.setTotalRooms(0);
        }
        
        building.setCreateTime(LocalDateTime.now());
        building.setUpdateTime(LocalDateTime.now());
        building.setDeleted(0);
        
        buildingMapper.insert(building);
        log.info("宿舍楼创建成功，ID: {}", building.getId());
        return building;
    }
    
    /**
     * 获取所有宿舍楼列表
     * 
     * @return 宿舍楼列表
     */
    public List<DormBuilding> getAllBuildings() {
        LambdaQueryWrapper<DormBuilding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormBuilding::getDeleted, 0);
        queryWrapper.orderByDesc(DormBuilding::getCreateTime);
        return buildingMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据ID获取宿舍楼
     * 
     * @param id 宿舍楼ID
     * @return 宿舍楼
     */
    public DormBuilding getBuildingById(Long id) {
        LambdaQueryWrapper<DormBuilding> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormBuilding::getId, id);
        queryWrapper.eq(DormBuilding::getDeleted, 0);
        return buildingMapper.selectOne(queryWrapper);
    }
    
    // ==================== 房间管理 ====================
    
    /**
     * 创建房间
     * 
     * @param room 房间对象
     * @return 创建的房间
     */
    @Transactional
    public DormRoom createRoom(DormRoom room) {
        log.info("创建房间，房间号: {}", room.getRoomNumber());
        
        if (room.getBuildingId() == null) {
            throw new IllegalArgumentException("宿舍楼ID不能为空");
        }
        if (room.getRoomNumber() == null || room.getRoomNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("房间号不能为空");
        }
        if (room.getCapacity() == null || room.getCapacity() <= 0) {
            throw new IllegalArgumentException("房间容量必须大于0");
        }
        
        // 检查宿舍楼是否存在
        DormBuilding building = getBuildingById(room.getBuildingId());
        if (building == null) {
            throw new IllegalArgumentException("宿舍楼不存在");
        }
        
        // 检查房间号是否已存在
        LambdaQueryWrapper<DormRoom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormRoom::getBuildingId, room.getBuildingId());
        queryWrapper.eq(DormRoom::getRoomNumber, room.getRoomNumber());
        queryWrapper.eq(DormRoom::getDeleted, 0);
        DormRoom existingRoom = roomMapper.selectOne(queryWrapper);
        if (existingRoom != null) {
            throw new IllegalArgumentException("该房间号已存在");
        }
        
        if (room.getCurrentOccupancy() == null) {
            room.setCurrentOccupancy(0);
        }
        if (room.getStatus() == null || room.getStatus().isEmpty()) {
            room.setStatus("AVAILABLE");
        }
        
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());
        room.setDeleted(0);
        
        roomMapper.insert(room);
        log.info("房间创建成功，房间ID: {}", room.getId());
        return room;
    }
    
    /**
     * 根据宿舍楼ID获取房间列表
     * 
     * @param buildingId 宿舍楼ID
     * @return 房间列表
     */
    public List<DormRoom> getRoomsByBuildingId(Long buildingId) {
        LambdaQueryWrapper<DormRoom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormRoom::getBuildingId, buildingId);
        queryWrapper.eq(DormRoom::getDeleted, 0);
        queryWrapper.orderByAsc(DormRoom::getFloor);
        queryWrapper.orderByAsc(DormRoom::getRoomNumber);
        return roomMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据ID获取房间
     * 
     * @param id 房间ID
     * @return 房间
     */
    public DormRoom getRoomById(Long id) {
        LambdaQueryWrapper<DormRoom> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormRoom::getId, id);
        queryWrapper.eq(DormRoom::getDeleted, 0);
        return roomMapper.selectOne(queryWrapper);
    }
    
    /**
     * 获取可用房间列表
     * 
     * @param buildingId 宿舍楼ID（可选）
     * @return 可用房间列表
     */
    public List<DormRoom> getAvailableRooms(Long buildingId) {
        LambdaQueryWrapper<DormRoom> queryWrapper = new LambdaQueryWrapper<>();
        if (buildingId != null) {
            queryWrapper.eq(DormRoom::getBuildingId, buildingId);
        }
        queryWrapper.eq(DormRoom::getStatus, "AVAILABLE");
        queryWrapper.eq(DormRoom::getDeleted, 0);
        // 只返回未满的房间
        queryWrapper.apply("current_occupancy < capacity");
        queryWrapper.orderByAsc(DormRoom::getFloor);
        queryWrapper.orderByAsc(DormRoom::getRoomNumber);
        return roomMapper.selectList(queryWrapper);
    }
    
    // ==================== 分配管理 ====================
    
    /**
     * 分配宿舍（检查容量，使用事务）
     * 
     * @param allocation 分配对象
     * @return 创建的分配记录
     */
    @Transactional
    public DormAllocation allocateDorm(DormAllocation allocation) {
        log.info("分配宿舍，学生ID: {}, 房间ID: {}", allocation.getStudentId(), allocation.getRoomId());
        
        // 检查必填字段
        if (allocation.getStudentId() == null) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        if (allocation.getRoomId() == null) {
            throw new IllegalArgumentException("房间ID不能为空");
        }
        if (allocation.getCheckInDate() == null) {
            allocation.setCheckInDate(LocalDate.now());
        }
        
        // 检查房间是否存在
        DormRoom room = getRoomById(allocation.getRoomId());
        if (room == null) {
            throw new IllegalArgumentException("房间不存在");
        }
        
        // 检查房间状态
        if (!"AVAILABLE".equals(room.getStatus())) {
            throw new IllegalArgumentException("房间不可用");
        }
        
        // 检查容量（关键：检查是否已满）
        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            throw new IllegalArgumentException("房间已满，无法分配");
        }
        
        // 检查学生是否已有在住的分配
        LambdaQueryWrapper<DormAllocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormAllocation::getStudentId, allocation.getStudentId());
        queryWrapper.eq(DormAllocation::getStatus, "ACTIVE");
        queryWrapper.eq(DormAllocation::getDeleted, 0);
        DormAllocation existingAllocation = allocationMapper.selectOne(queryWrapper);
        if (existingAllocation != null) {
            throw new IllegalArgumentException("该学生已有在住的宿舍分配");
        }
        
        // 创建分配记录
        allocation.setStatus("ACTIVE");
        allocation.setCreateTime(LocalDateTime.now());
        allocation.setUpdateTime(LocalDateTime.now());
        allocation.setDeleted(0);
        
        allocationMapper.insert(allocation);
        
        // 更新房间的当前入住人数（事务保证一致性）
        room.setCurrentOccupancy(room.getCurrentOccupancy() + 1);
        if (room.getCurrentOccupancy() >= room.getCapacity()) {
            room.setStatus("FULL");
        }
        room.setUpdateTime(LocalDateTime.now());
        roomMapper.updateById(room);
        
        log.info("宿舍分配成功，分配ID: {}", allocation.getId());
        return allocation;
    }
    
    /**
     * 退房（减少房间入住人数）
     * 
     * @param allocationId 分配ID
     * @param checkOutDate 退房日期（可选，默认为今天）
     * @return 更新后的分配记录
     */
    @Transactional
    public DormAllocation checkOut(Long allocationId, LocalDate checkOutDate) {
        log.info("退房，分配ID: {}", allocationId);
        
        DormAllocation allocation = allocationMapper.selectById(allocationId);
        if (allocation == null || allocation.getDeleted() == 1) {
            throw new IllegalArgumentException("分配记录不存在");
        }
        
        if (!"ACTIVE".equals(allocation.getStatus())) {
            throw new IllegalArgumentException("该分配记录不是有效状态，无法退房");
        }
        
        // 更新分配记录
        allocation.setCheckOutDate(checkOutDate != null ? checkOutDate : LocalDate.now());
        allocation.setStatus("COMPLETED");
        allocation.setUpdateTime(LocalDateTime.now());
        allocationMapper.updateById(allocation);
        
        // 更新房间的当前入住人数
        DormRoom room = getRoomById(allocation.getRoomId());
        if (room != null) {
            room.setCurrentOccupancy(Math.max(0, room.getCurrentOccupancy() - 1));
            if (room.getCurrentOccupancy() < room.getCapacity()) {
                room.setStatus("AVAILABLE");
            }
            room.setUpdateTime(LocalDateTime.now());
            roomMapper.updateById(room);
        }
        
        log.info("退房成功，分配ID: {}", allocationId);
        return allocation;
    }
    
    /**
     * 根据学生ID获取分配记录
     * 
     * @param studentId 学生ID
     * @return 分配记录列表
     */
    public List<DormAllocation> getAllocationsByStudentId(Long studentId) {
        LambdaQueryWrapper<DormAllocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormAllocation::getStudentId, studentId);
        queryWrapper.eq(DormAllocation::getDeleted, 0);
        queryWrapper.orderByDesc(DormAllocation::getCreateTime);
        return allocationMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据房间ID获取分配记录
     * 
     * @param roomId 房间ID
     * @return 分配记录列表
     */
    public List<DormAllocation> getAllocationsByRoomId(Long roomId) {
        LambdaQueryWrapper<DormAllocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormAllocation::getRoomId, roomId);
        queryWrapper.eq(DormAllocation::getDeleted, 0);
        queryWrapper.orderByDesc(DormAllocation::getCreateTime);
        return allocationMapper.selectList(queryWrapper);
    }
    
    /**
     * 根据ID获取分配记录
     * 
     * @param id 分配ID
     * @return 分配记录
     */
    public DormAllocation getAllocationById(Long id) {
        LambdaQueryWrapper<DormAllocation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DormAllocation::getId, id);
        queryWrapper.eq(DormAllocation::getDeleted, 0);
        return allocationMapper.selectOne(queryWrapper);
    }
}

