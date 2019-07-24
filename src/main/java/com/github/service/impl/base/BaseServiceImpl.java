package com.github.service.impl.base;

import com.github.ibatis.RegisterMappedStatement;
import com.github.ibatis.SelectType;
import com.github.ibatis.StatementPre;
import com.github.service.api.base.BaseService;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * author: yangzh
 * desc: 通用方法实现类
 */
public class BaseServiceImpl<M,N>  implements BaseService<N>,ApplicationContextAware {

    private final static Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);

    private ApplicationContext applicationContext;
    private RegisterMappedStatement registerMappedStatement;

    private Class paramClazz ;
    private SqlSessionTemplate template;
    private Class clazz;
    private Object object;
    private Method insert;
    private Method update;
    private Method deleteByPrimaryKey;
    private Method selectByPrimaryKey;
    private boolean state = false;
    private void init(){
        try {
            if(state == false){
                logger.info("---- 初始化通用构造类! ---- state:{}",state);
                clazz = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                paramClazz = (Class) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
                template = (SqlSessionTemplate)applicationContext.getBean("sqlSessionTemplate");
                object = template.getMapper(clazz);
                insert = clazz.getMethod("insert",paramClazz);
                update = clazz.getMethod("updateByPrimaryKey",paramClazz);
                deleteByPrimaryKey = clazz.getMethod("deleteByPrimaryKey",Integer.class);
                selectByPrimaryKey = clazz.getMethod("selectByPrimaryKey",Integer.class);
                this.registerMappedStatement = RegisterMappedStatement.getRegisterMappedStatement(template.getConfiguration());
                state = true;
                logger.info("---- 初始化通用构造类完成! ---- state:{}",state);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int insert(Object o) {
        init();
        try {
            return (Integer) insert.invoke(object,o);
        } catch (Exception e){
            logger.info("---- 通用新增异常! ---- exception:{}",e);
            e.printStackTrace();
            return 0;
        }
    }

    public int updateByPrimaryKey(Object o) {
        init();
        try {
            return (Integer) update.invoke(object,o);
        } catch (Exception e){
            logger.info("---- 通用更新异常! ---- exception:{}",e);
            e.printStackTrace();
            return 0;
        }
    }

    public int deleteByPrimaryKey(Integer id) {
        init();
        try {
            return (Integer) deleteByPrimaryKey.invoke(object,id);
        } catch (Exception e){
            logger.info("---- 通用删除异常! ---- exception:{}",e);
            e.printStackTrace();
            return 0;
        }
    }

    public N selectByPrimaryKey(Integer id) {
        init();
        try {
            return  (N)selectByPrimaryKey.invoke(object,id);
        } catch (Exception e){
            logger.info("---- 通用查询异常! ---- exception:{}",e);
            e.printStackTrace();
            return null;
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Integer selectCount(Class clazz, Map map) {
        init();
        registerMappedStatement.registerMappedStatement(clazz, SelectType.COUNT);
        List<Object> objects = template.selectList(StatementPre.PRE_COUNT_ID + clazz.getName(), map);
        if(objects == null || objects.size() == 0) return 0;
        return (Integer)objects.get(0);
    }

    @Override
    public List<N> selectAll(Class clazz, Map map) {
        init();
        registerMappedStatement.registerMappedStatement(clazz,SelectType.ALL);
        return template.selectList(StatementPre.PRE_ALL_ID + clazz.getName(),map);
    }

    @Override
    public List<N> selectPage(Class clazz, Map map) {
        init();
        registerMappedStatement.registerMappedStatement(clazz, SelectType.PAGE);
        return template.selectList(StatementPre.PRE_PAGE_ID + clazz.getName(),map);
    }
}
