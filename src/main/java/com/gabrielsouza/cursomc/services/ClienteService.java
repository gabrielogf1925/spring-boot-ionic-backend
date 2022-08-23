package com.gabrielsouza.cursomc.services;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gabrielsouza.cursomc.domain.Cidade;
import com.gabrielsouza.cursomc.domain.Cliente;
import com.gabrielsouza.cursomc.domain.Endereco;
import com.gabrielsouza.cursomc.domain.enums.Perfil;
import com.gabrielsouza.cursomc.domain.enums.TipoCliente;
import com.gabrielsouza.cursomc.dto.ClienteDTO;
import com.gabrielsouza.cursomc.dto.ClienteNewDTO;
import com.gabrielsouza.cursomc.repositories.CidadeRepository;
import com.gabrielsouza.cursomc.repositories.ClienteRepository;
import com.gabrielsouza.cursomc.repositories.EnderecoRepository;
import com.gabrielsouza.cursomc.security.UserSS;
import com.gabrielsouza.cursomc.services.exceptions.AuthorizationException;
import com.gabrielsouza.cursomc.services.exceptions.DataIntegrityException;
import com.gabrielsouza.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private BCryptPasswordEncoder pe;
	
	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private CidadeRepository cidadeRepository;
	
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	@Autowired
	private S3Service s3Service;
	
	@Autowired
	private ImageService imageService;
	
	@Value("${img.prefix.client.profile}")
	private String prefix;
	
	@Value("${img.profile.size}")
	private Integer size;
	
    public Cliente find(Integer id) {
		
		UserSS user = UserService.authenticated();
		if (user==null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}
    
	@Transactional
	public Cliente insert (Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		
		return obj;
		
	}
	
	public Cliente update (Cliente obj) {
		
		Cliente newObj = find(obj.getId());
		updateDate(newObj,obj);
		return repo.save(newObj);
	}
	
	

	public void delete (Integer id) {
		find(id);
		try {
			
			repo.deleteById(id);

			
		}catch (DataIntegrityViolationException e){
			
			throw new DataIntegrityException("Não é possivel exlcuir porque há pedidos relacionados");
			
		}
		
	}
	
	public List<Cliente> findAll(){
		
		return repo.findAll();
	}
	
	public Cliente findByEmail(String email) {
		UserSS user = UserService.authenticated();
		if (user == null || !user.hasRole(Perfil.ADMIN) && !email.equals(user.getUsername())) {
			throw new AuthorizationException("Acesso negado");
		}
	
		Cliente obj = repo.findByEmail(email);
		if (obj == null) {
			throw new ObjectNotFoundException(
					"Objeto não encontrado! Id: " + user.getId() + ", Tipo: " + Cliente.class.getName());
		}
		return obj;
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage ,String orderBy, String direction){
		
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction),
				orderBy);
		
		return repo.findAll(pageRequest);
		
		
	}
	
	public Cliente fromDTO(ClienteDTO objDTO) {
		
		return new  Cliente (objDTO.getId(), objDTO.getNome(),objDTO.getEmail(),null,null,null);
	}
	
      public Cliente fromDTO(ClienteNewDTO objDTO) {
		
		 Cliente cli = new Cliente  (null, objDTO.getNome(),objDTO.getEmail(),objDTO.getCpfoutCnpj(),TipoCliente.toEnum(objDTO.getTipo()),pe.encode(objDTO.getSenha()));
		 
		 Cidade cid =  new Cidade(objDTO.getCidadeId(),null,null);
		 
		 Endereco end =  new Endereco(null,objDTO.getLogadouro(),objDTO.getNumero(),objDTO.getComplemento(),objDTO.getBairro(),objDTO.getCep(),cli,cid);
		 
		 cli.getEnderecos().add(end);
		 
		 cli.getTelefones().add(objDTO.getTelefone1());
		 
		 if(objDTO.getTelefone2() != null) {
			 
			 cli.getTelefones().add(objDTO.getTelefone2());
		 }
		 
          if(objDTO.getTelefone3() != null) {
			 
			 cli.getTelefones().add(objDTO.getTelefone3());
		 }
          
          return cli;
	}
	
	private void updateDate(Cliente newObj, Cliente obj) {
		newObj.setEmail( obj.getEmail());
		newObj.setNome(obj.getNome());
		
	}
	
	public URI uploadProfilePicture(MultipartFile multipartFile) {
		UserSS user = UserService.authenticated();
		if (user == null) {
			throw new AuthorizationException("Acesso negado");
		}
		
		BufferedImage jpgImage = imageService.getJpgImageFromFile(multipartFile);
		jpgImage = imageService.cropSquare(jpgImage);
		jpgImage = imageService.resize(jpgImage, size);
		
		String fileName = prefix + user.getId() + ".jpg";
		
		return s3Service.uploadFile(imageService.getInputStream(jpgImage, "jpg"), fileName, "image");
	}

}
