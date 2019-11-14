package com.github.brunoabdon.commons.dal.util;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.Converter;

/**
 *
 * @author bruno
 */

@Converter(autoApply = true)
public class LocalDatePersistenceConverter 
        implements NullSafeAttributeConverter<LocalDate,Date> {
    
    @Override
    public LocalDate nullSafeConvertToEntityAttribute(final Date dbData) {
        return dbData.toLocalDate();    
    }

    @Override
    public Date nullSafeConvertToDatabaseColumn(final LocalDate attribute) {
        return java.sql.Date.valueOf(attribute);
    }
}