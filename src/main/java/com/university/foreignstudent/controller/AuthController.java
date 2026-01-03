package com.university.foreignstudent.controller;

import com.university.foreignstudent.dto.Result;
import com.university.foreignstudent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 * 提供用户注册和登录接口
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    /**
     * 用户注册
     * 
     * @param request 注册请求，包含 username、password、role
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String role = request.get("role");
            
            if (username == null || username.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            
            Map<String, Object> result = userService.register(username, password, role);
            return Result.success("注册成功", result);
        } catch (RuntimeException e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("注册异常", e);
            return Result.error("注册失败，请稍后重试");
        }
    }
    
    /**
     * 用户登录
     * 
     * @param request 登录请求，包含 username、password
     * @return 登录结果，包含 JWT token
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        log.info("=== 登录请求开始 ===");
        log.info("请求参数: {}", request);
        try {
            String username = request != null ? request.get("username") : null;
            String password = request != null ? request.get("password") : null;
            
            log.info("提取参数: username={}, password={}", username, password != null ? "***" : null);
            
            if (username == null || username.trim().isEmpty()) {
                log.warn("用户名为空");
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                log.warn("密码为空");
                return Result.error("密码不能为空");
            }
            
            log.info("开始调用 UserService.login: username={}", username);
            Map<String, Object> result = userService.login(username, password);
            log.info("UserService.login 调用成功");
            log.info("=== 登录请求成功 ===");
            return Result.success("登录成功", result);
        } catch (RuntimeException e) {
            log.error("登录失败 (RuntimeException): {}", e.getMessage(), e);
            log.error("异常堆栈:", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("登录异常 (Exception): 类型={}, 消息={}", e.getClass().getName(), e.getMessage(), e);
            log.error("完整异常堆栈:", e);
            return Result.error("登录失败，请稍后重试: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}

