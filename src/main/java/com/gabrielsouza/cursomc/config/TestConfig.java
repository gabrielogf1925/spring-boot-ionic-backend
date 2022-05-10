package com.gabrielsouza.cursomc.config;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.gabrielsouza.cursomc.services.DBService;
import com.gabrielsouza.cursomc.services.EmailService;
import com.gabrielsouza.cursomc.services.MockEmailService;

@Configuration
@Profile("test")
public class TestConfig {
	
	@Autowired
	private DBService dbService;
	
	@Bean
	public boolean InstantiateDatabase() throws ParseException {
		
		dbService.InstantiateTestDatabase();
		return true;
	}
	
	@Bean
	public EmailService emailService() {
		
		return new MockEmailService();
	}

}
