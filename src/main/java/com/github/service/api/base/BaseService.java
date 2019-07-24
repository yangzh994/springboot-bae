package com.github.service.api.base;

import com.github.ibatis.SelectMapper;

/**
 * author: yangzh
 * desc: 通用的dao方法
 */
public interface BaseService<T> extends SelectMapper<T> {

    public int insert(T t);

    public int updateByPrimaryKey(T t);

    public int deleteByPrimaryKey(Integer id);

    public T selectByPrimaryKey(Integer id);
}
