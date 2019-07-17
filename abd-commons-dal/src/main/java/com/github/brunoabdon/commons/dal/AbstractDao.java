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

import com.github.brunoabdon.commons.modelo.Entidade;

import javax.persistence.EntityManager;

/**
 *
 * @author Bruno Abdon
 * @param <E> o tipo da entidade persistida
 * @param <K> o tipo da chave da entidade
 */
public abstract class AbstractDao<E extends Entidade<K>, K> implements Dao<E,K>{

    private static final Logger LOG = 
        Logger.getLogger(AbstractDao.class.getName());
    
    private final Class<E> klass;

    public AbstractDao(final Class<E> klass) {
        this.klass = klass;
    }

    @Override
    public E find(final EntityManager em, final K key) throws DalException{
        E entity = em.find(klass, key);
        if(entity == null){
            throw new EntityNotFoundException(key);
        }
        return entity;
    }

    @Override
    public void criar(final EntityManager em, final E entity) 
            throws DalException {
        LOG.finest(() -> "Criando " + entity);
        validarPraCriacao(em,entity);
        em.persist(entity);
    }

    @Override
    public E atualizar(final EntityManager em, final E entity) 
            throws DalException {
        LOG.finest(() -> "Atualizando " + entity);
        validarPraAtualizacao(em,entity);
        find(em, entity.getId());
        return em.merge(entity);
    }

    @Override
    public void deletar(final EntityManager em, final K key) 
            throws DalException {
        LOG.finest(() -> "Deletando id " + key);
        final E entity = find(em, key);
        prepararDelecao(em,entity);
        em.remove(entity);
    }

    protected void validarPraCriacao(final EntityManager em, final E entity) 
            throws DalException{
        validar(em,entity);
    }

    protected void validarPraAtualizacao(final EntityManager em, final E entity) 
            throws DalException{
        validar(em,entity);
    }

    protected void validar(final EntityManager em, final E entity) 
            throws DalException {
    };

    protected void prepararDelecao(final EntityManager em, final E entity) 
            throws DalException {
    };
}