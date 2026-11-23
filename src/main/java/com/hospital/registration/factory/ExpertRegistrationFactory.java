package com.hospital.registration.factory;

import com.hospital.registration.Registration;
import com.hospital.schedule.ScheduleMapper;
import org.springframework.stereotype.Component;

/** 具体工厂：生产专家门诊挂号单 */
@Component
public class ExpertRegistrationFactory extends RegistrationFactory {

    private final ExpertRegistration product;

    public ExpertRegistrationFactory(ScheduleMapper scheduleMapper,
                                     ExpertRegistration product) {
        super(scheduleMapper);
        this.product = product;
    }

    @Override
    protected RegistrationProduct createProduct() {
        return product;
    }

    @Override
    public String regType() {
        return Registration.TYPE_EXPERT;
    }

    @Override
    public String describe() {
        return product.describe();
    }
}
