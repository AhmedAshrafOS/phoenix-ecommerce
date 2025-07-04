package com.vodafone.ecommerce.service;

import org.springframework.mail.SimpleMailMessage;

public interface AsyncEmailSender {

    void sendEmail(SimpleMailMessage email);
}
