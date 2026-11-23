package com.hospital.registration.factory;

import com.hospital.registration.Registration;
import com.hospital.schedule.ScheduleMapper;
import org.springframework.stereotype.Component;

/** 具体工厂：生产普通门诊挂号单 */
@Component
public class NormalRegistrationFactory extends RegistrationFactory {

    private final NormalRegistration product;

    public NormalRegistrationFactory(ScheduleMapper scheduleMapper,
                                     NormalRegistration product) {
        super(scheduleMapper);
        this.product = product;
    }

    @Override
    protected RegistrationProduct createProduct() {
        return product;
    }

    @Override
    public String regType() {
        return Registration.TYPE_NORMAL;
    }

    @Override
    public String describe() {
        return product.describe();
    }
}
