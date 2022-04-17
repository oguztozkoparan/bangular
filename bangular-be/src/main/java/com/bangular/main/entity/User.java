package com.bangular.main.entity;

import com.bangular.main.entity.DTO.UserDTO;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    @Column
    private String name;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    public UserDTO toUserDTO() {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(this.getName());
        userDTO.setUsername(this.getUsername());
        userDTO.setEmail(this.getEmail());
        userDTO.setPassword(this.getPassword());
        userDTO.setRoles(this.getRoles());
        return userDTO;
    }
}
