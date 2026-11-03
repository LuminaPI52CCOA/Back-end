package com.lumina.backend.configure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AgendaConfig {

    /**
     * Relogio da agenda no fuso da clinica. Testes substituem por Clock.fixed(...).
     */
    @Bean
    public Clock relogioAgenda(@Value("${lumina.agenda.fuso-horario:America/Sao_Paulo}") String fusoHorario) {
        return Clock.system(ZoneId.of(fusoHorario));
    }
}
