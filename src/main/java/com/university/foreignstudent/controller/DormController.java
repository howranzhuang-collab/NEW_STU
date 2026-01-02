package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.entity.DormAllocation;
import com.university.foreignstudent.entity.DormBuilding;
import com.university.foreignstudent.entity.DormRoom;
import com.university.foreignstudent.service.DormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 住宿管理控制器
 * 提供宿舍楼、房间、分配的 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/dorms")
@RequiredArgsConstructor
public class DormController {
    
    private final DormService dormService;
    
    // ==================== 宿舍楼相关接口 ====================
    
    /**
     * 获取所有宿舍楼列表
     * 
     * @return 宿舍楼列表
     */
    @GetMapping("/buildings")
    public Result<List<DormBuilding>> getAllBuildings() {
        try {
            List<DormBuilding> buildings = dormService.getAllBuildings();
            return Result.success(buildings);
        } catch (Exception e) {
            log.error("获取宿舍楼列表失败", e);
            return Result.error("获取宿舍楼列表失败");
        }
    }
    
    /**
     * 根据ID获取宿舍楼
     * 
     * @param id 宿舍楼ID
     * @return 宿舍楼信息
     */
    @GetMapping("/buildings/{id}")
    public Result<DormBuilding> getBuildingById(@PathVariable Long id) {
        try {
            DormBuilding building = dormService.getBuildingById(id);
            if (building == null) {
                return Result.error(404, "宿舍楼不存在");
            }
            return Result.success(building);
        } catch (Exception e) {
            log.error("获取宿舍楼失败", e);
            return Result.error("获取宿舍楼失败");
        }
    }
    
    /**
     * 创建宿舍楼
     * 
     * @param building 宿舍楼信息
     * @return 创建的宿舍楼
     */
    @PostMapping("/buildings")
    public Result<DormBuilding> createBuilding(@RequestBody DormBuilding building) {
        try {
            DormBuilding createdBuilding = dormService.createBuilding(building);
            return Result.success("宿舍楼创建成功", createdBuilding);
        } catch (IllegalArgumentException e) {
            log.error("创建宿舍楼失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建宿舍楼异常", e);
            return Result.error("创建宿舍楼失败，请稍后重试");
        }
    }
    
    // ==================== 房间相关接口 ====================
    
    /**
     * 根据宿舍楼ID获取房间列表
     * 
     * @param buildingId 宿舍楼ID
     * @return 房间列表
     */
    @GetMapping("/buildings/{buildingId}/rooms")
    public Result<List<DormRoom>> getRoomsByBuildingId(@PathVariable Long buildingId) {
        try {
            List<DormRoom> rooms = dormService.getRoomsByBuildingId(buildingId);
            return Result.success(rooms);
        } catch (Exception e) {
            log.error("获取房间列表失败", e);
            return Result.error("获取房间列表失败");
        }
    }
    
    /**
     * 获取可用房间列表
     * 
     * @param buildingId 宿舍楼ID（可选查询参数）
     * @return 可用房间列表
     */
    @GetMapping("/rooms/available")
    public Result<List<DormRoom>> getAvailableRooms(@RequestParam(required = false) Long buildingId) {
        try {
            List<DormRoom> rooms = dormService.getAvailableRooms(buildingId);
            return Result.success(rooms);
        } catch (Exception e) {
            log.error("获取可用房间列表失败", e);
            return Result.error("获取可用房间列表失败");
        }
    }
    
    /**
     * 根据ID获取房间
     * 
     * @param id 房间ID
     * @return 房间信息
     */
    @GetMapping("/rooms/{id}")
    public Result<DormRoom> getRoomById(@PathVariable Long id) {
        try {
            DormRoom room = dormService.getRoomById(id);
            if (room == null) {
                return Result.error(404, "房间不存在");
            }
            return Result.success(room);
        } catch (Exception e) {
            log.error("获取房间失败", e);
            return Result.error("获取房间失败");
        }
    }
    
    /**
     * 创建房间
     * 
     * @param room 房间信息
     * @return 创建的房间
     */
    @PostMapping("/rooms")
    public Result<DormRoom> createRoom(@RequestBody DormRoom room) {
        try {
            DormRoom createdRoom = dormService.createRoom(room);
            return Result.success("房间创建成功", createdRoom);
        } catch (IllegalArgumentException e) {
            log.error("创建房间失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建房间异常", e);
            return Result.error("创建房间失败，请稍后重试");
        }
    }
    
    // ==================== 分配相关接口 ====================
    
    /**
     * 分配宿舍（检查容量，使用事务）
     * 
     * @param allocation 分配信息
     * @return 创建的分配记录
     */
    @PostMapping("/allocations")
    public Result<DormAllocation> allocateDorm(@RequestBody DormAllocation allocation) {
        try {
            DormAllocation createdAllocation = dormService.allocateDorm(allocation);
            return Result.success("宿舍分配成功", createdAllocation);
        } catch (IllegalArgumentException e) {
            log.error("分配宿舍失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("分配宿舍异常", e);
            return Result.error("分配宿舍失败，请稍后重试");
        }
    }
    
    /**
     * 退房
     * 
     * @param allocationId 分配ID
     * @param request 包含 checkOutDate 的请求体（可选）
     * @return 更新后的分配记录
     */
    @PostMapping("/allocations/{allocationId}/checkout")
    public Result<DormAllocation> checkOut(
            @PathVariable Long allocationId,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            LocalDate checkOutDate = null;
            if (request != null && request.get("checkOutDate") != null) {
                Object checkOutDateObj = request.get("checkOutDate");
                if (checkOutDateObj instanceof String) {
                    checkOutDate = LocalDate.parse((String) checkOutDateObj);
                }
            }
            
            DormAllocation allocation = dormService.checkOut(allocationId, checkOutDate);
            return Result.success("退房成功", allocation);
        } catch (IllegalArgumentException e) {
            log.error("退房失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("退房异常", e);
            return Result.error("退房失败，请稍后重试");
        }
    }
    
    /**
     * 根据学生ID获取分配记录列表
     * 
     * @param studentId 学生ID
     * @return 分配记录列表
     */
    @GetMapping("/allocations/student/{studentId}")
    public Result<List<DormAllocation>> getAllocationsByStudentId(@PathVariable Long studentId) {
        try {
            List<DormAllocation> allocations = dormService.getAllocationsByStudentId(studentId);
            return Result.success(allocations);
        } catch (Exception e) {
            log.error("获取分配记录列表失败", e);
            return Result.error("获取分配记录列表失败");
        }
    }
    
    /**
     * 根据房间ID获取分配记录列表
     * 
     * @param roomId 房间ID
     * @return 分配记录列表
     */
    @GetMapping("/allocations/room/{roomId}")
    public Result<List<DormAllocation>> getAllocationsByRoomId(@PathVariable Long roomId) {
        try {
            List<DormAllocation> allocations = dormService.getAllocationsByRoomId(roomId);
            return Result.success(allocations);
        } catch (Exception e) {
            log.error("获取分配记录列表失败", e);
            return Result.error("获取分配记录列表失败");
        }
    }
    
    /**
     * 根据ID获取分配记录
     * 
     * @param id 分配ID
     * @return 分配记录
     */
    @GetMapping("/allocations/{id}")
    public Result<DormAllocation> getAllocationById(@PathVariable Long id) {
        try {
            DormAllocation allocation = dormService.getAllocationById(id);
            if (allocation == null) {
                return Result.error(404, "分配记录不存在");
            }
            return Result.success(allocation);
        } catch (Exception e) {
            log.error("获取分配记录失败", e);
            return Result.error("获取分配记录失败");
        }
    }
}

