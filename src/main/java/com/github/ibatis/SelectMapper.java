package com.github.ibatis;


import java.util.List;
import java.util.Map;

/**
 * author: yangzh
 * desc: 通用查询
 * @param <T>
 */
public interface SelectMapper<T>{

     Integer selectCount(Class clazz, Map map);

     List<T> selectAll(Class clazz, Map map);

     List<T> selectPage(Class clazz, Map map);

}
