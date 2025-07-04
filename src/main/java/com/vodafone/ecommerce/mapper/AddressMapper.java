package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.AddressRequestDTO;
import com.vodafone.ecommerce.model.dto.AddressResponseDTO;
import com.vodafone.ecommerce.model.entity.Address;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponseDTO toDTO(Address address);

    @Mapping(target = "customerProfile", ignore = true)
    @Mapping(source = "primary", target = "isPrimary")
    Address toEntity(AddressRequestDTO request);

    default Address toEntity(AddressRequestDTO request, CustomerProfile customer) {
        Address address = toEntity(request);
        address.setCustomerProfile(customer);
        return address;
    }

    @Mapping(source = "primary", target = "isPrimary")
    Address fromResponseToEntity(AddressResponseDTO request);
}
