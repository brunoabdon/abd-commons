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

import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Bruno Abdon
 */
public class Auth {

    private static final Auth INSTANCE = new Auth();
    
    private final Collection<String> currentAuthorizations;
    
    private Auth(){    
        this.currentAuthorizations =
            !Boolean.parseBoolean(System.getenv("ABD_AUTH_OMNI_EST_LICET"))
                ? new HashSet<>()
                : new HashSet<String>() {
                		private static final long serialVersionUID = -2604L;

						@Override
                        public boolean add(final String e) {return false;}

                        @Override
                        public boolean remove(final Object o) {return false;}

                        @Override
                        public boolean contains(final Object o) {return true;}
                    };
    }

    public static Auth getInstance(){
        return INSTANCE;
    }

    public String login(final String password) throws GeneralSecurityException{

        final String md5pass = DigestUtils.md5Hex(password);
  
        if(!"cbb72318565824672fdb2b7b4c84df20".equals(md5pass)){
            throw new GeneralSecurityException();
        }

        final String authToken = UUID.randomUUID().toString();
        currentAuthorizations.add(authToken);
        return authToken;
    }

    public boolean isValid(final String authToken){
        return currentAuthorizations.contains(authToken);
    }

    public void logout(final String authToken){
        currentAuthorizations.remove(authToken);
    }
}