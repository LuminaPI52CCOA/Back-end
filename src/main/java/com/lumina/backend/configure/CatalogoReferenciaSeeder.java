package com.lumina.backend.configure;

import com.lumina.backend.model.Convenio;
import com.lumina.backend.model.Dente;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.repository.ConvenioRepository;
import com.lumina.backend.repository.DenteRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import com.lumina.backend.repository.PerfilRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Carga idempotente dos catálogos de referência essenciais ao funcionamento da aplicação
 * (perfis, estados civis, especialidades odontológicas, numeração FDI de dentes e convênios base).
 */
@Slf4j
@Component
@Order(1)
@ConditionalOnProperty(name = "lumina.seed.referencia.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogoReferenciaSeeder implements CommandLineRunner {

    private final PerfilRepository perfilRepository;
    private final EstadoCivilRepository estadoCivilRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final DenteRepository denteRepository;
    private final ConvenioRepository convenioRepository;

    public CatalogoReferenciaSeeder(
            PerfilRepository perfilRepository,
            EstadoCivilRepository estadoCivilRepository,
            EspecialidadeRepository especialidadeRepository,
            DenteRepository denteRepository,
            ConvenioRepository convenioRepository) {
        this.perfilRepository = perfilRepository;
        this.estadoCivilRepository = estadoCivilRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.denteRepository = denteRepository;
        this.convenioRepository = convenioRepository;
    }

    @Override
    public void run(String... args) {
        try {
            seedPerfis();
            seedEstadosCivis();
            seedEspecialidades();
            seedDentes();
            seedConvenios();
        } catch (RuntimeException e) {
            log.warn("Aviso ao executar CatalogoReferenciaSeeder: {}", e.getMessage());
        }
    }

    private void seedPerfis() {
        if (perfilRepository.count() == 0) {
            perfilRepository.saveAll(List.of(
                    new Perfil(null, "Admin"),
                    new Perfil(null, "Dentista"),
                    new Perfil(null, "Recepcionista")
            ));
        }
    }

    private void seedEstadosCivis() {
        if (estadoCivilRepository.count() == 0) {
            estadoCivilRepository.saveAll(List.of(
                    new EstadoCivil(null, "Solteiro(a)"),
                    new EstadoCivil(null, "Casado(a)"),
                    new EstadoCivil(null, "Divorciado(a)"),
                    new EstadoCivil(null, "Viúvo(a)")
            ));
        }
    }

    private void seedEspecialidades() {
        if (especialidadeRepository.count() == 0) {
            especialidadeRepository.saveAll(List.of(
                    new Especialidade(null, "Clínica Geral"),
                    new Especialidade(null, "Ortodontia"),
                    new Especialidade(null, "Endodontia"),
                    new Especialidade(null, "Periodontia")
            ));
        }
    }

    private void seedDentes() {
        if (denteRepository.count() > 0) {
            return;
        }
        String[] nomes = {
                "Incisivo Central",
                "Incisivo Lateral",
                "Canino",
                "1º Pré-molar",
                "2º Pré-molar",
                "1º Molar",
                "2º Molar",
                "3º Molar (Siso)"
        };
        List<Dente> dentes = new ArrayList<>(32);
        for (int quadrante = 1; quadrante <= 4; quadrante++) {
            String arcada = quadrante <= 2 ? "SUPERIOR" : "INFERIOR";
            for (int posicao = 1; posicao <= 8; posicao++) {
                int numero = quadrante * 10 + posicao;
                dentes.add(new Dente(numero, nomes[posicao - 1] + " (" + numero + ")", arcada, quadrante));
            }
        }
        denteRepository.saveAll(dentes);
    }

    private void seedConvenios() {
        if (convenioRepository.count() == 0) {
            convenioRepository.saveAll(List.of(
                    new Convenio("Particular"),
                    new Convenio("Amil Dental"),
                    new Convenio("Bradesco Dental"),
                    new Convenio("SulAmérica Odonto")
            ));
        }
    }
}
