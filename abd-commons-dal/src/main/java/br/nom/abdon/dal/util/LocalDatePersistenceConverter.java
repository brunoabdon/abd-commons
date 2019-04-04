package br.nom.abdon.dal.util;

import java.sql.Date;
import java.time.LocalDate;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 *
 * @author bruno
 */

@Converter(autoApply = true)
public class LocalDatePersistenceConverter implements
    AttributeConverter<LocalDate,Date> {
    
    @Override
    public java.sql.Date convertToDatabaseColumn(final LocalDate entityValue) {
        return java.sql.Date.valueOf(entityValue);
    }

    @Override
    public LocalDate convertToEntityAttribute(final Date databaseValue) {
        return databaseValue.toLocalDate();
    }
}