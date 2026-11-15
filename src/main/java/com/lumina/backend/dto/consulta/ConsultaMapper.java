package com.lumina.backend.dto.consulta;

import com.lumina.backend.dto.cliente.ClienteMapper;
import com.lumina.backend.dto.especialidade.EspecialidadeResponse;
import com.lumina.backend.dto.usuario.UsuarioMapper;
import com.lumina.backend.dto.usuario.UsuarioResponse;
import com.lumina.backend.model.Consulta;
import com.lumina.backend.service.consulta.StatusConsulta;

import java.time.Duration;
import java.util.List;

public class ConsultaMapper {

    public static ConsultaResponse toResponse(Consulta consulta) {
        if (consulta == null) {
            return null;
        }

        UsuarioResponse usuario = UsuarioMapper.toDto(consulta.getUsuario());
        UsuarioResponse usuarioSemSenha = usuario != null ? usuario.semSenha() : null;

        Integer duracaoMinutos = (consulta.getHorarioInicio() != null && consulta.getHorarioFim() != null)
                ? (int) Duration.between(consulta.getHorarioInicio(), consulta.getHorarioFim()).toMinutes()
                : null;

        List<Integer> dentesOrdenados = consulta.getDentes() == null
                ? List.of()
                : consulta.getDentes().stream().sorted().toList();

        return new ConsultaResponse(
                consulta.getIdConsulta(),
                ClienteMapper.toDto(consulta.getCliente()),
                usuarioSemSenha,
                consulta.getData(),
                consulta.getHorarioInicio(),
                consulta.getHorarioFim(),
                duracaoMinutos,
                StatusConsulta.de(consulta.getStatus()).name(),
                EspecialidadeResponse.of(consulta.getEspecialidade()),
                consulta.getObservacoes(),
                dentesOrdenados,
                consulta.getTipoDente(),
                Boolean.TRUE.equals(consulta.getPularOdontograma())
        );
    }

    public static List<ConsultaResponse> toResponse(List<Consulta> consultas) {
        return consultas.stream()
                .map(ConsultaMapper::toResponse)
                .toList();
    }

    public static Consulta toEntity(ConsultaRequest consulta) {
        if (consulta == null) {
            return null;
        }

        Consulta consultaEntidade = new Consulta();
        consultaEntidade.setData(consulta.data());
        consultaEntidade.setHorarioFim(consulta.horarioFim());
        consultaEntidade.setHorarioInicio(consulta.horarioInicio());
        consultaEntidade.setObservacoes(consulta.observacoes());
        consultaEntidade.setTipoDente(consulta.tipoDente());
        consultaEntidade.setPularOdontograma(Boolean.TRUE.equals(consulta.pularOdontograma()));
        consultaEntidade.setDentes(consulta.dentes());

        return consultaEntidade;
    }
}
