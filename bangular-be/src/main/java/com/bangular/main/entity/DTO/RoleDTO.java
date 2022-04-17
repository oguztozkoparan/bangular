package com.bangular.main.entity.DTO;

import com.bangular.main.entity.Role;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class RoleDTO {

    @NotBlank
    @NotNull
    private String name;

    public Role toRole() {
        Role role = new Role();
        role.setName(this.name);
        return role;
    }
}
