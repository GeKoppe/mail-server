package org.koppe.cuf.mail.server.http.utils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.koppe.cuf.mail.server.common.exceptions.AuthenticationException;
import org.koppe.cuf.mail.server.db.jpa.User;
import org.koppe.cuf.mail.server.db.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.Setter;

public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    /**
     * Refresh id. Resets with every system restart, automatically invalidates all
     * tokens.
     */
    private static final String REFRESH_ID = UUID.randomUUID().toString();
    /**
     * Issuer of the token. Automatically invalidates all tokens on restart
     */
    private static final String ISSUER = "mail-server-" + UUID.randomUUID().toString();
    /**
     * Secret key
     */
    private static final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    /**
     * Expiration of tokens (60 minutes)
     */
    private static final long EXPIRATION_TIME = 3600000L;
    /**
     * Userservice
     */
    @Setter
    private static UserService srv = new UserService();

    /**
     * Generates a jwt based authentication token.
     * 
     * @param userName Username for which to generate the authentication token.
     * @return The generated auth token
     * @throws AuthenticationException If the given username does not exist in the
     *                                 system
     */
    public static final String generateToken(String userName) throws AuthenticationException {
        if (!srv.existsByName(userName)) {
            throw new AuthenticationException("Username " + userName + " does not exist in the system");
        }
        return Jwts.builder()
                .subject(userName)
                .issuer(ISSUER)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("user", userName)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 
     * @param userName
     * @return
     * @throws AuthenticationException
     */
    public static String generateRefreshToken(String userName) throws AuthenticationException {
        if (!srv.existsByName(userName)) {
            throw new AuthenticationException("Username " + userName + " does not exist in the system");
        }
        return Jwts.builder()
                .subject(userName)
                .issuer(ISSUER)
                .claim("refresh", userName)
                .claim("refresh-id", REFRESH_ID)
                .issuedAt(new Date())
                .signWith(SECRET_KEY)
                .compact();
    }

    public static boolean validate(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
            String user = claims.getPayload().get("user", String.class);
            if (user == null || user.isBlank()) {
                return false;
            }
            if (!srv.existsByName(user)) {
                return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Validates the given refresh token. Checks if it is a token at all, if it is a
     * refresh token and if the user name is given
     * 
     * @param token Token to be validated
     * @return True, if token is valid, false otherwise
     */
    private static boolean validateRefreshToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token);
            return ((String) claims.getPayload().get("refresh-id")).equals(REFRESH_ID)
                    && ((String) claims.getPayload().get("refresh")) != null
                    && !((String) claims.getPayload().get("refresh")).isBlank();
        } catch (Exception ex) {
            logger.info("Exception occurred during validation of refresh token", ex);
            return false;
        }
    }

    /**
     * 
     * @param refreshToken
     * @return
     * @throws TokenException
     */
    public static String refresh(String refreshToken) throws TokenException {
        if (!validateRefreshToken(refreshToken)) {
            return null;
        }

        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(refreshToken);
            String userName = (String) claims.getPayload().get("refresh");
            return generateToken(userName);
        } catch (Exception ex) {
            logger.info("Exception occurred while creating a new token", ex);
            throw new TokenException("Could not create a new token", ex);
        }
    }

    public static String getUser(String jwt) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(jwt);
            String user = claims.getPayload().get("user", String.class);
            if (user == null || user.isBlank()) {
                return null;
            }
            return user;
        } catch (Exception ex) {
            return null;
        }
    }

    public static User getJpaUser(String jwt) {
        try {
            Jws<Claims> claims = Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(jwt);
            String user = claims.getPayload().get("user", String.class);
            if (user == null || user.isBlank()) {
                return null;
            }
            List<User> userList = srv.findByName(user);
            return userList.isEmpty() ? null : userList.getFirst();
        } catch (Exception ex) {
            return null;
        }
    }
}
