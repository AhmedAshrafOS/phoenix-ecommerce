package com.vodafone.ecommerce.model.dto;

public interface UserUpdateRequestDTO {
    String getUsername();

    String getOldPassword();

    String getNewPassword();

    String getConfirmPassword();
}
