package com.dmc.service.impl;

import com.dmc.mapper.ResourceMapper;
import com.dmc.mapper.UserMapper;
import com.dmc.model.Resource;
import com.dmc.model.User;
import com.dmc.service.UserService;
import com.dmc.util.AppConst;
import com.dmc.util.id.IdUtil;
import com.google.common.base.Strings;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResourceMapper resourceMapper;

    @Override
    public User login(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", user.getUsername());
        params.put("password", DigestUtils.md5Hex(user.getPassword()));

        user = userMapper.login(params);
//        if (user != null) {
//            user.setRoleIds(userMapper.getUserRoleIds(user.getId()));
//        }


        return user;
    }


    @Override
    public void add(User user) {
        user.setId(IdUtil.generateId());
        if (userMapper.countUserName(user.getUsername()) > 0) {
            throw new RuntimeException("登录名已存在！");
        } else {
            user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            userMapper.save(user);
        }
    }

    @Override
    public User get(Long id) {
        return userMapper.getById(id);
    }

    @Override
    public void edit(User user) {
        if (userMapper.countUserName(user.getName()) > 0) {
            throw new RuntimeException("登录名已存在！");
        } else {
            if (!Strings.isNullOrEmpty(user.getPassword())) {
                user.setPassword(DigestUtils.md5Hex(user.getPassword()));
            }
            userMapper.update(user);
        }
    }

    @Override
    public void delete(Long id) {
        userMapper.deleteRoles(id);
        userMapper.deleteById(id);
    }

    @Override
    public void grant(User user) {
        Assert.notNull(user.getId(), "id 不能为空");
        Assert.notEmpty(user.getRoleIds(), "roleIds must have length; it must not be null or empty");


        userMapper.deleteRoles(user.getId());
        userMapper.saveRoles(user.getId(), user.getRoleIds());
    }

    @Override
    public List<String> resourceList(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", id);
        params.put("type", AppConst.RESOURCE_TYPE_METHOD);
        List<Resource> resources = resourceMapper.getResourceList(params);
        return resources.stream().map(Resource::getUrl).collect(Collectors.toList());
    }

    @Override
    public void editPwd(User user) {
        Assert.notNull(user, "user can not be null");
        Assert.hasText(user.getPassword(), "password mush have value");

        user.setPassword(DigestUtils.md5Hex(user.getPassword()));
        userMapper.update(user);
    }

    @Override
    public boolean editCurrentUserPwd(Long currUid, String oldPwd, String pwd) {
        User user = userMapper.getById(currUid);
        if (user.getPassword().equalsIgnoreCase(DigestUtils.md5Hex(oldPwd))) {// 说明原密码输入正确
            user.setPassword(DigestUtils.md5Hex(pwd));
            userMapper.update(user);
            return true;
        }
        return false;
    }

    @Override
    public List<String> getUserRoleNames(Long id) {
        return userMapper.getUserRoleNames(id);
    }

}
