package com.lumina.backend.service.anamnese;

import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Anamnese;
import com.lumina.backend.repository.AnamneseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnamneseService {

    private final AnamneseRepository repository;

    public AnamneseService(AnamneseRepository repository) {
        this.repository = repository;
    }

    public Anamnese buscarPorId(Integer id){
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Anamnese não encontrada!"));
    }
}
