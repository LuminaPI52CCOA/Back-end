package com.lumina.backend.configure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.ClienteConvenio;
import com.lumina.backend.model.Consulta;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga de dados de demonstração (usuários, pacientes e histórico de consultas).
 * Desabilitada automaticamente no perfil {@code prod} e controlável via {@code lumina.seed.demo.enabled}.
 */
@Slf4j
@Component
@Order(2)
@Profile("!prod")
@ConditionalOnProperty(name = "lumina.seed.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseSeeder implements CommandLineRunner {

    private static final String SENHA_PADRAO_HASH = "$2a$10$0/TKTGxdREbWaWjWYhwf6e9P1fPOAMMNqEnZgOG95jnSkHSfkkIrC";
    private static final String CLIENTES_DEMO_RESOURCE = "seed/clientes-demo.json";

    private static final LocalTime[] HORARIOS_INICIO = {
            LocalTime.of(8, 0),
            LocalTime.of(9, 0),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            LocalTime.of(13, 30),
            LocalTime.of(14, 30),
            LocalTime.of(15, 30),
            LocalTime.of(16, 30)
    };

    private static final String[] OBSERVACOES_PADRAO = {
            "Avaliação clínica e profilaxia periódica",
            "Acompanhamento ortodôntico mensal",
            "Restauração em resina composta",
            "Retorno pós-procedimento endodôntico",
            "Raspagem periodontal e orientação de higiene",
            "Clareamento dental supervisionado"
    };

    private final PerfilRepository perfilRepository;
    private final ConvenioRepository convenioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteConvenioRepository clienteConvenioRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final ConsultaRepository consultaRepository;
    private final ObjectMapper objectMapper;

    public DatabaseSeeder(PerfilRepository perfilRepository,
                          ConvenioRepository convenioRepository,
                          UsuarioRepository usuarioRepository,
                          ClienteRepository clienteRepository,
                          ClienteConvenioRepository clienteConvenioRepository,
                          EspecialidadeRepository especialidadeRepository,
                          ConsultaRepository consultaRepository,
                          ObjectMapper objectMapper) {
        this.perfilRepository = perfilRepository;
        this.convenioRepository = convenioRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.clienteConvenioRepository = clienteConvenioRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.consultaRepository = consultaRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) {
        try {
            List<Convenio> convenios = convenioRepository.findAll();
            seedUsuarios();
            seedClientes(convenios);
            seedConsultas();
        } catch (RuntimeException e) {
            log.warn("Aviso ao executar DatabaseSeeder: {}", e.getMessage());
        }
    }

    private void seedUsuarios() {
        List<Perfil> perfis = perfilRepository.findAll();
        Perfil perfilAdmin = encontrarPerfil(perfis, "Admin", 1);
        Perfil perfilDentista = encontrarPerfil(perfis, "Dentista", 2);
        Perfil perfilRecepcao = encontrarPerfil(perfis, "Recepcionista", 3);

        record UsuarioSeed(String nome, String email, Perfil perfil, String cpf, String cro, boolean ativo) {}

        List<UsuarioSeed> seeds = List.of(
                new UsuarioSeed("John Doe", "john@doe.com", perfilDentista, "12345678901", "SP-123456", true),
                new UsuarioSeed("Dra. Ana Maria Souza", "ana.souza@lumina.com", perfilDentista, "98765432100", "SP-654321", true),
                new UsuarioSeed("Administrador Lumina", "admin@lumina.com", perfilAdmin, "11122233344", null, true),
                new UsuarioSeed("Recepção Lumina", "recepcao@lumina.com", perfilRecepcao, "22233344455", null, true),
                new UsuarioSeed("Dr. Roberto Almeida", "roberto.almeida@lumina.com", perfilDentista, "33344455566", "SP-112233", true),
                new UsuarioSeed("Dra. Beatriz Ferreira", "beatriz.ferreira@lumina.com", perfilDentista, "44455566677", "SP-223344", true),
                new UsuarioSeed("Dr. Fernando Henrique Costa", "fernando.costa@lumina.com", perfilDentista, "55566677788", "SP-334455", true),
                new UsuarioSeed("Dra. Camila Rodrigues", "camila.rodrigues@lumina.com", perfilDentista, "66677788899", "SP-445566", true),
                new UsuarioSeed("Dr. Marcelo Ribeiro", "marcelo.ribeiro@lumina.com", perfilDentista, "77788899900", "SP-556677", true),
                new UsuarioSeed("Dra. Patrícia Mendes", "patricia.mendes@lumina.com", perfilDentista, "88899900011", "SP-667788", true),
                new UsuarioSeed("Dr. Gustavo Nascimento", "gustavo.nascimento@lumina.com", perfilDentista, "99900011122", "SP-778899", true),
                new UsuarioSeed("Dra. Helena Carvalho", "helena.carvalho@lumina.com", perfilDentista, "10120230344", "SP-889900", false)
        );

        int inseridos = 0;
        for (UsuarioSeed s : seeds) {
            if (usuarioRepository.findByEmail(s.email()).isEmpty()) {
                Usuario u = new Usuario();
                u.setNome(s.nome());
                u.setEmail(s.email());
                u.setSenha(SENHA_PADRAO_HASH);
                u.setPerfil(s.perfil());
                u.setCpf(s.cpf());
                u.setCro(s.cro());
                u.setAtivo(s.ativo());
                usuarioRepository.save(u);
                inseridos++;
            }
        }
        if (inseridos > 0) {
            log.info("DatabaseSeeder: {} novos usuarios/dentistas inseridos.", inseridos);
        }
    }

    private Perfil encontrarPerfil(List<Perfil> perfis, String nome, int idFallback) {
        return perfis.stream()
                .filter(p -> nome.equalsIgnoreCase(p.getNome()))
                .findFirst()
                .orElse(new Perfil(idFallback, nome));
    }

    private void seedClientes(List<Convenio> convenios) {
        List<ClienteSeed> seeds = carregarClientesDemo();

        int inseridos = 0;
        for (ClienteSeed s : seeds) {
            if (clienteRepository.findByCpf(s.cpf()).isEmpty() && clienteRepository.findByEmail(s.email()).isEmpty()) {
                Cliente salvo = clienteRepository.save(criarClienteEntidade(s));
                inseridos++;
                vincularConvenioInicial(salvo, convenios, s);
            }
        }
        if (inseridos > 0) {
            log.info("DatabaseSeeder: {} novos pacientes inseridos.", inseridos);
        }
    }

    private List<ClienteSeed> carregarClientesDemo() {
        ClassPathResource resource = new ClassPathResource(CLIENTES_DEMO_RESOURCE);
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<>() {});
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao carregar pacientes de demonstração", e);
        }
    }

    private Cliente criarClienteEntidade(ClienteSeed s) {
        Cliente c = new Cliente();
        c.setNome(s.nome());
        c.setCpf(s.cpf());
        c.setRg(s.rg());
        c.setDataNascimento(s.dataNascimento());
        c.setNumeroCelular(s.celular());
        c.setEmail(s.email());
        c.setSexo(s.sexo());
        c.setNaturalidade(s.naturalidade());
        c.setNacionalidade("Brasileira");
        c.setFkEstadoCivil(s.fkEstadoCivil());
        c.setEnderecoResidencial(s.endereco());
        c.setCep(s.cep());
        return c;
    }

    private void vincularConvenioInicial(Cliente cliente, List<Convenio> convenios, ClienteSeed s) {
        if (convenios.isEmpty()) {
            return;
        }
        Convenio conv = convenios.get(Math.min(s.indiceConvenio(), convenios.size() - 1));
        ClienteConvenio cc = new ClienteConvenio();
        cc.setCliente(cliente);
        cc.setConvenio(conv);
        cc.setNumeroInscricao("INSC-" + s.cpf().substring(0, 6));
        clienteConvenioRepository.save(cc);
    }

    private void seedConsultas() {
        if (consultaRepository.count() > 20) {
            return;
        }
        List<Usuario> dentistas = usuarioRepository.findAll().stream()
                .filter(u -> u.getPerfil() != null
                        && "Dentista".equalsIgnoreCase(u.getPerfil().getNome())
                        && !Boolean.FALSE.equals(u.getAtivo()))
                .toList();
        List<Cliente> clientes = clienteRepository.findAll();
        List<Especialidade> especialidades = especialidadeRepository.findAll();

        if (dentistas.isEmpty() || clientes.isEmpty() || especialidades.isEmpty()) {
            return;
        }

        LocalDate hoje = LocalDate.now();
        List<Consulta> novasConsultas = new ArrayList<>();
        int indiceCliente = 0;

        for (int diasAtras = 240; diasAtras >= 1; diasAtras--) {
            LocalDate data = hoje.minusDays(diasAtras);
            if (data.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            // Crescimento progressivo ao longo dos meses para refletir evolução real no dashboard
            int dentistasNoDia = diasAtras > 120 ? 1 : (diasAtras > 45 ? 2 : 3);
            int slotsPorDentista = data.getDayOfWeek() == DayOfWeek.SATURDAY
                    ? 5
                    : (diasAtras > 60 ? 6 : 7);

            for (int d = 0; d < Math.min(dentistasNoDia, dentistas.size()); d++) {
                Usuario dentista = dentistas.get((diasAtras + d) % dentistas.size());
                for (int s = 0; s < Math.min(slotsPorDentista, HORARIOS_INICIO.length); s++) {
                    Cliente cliente = clientes.get(indiceCliente % clientes.size());
                    indiceCliente++;
                    Especialidade esp = especialidades.get((diasAtras + s + d) % especialidades.size());
                    novasConsultas.add(criarConsultaDemo(data, dentista, cliente, esp, diasAtras, d, s));
                }
            }
        }

        consultaRepository.saveAll(novasConsultas);
        log.info("DatabaseSeeder: {} consultas históricas inseridas para o dashboard.", novasConsultas.size());
    }

    private Consulta criarConsultaDemo(LocalDate data, Usuario dentista, Cliente cliente,
                                     Especialidade esp, int diasAtras, int d, int slot) {
        LocalTime inicio = HORARIOS_INICIO[slot];
        String status = resolverStatusDemo(diasAtras, d, slot);

        Consulta c = new Consulta();
        c.setCliente(cliente);
        c.setUsuario(dentista);
        c.setEspecialidade(esp);
        c.setData(data);
        c.setHorarioInicio(inicio);
        c.setHorarioFim(inicio.plusMinutes(45));
        c.setStatus(status);
        c.setObservacoes(OBSERVACOES_PADRAO[(diasAtras + slot) % OBSERVACOES_PADRAO.length]);
        c.setPularOdontograma(true);
        c.setLembreteEnviado("CONCLUIDA".equals(status) || "CONFIRMADA".equals(status));
        return c;
    }

    private String resolverStatusDemo(int diasAtras, int d, int slot) {
        // Distribuição realista entre Concluída, Confirmada, Agendada e Cancelada
        int mod = (diasAtras * 7 + d * 3 + slot) % 16;
        if (mod == 0) {
            return "CANCELADA";
        }
        if (diasAtras <= 3 && mod <= 4) {
            return "AGENDADA";
        }
        if (diasAtras <= 10 && mod <= 5) {
            return "CONFIRMADA";
        }
        if (mod == 1) {
            return "CONFIRMADA";
        }
        return "CONCLUIDA";
    }

    private record ClienteSeed(
            String nome,
            String cpf,
            String rg,
            LocalDate dataNascimento,
            String celular,
            String email,
            char sexo,
            String naturalidade,
            int fkEstadoCivil,
            String endereco,
            String cep,
            int indiceConvenio
    ) {}
}
