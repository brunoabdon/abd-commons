//
//  ========================================================================
//  Copyright (c) 1995-2013 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package com.github.brunoabdon.commons.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;


public class CrossOriginFilter implements Filter {

    private static final Logger log = 
        Logger.getLogger(CrossOriginFilter.class.getName());
    
    // Request headers
    private static final String ORIGIN_HEADER = "Origin";
    private static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = 
        "Access-Control-Request-Method";
    private static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = 
        "Access-Control-Request-Headers";
    // Response headers
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = 
        "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = 
        "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = 
        "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_MAX_AGE_HEADER =
        "Access-Control-Max-Age";
    private static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER =
        "Access-Control-Allow-Credentials";
    
    // ENV CONFIG
    private static final String ABD_HTTP_ALLOWED_ORIGINS_ENVVAR = 
        "ABD_HTTP_ALLOWED_ORIGINS";
    
    // Implementation constants
    private static final String ANY_ORIGIN = "*";
    private static final List<String> SIMPLE_HTTP_METHODS = 
        Arrays.asList("GET", "POST", "HEAD");

    private static final Set<String> ALLOWED_METHODS = new HashSet<>(
        Arrays.asList("GET","POST","PUT","HEAD","OPTIONS","DELETE")
    );
            
    private static final Set<String> ALLOWED_HEADERS = new HashSet<>(
        Arrays.asList(
            "Accept",
            "Accept-Encoding",
            "Accept-Language",
            "Connection",
            "Content-Type",
            "Host",
            "Origin",
            "X-Abd-auth_token",
            "X-Requested-With")
    );

    private static final String PRE_FLIGHT_MAX_AGE = "1800";

    private static final String COMMIFIED_ALLOWED_METHODS = 
        StringUtils.join(ALLOWED_METHODS, ',');
    
    private static final String COMMIFIED_ALLOWED_HEADERS = 
        StringUtils.join(ALLOWED_HEADERS, ',');
    
    private boolean anyOriginAllowed;
    private final List<String> allowedOrigins = new ArrayList<>();

    @Override
    public void init(final FilterConfig config) throws ServletException
    {
        final String allowedOriginsConfig = 
            System.getenv(ABD_HTTP_ALLOWED_ORIGINS_ENVVAR);
        
        if (allowedOriginsConfig == null) {
            throw new ServletException(
                "Env var " 
                + ABD_HTTP_ALLOWED_ORIGINS_ENVVAR 
                + " não configurada"
            );
        }
        
        final String[] allowedOriginsArray = allowedOriginsConfig.split(",");

        log.log(
            Level.INFO,
            "{0} = {1}", 
            new Object[]{ABD_HTTP_ALLOWED_ORIGINS_ENVVAR, allowedOriginsConfig}
        );

        for (String allowedOrigin : allowedOriginsArray){
            allowedOrigin = allowedOrigin.trim();
            if (allowedOrigin.length() > 0){
                if (ANY_ORIGIN.equals(allowedOrigin)){
                    anyOriginAllowed = true;
                    this.allowedOrigins.clear();
                    break;
                } else {
                    this.allowedOrigins.add(allowedOrigin);
                }
            }
        }

    }

    @Override
    public void doFilter(
        final ServletRequest servletReq, 
        final ServletResponse servletRes, 
        final FilterChain chain) 
            throws IOException, ServletException{

        final HttpServletRequest request = (HttpServletRequest) servletReq;
        final HttpServletResponse response = (HttpServletResponse) servletRes;
        
        String origin = request.getHeader(ORIGIN_HEADER);
        // Is it a cross origin request ?
        if (origin != null && isEnabled(request)) {
            if (originMatches(origin)) {
                if (isSimpleRequest(request)) {
                    log.log(
                        Level.FINEST,
                        "Simple CORS request to {0}", 
                        request.getRequestURI()
                    );
                    handleSimpleResponse(request, response, origin);

                } else if (isPreflightRequest(request)) {
                    log.log(
                        Level.FINEST,"Preflight CORS request to {0}.",
                        request.getRequestURI()
                    );
                    handlePreflightResponse(request, response, origin);

                } else {
                    log.log(
                        Level.FINEST,
                        "Non-simple CORS request to {0}.", 
                        request.getRequestURI()
                    );
                    handleSimpleResponse(request, response, origin);
                }
            } else {
                log.log(
                    Level.FINEST, 
                    "CORS request {1}->{0} does not match allowed origins {2}", 
                    new Object[]{
                        request.getRequestURI(), 
                        origin, 
                        allowedOrigins
                    }
                );
            }
        }

        chain.doFilter(request, response);
    }

