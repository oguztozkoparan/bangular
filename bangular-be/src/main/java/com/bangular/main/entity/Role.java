package com.bangular.main.entity;

import com.bangular.main.entity.DTO.RoleDTO;
import lombok.*;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "role")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;

    public RoleDTO toRoleDTO() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setName(this.getName());
        return roleDTO;
    }

}
