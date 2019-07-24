package com.github.controller;

import com.github.models.User;
import com.github.service.api.UserService;
import com.github.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("sys/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/add")
    public String add(User user){
        userService.insert(user);
        return "SUCCESS";
    }

    @RequestMapping("/update")
    public String update(User user){
        userService.updateByPrimaryKey(user);
        return "SUCCESS";
    }

    @RequestMapping("/delete")
    public String delete(Integer id){
        userService.deleteByPrimaryKey(id);
        return "SUCCESS";
    }

    @RequestMapping("/findOne")
    public User findOne(Integer in){
        return userService.selectByPrimaryKey(in);
    }


    /**
     * 查询全部  条件传递User对象属性就好了
     * 如果要查name= 'yangzh' 传递user=yangzh就可以l
     * @param user
     * @return
     */
    @RequestMapping("/findAll")
    public List<User> findAll(User user){
        Map<String,Object> map = new HashMap();
        map.put("param",user);
        return userService.selectAll(User.class,map);
    }

    @RequestMapping("/findPage")
    public List<User> findPage(User user,Integer page,Integer size){
        Map<String,Object> map = new HashMap();
        map.put("param",user);
        //先根据条件查询总数
        Integer count = userService.selectCount(User.class, map);
        //分页查询
        return userService.selectPage(User.class, PageUtil.bulid(map,page,size));
    }

}
