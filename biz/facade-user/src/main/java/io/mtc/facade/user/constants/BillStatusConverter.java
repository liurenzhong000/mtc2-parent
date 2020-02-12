package io.mtc.facade.user.constants;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * jpa枚举转换
 *
 * @author Chinhin
 * 2018/7/27
 */
@Converter
public class BillStatusConverter implements AttributeConverter<BillStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(BillStatus attribute) {
        return attribute.getKey();
    }

    @Override
    public BillStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (BillStatus temp : BillStatus.values()) {
            if (temp.getKey().equals(dbData)) {
                return temp;
            }
        }
        return null;
    }
}
