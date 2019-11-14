/*
 * Copyright (C) 2015 Bruno Abdon
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
package com.github.brunoabdon.commons.rest;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.commons.dal.Dao;
import com.github.brunoabdon.commons.dal.EntityNotFoundException;
import com.github.brunoabdon.commons.util.modelo.Identifiable;

/**
 * Classe base para resources implementando operações de leitura de CRUD. 
 * <p><b>Obs:</b> Operação <i>listar</i> ainda não implementada.</p>
 * 
 * @param <E> O tipo da  entidade criada, lida.
 * @param <Key> O tipo da chave do elemento.
 *
 * @author Bruno Abdon
 */
public abstract class AbstractRestReadOnlyResource 
                        <E extends Identifiable<Key>, Key, PathKey> {

	private static final Logger log = 
        Logger.getLogger(AbstractRestReadOnlyResource.class.getName());

    @Context
    protected UriInfo uriInfo;
	
    @GET
    @Path("{id}")
    public Response pegar(
            final @Context Request request, 
            final @PathParam("id") PathKey pathId,
            final @Context HttpHeaders httpHeaders){

        final Response response;

        final Key id = getFullId(pathId);
        
        try {
            
            final E entity = getEntity(id);

            EntityTag tag =  makeTag(entity, httpHeaders);
            Response.ResponseBuilder builder = 
                request.evaluatePreconditions(tag);
            if(builder==null){
		//preconditions are not met and the cache is invalid
		//need to send new value with reponse code 200 (OK)
		builder = Response.ok(entity);
		builder.tag(tag);
            }
            response = builder.build();

        } catch (final EntityNotFoundException ex){
            log.log(Level.FINE, () -> "Not found " + id);
            throw new NotFoundException(ex);
        } catch (final DalException ex) {
            log.log(Level.FINE, "Erro ao tentar pegar.");
            throw new WebApplicationException(ex.getMessage(),BAD_REQUEST);
        }
        return response;
    }

    protected abstract Key getFullId(final PathKey pathId);

    protected E getEntity(final Key id) throws DalException {
        return getDao().find(id);
    }

    //precisava ser no 'crud' esse metodo
    protected Response buildResponse(
            final Request request, 
            final HttpHeaders headers,
            final List<? extends E> elements){

        final GenericEntity<List<? extends E>> genericEntity = 
            new GenericEntity<List<? extends E>>(elements){};
        
        return this.buildResponse(request, headers, genericEntity);
    }    

    //nao precisava ser no 'crud' esse metodo.
    private Response buildResponse(
            final Request request, 
            final HttpHeaders headers,
            final Object entity){

        final EntityTag tag = makeTag(entity, headers);

        Response.ResponseBuilder builder = request.evaluatePreconditions(tag);
        
        if(builder==null){
            //preconditions are not met and the cache is invalid
            //need to send new value with reponse code 200 (OK)
            
            builder = Response.ok(entity);
            builder.tag(tag);
        }
        return builder.build();
    }

    private EntityTag makeTag(
            final Object thing, final HttpHeaders httpHeaders) {

        final String accept = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);

        final int hashCode = 
            new HashCodeBuilder(3,23)
                .append(thing)
                .append(accept)
                .toHashCode();
        
        return new EntityTag(Integer.toString(hashCode));
    }

    protected abstract Dao<E,Key> getDao();
}