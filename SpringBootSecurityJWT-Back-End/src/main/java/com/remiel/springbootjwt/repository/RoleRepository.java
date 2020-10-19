/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.remiel.springbootjwt.repository;

import com.remiel.springbootjwt.models.ERole;
import com.remiel.springbootjwt.models.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ReMieL
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{
    
    Optional<Role> findByName(ERole name);
    
}
