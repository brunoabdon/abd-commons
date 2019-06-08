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
package br.nom.abdon.dal;

/**
 * Exceção que indica que uma busca por chave não encontrou o elemento 
 * procurado.
 * 
 * @author Bruno Abdon
 */
public class EntityNotFoundException extends DalException{

    private static final long serialVersionUID = 2789561489362885191L;
    
	private static final String ERRO_NOT_FOUND = "br.nom.abdon.dal.NOT_FOUND";
    
    public EntityNotFoundException(final Object ... params) {
        super(ERRO_NOT_FOUND,params);
    }
    
    
}
