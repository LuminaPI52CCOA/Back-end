package com.lumina.backend.service.usuario;

import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Resolve o usuário autenticado a partir do e-mail presente no contexto de segurança,
 * evitando que a camada web dependa diretamente do repositório.
 */
@Service
public class UsuarioLogadoService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioLogadoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
