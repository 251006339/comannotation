package com.write.bmybatis.orm.aop;

import com.write.bmybatis.orm.annotaton.BrianInsert;
import com.write.bmybatis.orm.annotaton.BrianParam;
import com.write.bmybatis.orm.annotaton.BrianSelect;
import com.write.bmybatis.utils.JDBCUtils;
import com.write.bmybatis.utils.SQLUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用反射到动态代理技术拦截接口
 */
public class BrianInvocationHandler implements InvocationHandler {

    private  Object object;

    public BrianInvocationHandler(Object object) {
        this.object = object;
    }

    /**
     *
     * @param proxy  代理对象  proxy
     * @param method 拦截的方法   method
     * @param args   方法上的参数
     * @return  Throwable
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Object result=null;

        //insert
        result = doInsert(method, args);

        //select
        result = doQuery(method, args);

        return result;
    }

          //方法是通过接口获得的方法;
    private int doInsert(Method method, Object[] args){
        //1.判断方法上是否有@BrianInsert注解
        BrianInsert brianInsert = method.getDeclaredAnnotation(BrianInsert.class);
        if(brianInsert != null){
            //2.获取该注解的上的insert语句
            String strSql = brianInsert.value();
            //3.获取参数方法上的参数和sql里面的参数匹配
            //定义一个Map集合 key为@BrianInsert的参数，value为方法参数
            //获取方法上的参数 name-->注解的值 value-->class类型
            ConcurrentHashMap<Object,Object> paramsMap = mappingData(method, args);
             //创建集合
            List<Object> sqlParams = new ArrayList<>();

            String[] strings = SQLUtils.sqlInsertParameter(strSql);
            Arrays.stream(strings).forEach(paramName -> {
                sqlParams.add(paramsMap.get(paramName));
            });
            //4.替换参数为?
            String newSql = SQLUtils.parameQuestion(strSql, strings);
            System.out.println("-----sql-----: " + newSql);

            //5.调用JDBC底层代码执行语句
            return JDBCUtils.insert(newSql,false,sqlParams);
        }
        return 0;
    }
    private Object doQuery(Method method, Object[] args) throws Exception {
        //1.判断方法上是否有@BrianInsert注解
        BrianSelect select = method.getDeclaredAnnotation(BrianSelect.class);
        if (select != null) {
            //2.获取该注解的上的query语句
            String strSql = select.value();
            //3.获取参数方法上的参数和sql里面的参数匹配
            //定义一个Map集合 key为@BrianQuery的参数，value为方法参数
            //获取方法上的参数  id ---integer 对象
            ConcurrentHashMap<Object, Object> paramsMap = mappingData(method, args);
             //存储具体实例对象
            List<Object> sqlParams = new ArrayList<>();
             //获得 id,name,gender 存储到集合中;-具体类型
            List<String> strings = SQLUtils.sqlSelectParameter(strSql);

            strings.stream().forEach(paramName -> {
                sqlParams.add(paramsMap.get(paramName));
            });
            //4.#{ }替换参数为?
            String newSql = SQLUtils.parameQuestion(strSql, strings);
            System.out.println("-----sql-----: " + newSql);

            //5.调用JDBC底层代码执行语句   select * from t_json where id=?  sqlParams-[Integer int]
            ResultSet query = JDBCUtils.query(newSql, sqlParams);

            //6.获取返回类型  TJson
            Class<?> returnType = method.getReturnType();


            if(!query.next()) {
                return null;
            }

            //向前移动一位
            query.previous();
            Object resultObject = returnType.newInstance();

            while(query.next()){
                for (String param: strings) {
                    //获取集合中的数据   param=id  根据id 到对象ResultSet类查找信息,id-->1
                    Object value = query.getObject(param);
                    Field field = returnType.getDeclaredField(param);
                    field.setAccessible(true);
                    field.set(resultObject,value);
                }
            }

            return resultObject;
        }

        return null;

    }

    private ConcurrentHashMap<Object,Object> mappingData(Method method, Object[] args){
        ConcurrentHashMap<Object,Object> paramsMaps = new ConcurrentHashMap<>();
        Parameter[] parameters = method.getParameters(); //参数就是一个id-获得参数上的注解对象--获得值
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            BrianParam brianParam = parameter.getAnnotation(BrianParam.class);
            if(brianParam != null) {
                //参数名称
                String paramName = brianParam.value();
                Object paramValue = args[i]; //把参数上注解的值name和args对象存储到hashmap中;
                paramsMaps.put(paramName,paramValue);
            }
        }
        return paramsMaps;
    }
}
