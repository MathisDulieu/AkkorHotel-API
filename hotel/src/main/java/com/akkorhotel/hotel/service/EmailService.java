package com.akkorhotel.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private JavaMailSender javaMailSender;

    private static final String APP_EMAIL = System.getenv("APP_EMAIL");
    private static final String MAIL_MODIFIED_USERNAME = System.getenv("MAIL_MODIFIED_USERNAME");

    public void sendEmail() {

    }

}
