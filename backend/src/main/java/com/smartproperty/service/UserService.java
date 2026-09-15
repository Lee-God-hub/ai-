package com.smartproperty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartproperty.dto.PageResult;
import com.smartproperty.entity.User;
import java.util.List;

public interface UserService extends IService<User> {
    
    boolean register(User user);
    
    User login(String username, String password);
    
    User getUserById(Long id);
    
    User getUserByUsername(String username);
    
    boolean updateUser(User user);
    
    boolean deleteUser(Long id);
    
    List<User> getAllUsers();
    
    List<User> getUsersByPage(Integer pageNum, Integer pageSize);
    
    PageResult<User> getUserPage(Integer pageNum, Integer pageSize, Integer role);
    
    boolean checkUsernameExists(String username);
    
    boolean checkPhoneExists(String phone);
    
    boolean updateUserStatus(Long id, Integer status);
}