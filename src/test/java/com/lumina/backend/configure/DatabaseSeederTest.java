package com.lumina.backend.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.ConvenioRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.PerfilRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSeederTest {

    @Mock
    private PerfilRepository perfilRepository;
    @Mock
    private ConvenioRepository convenioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ClienteConvenioRepository clienteConvenioRepository;
    @Mock
    private EspecialidadeRepository especialidadeRepository;
    @Mock
    private ConsultaRepository consultaRepository;

    private DatabaseSeeder seeder;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        seeder = new DatabaseSeeder(
                perfilRepository,
                convenioRepository,
                usuarioRepository,
                clienteRepository,
                clienteConvenioRepository,
                especialidadeRepository,
                consultaRepository,
                objectMapper
        );
    }

    @Test
    @DisplayName("Deve estar anotado com @Profile(\"!prod\") e flag lumina.seed.demo.enabled")
    void deveEstarDesabilitadoEmProducaoEControladoPorFlag() {
        Profile profile = DatabaseSeeder.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"!prod"}, profile.value());

        ConditionalOnProperty flag = DatabaseSeeder.class.getAnnotation(ConditionalOnProperty.class);
        assertNotNull(flag);
        assertArrayEquals(new String[]{"lumina.seed.demo.enabled"}, flag.name());
        assertEquals("true", flag.havingValue());
        assertTrue(flag.matchIfMissing());
    }

    @Test
    @DisplayName("Deve carregar pacientes do JSON e usuários demo quando base estiver vazia")
    void deveCarregarDadosDemonstracao() {
        Perfil dentista = new Perfil(2, "Dentista");
        Usuario usuarioDentista = new Usuario();
        usuarioDentista.setIdUsuario(1L);
        usuarioDentista.setPerfil(dentista);
        usuarioDentista.setAtivo(true);

        when(convenioRepository.findAll()).thenReturn(List.of(new Convenio("Particular")));
        when(perfilRepository.findAll()).thenReturn(List.of(new Perfil(1, "Admin"), dentista, new Perfil(3, "Recepcionista")));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByCpf(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));
        when(consultaRepository.count()).thenReturn(0L);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuarioDentista));
        when(clienteRepository.findAll()).thenReturn(List.of(new Cliente()));
        when(especialidadeRepository.findAll()).thenReturn(List.of(new Especialidade(1, "Clínica Geral")));

        seeder.run();

        verify(usuarioRepository, times(12)).save(any(Usuario.class));
        verify(clienteRepository, times(174)).save(any(Cliente.class));
        verify(clienteConvenioRepository, times(174)).save(any());
        verify(consultaRepository, times(1)).saveAll(any());
    }
}
