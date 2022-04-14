package com.gabrielsouza.cursomc.services.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.gabrielsouza.cursomc.domain.Cliente;
import com.gabrielsouza.cursomc.domain.enums.TipoCliente;
import com.gabrielsouza.cursomc.dto.ClienteNewDTO;
import com.gabrielsouza.cursomc.repositories.ClienteRepository;
import com.gabrielsouza.cursomc.resources.exceptions.FieldMessage;
import com.gabrielsouza.cursomc.services.validation.utils.BR;

public class ClienteInsertValidator implements ConstraintValidator<ClienteInsert, ClienteNewDTO> {
	
	@Autowired
	private ClienteRepository repo;
	
	public void initialize(ClienteInsertValidator ann) {
	}

	@Override
	public boolean isValid(ClienteNewDTO objDto, ConstraintValidatorContext context) {
		List<FieldMessage> list = new ArrayList<>();
		
		if(objDto.getTipo().equals(TipoCliente.PESSOAFISICA.getCod()) && !BR.validCPF(objDto.getCpfoutCnpj()))  {
			
			list.add(new FieldMessage("cpfoutCnpj", "CPF Inválido"));
		}
		
	    if(objDto.getTipo().equals(TipoCliente.PESSOAJURIDICA.getCod()) && !BR.validaCNPJ(objDto.getCpfoutCnpj()))  {
			
			list.add(new FieldMessage("cpfoutCnpj", "CNPJ Inválido"));
		}
	    
	    Cliente aux = repo.findByEmail(objDto.getEmail());
	    
	    if(aux != null) {
	    	list.add(new FieldMessage("email", "Email ja existente"));
	    }
		
		for (FieldMessage e : list) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(e.getMsg()).addPropertyNode(e.getFieldName())
					.addConstraintViolation();
		}
		return list.isEmpty();
	}
}
