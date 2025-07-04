package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.dto.AdminRequestDTO;
import com.vodafone.ecommerce.model.dto.AdminResponseDTO;
import com.vodafone.ecommerce.model.dto.AdminUpdateRequestDTO;
import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.entity.User;

import java.util.List;

public interface UserService {

    User getLoggedInUser();

    User registerUser(CustomerRequestDTO request);

    void deleteUser(User user);

    void addAdmin(AdminRequestDTO request);

    List<AdminResponseDTO> getAllAdmins();

    AdminResponseDTO getAdminProfile();

    void updateAdmin(AdminUpdateRequestDTO request, Long id);

    void deleteAdmin(Long id);

    boolean isAdmin(User user);
}
