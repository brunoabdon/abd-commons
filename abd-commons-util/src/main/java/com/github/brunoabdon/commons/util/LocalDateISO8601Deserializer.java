package com.github.brunoabdon.commons.util;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import java.time.format.DateTimeFormatter;

/**
 * {@link LocalDateDeserializer} usando o formato <a 
 * href="https://www.iso.org/iso-8601-date-and-time-format.html">ISO 8601</a>.
 * 
 * @author bruno abdon
 */
public class LocalDateISO8601Deserializer extends LocalDateDeserializer{
    
	private static final long serialVersionUID = 5003325769687069408L;

	public LocalDateISO8601Deserializer(){
        super(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
