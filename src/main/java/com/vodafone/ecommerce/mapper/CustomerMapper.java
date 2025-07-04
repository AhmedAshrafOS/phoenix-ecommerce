package com.vodafone.ecommerce.mapper;

import com.vodafone.ecommerce.model.dto.CustomerProfileResponseDTO;
import com.vodafone.ecommerce.model.dto.CustomerRequestDTO;
import com.vodafone.ecommerce.model.entity.Address;
import com.vodafone.ecommerce.model.entity.CustomerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerProfile mapToCustomer(CustomerRequestDTO request);

    @Mapping(source = "addresses", target = "addresses")
    CustomerProfileResponseDTO mapToCustomerDTO(CustomerProfile customer);

    default List<String> mapAddressListToStringList(List<Address> addresses) {
        if (addresses == null) return Collections.emptyList();
        return addresses.stream()
                .map(Address::getStreet)
                .toList();
    }
}
