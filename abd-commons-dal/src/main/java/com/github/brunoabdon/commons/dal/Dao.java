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

import javax.persistence.EntityManager;

import com.github.brunoabdon.commons.util.modelo.Identifiable;

/**
 *
 * @author Bruno Abdon
 * @param <E> o tipo da entidade persistida
 * @param <K> o tipo da chave da entidade
 */
public interface Dao<E extends Identifiable<K>,K> {

    public E find(final EntityManager em, final K key) throws DalException;

    public void criar(final EntityManager em, final E entity)
            throws DalException;

    public E atualizar(final EntityManager em, final E entity) 
            throws DalException;

    public void deletar(final EntityManager em, final K key) 
            throws DalException;
}
