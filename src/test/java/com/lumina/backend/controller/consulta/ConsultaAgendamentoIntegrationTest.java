package com.lumina.backend.controller.consulta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.exception.ConflitoHorarioException;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Dente;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.DenteRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.UsuarioRepository;
import com.lumina.backend.service.consulta.ConsultaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Fluxo completo de agendamento: HTTP + Spring Security + JPA (H2) com relogio fixo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ConsultaAgendamentoIntegrationTest.RelogioFixo.class)
@DisplayName("Integração do agendamento de consultas")
class ConsultaAgendamentoIntegrationTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    // Segunda-feira, 02/11/2026, 10:00 em Sao Paulo
    private static final LocalDate HOJE = LocalDate.of(2026, 11, 2);
    private static final LocalDate AMANHA = HOJE.plusDays(1);

    @TestConfiguration
    static class RelogioFixo {
        @Bean
        @Primary
        Clock relogioFixoDeTeste() {
            return Clock.fixed(ZonedDateTime.of(2026, 11, 2, 10, 0, 0, 0, ZONA).toInstant(), ZONA);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EspecialidadeRepository especialidadeRepository;

    @Autowired
    private DenteRepository denteRepository;

    @Autowired
    private ConsultaService consultaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long idDentista;
    private Long idOutroDentista;
    private Long idRecepcionista;
    private Long idCliente;
    private Integer idEspecialidade;

    @BeforeEach
    void prepararDados() {
        consultaRepository.deleteAll();

        if (denteRepository.count() == 0) {
            List<Dente> dentes = new ArrayList<>();
            for (int quadrante = 1; quadrante <= 4; quadrante++) {
                for (int posicao = 1; posicao <= 8; posicao++) {
                    dentes.add(new Dente(quadrante * 10 + posicao, "Dente " + (quadrante * 10 + posicao),
                            quadrante <= 2 ? "SUPERIOR" : "INFERIOR", quadrante));
                }
            }
            denteRepository.saveAll(dentes);
        }
        idEspecialidade = especialidadeRepository.findAllByOrderByNomeAsc().stream()
                .findFirst()
                .orElseGet(() -> especialidadeRepository.save(new Especialidade(null, "Clínica Geral")))
                .getIdEspecialidade();

        // Usuarios criados pelo DatabaseSeeder
        idDentista = usuarioRepository.findByEmail("john@doe.com").map(Usuario::getIdUsuario).orElseThrow();
        idOutroDentista = usuarioRepository.findByEmail("ana.souza@lumina.com").map(Usuario::getIdUsuario).orElseThrow();
        idRecepcionista = usuarioRepository.findByEmail("recepcao@lumina.com").map(Usuario::getIdUsuario).orElseThrow();

        idCliente = clienteRepository.findByCpf("90090090011").map(Cliente::getIdCliente).orElseGet(() -> {
            Cliente cliente = new Cliente();
            cliente.setNome("Paciente Integração");
            cliente.setCpf("90090090011");
            cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
            cliente.setNumeroCelular("11988887777");
            return clienteRepository.save(cliente).getIdCliente();
        });
    }

    private Map<String, Object> corpo(LocalDate data, String inicio, String fim) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("idCliente", idCliente);
        corpo.put("idUsuario", idDentista);
        corpo.put("idEspecialidade", idEspecialidade);
        corpo.put("data", data.toString());
        corpo.put("horarioInicio", inicio);
        corpo.put("horarioFim", fim);
        corpo.put("observacoes", "Paciente relata sensibilidade");
        corpo.put("dentes", List.of(26, 16));
        corpo.put("tipoDente", "Primeiro Molar");
        corpo.put("pularOdontograma", false);
        return corpo;
    }

    private MockHttpServletRequestBuilder comoRecepcao(MockHttpServletRequestBuilder builder) {
        return builder.with(user("recepcao@lumina.com").roles("RECEPCIONISTA"));
    }

    private MvcResult cadastrar(Map<String, Object> corpo, int statusEsperado) throws Exception {
        return mockMvc.perform(comoRecepcao(post("/consultas"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpo)))
                .andExpect(status().is(statusEsperado))
                .andReturn();
    }

    private long cadastrarComSucesso(LocalDate data, String inicio, String fim) throws Exception {
        MvcResult resultado = cadastrar(corpo(data, inicio, fim), 201);
        return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    @DisplayName("Sem autenticação as rotas de agendamento retornam 401")
    void semAutenticacaoRetorna401() throws Exception {
        mockMvc.perform(post("/consultas").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpo(AMANHA, "09:00", "09:30"))))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/consultas/horarios-disponiveis")
                        .param("idUsuario", "1").param("data", AMANHA.toString()).param("duracaoMinutos", "30"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/especialidades")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Dentista não cadastra consulta (403)")
    void dentistaNaoCadastra() throws Exception {
        mockMvc.perform(post("/consultas").with(user("john@doe.com").roles("DENTISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpo(AMANHA, "09:00", "09:30"))))
                .andExpect(status().isForbidden());
        assertEquals(0, consultaRepository.count());
    }

    @Test
    @DisplayName("Cadastro válido persiste os campos adicionais e é devolvido igual pela listagem")
    void cadastroValidoPersisteCamposAdicionais() throws Exception {
        long id = cadastrarComSucesso(AMANHA, "09:00:00", "09:30:00");

        Integer dentesGravados = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM consulta_dente WHERE fk_consulta = ?", Integer.class, id);
        assertEquals(2, dentesGravados);

        Consulta salva = consultaRepository.findById(id).orElseThrow();
        assertEquals("AGENDADA", salva.getStatus());
        assertEquals(idEspecialidade, salva.getEspecialidade().getIdEspecialidade());

        MvcResult lista = mockMvc.perform(comoRecepcao(get("/consultas"))
                        .param("dataInicio", AMANHA.toString()).param("dataFim", AMANHA.toString()))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode consulta = objectMapper.readTree(lista.getResponse().getContentAsString()).get(0);
        assertEquals(id, consulta.get("id").asLong());
        assertEquals(idCliente, consulta.get("cliente").get("idCliente").asLong());
        assertEquals(idDentista, consulta.get("usuario").get("idUsuario").asLong());
        assertTrue(consulta.get("usuario").get("senha").isNull());
        assertEquals("AGENDADA", consulta.get("status").asText());
        assertEquals(30, consulta.get("duracaoMinutos").asInt());
        assertEquals("09:00:00", consulta.get("horarioInicio").asText());
        assertEquals("Paciente relata sensibilidade", consulta.get("observacoes").asText());
        assertEquals("[16,26]", consulta.get("dentes").toString());
        assertEquals("Primeiro Molar", consulta.get("tipoDente").asText());
        assertFalse(consulta.get("pularOdontograma").asBoolean());
        assertEquals(idEspecialidade, consulta.get("especialidade").get("idEspecialidade").asInt());
    }

    @Test
    @DisplayName("Hoje com início futuro é aceito; horário passado, duração inválida e término após 18:00 retornam 400")
    void validacoesDeHorario() throws Exception {
        cadastrarComSucesso(HOJE, "10:30", "11:00");

        MvcResult passado = cadastrar(corpo(HOJE, "09:00", "09:30"), 400);
        assertTrue(passado.getResponse().getContentAsString().contains("passado"));
        cadastrar(corpo(AMANHA, "09:00", "10:15"), 400);
        cadastrar(corpo(AMANHA, "17:30", "18:30"), 400);

        Map<String, Object> semEspecialidade = corpo(AMANHA, "09:00", "09:30");
        semEspecialidade.remove("idEspecialidade");
        mockMvc.perform(comoRecepcao(post("/consultas"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semEspecialidade)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.idEspecialidade").exists());

        mockMvc.perform(comoRecepcao(post("/consultas"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCliente\":1,\"idUsuario\":1,\"idEspecialidade\":1,\"data\":\"03/11/2026\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Sobreposição retorna 409 e horários consecutivos são aceitos")
    void sobreposicaoEConsecutivos() throws Exception {
        cadastrarComSucesso(AMANHA, "09:00", "09:30");

        mockMvc.perform(comoRecepcao(post("/consultas"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(corpo(AMANHA, "09:15", "09:45"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").exists());

        cadastrarComSucesso(AMANHA, "09:30", "10:00");
        cadastrarComSucesso(AMANHA, "08:30", "09:00");

        // Outro dentista no mesmo horário não conflita
        Map<String, Object> outroDentista = corpo(AMANHA, "09:00", "09:30");
        outroDentista.put("idUsuario", idOutroDentista);
        cadastrar(outroDentista, 201);
    }

    @Test
    @DisplayName("Dentista inválido retorna 400 e inexistente retorna 404")
    void dentistaInvalido() throws Exception {
        Map<String, Object> recepcionista = corpo(AMANHA, "09:00", "09:30");
        recepcionista.put("idUsuario", idRecepcionista);
        cadastrar(recepcionista, 400);

        Map<String, Object> inexistente = corpo(AMANHA, "09:00", "09:30");
        inexistente.put("idUsuario", 999_999);
        cadastrar(inexistente, 404);

        Map<String, Object> denteInvalido = corpo(AMANHA, "09:00", "09:30");
        denteInvalido.put("dentes", List.of(16, 16));
        cadastrar(denteInvalido, 400);
    }

    @Test
    @DisplayName("Horários disponíveis refletem consultas e o cancelamento lógico libera o horário")
    void cancelamentoLiberaHorario() throws Exception {
        long id = cadastrarComSucesso(AMANHA, "09:00", "09:30");

        String disponiveis = mockMvc.perform(comoRecepcao(get("/consultas/horarios-disponiveis"))
                        .param("idUsuario", idDentista.toString())
                        .param("data", AMANHA.toString())
                        .param("duracaoMinutos", "30"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(disponiveis.contains("\"horarioInicio\":\"09:00:00\""));
        assertFalse(disponiveis.contains("\"horarioInicio\":\"08:45:00\""));
        assertTrue(disponiveis.contains("\"horarioInicio\":\"08:30:00\""));
        assertTrue(disponiveis.contains("\"horarioInicio\":\"09:30:00\""));
        assertTrue(disponiveis.contains("\"horarioInicio\":\"17:30:00\",\"horarioFim\":\"18:00:00\""));

        mockMvc.perform(comoRecepcao(delete("/consultas/" + id))).andExpect(status().isNoContent());

        Consulta cancelada = consultaRepository.findById(id).orElseThrow();
        assertEquals("CANCELADA", cancelada.getStatus());

        String aposCancelamento = mockMvc.perform(comoRecepcao(get("/consultas/horarios-disponiveis"))
                        .param("idUsuario", idDentista.toString())
                        .param("data", AMANHA.toString())
                        .param("duracaoMinutos", "30"))
                .andReturn().getResponse().getContentAsString();
        assertTrue(aposCancelamento.contains("\"horarioInicio\":\"09:00:00\""));

        cadastrarComSucesso(AMANHA, "09:00", "09:30");
    }

    @Test
    @DisplayName("Reagendamento não conflita consigo mesmo e preserva paciente e dentista")
    void reagendamentoSemConflitoConsigoMesmo() throws Exception {
        long id = cadastrarComSucesso(AMANHA, "09:00", "09:30");

        mockMvc.perform(comoRecepcao(put("/consultas/" + id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"" + AMANHA + "\",\"horarioInicio\":\"09:15\",\"horarioFim\":\"09:45\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.horarioInicio").value("09:15:00"))
                .andExpect(jsonPath("$.cliente.idCliente").value(idCliente))
                .andExpect(jsonPath("$.usuario.idUsuario").value(idDentista))
                .andExpect(jsonPath("$.dentes.length()").value(2));

        long outra = cadastrarComSucesso(AMANHA, "10:00", "10:30");
        mockMvc.perform(comoRecepcao(put("/consultas/" + outra))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"" + AMANHA + "\",\"horarioInicio\":\"09:30\",\"horarioFim\":\"10:00\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Dentista não reagenda consulta de outro dentista e altera status das próprias")
    void permissoesDoDentista() throws Exception {
        long id = cadastrarComSucesso(AMANHA, "09:00", "09:30");

        mockMvc.perform(put("/consultas/" + id).with(user("ana.souza@lumina.com").roles("DENTISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"" + AMANHA + "\",\"horarioInicio\":\"11:00\",\"horarioFim\":\"11:30\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/consultas/" + id + "/status").with(user("john@doe.com").roles("DENTISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        mockMvc.perform(patch("/consultas/" + id + "/status").with(user("john@doe.com").roles("DENTISTA"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONCLUIDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("Requisições simultâneas para o mesmo horário: apenas uma é gravada")
    void requisicoesConcorrentes() throws Exception {
        int participantes = 4;
        ExecutorService executor = Executors.newFixedThreadPool(participantes);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Long>> resultados = new ArrayList<>();
        try {
            for (int i = 0; i < participantes; i++) {
                Callable<Long> tarefa = () -> {
                    largada.await();
                    ConsultaRequest request = new ConsultaRequest(idCliente, idDentista, idEspecialidade, AMANHA,
                            LocalTime.of(14, 0), LocalTime.of(14, 30), null, List.of(), null, true);
                    return consultaService.cadastrar(request).getIdConsulta();
                };
                resultados.add(executor.submit(tarefa));
            }
            largada.countDown();

            int sucessos = 0;
            int conflitos = 0;
            for (Future<Long> resultado : resultados) {
                try {
                    assertNotNull(resultado.get(30, TimeUnit.SECONDS));
                    sucessos++;
                } catch (ExecutionException e) {
                    assertInstanceOf(ConflitoHorarioException.class, e.getCause(), "Falha inesperada: " + e.getCause());
                    conflitos++;
                }
            }
            assertEquals(1, sucessos);
            assertEquals(participantes - 1, conflitos);
            assertEquals(1, consultaRepository.count());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Lista de especialidades e de dentistas ativos para seleção")
    void listasAuxiliares() throws Exception {
        mockMvc.perform(comoRecepcao(get("/especialidades")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idEspecialidade").exists());

        String dentistas = mockMvc.perform(comoRecepcao(get("/usuarios/dentistas")).param("size", "50"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(dentistas.contains("John Doe"));
        assertFalse(dentistas.contains("Helena Carvalho"), "Dentista inativa não deve ser oferecida");
        assertFalse(dentistas.contains("Recepção Lumina"));
        assertFalse(dentistas.contains("senha"));

        mockMvc.perform(comoRecepcao(get("/usuarios/dentistas")).param("nome", "beatriz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));

        mockMvc.perform(get("/usuarios/dentistas").with(user("john@doe.com").roles("DENTISTA")))
                .andExpect(status().isForbidden());
    }
}
