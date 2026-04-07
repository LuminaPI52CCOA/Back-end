package com.lumina.backend.service.Perfil;

import com.lumina.backend.model.Perfil;
import com.lumina.backend.repository.PerfilRespository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PerfilService {
    private final PerfilRespository repository;

    public PerfilService(PerfilRespository repository) {
        this.repository = repository;
    }

    public List<Perfil> listar(){
        return repository.findAll();
    }
}
