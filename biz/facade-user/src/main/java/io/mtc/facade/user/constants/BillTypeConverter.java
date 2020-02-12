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
public class BillTypeConverter implements AttributeConverter<BillType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(BillType attribute) {
        return attribute.getKey();
    }

    @Override
    public BillType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (BillType temp : BillType.values()) {
            if (temp.getKey().equals(dbData)) {
                return temp;
            }
        }
        return null;
    }
}
