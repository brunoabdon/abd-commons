/*
 * Copyright (C) 2019 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.brunoabdon.commons.rest.providers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

/**
 * Classe base pra {@link MessageBodyReader}s de {@link Collection}s.
 *
 * @param <E> o tipo da entidade da colecao.
 * @author Bruno Abdon
 */
public abstract class CollectionMessageBodyReader<E> 
        implements MessageBodyReader<Collection<E>>{

    private final Supplier<Collection<E>> collectionSupplier;
    private final Class<E> elementType;
    private final JsonFactory jsonFactory;

    public CollectionMessageBodyReader(
            final Class<E> elementType,
            final Supplier<Collection<E>> collectionSupplier,
            final JsonFactory jsonFactory){
        this.collectionSupplier =  collectionSupplier;
        this.elementType = elementType;
        this.jsonFactory = jsonFactory;
    }
    
    public CollectionMessageBodyReader(
            final Class<E> elementType,
            final JsonFactory jsonFactory){
        this(elementType,ArrayList::new,jsonFactory);
    }
    
    @Override
    public boolean isReadable(
            final Class<?> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType) {

        final ParameterizedType pt = (ParameterizedType)genericType;
        final Type t = pt.getActualTypeArguments()[0];
        final Class<?> entityClass = 
            (t instanceof Class) 
                ? (Class<?>)t 
                : (Class<?>)((ParameterizedType)t).getRawType();
        return 
            Collection.class.isAssignableFrom(type)
            && elementType.isAssignableFrom(entityClass);
    }

    @Override
    public Collection<E> readFrom(
            final Class<Collection<E>> type, 
            final Type genericType, 
            final Annotation[] annotations, 
            final MediaType mediaType, 
            final MultivaluedMap<String, String> httpHeaders, 
            final InputStream entityStream) 
                throws IOException, WebApplicationException {

        final Collection<E> entities = collectionSupplier.get();
        
        try (final JsonParser jParser = jsonFactory.createParser(entityStream)){
        
            jParser.nextToken(); //START_ARRAY

            boolean leu;
            do {
                final E entity = 
                    tryToReadEntity(
                        annotations, 
                        mediaType, 
                        httpHeaders, 
                        jParser);

                if(leu = (entity != null)){
                    entities.add(entity);
                }
            } while(leu);

        }
        return entities;
    }

    /**
     * Tries to read an entity of type E from the jParser. It may be the case
     * that the last entity has already been read (or there were no entities to
     * begin with). The method must return <code>null</code> in such cases.
     * 
     * @param annotations from {@link MessageBodyReader#readFrom}.
     * @param mediaType from {@link MessageBodyReader#readFrom}.
     * @param httpHeaders from {@link MessageBodyReader#readFrom}.
     * @param jParser from {@link MessageBodyReader#readFrom}.
     * 
     * @return An entity read from the parser or <code>null</code> if an 'close 
     * array' token is immediately stumbled upon.
     * 
     * @throws IOException if some IO situation emerges. 
     */
    protected abstract E tryToReadEntity(
        final Annotation[] annotations, 
        final MediaType mediaType, 
        final MultivaluedMap<String, String> httpHeaders, 
        final JsonParser jParser) throws IOException;
    
}