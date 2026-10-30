package com.lumina.backend.service.midia;

import com.lumina.backend.dto.midia.TipoMidiaRequest;
import com.lumina.backend.dto.midia.TipoMidiaResponse;
import com.lumina.backend.model.TipoMidia;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.TipoMidiaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class TipoMidiaService {

    private final TipoMidiaRepository tipoMidiaRepository;

    public TipoMidiaService(TipoMidiaRepository tipoMidiaRepository) {
        this.tipoMidiaRepository = tipoMidiaRepository;
    }

    @Transactional(readOnly = true)
    public List<TipoMidiaResponse> listarDisponiveis(Usuario usuario) {
        List<TipoMidia> tipos;
        if (usuario != null && usuario.getIdUsuario() != null) {
            tipos = tipoMidiaRepository.findDisponiveisPorUsuario(usuario.getIdUsuario());
        } else {
            tipos = tipoMidiaRepository.findGlobais();
        }
        return tipos.stream().map(TipoMidiaResponse::fromEntity).toList();
    }

    @Transactional
    public TipoMidiaResponse criarPersonalizado(TipoMidiaRequest request, Usuario usuario) {
        TipoMidia novoTipo = new TipoMidia();
        novoTipo.setNome(request.getNome().trim());
        novoTipo.setDescricao(request.getDescricao() != null ? request.getDescricao().trim() : null);
        novoTipo.setUsuario(usuario);
        novoTipo.setAtivo(true);

        TipoMidia salvo = tipoMidiaRepository.save(novoTipo);
        String emailUsuario = (usuario != null) ? usuario.getEmail() : "SISTEMA";
        log.info("AUDIT: Usuario [{}] cadastrou novo tipo de midia [{}] ID [{}]", emailUsuario, salvo.getNome(), salvo.getIdTipoMidia());

        return TipoMidiaResponse.fromEntity(salvo);
    }
}
