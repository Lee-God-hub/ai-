package com.smartproperty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.User;
import com.smartproperty.mapper.UserMapper;
import com.smartproperty.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户业务逻辑层实现类
 * 实现用户相关的业务逻辑操作，使用BCrypt加密密码
 *
 * @author 毕业设计项目
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     *
     * @param user 用户对象
     * @return 注册结果
     */
    @Override
    @Transactional
    public boolean register(User user) {
        try {
            // 1. 检查用户名是否已存在
            if (checkUsernameExists(user.getUsername())) {
                throw new RuntimeException("用户名已存在");
            }

            // 2. 检查手机号是否已存在
            if (checkPhoneExists(user.getPhone())) {
                throw new RuntimeException("手机号已存在");
            }

            // 3. 对密码进行BCrypt加密
            String encryptedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(encryptedPassword);

            // 4. 设置默认值
            // 如果没有指定角色，默认为普通用户
            if (user.getRole() == null) {
                user.setRole(0);
            }
            // 验证角色值
            if (user.getRole() != 0 && user.getRole() != 1 && user.getRole() != 2) {
                throw new RuntimeException("角色参数错误");
            }
            user.setStatus(1); // 默认正常状态
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            // 5. 插入数据库
            return save(user);

        } catch (Exception e) {
            throw new RuntimeException("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功的用户对象
     */
    @Override
    public User login(String username, String password) {
        try {
            // 1. 根据用户名查询用户
            User user = userMapper.selectByUsername(username);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 2. 检查用户状态
            if (user.getStatus() != 1) {
                throw new RuntimeException("用户已被禁用");
            }

            // 3. 使用BCrypt验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("密码错误");
            }

            // 4. 更新最后登录时间
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateLastLoginTime(user.getId(), user.getLastLoginTime().toString());

            // 5. 返回用户信息（清除密码）
            user.setPassword(null);
            return user;

        } catch (Exception e) {
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户对象
     */
    @Override
    public User getUserById(Long id) {
        User user = getById(id);
        if (user != null) {
            user.setPassword(null); // 清除密码
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户对象
     */
    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user != null) {
            user.setPassword(null); // 清除密码
        }
        return user;
    }

    /**
     * 更新用户信息
     *
     * @param user 用户对象
     * @return 更新结果
     */
    @Override
    @Transactional
    public boolean updateUser(User user) {
        try {
            // 1. 检查用户是否存在
            User existingUser = getById(user.getId());
            if (existingUser == null) {
                throw new RuntimeException("用户不存在");
            }

            // 2. 如果修改了密码，需要BCrypt加密
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                String encryptedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(encryptedPassword);
            } else {
                // 保持原密码
                user.setPassword(existingUser.getPassword());
            }

            // 3. 更新修改时间
            user.setUpdateTime(LocalDateTime.now());

            // 4. 执行更新
            return updateById(user);

        } catch (Exception e) {
            throw new RuntimeException("更新用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        try {
            return removeById(id);
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询所有用户
     * 
     * @return 用户列表
     */
    @Override
    public List<User> getAllUsers() {
        List<User> users = list();
        // 清除所有用户的密码
        users.forEach(user -> user.setPassword(null));
        return users;
    }
    
    /**
     * 分页查询用户
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户列表
     */
    @Override
    public List<User> getUsersByPage(Integer pageNum, Integer pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        Page<User> result = page(page);
        List<User> users = result.getRecords();
        // 清除所有用户的密码
        users.forEach(user -> user.setPassword(null));
        return users;
    }
    
    /**
     * 分页查询用户（按角色）
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param role 角色
     * @return 分页结果
     */
    @Override
    public PageResult<User> getUserPage(Integer pageNum, Integer pageSize, Integer role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        Page<User> result = page(page, wrapper);
        List<User> users = result.getRecords();
        // 清除所有用户的密码
        users.forEach(user -> user.setPassword(null));
        
        PageResult<User> pageResult = new PageResult<>();
        pageResult.setRecords(users);
        pageResult.setTotal(result.getTotal());
        return pageResult;
    }
    
    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return 是否存在
     */
    @Override
    public boolean checkUsernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }
    
    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return 是否存在
     */
    @Override
    public boolean checkPhoneExists(String phone) {
        User user = userMapper.selectByPhone(phone);
        return user != null;
    }
    
    /**
     * 更新用户状态
     * 
     * @param id 用户ID
     * @param status 状态
     * @return 更新结果
     */
    @Override
    @Transactional
    public boolean updateUserStatus(Long id, Integer status) {
        try {
            LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(User::getId, id).set(User::getStatus, status);
            return update(wrapper);
        } catch (Exception e) {
            throw new RuntimeException("更新用户状态失败: " + e.getMessage());
        }
    }
}