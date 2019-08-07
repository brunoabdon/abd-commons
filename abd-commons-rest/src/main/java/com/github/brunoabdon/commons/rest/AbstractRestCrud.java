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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

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
public abstract class AbstractRestCrud<E extends Entidade<Key>,Key>
        extends AbstractRestReadOnlyResource<E, Key>{

    private static final Logger log = 
        Logger.getLogger(AbstractRestCrud.class.getName());

    private static final SecureRandom random = new SecureRandom();
    
    protected static final Response ERROR_MISSING_ENTITY = 
        Response.status(BAD_REQUEST)
                .entity("com.github.brunoabdon.commons.rest.MISSING_ENTITY")
                .build();

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

    @POST
    @Path("{id}")
    @Transactional
    public Response atualizar(final @PathParam("id") Key id, E entity) {

        Response response;

        if(entity == null){
            response = ERROR_MISSING_ENTITY;

        } else {

            try {
                
                final E entidadeAtualizar = getDao().find(entityManager, id);
                this.prencheValoresAtualizacao(entidadeAtualizar,entity);
                entity = getDao().atualizar(entityManager, entidadeAtualizar);

                response = Response.ok(entity).build();

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
    
    @SuppressWarnings("unused")
    protected void prencheValoresAtualizacao(
            final E entityPraAtualizar, 
            final E entidadeValoresNovos) {
    }

    private String makeError() {
        return new BigInteger(130, random).toString(32);
    }
}