    protected boolean isEnabled(HttpServletRequest request) {
        //WebSocket clients such as Chrome 5 implement a version of the 
        //WebSocket protocol that does not accept extra response headers on the 
        //upgrade response
        final Enumeration<?> connections = request.getHeaders("Connection");
        while (connections.hasMoreElements()) {
            final String connection = (String)connections.nextElement();
            if ("Upgrade".equalsIgnoreCase(connection)) {
                final Enumeration<?> upgrades = request.getHeaders("Upgrade");
                while (upgrades.hasMoreElements()) {
                    final String upgrade = (String)upgrades.nextElement();
                    if ("WebSocket".equalsIgnoreCase(upgrade)) return false;
                }
            }
        }
        return true;
    }

    private boolean originMatches(String originList)
    {
        if (anyOriginAllowed)
            return true;

        if (originList.trim().length() == 0)
            return false;

        String[] origins = originList.split(" ");
        for (String origin : origins)
        {
            if (origin.trim().length() == 0)
                continue;

            for (String allowedOrigin : allowedOrigins)
            {
                if (allowedOrigin.contains("*"))
                {
                    Matcher matcher = createMatcher(origin,allowedOrigin);
                    if (matcher.matches())
                        return true;
                }
                else if (allowedOrigin.equals(origin))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Matcher createMatcher(String origin, String allowedOrigin)
    {
        String regex = parseAllowedWildcardOriginToRegex(allowedOrigin);
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(origin);
    }

    private String parseAllowedWildcardOriginToRegex(String allowedOrigin)
    {
        String regex = allowedOrigin.replace(".","\\.");
        return regex.replace("*",".*"); // we want to be greedy here to match multiple subdomains, thus we use .*
    }

    private boolean isSimpleRequest(HttpServletRequest request)
    {
        String method = request.getMethod();
        if (SIMPLE_HTTP_METHODS.contains(method))
        {
            // TODO: implement better detection of simple headers
            // The specification says that for a request to be simple, custom 
            // request headers must be simple.
            // Here for simplicity I just check if there is a 
            // Access-Control-Request-Method header,
            // which is required for preflight requests
            return request
                    .getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) == null;
        }
        return false;
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        return 
            "OPTIONS".equalsIgnoreCase(request.getMethod())
            && request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER) != null;
    }

    private void handleSimpleResponse(
        final HttpServletRequest request, 
        final HttpServletResponse response, 
        final String origin) {
        
        log.log(Level.FINEST,"handling simple respnse");
        
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
    }

    private void handlePreflightResponse(
            final HttpServletRequest request, 
            final HttpServletResponse response, 
            final String origin) {
        
        log.log(Level.FINEST,"handling preflight");
        
        if (!isMethodAllowed(request) || !areHeadersAllowed(request)) return;

        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER, "true");
        response.setHeader(ACCESS_CONTROL_MAX_AGE_HEADER, PRE_FLIGHT_MAX_AGE);
        response.setHeader(
            ACCESS_CONTROL_ALLOW_METHODS_HEADER, 
            COMMIFIED_ALLOWED_METHODS
        );
        response.setHeader(
            ACCESS_CONTROL_ALLOW_HEADERS_HEADER, 
            COMMIFIED_ALLOWED_HEADERS
        );
    }

    private boolean isMethodAllowed(final HttpServletRequest request)
    {
        final String accessControlRequestMethod = 
            request.getHeader(ACCESS_CONTROL_REQUEST_METHOD_HEADER);
        
        log.log(Level.FINEST,
            "{0} is {1}", 
            new Object[]{
                ACCESS_CONTROL_REQUEST_METHOD_HEADER, 
                accessControlRequestMethod}
        );
        
        final boolean result = 
                accessControlRequestMethod != null
                && ALLOWED_METHODS.contains(accessControlRequestMethod);
        
        log.log(Level.FINEST,
                "Method {0} is {1} among allowed methods {2}", 
                new Object[]{
                    accessControlRequestMethod, 
                    (result ? "" : " not"),
                    ALLOWED_METHODS}
        );
        return result;
    }

    private boolean areHeadersAllowed(final HttpServletRequest request) {
        
        final String accessControlRequestHeaders = 
            request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS_HEADER);
        
        log.log(Level.FINEST,"{0} is {1}", 
            new Object[]{
                ACCESS_CONTROL_REQUEST_HEADERS_HEADER, 
                accessControlRequestHeaders});

        return 
            accessControlRequestHeaders != null 
            && 
            Arrays.asList(accessControlRequestHeaders.split(","))
            .parallelStream()
            .map(String::trim)
            .allMatch(
                providedHeader ->  
                    ALLOWED_HEADERS
                    .parallelStream()
                    .anyMatch(providedHeader::equalsIgnoreCase));
    }

    @Override
    public void destroy() {
        anyOriginAllowed = false;
        allowedOrigins.clear();
        ALLOWED_METHODS.clear();
        ALLOWED_HEADERS.clear();
    }
}