package com.lumina.backend.dto.anamnese;

import com.lumina.backend.model.Anamnese;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes unitários AnamneseMapper")
class AnamneseMapperTest {

    @Mock
    private ClienteRepository repositorioCliente;

    @InjectMocks
    private AnamneseMapper mapper;

    @Test
    @DisplayName("Deve retornar null quando entradas forem nulas")
    void deveRetornarNullParaEntradasNulas() {
        assertNull(mapper.toEntity(null));
        assertNull(AnamneseMapper.toDto((Anamnese) null));
    }

    @Test
    @DisplayName("Deve converter AnamneseRequest para Anamnese")
    void deveConverterRequestParaEntity() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        when(repositorioCliente.getReferenceById(1L)).thenReturn(cliente);

        AnamneseRequest request = new AnamneseRequest();
        request.setIdAnamnese(10L);
        request.setFkCliente(1L);
        request.setDataAnamnese(LocalDate.of(2026, 10, 22));
        request.setAlergiaMedicamentos(true);
        request.setDescricaoAlergiaMedicamentos("Penicilina");

        Anamnese entity = mapper.toEntity(request);

        assertNotNull(entity);
        assertEquals(10L, entity.getIdAnamnese());
        assertEquals(1L, entity.getFkCliente().getIdCliente());
        assertTrue(entity.getAlergiaMedicamentos());
        assertEquals("Penicilina", entity.getDescricaoAlergiaMedicamentos());
    }

    @Test
    @DisplayName("Deve converter Anamnese e lista de Anamnese para AnamneseResponse")
    void deveConverterEntityParaDto() {
        Cliente indicacao = new Cliente();
        indicacao.setIdCliente(2L);
        Cliente responsavel = new Cliente();
        responsavel.setIdCliente(3L);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNome("Ana");
        cliente.setClienteIndicacao(indicacao);
        cliente.setResponsavel(responsavel);

        Anamnese model = new Anamnese();
        model.setIdAnamnese(10L);
        model.setFkCliente(cliente);
        model.setDataAnamnese(LocalDate.of(2026, 10, 22));
        model.setGestante(false);

        List<AnamneseResponse> dtos = AnamneseMapper.toDto(List.of(model));

        assertEquals(1, dtos.size());
        AnamneseResponse dto = dtos.get(0);
        assertEquals(10L, dto.getIdAnamnese());
        assertEquals("Ana", dto.getAnamneseCliente().getNome());
        assertEquals(2, dto.getAnamneseCliente().getFkClienteIndicacao());
        assertEquals(3, dto.getAnamneseCliente().getFkResponsavel());
        assertFalse(dto.getGestante());
    }
}
