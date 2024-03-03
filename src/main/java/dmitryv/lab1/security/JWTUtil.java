package dmitryv.lab1.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import dmitryv.lab1.services.UserDetailsServiceImpl;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component public class JWTUtil {

//    @Value("${jwt.key}") private String KEY;
//    @Value("${jwt.validity}") private long VALIDITY;
//    @Autowired private UserDetailsServiceImpl userDetails;
//
//    public String generateToken(String username, List<String> roles) {
//        val claims = Jwts.claims().setSubject(username);
//        claims.put("roles", roles);
//        val now = new Date();
//        return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(new Date(now.getTime() + VALIDITY))
//                .signWith(SignatureAlgorithm.HS512, KEY).compact();
//    }
//
//    public boolean validateToken(String token) {
//        return !Jwts.parser().setSigningKey(KEY).parseClaimsJws(token).getBody().getExpiration().before(new Date());
//    }
//
//    private boolean isTokenExpired(String token) { return getExpirationDate(token).before(new Date()); }
//
//    public String resolveToken(HttpServletRequest req) {
//        val bearerToken = req.getHeader("Authorization");
//        return (bearerToken != null && bearerToken.startsWith("Bearer ")) ? bearerToken.substring(7) : null;
//    }
//
//    public Authentication getAuthentication(String token) {
//        UserDetails userDetails = this.userDetails.loadUserByUsername(getUsername(token));
//        Claims claims = Jwts.parser().setSigningKey(KEY).parseClaimsJws(token).getBody();
//        List<String> roles = claims.get("roles", List.class);
//        List<GrantedAuthority> authorities = roles.stream()
//                .map(role -> new SimpleGrantedAuthority(role))
//                .collect(Collectors.toList());
//        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
//    }
//
//    public String getUsername(String token) { return getClaim(token, Claims::getSubject); }
//
//    public Date getExpirationDate(String token) { return getClaim(token, Claims::getExpiration); }
//
//    public <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
//        return claimsResolver.apply(Jwts.parser().setSigningKey(KEY).parseClaimsJws(token).getBody());
//    }
//
//    public String decode(HttpServletRequest req) { return this.getUsername(this.resolveToken(req)); }
}
