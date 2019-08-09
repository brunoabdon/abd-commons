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

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.github.brunoabdon.commons.dal.DalException;
import com.github.brunoabdon.commons.dal.Dao;
import com.github.brunoabdon.commons.dal.EntityNotFoundException;
import com.github.brunoabdon.commons.util.modelo.Identifiable;

/**
 * Classe base para resources implementando operações básicas de CRUD.
 * 
 * @param <E> O tipo da  entidade criada, lida, atualizada e deletada.
 * @param <Key> O tipo da chave do elemento.
 *
 * @author Bruno Abdon
 */
public abstract class AbstractRestCrud<E extends Identifiable<Key>,Key>
        extends AbstractRestReadOnlyResource<E, Key>{

    private static final Logger log = 
        Logger.getLogger(AbstractRestCrud.class.getName());

    protected static final Response ERROR_MISSING_ENTITY = 
        Response.status(BAD_REQUEST)
                .entity("com.github.brunoabdon.commons.rest.MISSING_ENTITY")
                .build();

    @Context
    private UriInfo uriInfo;

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

                final URI uri = 
            		uriInfo
            			.getAbsolutePathBuilder()
            			.path(String.valueOf(entity.getId()))
            			.build(); 
                		
                response = 
                    Response.created(uri).entity(entity).build();

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

    @POST
    @Path("{id}")
    @Transactional
    public Response atualizar(final @PathParam("id") Key id, final E entity) {

        Response response;

        if(entity == null){
            response = ERROR_MISSING_ENTITY;

        } else {

            try {
            
                final E persistedEntity = 
                    this.getDao().atualizar(entityManager, id, entity);

                response = Response.ok(persistedEntity).build();

            } catch (final EntityNotFoundException ex){
                throw new NotFoundException(ex);
            } catch (final DalException e) {
                log.log(Level.FINE, "Erro ao tentar atualizar.", e);
                response =
                    Response.status(Response.Status.CONFLICT)
                            .entity(e.getMessage())
                            .build();
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
            
        } catch(final EntityNotFoundException ex){
            throw new NotFoundException(ex);
        } catch (final DalException e) {
            log.log(Level.FINE, "Erro ao tentar deletar.", e);
            response =
                Response.status(Response.Status.CONFLICT)
                        .entity(e.getMessage())
                        .build();
        }
        
        return response;
    }
    
}