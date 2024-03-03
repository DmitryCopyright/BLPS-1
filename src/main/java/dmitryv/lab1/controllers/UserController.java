package dmitryv.lab1.controllers;

import dmitryv.lab1.models.XmlUser;
import dmitryv.lab1.services.XmlUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import dmitryv.lab1.models.User;
import dmitryv.lab1.requests.UserRequest;
import dmitryv.lab1.responses.UserRes;
import dmitryv.lab1.security.JWTUtil;
import dmitryv.lab1.services.UserService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping(path = "/user")
public class UserController {

    @Autowired
    private UserService service;
    @Autowired
    private XmlUserDetailsService xmlUserDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(path = "all", produces = "application/json")
    @PreAuthorize("hasAuthority('MODERATOR')")
    public ResponseEntity<UserRes> getAll() {
        return new ResponseEntity<>(
                UserRes.builder().users(service.getAll()).build(),
                HttpStatus.OK);
    }

    @GetMapping(path = "{userId}", produces = "application/json")
    @PreAuthorize("hasAuthority('MODERATOR') or (hasAuthority('USER') and #userId == principal.userId)")
    public ResponseEntity<UserRes> getUser(@PathVariable long userId) {
        return service.get(userId)
                .map(u -> new ResponseEntity<>(
                        UserRes.builder().users(Collections.singletonList(u)).build(),
                        HttpStatus.OK)
                ).orElse(new ResponseEntity<>(
                        UserRes.builder().msg("User with this id didn't exist").build(),
                        HttpStatus.BAD_REQUEST));
    }

    @PutMapping(path = "register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserRes> register(@Valid @RequestBody UserRequest req) {
        if (service.exist(req.getEmail())) {
            return new ResponseEntity<>(
                    UserRes.builder().msg("User already exist").build(),
                    HttpStatus.CONFLICT);
        } else {
            String encodedPassword = passwordEncoder.encode(req.getPassword());
            User user = service.add(req.getEmail(), encodedPassword, req.getName(), req.isModerator())
                    .orElseThrow(() -> new RuntimeException("User creation failed"));

            // Добавляем пользователя в XML
            xmlUserDetailsService.saveUser(new XmlUser(user.getEmail(), encodedPassword, user.isModerator() ? "MODERATOR" : "USER"));

            // Теперь возвращаем ответ
            return new ResponseEntity<>(
                    UserRes.builder()
                            .msg("User registered successfully. Please log in.")
                            .build(),
                    HttpStatus.CREATED);
        }
    }

//    @DeleteMapping(path = "delete", consumes = "application/json", produces = "application/json")
//    public ResponseEntity<UserRes> delete(@Valid @RequestBody UserReq req, HttpServletRequest rawReq) {
//        return (service.get(jwtUtil.decode(rawReq)))
//                .map(u -> {
//                    try {
//                        System.out.println(u.getEmail());
//                        auth.authenticate(new UsernamePasswordAuthenticationToken(u.getEmail(), req.password));
//                        service.delete(u.getEmail());
//                        return new ResponseEntity<>(
//                                UserRes.builder().msg("Successful delete account").build(),
//                                HttpStatus.OK);
//                    } catch (AuthenticationException e) {
//                        return new ResponseEntity<>(
//                                UserRes.builder().msg("Bad password").build(),
//                                HttpStatus.UNAUTHORIZED);
//                    }
//                }).orElse(new ResponseEntity<>(
//                        UserRes.builder().msg("Wrong session token").build(),
//                        HttpStatus.UNAUTHORIZED));
//    }

}