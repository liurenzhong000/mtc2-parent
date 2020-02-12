package io.mtc.facade.user.constants;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 16:57
 * @Description:
 */
@Converter
public class PlatformTransferTypeConverter implements AttributeConverter<PlatformTransferType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PlatformTransferType attribute) {
        return attribute.getKey();
    }

    @Override
    public PlatformTransferType convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (PlatformTransferType temp : PlatformTransferType.values()) {
            if (temp.getKey().equals(dbData)) {
                return temp;
            }
        }
        return null;
    }
}
