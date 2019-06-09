package com.github.brunoabdon.commons.util;

import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

/**
 * {@link LocalDateSerializer} usando o formato <a 
 * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a>.
 * 
 * @author bruno abdon
 */
public class LocalDateISO8601Serializer extends LocalDateSerializer{
    
    private static final long serialVersionUID = 2850149492721026484L;
    
	public static final LocalDateISO8601Serializer INSTANCE = 
        new LocalDateISO8601Serializer();
    
    public LocalDateISO8601Serializer(){
        super(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
