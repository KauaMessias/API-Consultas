package com.example.consultas.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;
    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(Resend resend) {
        this.resend = resend;
    }

    @Async
    public void enviarEmail(String destinatario, String assunto, String mensagem) {
        CreateEmailOptions params = CreateEmailOptions.builder().from(remetente).to(destinatario).subject(assunto).html("<div> " + mensagem + "</div>").build();

        try {
            resend.emails().send(params);
        }catch (ResendException e){
            e.printStackTrace();
        }
    }

}
