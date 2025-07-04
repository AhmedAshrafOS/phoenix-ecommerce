package com.vodafone.ecommerce.service;

import com.vodafone.ecommerce.model.entity.ConfirmationToken;

public interface EmailService {

    void generateEmail(String to, ConfirmationToken token);

    String generateLinkForToken(ConfirmationToken token);
}
