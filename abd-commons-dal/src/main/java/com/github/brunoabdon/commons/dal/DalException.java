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

import java.io.Serializable;

/**
 * Exceção na camada de acesso a dados.
 * 
 * @author Bruno Abdon
 */
public class DalException extends Exception {

    private static final long serialVersionUID = 5128378150540858893L;
	private Serializable[] params;

    public DalException(final String message) {
        super(message);
    }

    public DalException(final String message, final Serializable ... params) {
        this(message);
        this.params = params;
    }

    public DalException(
            final Throwable cause,
            final String message, 
            final Serializable ... params) {
        super(message,cause);
        this.params = params;
    }
    
    public Object[] getParams() {
        return params;
    }
}