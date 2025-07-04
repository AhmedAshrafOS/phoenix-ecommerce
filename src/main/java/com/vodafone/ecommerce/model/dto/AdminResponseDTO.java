package com.vodafone.ecommerce.model.dto;

import com.vodafone.ecommerce.model.enums.AccountStatus;
import com.vodafone.ecommerce.model.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AdminResponseDTO {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private AccountStatus accountStatus;
}

