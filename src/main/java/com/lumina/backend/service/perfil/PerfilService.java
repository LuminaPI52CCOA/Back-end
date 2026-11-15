package com.lumina.backend.service.perfil;

import com.lumina.backend.model.Perfil;
import com.lumina.backend.repository.PerfilRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfilService {
    private final PerfilRepository repository;

    public PerfilService(PerfilRepository repository) {
        this.repository = repository;
    }

    public List<Perfil> listar(){
        return repository.findAll();
    }
}
