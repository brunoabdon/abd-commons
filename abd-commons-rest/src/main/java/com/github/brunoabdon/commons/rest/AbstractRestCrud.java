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

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.commons.dal.Dao;
import com.github.brunoabdon.commons.dal.EntityNotFoundException;
import com.github.brunoabdon.commons.modelo.Entidade;

/**
 * Classe base para resources implementando operações básicas de CRUD.
 * 
 * @param <E> O tipo da  entidade criada, lida, atualizada e deletada.
 * @param <Key> O tipo da chave do elemento.
 *
 * @author Bruno Abdon
 */
public abstract class AbstractRestCrud <E extends Entidade<Key>,Key>{

    private static final Logger log = 
        Logger.getLogger(AbstractRestCrud.class.getName());

    private static final SecureRandom random = new SecureRandom();
    
    protected static final Response ERROR_MISSING_ENTITY = 
        Response.status(BAD_REQUEST)
                .entity("com.github.brunoabdon.commons.rest.MISSING_ENTITY")
                .build();

    @PersistenceContext
    protected EntityManager entityManager;
    
    private final String path;

    public AbstractRestCrud(final String path) {
        this.path = path + "/" ;
    }

    @POST
    @Transactional
    public Response criar(final E entity) {

        Response response;

        if(entity == null){
            response = ERROR_MISSING_ENTITY;
        } else {

            try {


                final Dao<E, Key> dao = getDao();

                dao.criar(entityManager, entity);

                final URI uri = new URI(path + String.valueOf(entity.getId()));

                response = 
                    Response.created(uri).entity(entity).build();

            } catch (final URISyntaxException ex) {

                final String errorCode = makeError();

                log.log(Level.SEVERE, ex, () -> "[" + errorCode + "]");
                response = 
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("Erro: " + errorCode)
                            .build();

            } catch (final DalException e){
                log.log(Level.FINE, "Erro ao tentar criar.", e);
                response = 
                    Response.status(Response.Status.CONFLICT)
                            .entity(e.getMessage())
                            .build();
            }
        }
        return response;
    }

    @GET
    @Path("{id}")
    public Response pegar(
            final @Context Request request, 
            final @PathParam("id") Key id,
            final @Context HttpHeaders httpHeaders){

        final Response response;

        try {
            final E entity = getEntity(entityManager, id);

            EntityTag tag =  makeTag(entity, httpHeaders);
            Response.ResponseBuilder builder = request.evaluatePreconditions(tag);
            if(builder==null){
		//preconditions are not met and the cache is invalid
		//need to send new value with reponse code 200 (OK)
		builder = Response.ok(entity);
		builder.tag(tag);
            }
            response = builder.build();

        } catch (final EntityNotFoundException ex){
            log.log(Level.FINE , ex, () -> "Not found " + id);
            throw new NotFoundException(ex);
        } catch (final DalException ex) {
            log.log(Level.FINE, "Erro ao tentar pegar.", ex);
            throw new WebApplicationException(ex.getMessage(),BAD_REQUEST);
        }
        return response;
    }

    protected E getEntity(
            final EntityManager entityManager, 
            final Key id) throws DalException {
        return getDao().find(entityManager, id);
    }

    @POST
    @Path("{id}")
    @Transactional
    public Response atualizar(final @PathParam("id") Key id, E entity) {

        Response response;

        if(entity == null){
            response = ERROR_MISSING_ENTITY;

        } else {

            try {

                entity = prepararAtualizacao(entityManager, entity, id);

                entity = getDao().atualizar(entityManager, entity);

                response = Response.ok(entity).build();

            } catch ( EntityNotFoundException ex){
                throw new NotFoundException(ex);
            } catch (DalException e) {
                log.log(Level.FINE, "Erro ao tentar atualizar.", e);
                response =
                    Response.status(Response.Status.CONFLICT)
                            .entity(e.getMessage())
                            .build();
            } finally {
                entityManager.close();
            }
        }
        return response;
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deletar(@PathParam("id") Key id) {

        Response response;

        try {
            getDao().deletar(entityManager, id);
            
            response = Response.noContent().build();
            
        } catch(EntityNotFoundException ex){
            throw new NotFoundException(ex);
        } catch (DalException e) {
            log.log(Level.FINE, "Erro ao tentar deletar.", e);
            response =
                Response.status(Response.Status.CONFLICT)
                        .entity(e.getMessage())
                        .build();
        }
        
        return response;
    }
    
    //nao precisava ser no 'crud' esse metodo.
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

    protected E prepararAtualizacao(
            final EntityManager entityManager, 
            final E entity, 
            final Key id) {
        entity.setId(id);
        return entity;
    }

    private String makeError() {
        return new BigInteger(130, random).toString(32);
    }
}