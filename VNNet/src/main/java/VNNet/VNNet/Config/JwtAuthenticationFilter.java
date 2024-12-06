package VNNet.VNNet.Config;

import VNNet.VNNet.Service.CustomUserDetailsService;
import VNNet.VNNet.Service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        logger.info("Request URL: " + request.getRequestURL());
        logger.info("Request Method: " + request.getMethod());

        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            try {
                String phoneNumber = jwtService.extractPhoneNumber(jwt);
                logger.info("Extracted Phone Number: " + phoneNumber);

                if (phoneNumber != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = customUserDetailsService.loadUserByUsername(phoneNumber);
                    logger.info("User Details: " + userDetails);

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        logger.info("Authentication set in Security Context");
                    } else {
                        logger.warn("Invalid token");
                    }
                }
            } catch (Exception e) {
                logger.error("Error processing JWT", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
//THỰC THI TRƯỚC KHI YÊU CẦU REQUEST ĐƯỢC XỬ LÝ BỞI CONTROLLER
