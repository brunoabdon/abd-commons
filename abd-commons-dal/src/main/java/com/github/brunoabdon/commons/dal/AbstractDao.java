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
package com.github.brunoabdon.commons.dal;

import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.github.brunoabdon.commons.util.modelo.Identifiable;

/**
 *
 * @author Bruno Abdon
 * @param <E> o tipo da entidade persistida
 * @param <K> o tipo da chave da entidade
 */
public abstract class AbstractDao<E extends Identifiable<K>,K> 
        implements Dao<E,K>{

    private static final Logger LOG = 
        Logger.getLogger(AbstractDao.class.getName());
    
    @PersistenceContext
    private EntityManager em;
    
    private final Class<E> klass;

    public AbstractDao(final Class<E> klass) {
        this.klass = klass;
    }

    @Override
    public E find(final K key) throws DalException {
        final E entity = em.find(klass, key);
        if(entity == null){
            throw new EntityNotFoundException(key);
        }
        return entity;
    }

    @Override
    public void criar(final E entity) throws DalException {
        LOG.finest(() -> "Criando " + entity);
        validarPraCriacao(entity);
        em.persist(entity);
    }

    @Override
    public E atualizar(final K key, final E entity) throws DalException {
        
        validarPraAtualizacao(key,entity);
        
        final E persistedEntity = find(key);
        
        atualizarEntity(entity,persistedEntity);
        
        return persistedEntity;
    }

    protected void atualizarEntity(final E source, final E dest) {
        throw new UnsupportedOperationException("Atualzação não suportada.");
    };

    @Override
    public void deletar(final K key) throws DalException {
        LOG.finest(() -> "Deletando id " + key);
        final E entity = find(key);
        prepararDelecao(entity);
        em.remove(entity);
    }

    protected void validarPraCriacao(final E entity) throws DalException{
        validar(entity);
    }

    protected void validarPraAtualizacao(final K k, final E entity) 
            throws DalException{
        validar(entity);
    }

    protected void validar(final E entity) throws DalException {
    };

    protected void prepararDelecao(final E entity) throws DalException {
    };
    
    public EntityManager getEntityManager() {
        return this.em;
    }
    
}