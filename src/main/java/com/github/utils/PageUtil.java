package com.github.utils;

import java.util.Map;

/**
 * author: yangzh
 * desc: 通用分页需要的构造器（构造必要查询条件）
 */
public class PageUtil {

    private PageUtil(){}

    /**
     * 因为我们通用分页查询条件是offset,limit
     * 但是前端传递过来的是p,psize ,我们通过这个构造器 ，去构造我们需要的条件
     * @return
     */
    public static Map<String,Object> bulid(Map<String,Object> map,Integer p,Integer psize){
        Integer offset = (p-1) * psize;
        Integer limit = psize;
        map.put("offset",offset);
        map.put("limit",limit);
        return map;
    }
}
