/*
 * Copyright (C) 2016 Bruno Abdon
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
package br.nom.abdon.rest;

import java.util.logging.Logger;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author Bruno Abdon
 */
public class RestServiceUtils {

    private static final Logger log = 
        Logger.getLogger(RestServiceUtils.class.getName());

    public static Response buildResponse(
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

    public static EntityTag makeTag(
            final Object thing, final HttpHeaders httpHeaders) {
        
        final String accept = httpHeaders.getHeaderString(HttpHeaders.ACCEPT);

        final int hashCode = 
            new HashCodeBuilder(3,23)
                .append(thing)
                .append(accept)
                .toHashCode();
        
        return new EntityTag(Integer.toString(hashCode));

    }
}
