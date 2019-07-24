package com.github.ibatis;

import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * author: yangzh
 * desc: MappedStatement的注册器
 */
public class MappedStatementComm {

    private Configuration configuration;
    private SqlSourceComm sqlSourceComm;

    private MappedStatementComm(Configuration configuration){
        this.configuration = configuration;
        sqlSourceComm = SqlSourceComm.getSqlSourceComm(configuration);
    };

    private static MappedStatementComm mappedStatementComm;

    public static synchronized MappedStatementComm getMappedStatementComm(Configuration configuration){
        if(mappedStatementComm == null)
            mappedStatementComm = new MappedStatementComm(configuration);
        return mappedStatementComm;
    }

    public void registerMappedStatement(Class clazz,SelectType type){
            SqlSource sqlSouce = sqlSourceComm.createSqlSouce(clazz,type);
            String id;
            MappedStatement mappedStatement;
            switch (type) {
                case PAGE:
                    id =  StatementPre.PRE_PAGE_ID + clazz.getName();
                    mappedStatement = defaultMappedStatement(id, sqlSouce, clazz);
                    break;
                case COUNT:
                    id =  StatementPre.PRE_COUNT_ID + clazz.getName();
                    mappedStatement = defaultMappedStatement(id, sqlSouce, Integer.class);
                    break;
                case ALL:
                    id =  StatementPre.PRE_ALL_ID + clazz.getName();
                    mappedStatement = defaultMappedStatement(id, sqlSouce, clazz);
                    break;
                default:
                throw new IllegalArgumentException("无法找到对应的类型:" + type);
             }
            configuration.addMappedStatement(mappedStatement);
    }

    private SqlSource createSqlSource(Class clazz, SelectType type){
        SqlSource sqlSouce = sqlSourceComm.createSqlSouce(clazz,type);
        return sqlSouce;
    }

    private MappedStatement defaultMappedStatement(String id, SqlSource sqlSource, Class resultClass){
        return createMappedStatement(id,sqlSource, StatementType.PREPARED, SqlCommandType.SELECT
        ,null,null,null,null,null,resultClass
        ,null,false,true,false
        , new NoKeyGenerator(),null,null,null,new XMLLanguageDriver(),null);
    }

    private MappedStatement createMappedStatement(
            String id,
            SqlSource sqlSource,
            StatementType statementType,
            SqlCommandType sqlCommandType,
            Integer fetchSize,
            Integer timeout,
            String parameterMap,
            Class<?> parameterType,
            String resultMap,
            Class<?> resultType,
            ResultSetType resultSetType,
            boolean flushCache,
            boolean useCache,
            boolean resultOrdered,
            KeyGenerator keyGenerator,
            String keyProperty,
            String keyColumn,
            String databaseId,
            LanguageDriver lang,
            String resultSets) {
        boolean isSelect = sqlCommandType == SqlCommandType.SELECT;
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlSource, sqlCommandType)
                .fetchSize(fetchSize)
                .timeout(timeout)
                .statementType(statementType)
                .keyGenerator(keyGenerator)
                .keyProperty(keyProperty)
                .keyColumn(keyColumn)
                .databaseId(databaseId)
                .lang(lang)
                .resultOrdered(resultOrdered)
                .resultSets(resultSets)
                .resultMaps(getStatementResultMaps(resultType, id))
                .resultSetType(resultSetType)
                .flushCacheRequired(valueOrDefault(flushCache, !isSelect))
                .useCache(valueOrDefault(useCache, isSelect));
        return statementBuilder.build();
    }


    private <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private List<ResultMap> getStatementResultMaps(
            Class<?> resultType,
            String statementId) {
        List<ResultMap> resultMaps = new ArrayList<>();
         if (resultType != null) {
            ResultMap inlineResultMap = new ResultMap.Builder(
                    configuration,
                    statementId + "-Inline",
                    resultType,
                    new ArrayList<ResultMapping>(),
                    null).build();
            resultMaps.add(inlineResultMap);
        }
        return resultMaps;
    }
}
