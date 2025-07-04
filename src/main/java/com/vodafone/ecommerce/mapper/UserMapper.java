package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.AdminRequestDTO;
import com.vodafone.ecommerce.model.dto.AdminResponseDTO;
import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.dto.UserResponseDTO;
import com.vodafone.ecommerce.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", constant = "ADMIN")
    @Mapping(target = "accountStatus", constant = "ACTIVE")
    User mapToUserAdmin(AdminRequestDTO request);

    @Mapping(source = "username", target = "username")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(target = "role", constant = "CUSTOMER")
    @Mapping(target = "accountStatus", constant = "ACTIVE")
    User mapToUserForCreateUser(CustomerRequestDTO request);

    UserResponseDTO mapToDTO(User user);

    @Mapping(source = "userId", target = "userId")
    AdminResponseDTO toAdminResponseDTO(User user);

    List<AdminResponseDTO> toAdminResponseDTOList(List<User> users);
}
