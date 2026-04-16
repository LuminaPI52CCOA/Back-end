package com.lumina.backend.service.Convenio;

import com.lumina.backend.model.Convenio;
import com.lumina.backend.repository.ConvenioRepository;

import java.util.List;

public class ConvenioService {

    private final ConvenioRepository convenioRepository;

    public ConvenioService(ConvenioRepository convenioRepository) {
        this.convenioRepository = convenioRepository;
    }

    public List<Convenio> listar() {
        return convenioRepository.findAll();
    }
}
