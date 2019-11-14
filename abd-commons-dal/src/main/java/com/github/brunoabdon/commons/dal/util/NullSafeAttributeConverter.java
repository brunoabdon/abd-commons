package com.github.brunoabdon.commons.dal.util;

import javax.persistence.AttributeConverter;

public interface NullSafeAttributeConverter<X, Y> 
        extends AttributeConverter<X, Y> {

    @Override
    public default Y convertToDatabaseColumn(final X attribute) {
        return 
            attribute != null 
                ? nullSafeConvertToDatabaseColumn(attribute) 
                : null;
    }

    @Override
    public default X convertToEntityAttribute(Y dbData) {
        return 
            dbData != null
                ? nullSafeConvertToEntityAttribute(dbData)
                : null; 
    }

    public abstract X nullSafeConvertToEntityAttribute(Y dbData);

    public abstract Y nullSafeConvertToDatabaseColumn(X attribute);
}
