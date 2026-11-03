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
        if(consulta == null) {
            return null;
        }

        UsuarioResponse usuario = UsuarioMapper.toDto(consulta.getUsuario());
        if (usuario != null) {
            // O hash da senha do dentista nao deve trafegar na agenda.
            usuario.setSenha(null);
        }

        ConsultaResponse response = new ConsultaResponse(
                consulta.getIdConsulta(),
                ClienteMapper.toDto(consulta.getCliente()),
                usuario,
                consulta.getData(),
                consulta.getHorarioInicio(),
                consulta.getHorarioFim()
        );

        if (consulta.getHorarioInicio() != null && consulta.getHorarioFim() != null) {
            response.setDuracaoMinutos((int) Duration.between(consulta.getHorarioInicio(), consulta.getHorarioFim()).toMinutes());
        }
        response.setStatus(StatusConsulta.de(consulta.getStatus()).name());
        response.setEspecialidade(EspecialidadeResponse.of(consulta.getEspecialidade()));
        response.setObservacoes(consulta.getObservacoes());
        response.setDentes(consulta.getDentes() == null
                ? List.of()
                : consulta.getDentes().stream().sorted().toList());
        response.setTipoDente(consulta.getTipoDente());
        response.setPularOdontograma(Boolean.TRUE.equals(consulta.getPularOdontograma()));

        return response;
    }

    public static List<ConsultaResponse> toResponse(List<Consulta> consultas) {
        return consultas.stream()
                .map(ConsultaMapper::toResponse)
                .toList();

    }

    public static Consulta toEntity(ConsultaRequest consulta) {
        if(consulta == null) {
            return null;
        }

        Consulta consultaEntidade = new Consulta();
        consultaEntidade.setData(consulta.getData());
        consultaEntidade.setHorarioFim(consulta.getHorarioFim());
        consultaEntidade.setHorarioInicio(consulta.getHorarioInicio());
        consultaEntidade.setObservacoes(consulta.getObservacoes());
        consultaEntidade.setTipoDente(consulta.getTipoDente());
        consultaEntidade.setPularOdontograma(Boolean.TRUE.equals(consulta.getPularOdontograma()));
        consultaEntidade.setDentes(consulta.getDentes());

        return consultaEntidade;
    }
}
