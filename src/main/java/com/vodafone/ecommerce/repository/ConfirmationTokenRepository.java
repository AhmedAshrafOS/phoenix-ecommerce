package com.vodafone.ecommerce.repository;

import com.vodafone.ecommerce.model.entity.ConfirmationToken;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfirmationTokenRepository extends BaseRepository<ConfirmationToken, Long> {

    ConfirmationToken findByToken(String confirmationToken);
}
