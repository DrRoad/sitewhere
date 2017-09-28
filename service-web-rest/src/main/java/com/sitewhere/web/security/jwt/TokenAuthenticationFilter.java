package com.sitewhere.web.security.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sitewhere.microservice.security.InvalidJwtException;
import com.sitewhere.microservice.security.JwtExpiredException;
import com.sitewhere.microservice.security.TokenManagement;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.web.security.SitewhereGrantedAuthority;

/**
 * Filter that pulls JWT from authentication header and pushes it into Spring
 * {@link SecurityContextHolder}.
 * 
 * @author Derek
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    /** Static logger instance */
    private static Logger LOGGER = LogManager.getLogger();

    /** Header containing JWT on authentication */
    public static final String JWT_HEADER = "x-sitewhere-jwt";

    /** Authentication header */
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /** Token utility methods */
    private TokenManagement tokenUtils = new TokenManagement();

    /** User management implementation */
    private UserDetailsService userDetailsService;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.web.filter.OncePerRequestFilter#doFilterInternal(
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
	    throws IOException, ServletException {

	String jwt = getJwtFromHeader(request);
	if (jwt != null) {
	    LOGGER.debug("Found JWT header: " + jwt);
	    try {
		// Get username from token and load user.
		String username = getTokenUtils().getUsernameFromToken(jwt);
		List<IGrantedAuthority> auths = getTokenUtils().getGrantedAuthoritiesFromToken(jwt);
		List<GrantedAuthority> springAuths = new ArrayList<GrantedAuthority>();
		for (IGrantedAuthority auth : auths) {
		    springAuths.add(new SitewhereGrantedAuthority(auth));
		}

		// Create authentication
		JwtAuthenticationToken token = new JwtAuthenticationToken(username, springAuths, jwt);
		SecurityContextHolder.getContext().setAuthentication(token);
		chain.doFilter(request, response);
	    } catch (JwtExpiredException e) {
		LOGGER.error("Expired JWT.", e);
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT has expired.");
	    } catch (InvalidJwtException e) {
		LOGGER.error("Invalid JWT: " + jwt, e);
		response.sendError(HttpServletResponse.SC_FORBIDDEN, "JWT was invalid.");
	    }
	} else {
	    LOGGER.debug("No JWT found in header.");
	    chain.doFilter(request, response);
	}
    }

    /**
     * Load JWT from authentication header.
     * 
     * @param request
     * @return
     */
    protected String getJwtFromHeader(HttpServletRequest request) {
	String authHeader = request.getHeader(AUTHORIZATION_HEADER);
	LOGGER.debug("Authorization header is: " + authHeader);
	if (authHeader != null && authHeader.startsWith("Bearer ")) {
	    return authHeader.substring(7);
	}
	return null;
    }

    public TokenManagement getTokenUtils() {
	return tokenUtils;
    }

    public void setTokenUtils(TokenManagement tokenUtils) {
	this.tokenUtils = tokenUtils;
    }

    public UserDetailsService getUserDetailsService() {
	return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
	this.userDetailsService = userDetailsService;
    }
}