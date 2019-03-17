package io.mtc.facade.bitcoin.data.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @Auther: hyp
 * @Date: 2019/3/16 10:15
 * @Description:
 */
@Converter
public class RechargeStatusConverter implements AttributeConverter<RechargeStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RechargeStatus attribute) {
        return attribute.getKey();
    }

    @Override
    public RechargeStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (RechargeStatus temp : RechargeStatus.values()) {
            if (temp.getKey().equals(dbData)) {
                return temp;
            }
        }
        return null;
    }
}