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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoReferenciaSeederTest {

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private EstadoCivilRepository estadoCivilRepository;

    @Mock
    private EspecialidadeRepository especialidadeRepository;

    @Mock
    private DenteRepository denteRepository;

    @Mock
    private ConvenioRepository convenioRepository;

    @InjectMocks
    private CatalogoReferenciaSeeder seeder;

    @Test
    @DisplayName("Deve popular catálogos base quando tabelas estiverem vazias")
    void devePopularCatalogosQuandoVazios() {
        when(perfilRepository.count()).thenReturn(0L);
        when(estadoCivilRepository.count()).thenReturn(0L);
        when(especialidadeRepository.count()).thenReturn(0L);
        when(denteRepository.count()).thenReturn(0L);
        when(convenioRepository.count()).thenReturn(0L);

        seeder.run();

        verify(perfilRepository).saveAll(argThat((List<Perfil> l) -> l != null && l.size() == 3));
        verify(estadoCivilRepository).saveAll(argThat((List<EstadoCivil> l) -> l != null && l.size() == 4));
        verify(especialidadeRepository).saveAll(argThat((List<Especialidade> l) -> l != null && l.size() == 4));
        verify(denteRepository).saveAll(argThat((List<Dente> l) -> l != null && l.size() == 32));
        verify(convenioRepository).saveAll(argThat((List<Convenio> l) -> l != null && l.size() == 4));
    }

    @Test
    @DisplayName("Não deve reinserir catálogos quando já existirem registros")
    void naoDeveReinserirQuandoJaPopulados() {
        when(perfilRepository.count()).thenReturn(3L);
        when(estadoCivilRepository.count()).thenReturn(4L);
        when(especialidadeRepository.count()).thenReturn(4L);
        when(denteRepository.count()).thenReturn(32L);
        when(convenioRepository.count()).thenReturn(4L);

        seeder.run();

        verify(perfilRepository, never()).saveAll(anyList());
        verify(estadoCivilRepository, never()).saveAll(anyList());
        verify(especialidadeRepository, never()).saveAll(anyList());
        verify(denteRepository, never()).saveAll(anyList());
        verify(convenioRepository, never()).saveAll(anyList());
    }
}
