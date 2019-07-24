package com.github.ibatis;

import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * author: yangzh
 * desc: 自定义MappedStatement容器
 */
public class RegisterMappedStatement {

    private static Map<String,Object> include = new HashMap();

    private static RegisterMappedStatement registerMappedStatement;

    private static MappedStatementComm mappedStatementComm;

    private Configuration configuration;

    private RegisterMappedStatement(Configuration configuration){
        this.configuration = configuration;
        mappedStatementComm = MappedStatementComm.getMappedStatementComm(configuration);
    }
    public static synchronized RegisterMappedStatement getRegisterMappedStatement(Configuration configuration){
        if(registerMappedStatement == null)
            registerMappedStatement = new RegisterMappedStatement(configuration);
        return registerMappedStatement;
    }

    public void registerMappedStatement(Class clazz,SelectType type){
        String key = clazz.getName() + type;
        if(!include.containsKey(key)){
            mappedStatementComm.registerMappedStatement(clazz,type);
            include.put(key,key);
        }
    }
}
