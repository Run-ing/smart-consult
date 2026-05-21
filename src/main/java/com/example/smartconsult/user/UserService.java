package com.example.smartconsult.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.smartconsult.exception.BusinessException;
import com.example.smartconsult.user.entity.SysUser;
import com.example.smartconsult.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;

    public UserService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public SysUser findByPhone(String phone) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, phone)
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
    }

    public SysUser findById(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException(401, "登录状态无效");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "用户已被禁用");
        }
        return user;
    }

    public SysUser getOrCreateByPhone(String phone) {
        SysUser existing = findByPhone(phone);
        if (existing != null) {
            return existing;
        }

        LocalDateTime now = LocalDateTime.now();
        SysUser user = new SysUser();
        user.setPhone(phone);
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        sysUserMapper.insert(user);
        return user;
    }

    public void updateLastLoginTime(Long userId, LocalDateTime loginTime) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginTime(loginTime);
        user.setUpdatedAt(loginTime);
        sysUserMapper.updateById(user);
    }
}
