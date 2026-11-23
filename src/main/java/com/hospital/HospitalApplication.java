package com.hospital;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 医院预约挂号与就诊系统 启动类
 *
 * 小组成员：
 *   刘嘉钊 —— 挂号模块（策略模式 + 工厂方法模式）
 *   陈梓健 —— 就诊与通知模块（模板方法模式 + 观察者模式）
 *   冯冠丰 —— 号源与基础模块（单例模式 + 建造者模式）
 */
@SpringBootApplication
@MapperScan(basePackages = "com.hospital", annotationClass = Mapper.class)
@EnableScheduling
public class HospitalApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
        System.out.println("===============================================");
        System.out.println("  医院预约挂号与就诊系统启动成功");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("===============================================");
    }
}
