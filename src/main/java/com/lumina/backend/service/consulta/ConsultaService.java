package com.lumina.backend.service.consulta;

import com.lumina.backend.dto.consulta.ConsultaMapper;
import com.lumina.backend.dto.consulta.ConsultaRequest;
import com.lumina.backend.dto.consulta.ReagendamentoRequest;
import com.lumina.backend.exception.AgendamentoInvalidoException;
import com.lumina.backend.exception.ConflitoHorarioException;
import com.lumina.backend.exception.EntidadeNaoEncontrada;
import com.lumina.backend.model.Cliente;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.model.Dente;
import com.lumina.backend.model.Especialidade;
import com.lumina.backend.model.NotificacaoWhatsapp;
import com.lumina.backend.model.Usuario;
import com.lumina.backend.repository.ClienteRepository;
import com.lumina.backend.repository.ConsultaRepository;
import com.lumina.backend.repository.DenteRepository;
import com.lumina.backend.repository.EspecialidadeRepository;
import com.lumina.backend.repository.IntervaloHorario;
import com.lumina.backend.repository.NotificacaoWhatsappRepository;
import com.lumina.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConsultaService {
    private static final long PERIODO_MAXIMO_DIAS = 366;

    private final ConsultaRepository consultaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final DenteRepository denteRepository;
    private final NotificacaoWhatsappRepository notificacaoWhatsappRepository;
    private final RegrasAgendamento regrasAgendamento;

    public ConsultaService(ConsultaRepository consultaRepository,
                           UsuarioRepository usuarioRepository,
                           ClienteRepository clienteRepository,
                           EspecialidadeRepository especialidadeRepository,
                           DenteRepository denteRepository,
                           NotificacaoWhatsappRepository notificacaoWhatsappRepository,
                           RegrasAgendamento regrasAgendamento) {
        this.consultaRepository = consultaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.especialidadeRepository = especialidadeRepository;
        this.denteRepository = denteRepository;
        this.notificacaoWhatsappRepository = notificacaoWhatsappRepository;
        this.regrasAgendamento = regrasAgendamento;
    }

    public List<Consulta> listar() {
        return consultaRepository.findAll();
    }

    public List<Consulta> listar(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return listar();
        }
        validarPeriodo(dataInicio, dataFim);
        return consultaRepository.buscarPorPeriodo(null, dataInicio, dataFim);
    }

    public List<Consulta> listarPorDentista(Long idUsuario){
        return consultaRepository.findByUsuarioIdUsuario(idUsuario);
    }

    public List<Consulta> listarPorDentista(Long idUsuario, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null && dataFim == null) {
            return listarPorDentista(idUsuario);
        }
        validarPeriodo(dataInicio, dataFim);
        return consultaRepository.buscarPorPeriodo(idUsuario, dataInicio, dataFim);
    }

    @Transactional
    public Consulta cadastrar(ConsultaRequest consulta) {
        // Validacoes sem acesso ao banco primeiro.
        regrasAgendamento.validarIntervalo(consulta.getData(), consulta.getHorarioInicio(), consulta.getHorarioFim());
        boolean pularOdontograma = Boolean.TRUE.equals(consulta.getPularOdontograma());
        Set<Integer> dentes = pularOdontograma ? Set.of() : normalizarDentes(consulta.getDentes());
        String tipoDente = pularOdontograma ? null : normalizarTipoDente(consulta.getTipoDente());

        // O bloqueio do dentista precisa ser a primeira leitura da transacao: no MySQL (REPEATABLE READ)
        // o snapshot nasce na primeira leitura comum, e a busca de conflitos deve enxergar o que a
        // transacao concorrente acabou de gravar.
        bloquearAgendaDoDentista(consulta.getIdUsuario());

        Usuario dentista = buscarDentistaAtivo(consulta.getIdUsuario());
        Cliente cliente = clienteRepository.findById(consulta.getIdCliente())
                .orElseThrow(() -> new EntidadeNaoEncontrada("Cliente não encontrado"));
        validarPacienteAtivo(cliente);
        Especialidade especialidade = buscarEspecialidade(consulta.getIdEspecialidade());
        validarDentesCadastrados(dentes);

        verificarConflito(dentista.getIdUsuario(), consulta.getData(),
                consulta.getHorarioInicio(), consulta.getHorarioFim(), null);

        Consulta consultaEntidade = ConsultaMapper.toEntity(consulta);
        consultaEntidade.setCliente(cliente);
        consultaEntidade.setUsuario(dentista);
        consultaEntidade.setEspecialidade(especialidade);
        consultaEntidade.setObservacoes(normalizarTexto(consulta.getObservacoes()));
        consultaEntidade.setPularOdontograma(pularOdontograma);
        consultaEntidade.setDentes(dentes);
        consultaEntidade.setTipoDente(tipoDente);
        consultaEntidade.setStatus(StatusConsulta.AGENDADA.name());
        consultaEntidade.setLembreteEnviado(false);

        return consultaRepository.save(consultaEntidade);
    }

    public Consulta buscarPorId(Long id) {

        return consultaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Consulta não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<IntervaloHorario> listarHorariosDisponiveis(Long idUsuario, LocalDate data,
                                                            Integer duracaoMinutos, Long idConsultaIgnorada) {
        regrasAgendamento.validarDuracao(duracaoMinutos);
        buscarDentistaAtivo(idUsuario);
        List<IntervaloHorario> ocupados = consultaRepository.buscarIntervalosOcupados(idUsuario, data, idConsultaIgnorada);
        return regrasAgendamento.horariosDisponiveis(data, duracaoMinutos, ocupados);
    }

    /**
     * Altera somente data, horarios e (opcionalmente) observacoes. Paciente, dentista,
     * especialidade e odontograma sao preservados.
     */
    @Transactional
    public Consulta reagendar(Long id, ReagendamentoRequest request) {
        // Primeiras leituras da transacao sao bloqueantes (ver comentario em cadastrar).
        Long idDentista = consultaRepository.bloquearParaAtualizacao(id).stream()
                .findFirst()
                .map(Number::longValue)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Consulta não encontrada"));
        bloquearAgendaDoDentista(idDentista);

        Consulta consultaExistente = buscarPorId(id);
        StatusConsulta status = StatusConsulta.de(consultaExistente.getStatus());
        if (status.finalizado()) {
            throw new AgendamentoInvalidoException("Consulta " + status.name() + " não pode ser reagendada.");
        }

        if (request.getObservacoes() != null) {
            consultaExistente.setObservacoes(normalizarTexto(request.getObservacoes()));
        }

        boolean horarioAlterado = !Objects.equals(consultaExistente.getData(), request.getData())
                || !Objects.equals(consultaExistente.getHorarioInicio(), request.getHorarioInicio())
                || !Objects.equals(consultaExistente.getHorarioFim(), request.getHorarioFim());

        if (horarioAlterado) {
            regrasAgendamento.validarIntervalo(request.getData(), request.getHorarioInicio(), request.getHorarioFim());
            buscarDentistaAtivo(idDentista);
            validarPacienteAtivo(consultaExistente.getCliente());
            verificarConflito(idDentista, request.getData(), request.getHorarioInicio(), request.getHorarioFim(), id);

            consultaExistente.setData(request.getData());
            consultaExistente.setHorarioInicio(request.getHorarioInicio());
            consultaExistente.setHorarioFim(request.getHorarioFim());

            // Lembrete Alexa deve disparar de novo no novo horario; a confirmacao anterior valia
            // para o horario antigo.
            consultaExistente.setLembreteEnviado(false);
            consultaExistente.setAlexaReminderId(null);
            if (status == StatusConsulta.CONFIRMADA) {
                consultaExistente.setStatus(StatusConsulta.AGENDADA.name());
            }
            atualizarNotificacaoAposReagendamento(consultaExistente);
        }

        return consultaRepository.save(consultaExistente);
    }

    @Transactional
    public Consulta alterarStatus(Long id, String novoStatus) {
        StatusConsulta destino = StatusConsulta.deRequisicao(novoStatus);
        Consulta consulta = buscarPorId(id);
        StatusConsulta atual = StatusConsulta.de(consulta.getStatus());

        if (destino == atual) {
            return consulta;
        }
        if (destino == StatusConsulta.CANCELADA) {
            return cancelarConsulta(consulta);
        }
        if (atual.finalizado()) {
            throw new AgendamentoInvalidoException("Consulta " + atual.name() + " não pode ter o status alterado.");
        }
        if (destino == StatusConsulta.CONCLUIDA) {
            throw new AgendamentoInvalidoException("Status CONCLUIDA não pode ser definido por esta operação.");
        }

        consulta.setStatus(destino.name());
        if (destino == StatusConsulta.AGENDADA) {
            // Desfaz a confirmacao para que o link do WhatsApp volte a valer.
            notificacaoWhatsappRepository.findByConsultaIdConsulta(consulta.getIdConsulta())
                    .ifPresent(notificacao -> {
                        notificacao.setConfirmadoEm(null);
                        notificacaoWhatsappRepository.save(notificacao);
                    });
        }
        return consultaRepository.save(consulta);
    }

    /**
     * Cancelamento logico: preserva o historico e libera o horario.
     */
    @Transactional
    public void cancelar(Long id) {
        cancelarConsulta(buscarPorId(id));
    }

    private Consulta cancelarConsulta(Consulta consulta) {
        StatusConsulta atual = StatusConsulta.de(consulta.getStatus());
        if (atual == StatusConsulta.CANCELADA) {
            return consulta;
        }
        if (atual == StatusConsulta.CONCLUIDA) {
            throw new AgendamentoInvalidoException("Consulta concluída não pode ser cancelada.");
        }

        consulta.setStatus(StatusConsulta.CANCELADA.name());
        notificacaoWhatsappRepository.findByConsultaIdConsulta(consulta.getIdConsulta())
                .ifPresent(notificacao -> {
                    notificacao.setStatus(NotificacaoWhatsapp.STATUS_CANCELADO);
                    notificacaoWhatsappRepository.save(notificacao);
                });
        return consultaRepository.save(consulta);
    }

    private void atualizarNotificacaoAposReagendamento(Consulta consulta) {
        notificacaoWhatsappRepository.findByConsultaIdConsulta(consulta.getIdConsulta())
                .ifPresent(notificacao -> {
                    notificacao.setDataConsulta(consulta.getData());
                    notificacao.setHorarioConsulta(consulta.getHorarioInicio());
                    notificacao.setLimiteConfirmacaoEm(LocalDateTime.of(consulta.getData(), consulta.getHorarioInicio()));
                    // Link antigo deixa de valer: ele confirmava o horario anterior.
                    notificacao.setTokenConfirmacao(UUID.randomUUID().toString());
                    notificacao.setConfirmadoEm(null);
                    notificacao.setDisparadoEm(null);
                    notificacao.setUsuarioDisparo(null);
                    if (!NotificacaoWhatsapp.STATUS_FALHA_CONTATO_INVALIDO.equalsIgnoreCase(notificacao.getStatus())) {
                        notificacao.setStatus(NotificacaoWhatsapp.STATUS_PENDENTE);
                    }
                    notificacaoWhatsappRepository.save(notificacao);
                });
    }

    private void bloquearAgendaDoDentista(Long idUsuario) {
        List<Number> bloqueado = usuarioRepository.bloquearParaAgendamento(idUsuario);
        if (bloqueado == null || bloqueado.isEmpty()) {
            throw new EntidadeNaoEncontrada("Dentista não encontrado");
        }
    }

    private Usuario buscarDentistaAtivo(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Dentista não encontrado"));
        boolean dentista = usuario.getPerfil() != null
                && "DENTISTA".equalsIgnoreCase(usuario.getPerfil().getNome());
        if (!dentista || Boolean.FALSE.equals(usuario.getAtivo())) {
            throw new AgendamentoInvalidoException("O usuário informado não é um dentista ativo.");
        }
        return usuario;
    }

    private void validarPacienteAtivo(Cliente cliente) {
        // A coluna cliente.ativo e anulavel e os cadastros atuais nao a preenchem: nulo = ativo.
        if (cliente != null && Boolean.FALSE.equals(cliente.getAtivo())) {
            throw new AgendamentoInvalidoException("O paciente informado está inativo.");
        }
    }

    private Especialidade buscarEspecialidade(Integer idEspecialidade) {
        if (idEspecialidade == null) {
            throw new AgendamentoInvalidoException("A especialidade é obrigatória.");
        }
        return especialidadeRepository.findById(idEspecialidade)
                .orElseThrow(() -> new EntidadeNaoEncontrada("Especialidade não encontrada"));
    }

    private void verificarConflito(Long idUsuario, LocalDate data, LocalTime inicio, LocalTime fim, Long idConsultaIgnorada) {
        consultaRepository.buscarIntervalosOcupados(idUsuario, data, idConsultaIgnorada).stream()
                .filter(ocupado -> RegrasAgendamento.sobrepoe(inicio, fim, ocupado.inicio(), ocupado.fim()))
                .findFirst()
                .ifPresent(ocupado -> {
                    throw new ConflitoHorarioException(
                            "O dentista já possui consulta entre " + ocupado.inicio() + " e " + ocupado.fim()
                                    + " neste dia. Escolha outro horário.");
                });
    }

    private Set<Integer> normalizarDentes(List<Integer> dentes) {
        if (dentes == null || dentes.isEmpty()) {
            return Set.of();
        }
        Set<Integer> normalizados = new TreeSet<>();
        for (Integer dente : dentes) {
            if (dente == null || !RegrasAgendamento.DENTES_SUPORTADOS.contains(dente)) {
                throw new AgendamentoInvalidoException(
                        "Dente " + dente + " não é suportado pelo odontograma (11-18, 21-28, 31-38, 41-48).");
            }
            if (!normalizados.add(dente)) {
                throw new AgendamentoInvalidoException("O dente " + dente + " foi informado mais de uma vez.");
            }
        }
        return normalizados;
    }

    private String normalizarTipoDente(String tipoDente) {
        String tipo = normalizarTexto(tipoDente);
        if (tipo != null && !RegrasAgendamento.TIPOS_DENTE.contains(tipo)) {
            throw new AgendamentoInvalidoException("Tipo de dente inválido: " + tipo + ".");
        }
        return tipo;
    }

    private void validarDentesCadastrados(Set<Integer> dentes) {
        if (dentes.isEmpty()) {
            return;
        }
        Set<Integer> cadastrados = denteRepository.findAllById(dentes).stream()
                .map(Dente::getNumeroDente)
                .collect(Collectors.toSet());
        Set<Integer> ausentes = new LinkedHashSet<>(dentes);
        ausentes.removeAll(cadastrados);
        if (!ausentes.isEmpty()) {
            throw new EntidadeNaoEncontrada("Dente(s) não cadastrado(s): " + ausentes);
        }
    }

    private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null) {
            if (dataFim.isBefore(dataInicio)) {
                throw new AgendamentoInvalidoException("dataFim deve ser igual ou posterior a dataInicio.");
            }
            if (ChronoUnit.DAYS.between(dataInicio, dataFim) > PERIODO_MAXIMO_DIAS) {
                throw new AgendamentoInvalidoException("O período consultado não pode exceder " + PERIODO_MAXIMO_DIAS + " dias.");
            }
        }
    }

    private static String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
