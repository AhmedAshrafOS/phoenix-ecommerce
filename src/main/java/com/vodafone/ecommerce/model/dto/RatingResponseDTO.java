package com.vodafone.ecommerce.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class RatingResponseDTO {
    private String comment;
    private String firstName;
    private String lastName;
}
