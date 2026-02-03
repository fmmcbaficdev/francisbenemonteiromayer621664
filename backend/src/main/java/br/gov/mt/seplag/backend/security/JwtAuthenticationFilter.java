package br.gov.mt.seplag.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;  // ← Interface, não a classe concreta

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/") || path.isEmpty() ||
                path.startsWith("/v1/auth/") ||
                path.startsWith("/actuator/") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Usuário autenticado: {}", username);
                }
            }

        } catch (ExpiredJwtException e) {
            log.warn("Token JWT expirado para IP: {}", request.getRemoteAddr());
            sendErrorResponse(response, "TOKEN_EXPIRADO", "Token expirado. Utilize o endpoint de refresh.");
            return;

        } catch (SignatureException e) {
            log.warn("Assinatura JWT inválida - possível tentativa de falsificação. IP: {}", request.getRemoteAddr());
            sendErrorResponse(response, "TOKEN_INVALIDO", "Token inválido.");
            return;

        } catch (MalformedJwtException e) {
            log.warn("Token JWT malformado. IP: {}", request.getRemoteAddr());
            sendErrorResponse(response, "TOKEN_MALFORMADO", "Token malformado.");
            return;

        } catch (Exception e) {
            log.error("Erro inesperado ao processar JWT. IP: {}", request.getRemoteAddr(), e);
            sendErrorResponse(response, "ERRO_AUTENTICACAO", "Erro ao processar autenticação.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Envia resposta de erro padronizada em formato JSON
     */
    private void sendErrorResponse(HttpServletResponse response, String codigo, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("""
                {
                  "codigo": "%s",
                  "mensagem": "%s"
                }
                """, codigo, mensagem));
    }
}