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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
public abstract class AbstractRestCrud<E extends Identifiable<Key>,Key,PathKey>
        extends AbstractRestReadOnlyResource<E, Key, PathKey>{

    private static final Logger log = 
        Logger.getLogger(AbstractRestCrud.class.getName());

    protected static final Response ERROR_MISSING_ENTITY = 
        Response.status(BAD_REQUEST)
                .entity("com.github.brunoabdon.commons.rest.MISSING_ENTITY")
                .build();
    
    @PUT
    @Path("{id}")
    @Transactional
    public Response criarOuAtualizar(
    		final @PathParam("id") PathKey pathId,
    		final E entity) {
    	
    	Response response;
    	
    	final Key id = getFullId(pathId);
    	this.defineChave(entity,id);
    	
    	try {
			getDao().find(id);
			response = atualizar_(id,entity);
			
		} catch (final EntityNotFoundException e) {
			response = this.criar(entity);
		} catch (final DalException e) {
			response = dealWith(e);
		}
    	
    	return response;    	
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

                dao.criar(entity);

                final URI uri = 
            		uriInfo
            			.getAbsolutePathBuilder()
            			.path(String.valueOf(entity.getId()))
            			.build(); 
                		
                response = 
                    Response.created(uri).entity(entity).build();

            } catch (final DalException e){
                response = dealWith(e);
            }
        }
        return response;
    }

    @POST
    @Path("{id}")
    @Transactional
    public Response atualizar(
            final @PathParam("id") PathKey pathId, 
            final E entity) {

        Response response;

        if(entity == null){
            response = ERROR_MISSING_ENTITY;

        } else {

            final Key id = getFullId(pathId);
            
            response = atualizar_(id, entity);
        }
        return response;
    }

	private Response atualizar_(final Key id, final E entity) {

		Response response;
		
		try {
		
		    final E persistedEntity = 
		        this.getDao().atualizar(id, entity);

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
		return response;
	}

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deletar(@PathParam("id") final PathKey pathId) {

        Response response;

        final Key id = getFullId(pathId);
        
        try {
            getDao().deletar(id);
            
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
    
    protected void defineChave(final E entity, final Key id) {
    	log.warning("Pra criar por PUT, é bom setar a chave.");
    };
    
	protected Response dealWith(final DalException e) {
		Response response;
		log.log(Level.FINE, "Erro ao tentar criar.", e);
		response = 
		    Response.status(Response.Status.CONFLICT)
		            .entity(e.getMessage())
		            .build();
		return response;
	}


    
}