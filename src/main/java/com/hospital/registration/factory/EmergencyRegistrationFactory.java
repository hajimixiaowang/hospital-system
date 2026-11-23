package com.hospital.registration.factory;

import com.hospital.registration.Registration;
import com.hospital.schedule.ScheduleMapper;
import org.springframework.stereotype.Component;

/** 具体工厂：生产急诊挂号单（急诊号将获得优先就诊排序） */
@Component
public class EmergencyRegistrationFactory extends RegistrationFactory {

    private final EmergencyRegistration product;

    public EmergencyRegistrationFactory(ScheduleMapper scheduleMapper,
                                        EmergencyRegistration product) {
        super(scheduleMapper);
        this.product = product;
    }

    @Override
    protected RegistrationProduct createProduct() {
        return product;
    }

    @Override
    public String regType() {
        return Registration.TYPE_EMERGENCY;
    }

    @Override
    public String describe() {
        return product.describe();
    }
}
