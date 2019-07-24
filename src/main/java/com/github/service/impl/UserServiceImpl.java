package com.github.service.impl;

import com.github.mapper.UserMapper;
import com.github.models.User;
import com.github.service.api.UserService;
import com.github.service.impl.base.BaseServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper,User> implements UserService {
}
