package com.bangular.main.controller;

//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.interfaces.DecodedJWT;
import com.bangular.main.entity.DTO.AddRoleDTO;
import com.bangular.main.entity.DTO.UserDTO;
import com.bangular.main.entity.Role;
import com.bangular.main.entity.User;
import com.bangular.main.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok().body(userService.getUsers());
    }

    @PostMapping("/user/save")
    public ResponseEntity<User> saveUser(@RequestBody UserDTO userDTO) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveUser(userDTO));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok().body(userService.getUser(username));
    }

    @PostMapping("/role/save")
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role/save").toUriString());
        return ResponseEntity.created(uri).body(userService.saveRole(role));
    }

    @PostMapping("/role/addToUser")
    public ResponseEntity<?> addRoleToUser(@RequestBody AddRoleDTO addRoleDTO){
        userService.addRoleToUser(addRoleDTO.getUsername(), addRoleDTO.getRoleName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO userDTO) {
        if (userService.checkIfUserExist(userDTO.toUser().getEmail())) {
            return ResponseEntity.badRequest().build();
        } else {
            userService.saveUser(userDTO);
            return ResponseEntity.ok().build();
        }
    }

//    @GetMapping("/token/refresh")
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//            try {
//                String refreshToken = authorizationHeader.substring("Bearer ".length());
//                Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
//                JWTVerifier verifier = JWT.require(algorithm).build();
//                DecodedJWT decodedJWT = verifier.verify(refreshToken);
//                String username = decodedJWT.getSubject();
//                User user = userService.getUser(username);
//                String accessToken = JWT.create()
//                        .withSubject(user.getUsername())
//                        .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 60 * 1000))
//                        .withIssuer(request.getRequestURL().toString())
//                        .withClaim("roles",
//                                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
//                        .sign(algorithm);
//
//                Map<String, String> tokens = new HashMap<>();
//                tokens.put("access_token", accessToken);
//                tokens.put("refresh_token", refreshToken);
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                new ObjectMapper().writeValue(response.getOutputStream(), tokens);
//
//            } catch (Exception exception) {
//                response.setHeader("error", exception.getMessage());
//                response.setStatus(HttpStatus.FORBIDDEN.value());
//                Map<String, String> error = new HashMap<>();
//                error.put("error", exception.getMessage());
//                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                new ObjectMapper().writeValue(response.getOutputStream(), error);
//            }
//        } else {
//            throw new RuntimeException("Refresh token not found!");
//        }
//
//    }

}
