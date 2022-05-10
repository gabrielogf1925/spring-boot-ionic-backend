package com.gabrielsouza.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.gabrielsouza.cursomc.domain.Pedido;

public interface EmailService {
	
	void sendOrderEmailConfirmationEmail(Pedido obj);
	
	void sendEmail(SimpleMailMessage msg);

}
