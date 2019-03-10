package io.mtc.facade.user.constants;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * @Auther: hyp
 * @Date: 2019/3/8 16:57
 * @Description:
 */
@Converter
public class PlatformTransferStatusConverter implements AttributeConverter<PlatformTransferStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PlatformTransferStatus attribute) {
        return attribute.getKey();
    }

    @Override
    public PlatformTransferStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }
        for (PlatformTransferStatus temp : PlatformTransferStatus.values()) {
            if (temp.getKey().equals(dbData)) {
                return temp;
            }
        }
        return null;
    }
}