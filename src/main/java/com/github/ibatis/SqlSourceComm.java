package com.github.ibatis;

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.*;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.*;

/**
 * author: yangzh
 * desc : 生成sqlSource类
 */
public class SqlSourceComm {
    private static Map<Class, String> dbTableNameMap = new HashMap<>(128);
    private static Map<String, SqlNode> dbTableFieldSqlNodeMap = new HashMap<>(128);
    private static Map<Class, SqlSource> dbTableAllSqlSourceMap = new HashMap<>(128);
    private static Map<Class, SqlSource> dbTableCountSqlSourceMap = new HashMap<>(128);
    private static Map<Class, SqlSource> dbTablePageSqlSourceMap = new HashMap<>(128);
    //暂时无用,后期用来
    private boolean isLru = false;
    private Map<Class, Object> lruMap;
    private Class eldestKey;
    private static Map<Class, List<String>> beanFieldsMap = new HashMap<>(128);
    private static List<String> excludefields = new ArrayList<>();
    //暂时无用,后期如果用它来维护不同bean，不需要的字段
    private static Map<Class, List<String>> class_excludefieldsMap = new HashMap<>();
    private Configuration configuration;
    private static final String SELECT_COUNT_NODE_STATIC = "SELECT count(1) FROM {0}";
    private static final String SELECT_All_NODE_STATIC = "SELECT * FROM {0} ";
    private static final String SELECT_LIMIT_NODE_STATIC = " LIMIT #{offset},#{limit}";
    private static final String IF_NODE_STATIC = "param.{0} != null and param.{1} != {2}";
    private static final String STATIC_NODE = " and {0} = {1} ";


    private static SqlSourceComm sqlSourceComm;

    private SqlSourceComm(Configuration configuration) {
        this.configuration = configuration;
        initExclude();
        initDbTable();
    }

