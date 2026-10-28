package com.lumina.backend.service.especialidade;

import com.lumina.backend.model.Especialidade;
import com.lumina.backend.repository.EspecialidadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    public EspecialidadeService(EspecialidadeRepository especialidadeRepository) {
        this.especialidadeRepository = especialidadeRepository;
    }

    public List<Especialidade> listar() {
        return especialidadeRepository.findAllByOrderByNomeAsc();
    }
}
