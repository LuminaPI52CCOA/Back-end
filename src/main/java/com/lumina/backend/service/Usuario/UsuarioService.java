package com.lumina.backend.service.Usuario;

import com.lumina.backend.domain.usuario.UsuarioCommand;
import com.lumina.backend.domain.usuario.UsuarioId;
import com.lumina.backend.domain.usuario.UsuarioRepositoryPort;
import com.lumina.backend.domain.usuario.Usuario;
import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
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
    private UsuarioRepositoryPort repositoryPort;

    @Autowired
    public UsuarioService(@Autowired(required = false) UsuarioRepositoryPort repositoryPort,
                          @Autowired(required = false) AuthenticationManager authenticationManager,
                          @Autowired(required = false) GerenciadorTokenJwt gerenciadorTokenJwt,
                          @Autowired(required = false) UsuarioRepository usuarioRepository,
                          @Autowired(required = false) PasswordEncoder passwordEncoder) {
        this.repositoryPort = repositoryPort;
        this.authenticationManager = authenticationManager;
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioService(UsuarioRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    public Usuario salvar(Usuario usuario) {
        if (repositoryPort != null) {
            return repositoryPort.salvar(usuario);
        }
        if (usuarioRepository != null) {
            com.lumina.backend.model.Usuario model = toModel(usuario);
            com.lumina.backend.model.Usuario saved = usuarioRepository.save(model);
            return toDomain(saved);
        }
        return usuario;
    }

    public com.lumina.backend.model.Usuario salvar(com.lumina.backend.model.Usuario model) {
        if (usuarioRepository != null) {
            return usuarioRepository.save(model);
        }
        if (repositoryPort != null) {
            Usuario domain = toDomain(model);
            Usuario saved = repositoryPort.salvar(domain);
            return toModel(saved);
        }
        return model;
    }

    public List<Usuario> listar() {
        if (repositoryPort != null) {
            return repositoryPort.buscarTodos();
        }
        if (usuarioRepository != null) {
            return usuarioRepository.findAll().stream()
                    .map(this::toDomain)
                    .toList();
        }
        return List.of();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (repositoryPort != null) {
            return repositoryPort.buscarPorId(UsuarioId.of(id));
        }
        if (usuarioRepository != null) {
            return usuarioRepository.findById(id).map(this::toDomain);
        }
        return Optional.empty();
    }

    public UsuarioTokenDto autenticar(com.lumina.backend.model.Usuario usuario) {
        final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(), usuario.getSenha());

        final Authentication authentication = this.authenticationManager.authenticate(credentials);

        com.lumina.backend.model.Usuario usuarioAutenticado =
                usuarioRepository.findByEmail(usuario.getEmail())
                        .orElseThrow(
                                () -> new ResponseStatusException(404, "Email do usuário não cadastrado", null)
                        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = gerenciadorTokenJwt.generateToken(authentication);

        return UsuarioMapper.of(usuarioAutenticado, token);
    }

    public void deletar(Boolean ativo, Long id) {
        if (repositoryPort != null) {
            Optional<Usuario> usuario = repositoryPort.buscarPorId(UsuarioId.of(id));
            if (usuario.isEmpty()) {
                throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
            }
            repositoryPort.deletarLogicamente(UsuarioId.of(id), ativo);
        } else if (usuarioRepository != null) {
            Optional<com.lumina.backend.model.Usuario> usuario = usuarioRepository.findById(id);
            if (usuario.isEmpty()) {
                throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
            }
            usuarioRepository.logicalDelete(ativo, id);
        }
    }

    public Usuario atualizar(UsuarioRequest usuarios, Long id) {
        if (repositoryPort != null) {
            Usuario usuarioExistente = repositoryPort.buscarPorId(UsuarioId.of(id))
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id)));

            UsuarioCommand command = UsuarioMapper.toCommand(usuarios);
            Usuario usuarioAtualizado = usuarioExistente.atualizar(command);

            return repositoryPort.salvar(usuarioAtualizado);
        } else if (usuarioRepository != null) {
            com.lumina.backend.model.Usuario usuarioExistente = usuarioRepository.findById(id)
                    .orElseThrow(() -> new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id)));
            if (usuarios.getNome() != null) usuarioExistente.setNome(usuarios.getNome());
            if (usuarios.getEmail() != null) usuarioExistente.setEmail(usuarios.getEmail());
            if (usuarios.getCpf() != null) usuarioExistente.setCpf(usuarios.getCpf());
            com.lumina.backend.model.Usuario saved = usuarioRepository.save(usuarioExistente);
            return toDomain(saved);
        }
        throw new EntidadeNaoEncontrada("Usuario de id: %d não encontrado".formatted(id));
    }

    private Usuario toDomain(com.lumina.backend.model.Usuario model) {
        if (model == null) return null;
        UsuarioId id = model.getIdUsuario() != null ? UsuarioId.of(model.getIdUsuario()) : null;
        UsuarioCommand command = new UsuarioCommand(
                model.getNome(),
                model.getCpf(),
                model.getEmail(),
                model.getSenha(),
                model.getFkPerfil(),
                model.getCro(),
                model.getAtivo()
        );
        return id != null ? Usuario.reconstruir(id, command) : Usuario.criar(command);
    }

    private com.lumina.backend.model.Usuario toModel(Usuario domain) {
        if (domain == null) return null;
        com.lumina.backend.model.Usuario model = new com.lumina.backend.model.Usuario();
        if (domain.getId() != null) model.setIdUsuario(domain.getId().getValue());
        model.setNome(domain.getNome());
        model.setCpf(domain.getCpf());
        model.setEmail(domain.getEmail());
        model.setSenha(domain.getSenha());
        model.setFkPerfil(domain.getFkPerfil());
        model.setCro(domain.getCro());
        model.setAtivo(domain.getAtivo());
        return model;
    }
}
