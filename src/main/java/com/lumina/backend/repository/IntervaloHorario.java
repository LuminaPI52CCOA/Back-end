package com.lumina.backend.repository;

import java.time.LocalTime;

/**
 * Projeção leve de uma consulta que ocupa a agenda do dentista (somente início e fim).
 */
public record IntervaloHorario(LocalTime inicio, LocalTime fim) {
}
