package com.lumina.backend.configure;

import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.ClienteConvenio;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.model.Dente;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.ConvenioRepository;
import com.lumina.backend.repository.DenteRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import com.lumina.backend.repository.PerfilRespository;
import com.lumina.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final String SENHA_PADRAO_HASH = "$2a$10$0/TKTGxdREbWaWjWYhwf6e9P1fPOAMMNqEnZgOG95jnSkHSfkkIrC";

    private final PerfilRespository perfilRepository;
    private final EstadoCivilRepository estadoCivilRepository;
    private final ConvenioRepository convenioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final ClienteConvenioRepository clienteConvenioRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final DenteRepository denteRepository;
    private final ConsultaRepository consultaRepository;

    public DatabaseSeeder(PerfilRespository perfilRepository,
                          EstadoCivilRepository estadoCivilRepository,
                          ConvenioRepository convenioRepository,
                          UsuarioRepository usuarioRepository,
                          ClienteRepository clienteRepository,
                          ClienteConvenioRepository clienteConvenioRepository,
                          EspecialidadeRepository especialidadeRepository,
                          DenteRepository denteRepository,
                          ConsultaRepository consultaRepository) {
        this.perfilRepository = perfilRepository;
        this.estadoCivilRepository = estadoCivilRepository;
        this.convenioRepository = convenioRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.clienteConvenioRepository = clienteConvenioRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.denteRepository = denteRepository;
        this.consultaRepository = consultaRepository;
    }

    @Override
    public void run(String... args) {
        try {
            seedPerfis();
            seedEstadosCivis();
            seedEspecialidades();
            seedDentes();
            List<Convenio> convenios = seedConvenios();
            seedUsuarios();
            seedClientes(convenios);
            seedConsultas();
        } catch (Exception e) {
            log.warn("Aviso ao executar DatabaseSeeder: {}", e.getMessage());
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
        List<Dente> dentes = new ArrayList<>();
        for (int quadrante = 1; quadrante <= 4; quadrante++) {
            String arcada = quadrante <= 2 ? "SUPERIOR" : "INFERIOR";
            for (int posicao = 1; posicao <= 8; posicao++) {
                int numero = quadrante * 10 + posicao;
                dentes.add(new Dente(numero, nomes[posicao - 1] + " (" + numero + ")", arcada, quadrante));
            }
        }
        denteRepository.saveAll(dentes);
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

    private List<Convenio> seedConvenios() {
        if (convenioRepository.count() == 0) {
            convenioRepository.saveAll(List.of(
                    new Convenio("Particular"),
                    new Convenio("Amil Dental"),
                    new Convenio("Bradesco Dental"),
                    new Convenio("SulAmérica Odonto")
            ));
        }
        return convenioRepository.findAll();
    }

    private void seedUsuarios() {
        List<Perfil> perfis = perfilRepository.findAll();
        Perfil perfilAdmin = perfis.stream().filter(p -> "Admin".equalsIgnoreCase(p.getNome())).findFirst().orElse(new Perfil(1, "Admin"));
        Perfil perfilDentista = perfis.stream().filter(p -> "Dentista".equalsIgnoreCase(p.getNome())).findFirst().orElse(new Perfil(2, "Dentista"));
        Perfil perfilRecepcao = perfis.stream().filter(p -> "Recepcionista".equalsIgnoreCase(p.getNome())).findFirst().orElse(new Perfil(3, "Recepcionista"));

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

    private void seedClientes(List<Convenio> convenios) {
        if (clienteRepository.count() > 0) {
            return;
        }

        record ClienteSeed(String nome, String cpf, String rg, LocalDate dataNascimento, String celular,
                           String email, char sexo, String naturalidade, int fkEstadoCivil,
                           String endereco, String cep, int indiceConvenio) {}

        List<ClienteSeed> seeds = List.of(
                new ClienteSeed("Carlos Silva", "12312312345", "123456789", LocalDate.of(1990, 5, 15), "11987654321", "carlos.silva@email.com", 'M', "São Paulo", 1, "Rua das Flores, 120", "01001000", 0),
                new ClienteSeed("Maria Oliveira", "23423423456", "234567890", LocalDate.of(1985, 8, 20), "11912345678", "maria.oliveira@email.com", 'F', "Campinas", 2, "Av. Paulista, 1500", "01310100", 1),
                new ClienteSeed("Lucas Santos", "34534534567", "345678901", LocalDate.of(2000, 11, 10), "11998877665", "lucas.santos@email.com", 'M', "Santos", 1, "Rua Augusta, 450", "01305000", 2),
                new ClienteSeed("Juliana Costa", "45645645678", "456789012", LocalDate.of(1995, 3, 25), "11977665544", "juliana.costa@email.com", 'F', "São Paulo", 1, "Alameda Santos, 800", "01419001", 0),
                new ClienteSeed("Enzo Oliveira", "56756756789", "567890123", LocalDate.of(2016, 4, 10), "11954684815", "enzo.oliveira@email.com", 'M', "Campinas", 1, "Av. Paulista, 1500", "01310100", 1),
                new ClienteSeed("Roberto Ferreira", "67867867890", "678901234", LocalDate.of(1978, 11, 3), "1133445566", "roberto.ferreira@email.com", 'M', "São Paulo", 2, "Rua da Consolação, 900", "01301000", 0)
        );

        int inseridos = 0;
        for (ClienteSeed s : seeds) {
            if (clienteRepository.findByCpf(s.cpf()).isEmpty() && clienteRepository.findByEmail(s.email()).isEmpty()) {
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
                Cliente salvo = clienteRepository.save(c);
                inseridos++;

                if (!convenios.isEmpty()) {
                    Convenio conv = convenios.get(Math.min(s.indiceConvenio(), convenios.size() - 1));
                    ClienteConvenio cc = new ClienteConvenio();
                    cc.setCliente(salvo);
                    cc.setConvenio(conv);
                    cc.setNumeroInscricao("INSC-" + s.cpf().substring(0, 6));
                    clienteConvenioRepository.save(cc);
                }
            }
        }
        if (inseridos > 0) {
            log.info("DatabaseSeeder: {} pacientes básicos inseridos no banco.", inseridos);
        }
    }

    private void seedConsultas() {
        if (consultaRepository.count() > 0) {
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

        // Semana vigente (ontem, hoje, amanhã, depois de amanhã)
        novasConsultas.add(criarConsultaSeed(clientes.get(0), dentistas.get(0), especialidades.get(0), hoje, LocalTime.of(9, 0), "CONCLUIDA", "Avaliação clínica periódica"));
        novasConsultas.add(criarConsultaSeed(clientes.get(1 % clientes.size()), dentistas.get(0), especialidades.get(1 % especialidades.size()), hoje, LocalTime.of(14, 30), "AGENDADA", "Restauração em resina composta"));
        novasConsultas.add(criarConsultaSeed(clientes.get(2 % clientes.size()), dentistas.get(1 % dentistas.size()), especialidades.get(0), hoje, LocalTime.of(11, 0), "AGENDADA", "Profilaxia e raspagem"));
        novasConsultas.add(criarConsultaSeed(clientes.get(0), dentistas.get(0), especialidades.get(0), hoje.plusDays(1), LocalTime.of(10, 0), "AGENDADA", "Retorno"));
        novasConsultas.add(criarConsultaSeed(clientes.get(1 % clientes.size()), dentistas.get(1 % dentistas.size()), especialidades.get(1 % especialidades.size()), hoje.plusDays(1), LocalTime.of(14, 0), "CONFIRMADA", "Acompanhamento"));
        novasConsultas.add(criarConsultaSeed(clientes.get(2 % clientes.size()), dentistas.get(0), especialidades.get(0), hoje.minusDays(1), LocalTime.of(10, 0), "CONCLUIDA", "Manutenção preventiva"));

        // Semana passada
        novasConsultas.add(criarConsultaSeed(clientes.get(0), dentistas.get(0), especialidades.get(0), hoje.minusDays(7), LocalTime.of(9, 0), "CONCLUIDA", "Profilaxia periódica"));
        novasConsultas.add(criarConsultaSeed(clientes.get(1 % clientes.size()), dentistas.get(1 % dentistas.size()), especialidades.get(1 % especialidades.size()), hoje.minusDays(7), LocalTime.of(15, 0), "CONCLUIDA", "Restauração"));
        novasConsultas.add(criarConsultaSeed(clientes.get(2 % clientes.size()), dentistas.get(0), especialidades.get(0), hoje.minusDays(8), LocalTime.of(10, 30), "CONCLUIDA", "Retorno pós-procedimento"));
        novasConsultas.add(criarConsultaSeed(clientes.get(0), dentistas.get(1 % dentistas.size()), especialidades.get(1 % especialidades.size()), hoje.minusDays(9), LocalTime.of(16, 0), "CANCELADA", "Cancelamento informado pelo paciente"));

        consultaRepository.saveAll(novasConsultas);
        log.info("DatabaseSeeder: {} consultas (semana vigente e anterior) inseridas.", novasConsultas.size());
    }

    private Consulta criarConsultaSeed(Cliente cliente, Usuario dentista, Especialidade especialidade,
                                       LocalDate data, LocalTime inicio, String status, String observacoes) {
        Consulta c = new Consulta();
        c.setCliente(cliente);
        c.setUsuario(dentista);
        c.setEspecialidade(especialidade);
        c.setData(data);
        c.setHorarioInicio(inicio);
        c.setHorarioFim(inicio.plusMinutes(45));
        c.setStatus(status);
        c.setObservacoes(observacoes);
        c.setPularOdontograma(true);
        c.setLembreteEnviado("CONCLUIDA".equals(status));
        return c;
    }
}
