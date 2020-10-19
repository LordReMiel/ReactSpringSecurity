/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.remiel.springbootjwt.controllers;

import com.remiel.springbootjwt.models.ERole;
import com.remiel.springbootjwt.models.Role;
import com.remiel.springbootjwt.models.User;
import com.remiel.springbootjwt.payload.request.LoginRequest;
import com.remiel.springbootjwt.payload.request.SignupRequest;
import com.remiel.springbootjwt.payload.response.JwtResponse;
import com.remiel.springbootjwt.payload.response.MessageResponse;
import com.remiel.springbootjwt.repository.RoleRepository;
import com.remiel.springbootjwt.repository.UserRepository;
import com.remiel.springbootjwt.security.jwt.JwtUtils;
import com.remiel.springbootjwt.security.services.UserDetailsImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ReMieL
 */

@CrossOrigin (origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
        @Autowired
        AuthenticationManager authenticationManager;
        
        @Autowired
        UserRepository userRepository;
        
        @Autowired
        RoleRepository roleRepository;
        
        @Autowired
        PasswordEncoder encoder;
        
        @Autowired
        JwtUtils jwtUtils;
    
    
    
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
       
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            List <String> roles = userDetails.getAuthorities().stream()
                                  .map(item -> item.getAuthority())
                                  .collect(Collectors.toList());
            
            
            
            return ResponseEntity.ok(new JwtResponse(jwt,
                                                      userDetails.getId(),
                                                      userDetails.getUsername(),
                                                      userDetails.getEmail(),
                                                      roles));
        
    }
    
    
    
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
            
        if (userRepository.existsByUsername(signupRequest.getUsername())){
            
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error:Username is already taken!!"));
        }
        
        
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            
            
            return ResponseEntity
                    .badRequest()
                    .body((new MessageResponse("Error: Email is already in use!!")));
        }
        
        //Create new User account
        
        User user = new User(signupRequest.getUsername(), encoder.encode(signupRequest.getPassword()) ,signupRequest.getEmail());
        
        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();
        
        if (strRoles == null) {
            
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        }else {
            
            strRoles.forEach(role -> { 
                
                switch(role) {
                    
                    case "admin" : 
                                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                                         .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

                                        roles.add(adminRole);
                                        break;
                        
                    case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
		    default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
                                        break;
                        
                }
            });
        }
        
                user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        
    }
    
}