    private void initLru(final int size) {
        lruMap = new LinkedHashMap<Class, Object>(size, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Class, Object> eldest) {
                boolean tooBig = size() > size;
                if (tooBig) {
                    eldestKey = eldest.getKey();
                }
                return tooBig;
            }
        };
    }

    public void openLru(int size) {
        initLru(size);
        isLru = true;
    }

    private void cycleKeyList(Class key) {
        lruMap.put(key, key);
        if (eldestKey != null) {
            dbTableAllSqlSourceMap.remove(eldestKey);
            dbTableCountSqlSourceMap.remove(eldestKey);
            dbTablePageSqlSourceMap.remove(eldestKey);
            eldestKey = null;
        }
    }

    public static synchronized SqlSourceComm getSqlSourceComm(Configuration configuration) {
        if (sqlSourceComm == null)
            sqlSourceComm = new SqlSourceComm(configuration);
        return sqlSourceComm;
    }

    private void initDbTable() {
       // dbTableNameMap.put(Role.class, "role");
    }

    private void initExclude() {
        excludefields.add("id");
        excludefields.add("serialVersionUID");
        initClassExclude();
    }

    private void initClassExclude() {

    }

    private String getTableName(Class clazz) {
        if (dbTableNameMap.containsKey(clazz)) {
            String name = dbTableNameMap.get(clazz);
            if (name != null && name.length() > 0) {
                return name;
            }
        }
        return null;
    }

    public SqlSource createSqlSouce(Class clazz, SelectType type) {
        SqlSource sqlSource;
        String tableName = getTableName(clazz);
        //注册表名
        if(tableName == null || tableName.length() == 0){
            String tname = initTableName(clazz);
            //注入表明
            dbTableNameMap.put(clazz,tname);
        }
        switch (type) {
            case PAGE:
                sqlSource = createPageSqlSouce(clazz,SELECT_All_NODE_STATIC);
                break;
            case COUNT:
                sqlSource = createCountSqlSouce(clazz,SELECT_COUNT_NODE_STATIC);
                break;
            case ALL:
                sqlSource = createAllSqlSouce(clazz,SELECT_All_NODE_STATIC);
                break;
            default:
                throw new IllegalArgumentException("无法找到对应的类型:" + type);
        }
        if (isLru) cycleKeyList(clazz);
        return sqlSource;
    }

    private SqlSource createCountSqlSouce(Class clazz, String static_sql) {
        if (dbTableCountSqlSourceMap.containsKey(clazz)) {
            return dbTableCountSqlSourceMap.get(clazz);
        }
        String tableName = getTableName(clazz);
        if (tableName == null || tableName.length() == 0)
            throw new NoSuchTableNameException("无法找到对应的表名!");
        SqlSource sqlSource = new DynamicSqlSource(configuration, createRootSqlNode(tableName, createWhereNode(createFieldSqlNodes(clazz)), SELECT_COUNT_NODE_STATIC, false));
        dbTableCountSqlSourceMap.put(clazz, sqlSource);
        return sqlSource;
    }

    private SqlSource createPageSqlSouce(Class clazz, String static_sql) {
        if (dbTablePageSqlSourceMap.containsKey(clazz)) {
            return dbTablePageSqlSourceMap.get(clazz);
        }
        String tableName = getTableName(clazz);
        if (tableName == null || tableName.length() == 0)
            throw new NoSuchTableNameException("无法找到对应的表名!");
        SqlSource sqlSource = new DynamicSqlSource(configuration, createRootSqlNode(tableName, createWhereNode(createFieldSqlNodes(clazz)), SELECT_All_NODE_STATIC, true));
        dbTablePageSqlSourceMap.put(clazz, sqlSource);
        return sqlSource;
    }

    private SqlSource createAllSqlSouce(Class clazz, String static_sql) {
        if (dbTableAllSqlSourceMap.containsKey(clazz)) {
            return dbTableAllSqlSourceMap.get(clazz);
        }
        String tableName = getTableName(clazz);
        if (tableName == null || tableName.length() == 0)
            throw new NoSuchTableNameException("无法找到对应的表名!");
        SqlSource sqlSource = new DynamicSqlSource(configuration, createRootSqlNode(tableName, createWhereNode(createFieldSqlNodes(clazz)), SELECT_All_NODE_STATIC, false));
        dbTableAllSqlSourceMap.put(clazz, sqlSource);
        return sqlSource;
    }

    private List<String> getBeanField(Class clazz) {
        return getDeclaredFields(clazz);
    }

    private List<String> getDeclaredFields(Class clazz) {
        List<String> fds = beanFieldsMap.get(clazz);
        if (fds != null && fds.size() > 0) {
            return fds;
        }
        List<String> list = new ArrayList<String>();
        Field[] fields = clazz.getDeclaredFields();
        List<String> clazz_excludefields = class_excludefieldsMap.get(clazz);
        for (Field f : fields) {
            String name = f.getName();
            if (!excludefields.contains(name))
                list.add(f.getName());
        }
        if (clazz_excludefields != null && clazz_excludefields.size() > 0)
            list.removeAll(clazz_excludefields);
        if (list != null && list.size() > 0) {
            beanFieldsMap.put(clazz, list);
            return list;
        }
        return null;
    }

    private List<SqlNode> createFieldSqlNodes(Class clazz) {
        List<String> fields = getBeanField(clazz);
        if (fields == null || fields.size() == 0) {
            throw new NoSuchBeanFieldException("无法找到对应字段!");
        }
        List<SqlNode> nodes = new ArrayList<>();
        for (String field : fields) {
            SqlNode sqlNode = dbTableFieldSqlNodeMap.get(clazz.getName() + field);
            if (sqlNode == null) {
                sqlNode = createFieldSqlNode(field);
                dbTableFieldSqlNodeMap.put(clazz.getName() + field, sqlNode);
            }
            nodes.add(sqlNode);
        }
        return nodes;
    }

    private SqlNode createFieldSqlNode(String field) {
        String if_node_sql = MessageFormat.format(IF_NODE_STATIC, field, field,"''");
        String text_node_sql = MessageFormat.format(STATIC_NODE, getTableFieldName(field), "#{param." + field + "}");
        SqlNode textSqlNode = createStaticTextSqlNode(text_node_sql);
        List<SqlNode> nodes = new ArrayList<>();
        nodes.add(textSqlNode);
        return createIfSqlNode(new MixedSqlNode(nodes), if_node_sql);
    }

    private SqlNode createStaticTextSqlNode(String sql) {
        StaticTextSqlNode textSqlNode = new StaticTextSqlNode(sql);
        return textSqlNode;
    }

    private SqlNode createIfSqlNode(SqlNode sqlNode, String if_sql) {
        IfSqlNode ifSqlNode = new IfSqlNode(sqlNode, if_sql);
        return ifSqlNode;
    }

    private SqlNode createRootSqlNode(String tableName, SqlNode whereSqlNode, String select_node, boolean isPage) {
        String tableSql = MessageFormat.format(select_node, tableName);
        SqlNode tableSqlNode = new StaticTextSqlNode(tableSql);
        List<SqlNode> nodes = new ArrayList<>();
        nodes.add(tableSqlNode);
        nodes.add(whereSqlNode);
        if (isPage) nodes.add(new StaticTextSqlNode(SELECT_LIMIT_NODE_STATIC));
        SqlNode rootSqlNode = new MixedSqlNode(nodes);
        return rootSqlNode;
    }

    private SqlNode createWhereNode(List<SqlNode> sqlNodes) {
        SqlNode textSqlNode = new StaticTextSqlNode(" 1=1 ");
        SqlNode ifSqlNode = new IfSqlNode(new MixedSqlNode(sqlNodes), "param != null");
        List<SqlNode> nodes = new ArrayList<>();
        nodes.add(textSqlNode);
        nodes.add(ifSqlNode);
        SqlNode whereSqlNode = new WhereSqlNode(configuration, new MixedSqlNode(nodes));
        return whereSqlNode;
    }

    private String getTableFieldName(String str) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            Character charAt = str.charAt(i);
            if (Character.toUpperCase(charAt) == charAt) {
                buffer.append("_" + charAt);
                continue;
            }
            buffer.append(charAt);
        }
        return buffer.toString();
    }

    private String initTableName(Class clazz) {
        StringBuffer buffer = new StringBuffer();
        String clazzName = clazz.getSimpleName();
        for (int i = 0; i < clazzName.length(); i++) {
            Character charAt = clazzName.charAt(i);
            if(i == 0){
                buffer.append(Character.toLowerCase(charAt));
                continue;
            }
            if (Character.toUpperCase(charAt) == charAt) {
                buffer.append("_" + Character.toLowerCase(charAt));
                continue;
            }
            buffer.append(charAt);
        }
        return buffer.toString();
    }
}
