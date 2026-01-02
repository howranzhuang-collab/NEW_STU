package com.university.foreignstudent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.User;
import com.university.foreignstudent.mapper.UserMapper;
import com.university.foreignstudent.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务类
 * 提供用户注册、登录等业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     * 
     * @param username 用户名
     * @param password 密码
     * @param role 角色
     * @return 注册结果
     */
    @Transactional
    public Map<String, Object> register(String username, String password, String role) {
        log.info("开始注册用户: {}", username);
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User existingUser = userMapper.selectOne(queryWrapper);
        
        if (existingUser != null) {
            log.warn("用户名已存在: {}", username);
            throw new RuntimeException("用户名已存在");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : "STUDENT");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        userMapper.insert(user);
        log.info("用户注册成功: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注册成功");
        result.put("userId", user.getId());
        return result;
    }
    
    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param password 密码
     * @return 登录结果，包含 JWT token
     */
    public Map<String, Object> login(String username, String password) {
        log.info("用户尝试登录: {}", username);
        
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getDeleted, 0);
        User user = userMapper.selectOne(queryWrapper);
        
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("密码错误: {}", username);
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 生成 JWT token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        log.info("用户登录成功: {}", username);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("role", user.getRole());
        return result;
    }
    
    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象
     */
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getDeleted, 0);
        return userMapper.selectOne(queryWrapper);
    }
}

