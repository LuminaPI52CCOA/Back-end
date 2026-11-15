package com.lumina.backend.service.usuario;

import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioRequest;
import com.lumina.backend.dto.usuario.UsuarioTokenDto;
import com.lumina.backend.exception.EntidadeNaoEncontradaException;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.security.GerenciadorTokenJwt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final GerenciadorTokenJwt gerenciadorTokenJwt;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          AuthenticationManager authenticationManager,
                          GerenciadorTokenJwt gerenciadorTokenJwt,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.authenticationManager = authenticationManager;
        this.gerenciadorTokenJwt = gerenciadorTokenJwt;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Page<Usuario> listarPaginado(Integer fkPerfil, String nome, Pageable pageable) {
        String filtroNome = (nome == null || nome.isBlank()) ? null : nome.trim();
        return usuarioRepository.buscarComFiltros(fkPerfil, filtroNome, paginacaoOrdenadaPorNome(pageable));
    }

    /**
     * Dentistas ativos (perfil DENTISTA) para seletores de agendamento, com busca parcial por nome.
     */
    public Page<Usuario> listarDentistasAtivos(String nome, Pageable pageable) {
        String filtro = (nome == null || nome.isBlank()) ? null : nome.trim();
        return usuarioRepository.buscarDentistasAtivos(filtro, paginacaoOrdenadaPorNome(pageable));
    }

    public Optional<Usuario> buscarPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return usuarioRepository.findById(id);
    }

    public UsuarioTokenDto autenticar(Usuario usuario) {
        final UsernamePasswordAuthenticationToken credentials = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(), usuario.getSenha());

        final Authentication authentication = this.authenticationManager.authenticate(credentials);

        Usuario usuarioAutenticado = usuarioRepository.findByEmail(usuario.getEmail())
                .orElseThrow(() -> new ResponseStatusException(404, "Email do usuário não cadastrado", null));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final String token = gerenciadorTokenJwt.generateToken(authentication);

        return UsuarioMapper.of(usuarioAutenticado, token);
    }

    public void deletar(Boolean ativo, Long id) {
        buscarExistente(id);
        usuarioRepository.logicalDelete(ativo, id);
    }

    public Usuario atualizar(UsuarioRequest request, Long id) {
        Usuario existente = buscarExistente(id);

        existente.setNome(valorOuAtual(request.nome(), existente.getNome()));
        existente.setCpf(valorOuAtual(request.cpf(), existente.getCpf()));
        existente.setEmail(valorOuAtual(request.email(), existente.getEmail()));
        existente.setSenha(definirSenha(request.senha(), existente.getSenha()));
        if (request.fkPerfil() != null) {
            existente.setPerfil(new Perfil(request.fkPerfil(), null));
        }
        existente.setCro(valorOuAtual(request.cro(), existente.getCro()));
        existente.setAtivo(valorOuAtual(request.ativo(), existente.getAtivo()));

        return usuarioRepository.save(existente);
    }

    private Usuario buscarExistente(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuario de id: %d não encontrado".formatted(id)));
    }

    private static PageRequest paginacaoOrdenadaPorNome(Pageable pageable) {
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.ASC, "nome");
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private static <T> T valorOuAtual(T novo, T atual) {
        return novo != null ? novo : atual;
    }

    private String definirSenha(String novaSenha, String senhaAtual) {
        if (novaSenha == null || novaSenha.isBlank()) {
            return senhaAtual;
        }
        return passwordEncoder.encode(novaSenha);
    }
}
