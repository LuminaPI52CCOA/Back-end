package com.lumina.backend.configure;

import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.ClienteConvenio;
import com.lumina.backend.model.Convenio;
import com.lumina.backend.model.EstadoCivil;
import com.lumina.backend.model.Perfil;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteConvenioRepository;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConvenioRepository;
import com.lumina.backend.repository.EstadoCivilRepository;
import com.lumina.backend.repository.PerfilRespository;
import com.lumina.backend.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

    public DatabaseSeeder(PerfilRespository perfilRepository,
                          EstadoCivilRepository estadoCivilRepository,
                          ConvenioRepository convenioRepository,
                          UsuarioRepository usuarioRepository,
                          ClienteRepository clienteRepository,
                          ClienteConvenioRepository clienteConvenioRepository) {
        this.perfilRepository = perfilRepository;
        this.estadoCivilRepository = estadoCivilRepository;
        this.convenioRepository = convenioRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.clienteConvenioRepository = clienteConvenioRepository;
    }

    @Override
    public void run(String... args) {
        try {
            seedPerfis();
            seedEstadosCivis();
            List<Convenio> convenios = seedConvenios();
            seedUsuarios();
            seedClientes(convenios);
        } catch (Exception e) {
            log.warn("Aviso ao executar DatabaseSeeder: {}", e.getMessage());
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
        record ClienteSeed(String nome, String cpf, String rg, LocalDate dataNascimento, String celular,
                           String email, char sexo, String naturalidade, int fkEstadoCivil,
                           String endereco, String cep, int indiceConvenio) {}

        List<ClienteSeed> seeds = List.of(
                new ClienteSeed("Carlos Silva", "12312312345", "123456789", LocalDate.of(1990, 5, 15), "11987654321", "carlos.silva@email.com", 'M', "São Paulo", 1, "Rua das Flores, 120", "01001000", 0),
                new ClienteSeed("Maria Oliveira", "23423423456", "234567890", LocalDate.of(1985, 8, 20), "11912345678", "maria.oliveira@email.com", 'F', "Campinas", 2, "Av. Paulista, 1500", "01310100", 1),
                new ClienteSeed("Lucas Santos", "34534534567", "345678901", LocalDate.of(2000, 11, 10), "11998877665", "lucas.santos@email.com", 'M', "Santos", 1, "Rua Augusta, 450", "01305000", 2),
                new ClienteSeed("Juliana Costa", "45645645678", "456789012", LocalDate.of(1995, 3, 25), "11977665544", "juliana.costa@email.com", 'F', "São Paulo", 1, "Alameda Santos, 800", "01419001", 0),
                new ClienteSeed("Adriana Pereira", "50150150101", "501234561", LocalDate.of(1988, 2, 14), "11981112201", "adriana.pereira@email.com", 'F', "São Paulo", 2, "Rua Oscar Freire, 210", "01426000", 1),
                new ClienteSeed("Bruno Gonçalves", "50250250202", "501234562", LocalDate.of(1992, 7, 9), "11981112202", "bruno.goncalves@email.com", 'M', "Guarulhos", 1, "Rua Voluntários da Pátria, 900", "02010000", 0),
                new ClienteSeed("Carolina Nogueira", "50350350303", "501234563", LocalDate.of(2001, 1, 30), "11981112203", "carolina.nogueira@email.com", 'F', "São Paulo", 1, "Rua Vergueiro, 2100", "04101000", 3),
                new ClienteSeed("Daniel Martins", "50450450404", "501234564", LocalDate.of(1975, 11, 22), "11981112204", "daniel.martins@email.com", 'M', "Sorocaba", 2, "Av. Ibirapuera, 1800", "04028001", 2),
                new ClienteSeed("Eduarda Barbosa", "50550550505", "501234565", LocalDate.of(2016, 4, 18), "11981112205", "eduarda.barbosa@email.com", 'F', "São Paulo", 1, "Rua Domingos de Morais, 320", "04010000", 1),
                new ClienteSeed("Felipe Azevedo", "50650650606", "501234566", LocalDate.of(1982, 9, 5), "11981112206", "felipe.azevedo@email.com", 'M', "Santo André", 3, "Rua Bela Cintra, 740", "01415000", 0),
                new ClienteSeed("Gabriela Rocha", "50750750707", "501234567", LocalDate.of(1997, 12, 12), "11981112207", "gabriela.rocha@email.com", 'F', "São Bernardo", 1, "Rua Haddock Lobo, 510", "01414001", 3),
                new ClienteSeed("Henrique Dias", "50850850808", "501234568", LocalDate.of(1960, 6, 28), "11981112208", "henrique.dias@email.com", 'M', "São Paulo", 2, "Av. Brigadeiro Luís Antônio, 1200", "01318001", 2),
                new ClienteSeed("Isabela Cardoso", "50950950909", "501234569", LocalDate.of(2018, 8, 3), "11981112209", "isabela.cardoso@email.com", 'F', "Campinas", 1, "Rua da Consolação, 1100", "01302000", 1),
                new ClienteSeed("João Pedro Teixeira", "51051051010", "501234570", LocalDate.of(1994, 3, 17), "11981112210", "joao.teixeira@email.com", 'M', "São Paulo", 1, "Rua Harmonia, 77", "05435000", 0),
                new ClienteSeed("Larissa Monteiro", "51151151111", "501234571", LocalDate.of(1989, 10, 25), "11981112211", "larissa.monteiro@email.com", 'F', "Jundiaí", 2, "Rua Teodoro Sampaio, 1400", "05406100", 2),
                new ClienteSeed("Matheus Moreira", "51251251212", "501234572", LocalDate.of(1999, 1, 8), "11981112212", "matheus.moreira@email.com", 'M', "São Paulo", 1, "Rua Cardeal Arcoverde, 600", "05408000", 0),
                new ClienteSeed("Natália Freitas", "51351351313", "501234573", LocalDate.of(1963, 5, 19), "11981112213", "natalia.freitas@email.com", 'F', "Santos", 4, "Av. Rebouças, 2300", "05402300", 3),
                new ClienteSeed("Otávio Vieira", "51451451414", "501234574", LocalDate.of(1980, 12, 1), "11981112214", "otavio.vieira@email.com", 'M', "São Paulo", 2, "Rua Estados Unidos, 430", "01427000", 1),
                new ClienteSeed("Priscila Barros", "51551551515", "501234575", LocalDate.of(1993, 7, 14), "11981112215", "priscila.barros@email.com", 'F', "Osasco", 1, "Rua Pamplona, 890", "01405001", 0),
                new ClienteSeed("Rafael Cavalcanti", "51651651616", "501234576", LocalDate.of(1978, 4, 2), "11981112216", "rafael.cavalcanti@email.com", 'M', "Recife", 2, "Alameda Jaú, 350", "01420000", 2),
                new ClienteSeed("Sabrina Campos", "51751751717", "501234577", LocalDate.of(2003, 9, 21), "11981112217", "sabrina.campos@email.com", 'F', "São Paulo", 1, "Rua Tutóia, 620", "04007003", 0),
                new ClienteSeed("Thiago Correia", "51851851818", "501234578", LocalDate.of(1991, 2, 27), "11981112218", "thiago.correia@email.com", 'M', "São Paulo", 2, "Rua Joaquim Floriano, 500", "04534002", 1),
                new ClienteSeed("Vanessa Araújo", "51951951919", "501234579", LocalDate.of(1958, 11, 11), "11981112219", "vanessa.araujo@email.com", 'F', "Ribeirão Preto", 4, "Av. Faria Lima, 3100", "01452000", 3),
                new ClienteSeed("William Pinto", "52052052020", "501234580", LocalDate.of(1986, 6, 6), "11981112220", "william.pinto@email.com", 'M', "São Paulo", 3, "Rua Clélia, 1050", "05042000", 0),
                new ClienteSeed("Arthur Almeida Gomes", "60010370739", "600010191", LocalDate.of(1969, 2, 2), "11970011023", "arthur.gomes.1@email.com", 'M', "Campinas", 2, "Av. Brasil, 107", "01430000", 1),
                new ClienteSeed("Heitor Batista Martins", "60020741468", "600020382", LocalDate.of(1970, 3, 3), "11970021046", "heitor.martins.2@email.com", 'M', "Santos", 3, "Rua Frei Caneca, 114", "01307001", 2),
                new ClienteSeed("Bernardo Cavalcanti Nogueira", "60031112197", "600030573", LocalDate.of(1971, 4, 4), "11970031069", "bernardo.nogueira.3@email.com", 'M', "Guarulhos", 1, "Rua Augusta, 121", "01305100", 3),
                new ClienteSeed("Davi Dantas Reis", "60041482926", "600040764", LocalDate.of(1950, 5, 5), "11970041092", "davi.reis.4@email.com", 'M', "Sorocaba", 2, "Alameda Itu, 128", "01421000", 0),
                new ClienteSeed("Lorenzo Fonseca Souza", "60051853655", "600050955", LocalDate.of(2020, 6, 6), "11970051115", "lorenzo.souza.5@email.com", 'M', "Santo André", 1, "Rua Peixoto Gomide, 135", "01409001", 1),
                new ClienteSeed("Théo Barbosa Leite", "60062224384", "600061146", LocalDate.of(1974, 7, 7), "11970061138", "theo.leite.6@email.com", 'M', "São Bernardo do Campo", 1, "Av. Angélica, 142", "01227200", 2),
                new ClienteSeed("Pedro Carvalho Monteiro", "60072595113", "600071337", LocalDate.of(1975, 8, 8), "11970071161", "pedro.monteiro.7@email.com", 'M', "Osasco", 2, "Rua Maranhão, 149", "01240000", 3),
                new ClienteSeed("Gabriel Cruz Pereira", "60082965842", "600081528", LocalDate.of(1976, 9, 9), "11970081184", "gabriel.pereira.8@email.com", 'M', "Ribeirão Preto", 3, "Rua Sergipe, 156", "01243001", 0),
                new ClienteSeed("Enzo Ferreira Sampaio", "60093336571", "600091719", LocalDate.of(1955, 10, 10), "11970091207", "enzo.sampaio.9@email.com", 'M', "São José dos Campos", 4, "Av. Higienópolis, 163", "01238001", 1),
                new ClienteSeed("Matheus Araújo Vieira", "60103707300", "600101900", LocalDate.of(2016, 11, 11), "11970101230", "matheus.vieira.10@email.com", 'M', "Jundiaí", 1, "Rua Cardoso de Almeida, 170", "05013001", 2),
                new ClienteSeed("Lucas Campos Martins", "60114078039", "600112091", LocalDate.of(1979, 12, 12), "11970111253", "lucas.martins.11@email.com", 'M', "Piracicaba", 3, "Rua Homem de Melo, 177", "05007001", 3),
                new ClienteSeed("Benjamin Costa Nogueira", "60124448768", "600122282", LocalDate.of(1980, 1, 13), "11970121276", "benjamin.nogueira.12@email.com", 'M', "Bauru", 1, "Rua Apinajés, 184", "05017000", 0),
                new ClienteSeed("Nicolas Farias Reis", "60134819497", "600132473", LocalDate.of(1981, 2, 14), "11970131299", "nicolas.reis.13@email.com", 'M', "Mogi das Cruzes", 2, "Av. Sumaré, 191", "05016090", 1),
                new ClienteSeed("Guilherme Alves Souza", "60145180226", "600142664", LocalDate.of(1960, 3, 15), "11970141322", "guilherme.souza.14@email.com", 'M', "Barueri", 2, "Rua Cayowaá, 198", "05018000", 2),
                new ClienteSeed("Rafael Borges Leite", "60155550955", "600152855", LocalDate.of(2021, 4, 16), "11970151345", "rafael.leite.15@email.com", 'M', "São Paulo", 1, "Rua Fradique Coutinho, 205", "05416010", 3),
                new ClienteSeed("Joaquim Coelho Monteiro", "60165921684", "600163046", LocalDate.of(1984, 5, 17), "11970161368", "joaquim.monteiro.16@email.com", 'M', "Campinas", 2, "Rua dos Pinheiros, 212", "05422001", 0),
                new ClienteSeed("Samuel Dias Pereira", "60176292413", "600173237", LocalDate.of(1985, 6, 18), "11970171391", "samuel.pereira.17@email.com", 'M', "Santos", 3, "Rua Girassol, 219", "05433001", 1),
                new ClienteSeed("Enzo Gabriel Freitas Sampaio", "60186663142", "600183428", LocalDate.of(1986, 7, 19), "11970181414", "enzo.gabriel.sampaio.18@email.com", 'M', "Guarulhos", 1, "Rua Aspicuelta, 226", "05433010", 2),
                new ClienteSeed("João Miguel Barros Vieira", "60197033871", "600193619", LocalDate.of(1946, 8, 20), "11970191437", "joao.miguel.vieira.19@email.com", 'M', "Sorocaba", 4, "Av. Pedroso de Morais, 233", "05419000", 3),
                new ClienteSeed("Henrique Castro Marques", "60207404600", "600203800", LocalDate.of(2017, 9, 21), "11970201460", "henrique.marques.20@email.com", 'M', "Santo André", 1, "Rua Joaquim Antunes, 240", "05415010", 0),
                new ClienteSeed("Gustavo Cunha Nogueira", "60217775339", "600213991", LocalDate.of(1989, 10, 22), "11970211483", "gustavo.nogueira.21@email.com", 'M', "São Bernardo do Campo", 1, "Rua Mateus Grou, 247", "05415000", 1),
                new ClienteSeed("Murilo Figueiredo Reis", "60228146068", "600224182", LocalDate.of(1990, 11, 23), "11970221506", "murilo.reis.22@email.com", 'M', "Osasco", 2, "Rua Afonso Braz, 254", "04511011", 2),
                new ClienteSeed("Pedro Henrique Azevedo Souza", "60238516797", "600234373", LocalDate.of(1991, 12, 24), "11970231529", "pedro.henrique.souza.23@email.com", 'M', "Ribeirão Preto", 3, "Rua Diogo Jácome, 261", "04512001", 3),
                new ClienteSeed("Pietro Cardoso Leite", "60248887526", "600244564", LocalDate.of(1951, 1, 25), "11970241552", "pietro.leite.24@email.com", 'M', "São José dos Campos", 2, "Av. Santo Amaro, 268", "04506000", 0),
                new ClienteSeed("Lucca Coutinho Monteiro", "60259258255", "600254755", LocalDate.of(2022, 2, 26), "11970251575", "lucca.monteiro.25@email.com", 'M', "Jundiaí", 1, "Rua Clodomiro Amazonas, 275", "04537011", 1),
                new ClienteSeed("Felipe Fernandes Pereira", "60269628984", "600264946", LocalDate.of(1994, 3, 27), "11970261598", "felipe.pereira.26@email.com", 'M', "Piracicaba", 3, "Rua João Cachoeira, 282", "04535001", 2),
                new ClienteSeed("João Pedro Andrade Sampaio", "60279999713", "600275137", LocalDate.of(1995, 4, 28), "11970271621", "joao.pedro.sampaio.27@email.com", 'M', "Bauru", 1, "Rua Bandeira Paulista, 289", "04532001", 3),
                new ClienteSeed("Isaac Brito Vieira", "60280360442", "600285328", LocalDate.of(1996, 5, 1), "11970281644", "isaac.vieira.28@email.com", 'M', "Mogi das Cruzes", 2, "Rua Tabapuã, 296", "04533001", 0),
                new ClienteSeed("Benício Correia Marques", "60290731171", "600295519", LocalDate.of(1956, 6, 2), "11970291667", "benicio.marques.29@email.com", 'M', "Barueri", 4, "Av. Horácio Lafer, 303", "04538082", 1),
                new ClienteSeed("Daniel Duarte Neves", "60301101900", "600305700", LocalDate.of(2018, 7, 3), "11970301690", "daniel.neves.30@email.com", 'M', "São Paulo", 1, "Rua das Palmeiras, 310", "01226010", 2),
                new ClienteSeed("Anthony Almeida Reis", "60311472639", "600315891", LocalDate.of(1999, 8, 4), "11970311713", "anthony.reis.31@email.com", 'M', "Campinas", 2, "Av. Brasil, 317", "01430000", 3),
                new ClienteSeed("Leonardo Batista Souza", "60321843368", "600326082", LocalDate.of(2000, 9, 5), "11970321736", "leonardo.souza.32@email.com", 'M', "Santos", 3, "Rua Frei Caneca, 324", "01307001", 0),
                new ClienteSeed("Davi Lucca Cavalcanti Leite", "60332214097", "600336273", LocalDate.of(2001, 10, 6), "11970331759", "davi.lucca.leite.33@email.com", 'M', "Guarulhos", 1, "Rua Augusta, 331", "01305100", 1),
                new ClienteSeed("Bryan Dantas Monteiro", "60342584826", "600346464", LocalDate.of(1961, 11, 7), "11970341782", "bryan.monteiro.34@email.com", 'M', "Sorocaba", 2, "Alameda Itu, 338", "01421000", 2),
                new ClienteSeed("Eduardo Fonseca Pereira", "60352955555", "600356655", LocalDate.of(2023, 12, 8), "11970351805", "eduardo.pereira.35@email.com", 'M', "Santo André", 1, "Rua Peixoto Gomide, 345", "01409001", 3),
                new ClienteSeed("João Lucas Barbosa Sampaio", "60363326284", "600366846", LocalDate.of(2004, 1, 9), "11970361828", "joao.lucas.sampaio.36@email.com", 'M', "São Bernardo do Campo", 1, "Av. Angélica, 352", "01227200", 0),
                new ClienteSeed("Victor Carvalho Vieira", "60373697013", "600377037", LocalDate.of(2005, 2, 10), "11970371851", "victor.vieira.37@email.com", 'M', "Osasco", 1, "Rua Maranhão, 359", "01240000", 1),
                new ClienteSeed("João Cruz Marques", "60384067742", "600387228", LocalDate.of(2006, 3, 11), "11970381874", "joao.marques.38@email.com", 'M', "Ribeirão Preto", 1, "Rua Sergipe, 366", "01243001", 2),
                new ClienteSeed("Cauã Ferreira Neves", "60394438471", "600397419", LocalDate.of(1947, 4, 12), "11970391897", "caua.neves.39@email.com", 'M', "São José dos Campos", 4, "Av. Higienópolis, 373", "01238001", 3),
                new ClienteSeed("Antônio Araújo Ramos", "60404809200", "600407600", LocalDate.of(2019, 5, 13), "11970401920", "antonio.ramos.40@email.com", 'M', "Jundiaí", 1, "Rua Cardoso de Almeida, 380", "05013001", 0),
                new ClienteSeed("Vicente Campos Souza", "60415179939", "600417791", LocalDate.of(2009, 6, 14), "11970411943", "vicente.souza.41@email.com", 'M', "Piracicaba", 1, "Rua Homem de Melo, 387", "05007001", 1),
                new ClienteSeed("Caleb Costa Leite", "60425540668", "600427982", LocalDate.of(2010, 7, 15), "11970421966", "caleb.leite.42@email.com", 'M', "Bauru", 1, "Rua Apinajés, 394", "05017000", 2),
                new ClienteSeed("Gael Farias Monteiro", "60435911397", "600438173", LocalDate.of(2011, 8, 16), "11970431989", "gael.monteiro.43@email.com", 'M', "Mogi das Cruzes", 1, "Av. Sumaré, 401", "05016090", 3),
                new ClienteSeed("Bento Alves Pereira", "60446282126", "600448364", LocalDate.of(1952, 9, 17), "11970442012", "bento.pereira.44@email.com", 'M', "Barueri", 2, "Rua Cayowaá, 408", "05018000", 0),
                new ClienteSeed("Caio Borges Sampaio", "60456652855", "600458555", LocalDate.of(2015, 10, 18), "11970452035", "caio.sampaio.45@email.com", 'M', "São Paulo", 1, "Rua Fradique Coutinho, 415", "05416010", 1),
                new ClienteSeed("Emanuel Coelho Vieira", "60467023584", "600468746", LocalDate.of(1970, 11, 19), "11970462058", "emanuel.vieira.46@email.com", 'M', "Campinas", 2, "Rua dos Pinheiros, 422", "05422001", 2),
                new ClienteSeed("Vinícius Dias Marques", "60477394313", "600478937", LocalDate.of(1971, 12, 20), "11970472081", "vinicius.marques.47@email.com", 'M', "Santos", 3, "Rua Girassol, 429", "05433001", 3),
                new ClienteSeed("João Guilherme Freitas Neves", "60487765042", "600489128", LocalDate.of(1972, 1, 21), "11970482104", "joao.guilherme.neves.48@email.com", 'M', "Guarulhos", 1, "Rua Aspicuelta, 436", "05433010", 0),
                new ClienteSeed("Davi Lucas Barros Ramos", "60498135771", "600499319", LocalDate.of(1957, 2, 22), "11970492127", "davi.lucas.ramos.49@email.com", 'M', "Sorocaba", 4, "Av. Pedroso de Morais, 443", "05419000", 1),
                new ClienteSeed("Noah Castro Soares", "60508506500", "600509500", LocalDate.of(2020, 3, 23), "11970502150", "noah.soares.50@email.com", 'M', "Santo André", 1, "Rua Joaquim Antunes, 450", "05415010", 2),
                new ClienteSeed("João Gabriel Cunha Leite", "60518877239", "600519691", LocalDate.of(1975, 4, 24), "11970512173", "joao.gabriel.leite.51@email.com", 'M', "São Bernardo do Campo", 1, "Rua Mateus Grou, 457", "05415000", 3),
                new ClienteSeed("João Victor Figueiredo Monteiro", "60529247968", "600529882", LocalDate.of(1976, 5, 25), "11970522196", "joao.victor.monteiro.52@email.com", 'M', "Osasco", 2, "Rua Afonso Braz, 464", "04511011", 0),
                new ClienteSeed("Luiz Miguel Azevedo Pereira", "60539618697", "600530073", LocalDate.of(1977, 6, 26), "11970532219", "luiz.miguel.pereira.53@email.com", 'M', "Ribeirão Preto", 3, "Rua Diogo Jácome, 471", "04512001", 1),
                new ClienteSeed("Francisco Cardoso Sampaio", "60549989426", "600540264", LocalDate.of(1962, 7, 27), "11970542242", "francisco.sampaio.54@email.com", 'M', "São José dos Campos", 2, "Av. Santo Amaro, 478", "04506000", 2),
                new ClienteSeed("Kaique Coutinho Vieira", "60550350155", "600550455", LocalDate.of(2016, 8, 28), "11970552265", "kaique.vieira.55@email.com", 'M', "Jundiaí", 1, "Rua Clodomiro Amazonas, 485", "04537011", 3),
                new ClienteSeed("Otávio Fernandes Marques", "60560720884", "600560646", LocalDate.of(1980, 9, 1), "11970562288", "otavio.marques.56@email.com", 'M', "Piracicaba", 3, "Rua João Cachoeira, 492", "04535001", 0),
                new ClienteSeed("Augusto Andrade Neves", "60571091613", "600570837", LocalDate.of(1981, 10, 2), "11970572311", "augusto.neves.57@email.com", 'M', "Bauru", 1, "Rua Bandeira Paulista, 499", "04532001", 1),
                new ClienteSeed("Levi Brito Ramos", "60581462342", "600581028", LocalDate.of(1982, 11, 3), "11970582334", "levi.ramos.58@email.com", 'M', "Mogi das Cruzes", 2, "Rua Tabapuã, 506", "04533001", 2),
                new ClienteSeed("Yuri Correia Soares", "60591833071", "600591219", LocalDate.of(1948, 12, 4), "11970592357", "yuri.soares.59@email.com", 'M', "Barueri", 4, "Av. Horácio Lafer, 513", "04538082", 3),
                new ClienteSeed("Enrico Duarte Leal", "60602203800", "600601400", LocalDate.of(2021, 1, 5), "11970602380", "enrico.leal.60@email.com", 'M', "São Paulo", 1, "Rua das Palmeiras, 520", "01226010", 0),
                new ClienteSeed("Thiago Almeida Monteiro", "60612574539", "600611591", LocalDate.of(1985, 2, 6), "11970612403", "thiago.monteiro.61@email.com", 'M', "Campinas", 2, "Av. Brasil, 527", "01430000", 1),
                new ClienteSeed("Ian Batista Pereira", "60622945268", "600621782", LocalDate.of(1986, 3, 7), "11970622426", "ian.pereira.62@email.com", 'M', "Santos", 3, "Rua Frei Caneca, 534", "01307001", 2),
                new ClienteSeed("Victor Hugo Cavalcanti Sampaio", "60633315997", "600631973", LocalDate.of(1987, 4, 8), "11970632449", "victor.hugo.sampaio.63@email.com", 'M', "Guarulhos", 1, "Rua Augusta, 541", "01305100", 3),
                new ClienteSeed("Thomas Dantas Vieira", "60643686726", "600642164", LocalDate.of(1953, 5, 9), "11970642472", "thomas.vieira.64@email.com", 'M', "Sorocaba", 2, "Alameda Itu, 548", "01421000", 0),
                new ClienteSeed("Henry Fonseca Marques", "60654057455", "600652355", LocalDate.of(2017, 6, 10), "11970652495", "henry.marques.65@email.com", 'M', "Santo André", 1, "Rua Peixoto Gomide, 555", "01409001", 1),
                new ClienteSeed("Luiz Felipe Barbosa Neves", "60664428184", "600662546", LocalDate.of(1990, 7, 11), "11970662518", "luiz.felipe.neves.66@email.com", 'M', "São Bernardo do Campo", 1, "Av. Angélica, 562", "01227200", 2),
                new ClienteSeed("Ryan Carvalho Ramos", "60674798913", "600672737", LocalDate.of(1991, 8, 12), "11970672541", "ryan.ramos.67@email.com", 'M', "Osasco", 2, "Rua Maranhão, 569", "01240000", 3),
                new ClienteSeed("Arthur Miguel Cruz Soares", "60685169642", "600682928", LocalDate.of(1992, 9, 13), "11970682564", "arthur.miguel.soares.68@email.com", 'M', "Ribeirão Preto", 3, "Rua Sergipe, 576", "01243001", 0),
                new ClienteSeed("Davi Luiz Ferreira Leal", "60695530371", "600693119", LocalDate.of(1958, 10, 14), "11970692587", "davi.luiz.leal.69@email.com", 'M', "São José dos Campos", 4, "Av. Higienópolis, 583", "01238001", 1),
                new ClienteSeed("Nathan Araújo Miranda", "60705901100", "600703300", LocalDate.of(2022, 11, 15), "11970702610", "nathan.miranda.70@email.com", 'M', "Jundiaí", 1, "Rua Cardoso de Almeida, 590", "05013001", 2),
                new ClienteSeed("Pedro Lucas Campos Pereira", "60716271839", "600713491", LocalDate.of(1995, 12, 16), "11970712633", "pedro.lucas.pereira.71@email.com", 'M', "Piracicaba", 3, "Rua Homem de Melo, 597", "05007001", 3),
                new ClienteSeed("Davi Miguel Costa Sampaio", "60726642568", "600723682", LocalDate.of(1996, 1, 17), "11970722656", "davi.miguel.sampaio.72@email.com", 'M', "Bauru", 1, "Rua Apinajés, 604", "05017000", 0),
                new ClienteSeed("Raul Farias Vieira", "60737013297", "600733873", LocalDate.of(1997, 2, 18), "11970732679", "raul.vieira.73@email.com", 'M', "Mogi das Cruzes", 2, "Av. Sumaré, 611", "05016090", 1),
                new ClienteSeed("Pedro Miguel Alves Marques", "60747384026", "600744064", LocalDate.of(1963, 3, 19), "11970742702", "pedro.miguel.marques.74@email.com", 'M', "Barueri", 2, "Rua Cayowaá, 618", "05018000", 2),
                new ClienteSeed("Luiz Henrique Borges Neves", "60757754755", "600754255", LocalDate.of(2018, 4, 20), "11970752725", "luiz.henrique.neves.75@email.com", 'M', "São Paulo", 1, "Rua Fradique Coutinho, 625", "05416010", 3),
                new ClienteSeed("Helena Coelho Ramos", "60768125484", "600764446", LocalDate.of(2000, 5, 21), "11970762748", "helena.ramos.76@email.com", 'F', "Campinas", 2, "Rua dos Pinheiros, 632", "05422001", 0),
                new ClienteSeed("Alice Dias Soares", "60778496213", "600774637", LocalDate.of(2001, 6, 22), "11970772771", "alice.soares.77@email.com", 'F', "Santos", 1, "Rua Girassol, 639", "05433001", 1),
                new ClienteSeed("Laura Freitas Leal", "60788866942", "600784828", LocalDate.of(2002, 7, 23), "11970782794", "laura.leal.78@email.com", 'F', "Guarulhos", 1, "Rua Aspicuelta, 646", "05433010", 2),
                new ClienteSeed("Manuela Barros Miranda", "60799237671", "600795019", LocalDate.of(1949, 8, 24), "11970792817", "manuela.miranda.79@email.com", 'F', "Sorocaba", 4, "Av. Pedroso de Morais, 653", "05419000", 3),
                new ClienteSeed("Valentina Castro Peixoto", "60809608400", "600805200", LocalDate.of(2023, 9, 25), "11970802840", "valentina.peixoto.80@email.com", 'F', "Santo André", 1, "Rua Joaquim Antunes, 660", "05415010", 0),
                new ClienteSeed("Sophia Cunha Sampaio", "60819979139", "600815391", LocalDate.of(2005, 10, 26), "11970812863", "sophia.sampaio.81@email.com", 'F', "São Bernardo do Campo", 1, "Rua Mateus Grou, 667", "05415000", 1),
                new ClienteSeed("Isabella Figueiredo Vieira", "60820349868", "600825582", LocalDate.of(2006, 11, 27), "11970822886", "isabella.vieira.82@email.com", 'F', "Osasco", 1, "Rua Afonso Braz, 674", "04511011", 2),
                new ClienteSeed("Heloísa Azevedo Marques", "60830710597", "600835773", LocalDate.of(2007, 12, 28), "11970832909", "heloisa.marques.83@email.com", 'F', "Ribeirão Preto", 1, "Rua Diogo Jácome, 681", "04512001", 3),
                new ClienteSeed("Luiza Cardoso Neves", "60841081326", "600845964", LocalDate.of(1954, 1, 1), "11970842932", "luiza.neves.84@email.com", 'F', "São José dos Campos", 2, "Av. Santo Amaro, 688", "04506000", 0),
                new ClienteSeed("Júlia Coutinho Ramos", "60851452055", "600856155", LocalDate.of(2019, 2, 2), "11970852955", "julia.ramos.85@email.com", 'F', "Jundiaí", 1, "Rua Clodomiro Amazonas, 695", "04537011", 1),
                new ClienteSeed("Lorena Fernandes Soares", "60861822784", "600866346", LocalDate.of(2010, 3, 3), "11970862978", "lorena.soares.86@email.com", 'F', "Piracicaba", 1, "Rua João Cachoeira, 702", "04535001", 2),
                new ClienteSeed("Lívia Andrade Leal", "60872193513", "600876537", LocalDate.of(2011, 4, 4), "11970873001", "livia.leal.87@email.com", 'F', "Bauru", 1, "Rua Bandeira Paulista, 709", "04532001", 3),
                new ClienteSeed("Maria Luiza Brito Miranda", "60882564242", "600886728", LocalDate.of(1968, 5, 5), "11970883024", "maria.luiza.miranda.88@email.com", 'F', "Mogi das Cruzes", 2, "Rua Tabapuã, 716", "04533001", 0),
                new ClienteSeed("Cecília Correia Peixoto", "60892934971", "600896919", LocalDate.of(1959, 6, 6), "11970893047", "cecilia.peixoto.89@email.com", 'F', "Barueri", 4, "Av. Horácio Lafer, 723", "04538082", 1),
                new ClienteSeed("Eloá Duarte Rodrigues", "60903305700", "600907100", LocalDate.of(2015, 7, 7), "11970903070", "eloa.rodrigues.90@email.com", 'F', "São Paulo", 1, "Rua das Palmeiras, 730", "01226010", 2),
                new ClienteSeed("Giovanna Almeida Vieira", "60913676439", "600917291", LocalDate.of(1971, 8, 8), "11970913093", "giovanna.vieira.91@email.com", 'F', "Campinas", 2, "Av. Brasil, 737", "01430000", 3),
                new ClienteSeed("Maria Clara Batista Marques", "60924047168", "600927482", LocalDate.of(1972, 9, 9), "11970923116", "maria.clara.marques.92@email.com", 'F', "Santos", 3, "Rua Frei Caneca, 744", "01307001", 0),
                new ClienteSeed("Maria Eduarda Cavalcanti Neves", "60934417897", "600937673", LocalDate.of(1973, 10, 10), "11970933139", "maria.eduarda.neves.93@email.com", 'F', "Guarulhos", 1, "Rua Augusta, 751", "01305100", 1),
                new ClienteSeed("Mariana Dantas Ramos", "60944788626", "600947864", LocalDate.of(1964, 11, 11), "11970943162", "mariana.ramos.94@email.com", 'F', "Sorocaba", 2, "Alameda Itu, 758", "01421000", 2),
                new ClienteSeed("Lara Fonseca Soares", "60955159355", "600958055", LocalDate.of(2020, 12, 12), "11970953185", "lara.soares.95@email.com", 'F', "Santo André", 1, "Rua Peixoto Gomide, 765", "01409001", 3),
                new ClienteSeed("Beatriz Barbosa Leal", "60965520084", "600968246", LocalDate.of(1976, 1, 13), "11970963208", "beatriz.leal.96@email.com", 'F', "São Bernardo do Campo", 1, "Av. Angélica, 772", "01227200", 0),
                new ClienteSeed("Antonella Carvalho Miranda", "60975890813", "600978437", LocalDate.of(1977, 2, 14), "11970973231", "antonella.miranda.97@email.com", 'F', "Osasco", 2, "Rua Maranhão, 779", "01240000", 1),
                new ClienteSeed("Maria Júlia Cruz Peixoto", "60986261542", "600988628", LocalDate.of(1978, 3, 15), "11970983254", "maria.julia.peixoto.98@email.com", 'F', "Ribeirão Preto", 3, "Rua Sergipe, 786", "01243001", 2),
                new ClienteSeed("Emanuelly Ferreira Rodrigues", "60996632271", "600998819", LocalDate.of(1950, 4, 16), "11970993277", "emanuelly.rodrigues.99@email.com", 'F', "São José dos Campos", 4, "Av. Higienópolis, 793", "01238001", 3),
                new ClienteSeed("Isadora Araújo Vargas", "61007003000", "601009000", LocalDate.of(2016, 5, 17), "11971003300", "isadora.vargas.100@email.com", 'F', "Jundiaí", 1, "Rua Cardoso de Almeida, 800", "05013001", 0),
                new ClienteSeed("Ana Clara Campos Marques", "61017373739", "601019191", LocalDate.of(1981, 6, 18), "11971013323", "ana.clara.marques.101@email.com", 'F', "Piracicaba", 3, "Rua Homem de Melo, 807", "05007001", 1),
                new ClienteSeed("Melissa Costa Neves", "61027744468", "601029382", LocalDate.of(1982, 7, 19), "11971023346", "melissa.neves.102@email.com", 'F', "Bauru", 1, "Rua Apinajés, 814", "05017000", 2),
                new ClienteSeed("Ana Luiza Farias Ramos", "61038115197", "601039573", LocalDate.of(1983, 8, 20), "11971033369", "ana.luiza.ramos.103@email.com", 'F', "Mogi das Cruzes", 2, "Av. Sumaré, 821", "05016090", 3),
                new ClienteSeed("Ana Júlia Alves Soares", "61048485926", "601049764", LocalDate.of(1955, 9, 21), "11971043392", "ana.julia.soares.104@email.com", 'F', "Barueri", 2, "Rua Cayowaá, 828", "05018000", 0),
                new ClienteSeed("Esther Borges Leal", "61058856655", "601059955", LocalDate.of(2021, 10, 22), "11971053415", "esther.leal.105@email.com", 'F', "São Paulo", 1, "Rua Fradique Coutinho, 835", "05416010", 1),
                new ClienteSeed("Lavínia Coelho Miranda", "61069227384", "601060146", LocalDate.of(1986, 11, 23), "11971063438", "lavinia.miranda.106@email.com", 'F', "Campinas", 2, "Rua dos Pinheiros, 842", "05422001", 2),
                new ClienteSeed("Maitê Dias Peixoto", "61079598113", "601070337", LocalDate.of(1987, 12, 24), "11971073461", "maite.peixoto.107@email.com", 'F', "Santos", 3, "Rua Girassol, 849", "05433001", 3),
                new ClienteSeed("Maria Cecília Freitas Rodrigues", "61089968842", "601080528", LocalDate.of(1988, 1, 25), "11971083484", "maria.cecilia.rodrigues.108@email.com", 'F', "Guarulhos", 1, "Rua Aspicuelta, 856", "05433010", 0),
                new ClienteSeed("Maria Alice Barros Vargas", "61090339571", "601090719", LocalDate.of(1960, 2, 26), "11971093507", "maria.alice.vargas.109@email.com", 'F', "Sorocaba", 4, "Av. Pedroso de Morais, 863", "05419000", 1),
                new ClienteSeed("Sarah Castro Machado", "61100700300", "601100900", LocalDate.of(2017, 3, 27), "11971103530", "sarah.machado.110@email.com", 'F', "Santo André", 1, "Rua Joaquim Antunes, 870", "05415010", 2),
                new ClienteSeed("Elisa Cunha Neves", "61111071039", "601111091", LocalDate.of(1991, 4, 28), "11971113553", "elisa.neves.111@email.com", 'F', "São Bernardo do Campo", 1, "Rua Mateus Grou, 877", "05415000", 3),
                new ClienteSeed("Liz Figueiredo Ramos", "61121441768", "601121282", LocalDate.of(1992, 5, 1), "11971123576", "liz.ramos.112@email.com", 'F', "Osasco", 2, "Rua Afonso Braz, 884", "04511011", 0),
                new ClienteSeed("Yasmin Azevedo Soares", "61131812497", "601131473", LocalDate.of(1993, 6, 2), "11971133599", "yasmin.soares.113@email.com", 'F', "Ribeirão Preto", 3, "Rua Diogo Jácome, 891", "04512001", 1),
                new ClienteSeed("Isabelly Cardoso Leal", "61142183226", "601141664", LocalDate.of(1946, 7, 3), "11971143622", "isabelly.leal.114@email.com", 'F', "São José dos Campos", 2, "Av. Santo Amaro, 898", "04506000", 2),
                new ClienteSeed("Alícia Coutinho Miranda", "61152553955", "601151855", LocalDate.of(2022, 8, 4), "11971153645", "alicia.miranda.115@email.com", 'F', "Jundiaí", 1, "Rua Clodomiro Amazonas, 905", "04537011", 3),
                new ClienteSeed("Clara Fernandes Peixoto", "61162924684", "601162046", LocalDate.of(1996, 9, 5), "11971163668", "clara.peixoto.116@email.com", 'F', "Piracicaba", 3, "Rua João Cachoeira, 912", "04535001", 0),
                new ClienteSeed("Isis Andrade Rodrigues", "61173295413", "601172237", LocalDate.of(1997, 10, 6), "11971173691", "isis.rodrigues.117@email.com", 'F', "Bauru", 1, "Rua Bandeira Paulista, 919", "04532001", 1),
                new ClienteSeed("Rebeca Brito Vargas", "61183666142", "601182428", LocalDate.of(1998, 11, 7), "11971183714", "rebeca.vargas.118@email.com", 'F', "Mogi das Cruzes", 2, "Rua Tabapuã, 926", "04533001", 2),
                new ClienteSeed("Rafaela Correia Machado", "61194036871", "601192619", LocalDate.of(1951, 12, 8), "11971193737", "rafaela.machado.119@email.com", 'F', "Barueri", 4, "Av. Horácio Lafer, 933", "04538082", 3),
                new ClienteSeed("Marina Duarte Nascimento", "61204407600", "601202800", LocalDate.of(2018, 1, 9), "11971203760", "marina.nascimento.120@email.com", 'F', "São Paulo", 1, "Rua das Palmeiras, 940", "01226010", 0),
                new ClienteSeed("Ana Laura Almeida Ramos", "61214778339", "601212991", LocalDate.of(2001, 2, 10), "11971213783", "ana.laura.ramos.121@email.com", 'F', "Campinas", 1, "Av. Brasil, 947", "01430000", 1),
                new ClienteSeed("Maria Helena Batista Soares", "61225149068", "601223182", LocalDate.of(2002, 3, 11), "11971223806", "maria.helena.soares.122@email.com", 'F', "Santos", 1, "Rua Frei Caneca, 954", "01307001", 2),
                new ClienteSeed("Agatha Cavalcanti Leal", "61235519797", "601233373", LocalDate.of(2003, 4, 12), "11971233829", "agatha.leal.123@email.com", 'F', "Guarulhos", 1, "Rua Augusta, 961", "01305100", 3),
                new ClienteSeed("Gabriela Dantas Miranda", "61245880526", "601243564", LocalDate.of(1956, 5, 13), "11971243852", "gabriela.miranda.124@email.com", 'F', "Sorocaba", 2, "Alameda Itu, 968", "01421000", 0),
                new ClienteSeed("Catarina Fonseca Peixoto", "61256251255", "601253755", LocalDate.of(2023, 6, 14), "11971253875", "catarina.peixoto.125@email.com", 'F', "Santo André", 1, "Rua Peixoto Gomide, 975", "01409001", 1),
                new ClienteSeed("Letícia Barbosa Rodrigues", "61266621984", "601263946", LocalDate.of(2006, 7, 15), "11971263898", "leticia.rodrigues.126@email.com", 'F', "São Bernardo do Campo", 1, "Av. Angélica, 982", "01227200", 2),
                new ClienteSeed("Mirella Carvalho Vargas", "61276992713", "601274137", LocalDate.of(2007, 8, 16), "11971273921", "mirella.vargas.127@email.com", 'F', "Osasco", 1, "Rua Maranhão, 989", "01240000", 3),
                new ClienteSeed("Nicole Cruz Machado", "61287363442", "601284328", LocalDate.of(2008, 9, 17), "11971283944", "nicole.machado.128@email.com", 'F', "Ribeirão Preto", 1, "Rua Sergipe, 996", "01243001", 0),
                new ClienteSeed("Luna Ferreira Nascimento", "61297734171", "601294519", LocalDate.of(1961, 10, 18), "11971293967", "luna.nascimento.129@email.com", 'F', "São José dos Campos", 4, "Av. Higienópolis, 1003", "01238001", 1),
                new ClienteSeed("Olívia Araújo Queiroz", "61308104900", "601304700", LocalDate.of(2019, 11, 19), "11971303990", "olivia.queiroz.130@email.com", 'F', "Jundiaí", 1, "Rua Cardoso de Almeida, 1010", "05013001", 2),
                new ClienteSeed("Vitória Campos Soares", "61318475639", "601314891", LocalDate.of(2011, 12, 20), "11971314013", "vitoria.soares.131@email.com", 'F', "Piracicaba", 1, "Rua Homem de Melo, 1017", "05007001", 3),
                new ClienteSeed("Maria Vitória Costa Leal", "61328846368", "601325082", LocalDate.of(1968, 1, 21), "11971324036", "maria.vitoria.leal.132@email.com", 'F', "Bauru", 1, "Rua Apinajés, 1024", "05017000", 0),
                new ClienteSeed("Allana Farias Miranda", "61339217097", "601335273", LocalDate.of(1969, 2, 22), "11971334059", "allana.miranda.133@email.com", 'F', "Mogi das Cruzes", 2, "Av. Sumaré, 1031", "05016090", 1),
                new ClienteSeed("Milena Alves Peixoto", "61349587826", "601345464", LocalDate.of(1947, 3, 23), "11971344082", "milena.peixoto.134@email.com", 'F', "Barueri", 2, "Rua Cayowaá, 1038", "05018000", 2),
                new ClienteSeed("Emilly Borges Rodrigues", "61359958555", "601355655", LocalDate.of(2015, 4, 24), "11971354105", "emilly.rodrigues.135@email.com", 'F', "São Paulo", 1, "Rua Fradique Coutinho, 1045", "05416010", 3),
                new ClienteSeed("Ayla Coelho Vargas", "61360329284", "601365846", LocalDate.of(1972, 5, 25), "11971364128", "ayla.vargas.136@email.com", 'F', "Campinas", 2, "Rua dos Pinheiros, 1052", "05422001", 0),
                new ClienteSeed("Maya Dias Machado", "61370690013", "601376037", LocalDate.of(1973, 6, 26), "11971374151", "maya.machado.137@email.com", 'F', "Santos", 3, "Rua Girassol, 1059", "05433001", 1),
                new ClienteSeed("Bianca Freitas Nascimento", "61381060742", "601386228", LocalDate.of(1974, 7, 27), "11971384174", "bianca.nascimento.138@email.com", 'F', "Guarulhos", 1, "Rua Aspicuelta, 1066", "05433010", 2),
                new ClienteSeed("Clarice Barros Queiroz", "61391431471", "601396419", LocalDate.of(1952, 8, 28), "11971394197", "clarice.queiroz.139@email.com", 'F', "Sorocaba", 4, "Av. Pedroso de Morais, 1073", "05419000", 3),
                new ClienteSeed("Aurora Castro Siqueira", "61401802200", "601406600", LocalDate.of(2020, 9, 1), "11971404220", "aurora.siqueira.140@email.com", 'F', "Santo André", 1, "Rua Joaquim Antunes, 1080", "05415010", 0),
                new ClienteSeed("Larissa Cunha Leal", "61412172939", "601416791", LocalDate.of(1977, 10, 2), "11971414243", "larissa.leal.141@email.com", 'F', "São Bernardo do Campo", 1, "Rua Mateus Grou, 1087", "05415000", 1),
                new ClienteSeed("Maria Fernanda Figueiredo Miranda", "61422543668", "601426982", LocalDate.of(1978, 11, 3), "11971424266", "maria.fernanda.miranda.142@email.com", 'F', "Osasco", 2, "Rua Afonso Braz, 1094", "04511011", 2),
                new ClienteSeed("Pietra Azevedo Peixoto", "61432914397", "601437173", LocalDate.of(1979, 12, 4), "11971434289", "pietra.peixoto.143@email.com", 'F', "Ribeirão Preto", 3, "Rua Diogo Jácome, 1101", "04512001", 3),
                new ClienteSeed("Laís Cardoso Rodrigues", "61443285126", "601447364", LocalDate.of(1957, 1, 5), "11971444312", "lais.rodrigues.144@email.com", 'F', "São José dos Campos", 2, "Av. Santo Amaro, 1108", "04506000", 0),
                new ClienteSeed("Stella Coutinho Vargas", "61453655855", "601457555", LocalDate.of(2016, 2, 6), "11971454335", "stella.vargas.145@email.com", 'F', "Jundiaí", 1, "Rua Clodomiro Amazonas, 1115", "04537011", 1),
                new ClienteSeed("Eduarda Fernandes Machado", "61464026584", "601467746", LocalDate.of(1982, 3, 7), "11971464358", "eduarda.machado.146@email.com", 'F', "Piracicaba", 3, "Rua João Cachoeira, 1122", "04535001", 2),
                new ClienteSeed("Carolina Andrade Nascimento", "61474397313", "601477937", LocalDate.of(1983, 4, 8), "11971474381", "carolina.nascimento.147@email.com", 'F', "Bauru", 1, "Rua Bandeira Paulista, 1129", "04532001", 3),
                new ClienteSeed("Bárbara Brito Queiroz", "61484768042", "601488128", LocalDate.of(1984, 5, 9), "11971484404", "barbara.queiroz.148@email.com", 'F', "Mogi das Cruzes", 2, "Rua Tabapuã, 1136", "04533001", 0),
                new ClienteSeed("Camila Correia Siqueira", "61495138771", "601498319", LocalDate.of(1962, 6, 10), "11971494427", "camila.siqueira.149@email.com", 'F', "Barueri", 4, "Av. Horácio Lafer, 1143", "04538082", 1),
                new ClienteSeed("Amanda Duarte Lacerda", "61505509500", "601508500", LocalDate.of(2021, 7, 11), "11971504450", "amanda.lacerda.150@email.com", 'F', "São Paulo", 1, "Rua das Palmeiras, 1150", "01226010", 2)
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
            log.info("DatabaseSeeder: {} novos pacientes inseridos.", inseridos);
        }
    }
}
