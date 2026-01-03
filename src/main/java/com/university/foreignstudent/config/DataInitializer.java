package com.university.foreignstudent.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.university.foreignstudent.entity.User;
import com.university.foreignstudent.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 数据初始化器
 * 应用启动时自动创建测试账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        log.info("开始初始化测试账号...");
        
        // 创建管理员账号 admin1
        createUserIfNotExists("admin1", "admin1", "ADMIN");
        
        // 创建学生账号 sushan1
        createUserIfNotExists("sushan1", "sushan1", "STUDENT");
        
        log.info("测试账号初始化完成");
    }
    
    /**
     * 如果用户不存在则创建
     */
    private void createUserIfNotExists(String username, String password, String role) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        queryWrapper.eq(User::getDeleted, 0);
        User existingUser = userMapper.selectOne(queryWrapper);
        
        if (existingUser != null) {
            log.info("用户 {} 已存在，跳过创建", username);
            return;
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setDeleted(0);
        
        userMapper.insert(user);
        log.info("成功创建用户: {} (角色: {})", username, role);
    }
}

