package com.sky.aspectj;


import com.sky.annotation.Autofill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutofillAspect {

    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.Autofill) ")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoBefore(JoinPoint joinPoint){
//        获取数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Autofill autofill = signature.getMethod().getAnnotation(Autofill.class);
        OperationType value = autofill.value();
//        获取需要赋值的对象
        Object[] args = joinPoint.getArgs();
        Object arg = args[0];
//        获取赋值的值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
//        赋值
        if (value == OperationType.INSERT) {
            try {
                Method setcreateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
                Method setupdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                Method setcreateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setupdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);

                setupdateUser.invoke(arg,currentId);
                setcreateUser.invoke(arg,currentId);
                setcreateTime.invoke(arg,now);
                setupdateTime.invoke(arg,now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }else if (value == OperationType.UPDATE) {
            try {
                Method setupdateUser = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
                Method setupdateTime = arg.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);

                setupdateUser.invoke(arg,currentId);
                setupdateTime.invoke(arg,now);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
