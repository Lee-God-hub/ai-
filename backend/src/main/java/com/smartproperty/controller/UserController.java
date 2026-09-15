package com.smartproperty.controller;

import com.smartproperty.common.Result;
import com.smartproperty.common.RoleConstants;
import com.smartproperty.dto.LoginRequest;
import com.smartproperty.dto.LoginResponse;
import com.smartproperty.entity.User;
import com.smartproperty.mapper.PropertyMapper;
import com.smartproperty.mapper.AppointmentMapper;
import com.smartproperty.mapper.FavoriteMapper;
import com.smartproperty.service.UserService;
import com.smartproperty.util.JwtUtil;
import com.smartproperty.util.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求，提供RESTful API接口，支持JWT认证
 *
 * @author 毕业设计项目
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PropertyMapper propertyMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

    /**
     * 用户注册接口
     *
     * @param user 用户注册信息
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody User user) {
        try {
            // 验证必要参数
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                return Result.paramError("用户名不能为空");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                return Result.paramError("密码不能为空");
            }
            if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
                return Result.paramError("手机号不能为空");
            }
            
            // 验证角色参数（如果提供）
            if (user.getRole() != null && user.getRole() != 0 && user.getRole() != 1) {
                return Result.paramError("角色参数错误，只能是0（普通用户）或1（房东）");
            }

            // 执行注册
            boolean success = userService.register(user);
            if (success) {
                Map<String, Object> data = new HashMap<>();
                data.put("username", user.getUsername());
                data.put("role", user.getRole());
                data.put("message", "注册成功");
                return Result.success("注册成功", data);
            } else {
                return Result.error("注册失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误，注册失败");
        }
    }

    /**
     * 用户登录接口（返回JWT令牌）
     *
     * @param loginRequest 登录请求参数
     * @return 登录结果，包含JWT令牌和用户信息
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String username = loginRequest.getUsername();
            String password = loginRequest.getPassword();

            // 执行登录
            User user = userService.login(username, password);

            // 生成JWT令牌
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 构建响应对象
            LoginResponse loginResponse = new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getRealName(),
                    user.getPhone(),
                    user.getEmail(),
                    user.getRole(),
                    user.getAvatar()
            );

            return Result.success("登录成功", loginResponse);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误，登录失败");
        }
    }

    /**
     * 获取当前登录用户信息（需要JWT认证）
     *
     * @return 当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser() {
        try {
            Long userId = UserContext.getUserId();
            if (userId == null) {
                return Result.unauthorized("未登录");
            }

            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.notFound("用户不存在");
            }

            return Result.success(user);
        } catch (Exception e) {
            return Result.error("获取用户信息失败");
        }
    }
    
    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id:\\d+}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return Result.notFound("用户不存在");
            }
            return Result.success(user);
        } catch (Exception e) {
            return Result.error("查询用户失败");
        }
    }
    
    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param user 用户信息
     * @return 更新结果
     */
    @PutMapping("/{id:\\d+}")
    public Result<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            boolean success = userService.updateUser(user);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("系统错误，更新失败");
        }
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id:\\d+}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            boolean success = userService.deleteUser(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            return Result.error("系统错误，删除失败");
        }
    }
    
    /**
     * 查询所有用户
     * 
     * @return 用户列表
     */
    @GetMapping("/all")
    public Result<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("查询用户列表失败");
        }
    }
    
    /**
     * 分页查询用户
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    @GetMapping("/page")
    public Result<List<User>> getUsersByPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            List<User> users = userService.getUsersByPage(pageNum, pageSize);
            return Result.success(users);
        } catch (Exception e) {
            return Result.error("分页查询失败");
        }
    }
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 检查结果
     */
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsernameExists(@RequestParam String username) {
        try {
            boolean exists = userService.checkUsernameExists(username);
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", exists);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查用户名失败");
        }
    }
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 检查结果
     */
    @GetMapping("/check-phone")
    public Result<Map<String, Boolean>> checkPhoneExists(@RequestParam String phone) {
        try {
            boolean exists = userService.checkPhoneExists(phone);
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", exists);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检查手机号失败");
        }
    }
    
    /**
     * 系统统计数据接口（管理员）
     * 
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Long>> getStatistics() {
        try {
            if (!RoleConstants.isAdmin(UserContext.getRole())) {
                return Result.forbidden("只有管理员可以查看统计数据");
            }
            
            Map<String, Long> statistics = new HashMap<>();
            statistics.put("userCount", userService.count());
            statistics.put("propertyCount", propertyMapper.selectCount(null));
            statistics.put("appointmentCount", appointmentMapper.selectCount(null));
            statistics.put("favoriteCount", favoriteMapper.selectCount(null));
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取统计数据失败");
        }
    }

    /**
     * 健康检查接口
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("用户服务运行正常");
    }
}