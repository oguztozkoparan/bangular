package com.bangular.main.entity.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class AddRoleDTO {

    @NotNull
    @NotBlank
    private String username;

    @NotNull
    @NotBlank
    private String roleName;

}
