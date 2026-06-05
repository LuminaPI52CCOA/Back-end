package com.lumina.backend.service.Usuario;

import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.swagger.GerenciadorTokenJwt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private PasswordEncoder passwordEncoder;
    private UsuarioRepository usuarioRepository;
    private GerenciadorTokenJwt gerenciadorTokenJwt;
    private AuthenticationManager authenticationManager;
    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository, AuthenticationManager authenticationManager, GerenciadorTokenJwt gerenciadorTokenJwt, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.authenticationManager = authenticationManager;
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvar(Usuario usuario){
        return repository.save(usuario);
    }

    public List<Usuario> listar(){
        return repository.findAll();
    }
    public Optional<Usuario> buscarPorId(Long id){
        return Optional.of(repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada(
                        "Usuario de id: %d não encontrado".formatted(id))));
    }

    public UsuarioTokenDto autenticar(Usuario usuario) {

        final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(), usuario.getSenha());

        final Authentication authentication = this.authenticationManager.authenticate(credentials);

        Usuario usuarioAutenticado =
                usuarioRepository.findByEmail(usuario.getEmail())
                        .orElseThrow(
                                () -> new ResponseStatusException(404, "Email do usuário não cadastrado", null)
                        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = gerenciadorTokenJwt.generateToken(authentication);

        return UsuarioMapper.of(usuarioAutenticado, token);
    }

    public int deletar(Boolean ativo, Long id){
        Optional<Usuario> usuario = repository.findById(id);
        if(usuario.isEmpty()) {
            throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
        }
            return repository.logicalDelete(ativo, id);
    }
    public int atualizar(UsuarioRequest usuarios, Long id){

        Optional<Usuario> usuario = repository.findById(id);
        if(usuario.isEmpty()){
            throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
        }

        return repository.atualizarPeloId(usuarios, id);
    }


}
