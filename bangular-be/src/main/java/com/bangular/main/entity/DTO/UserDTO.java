package com.bangular.main.entity.DTO;

import com.bangular.main.entity.Role;
import com.bangular.main.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;

@Data
public class UserDTO {

    @NotBlank(message = "Name cannot be blank.")
    @NotNull(message = "Name is required!")
    private String name;

    @NotBlank(message = "Username cannot be blank.")
    @NotNull(message = "Username is required!")
    private String username;

    @NotBlank(message = "Email cannot be blank.")
    @NotNull(message = "Email is required!")
    @Email(message = "Email is invalid!")
    private String email;

    @NotBlank(message = "Password cannot be blank.")
    @NotNull(message = "Password is required!")
    private String password;

    private Collection<Role> roles = new ArrayList<>();

    public User toUser() {
        User user = new User();
        user.setName(this.name);
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPassword(this.password);
        user.setRoles(this.roles);
        return user;
    }
}
