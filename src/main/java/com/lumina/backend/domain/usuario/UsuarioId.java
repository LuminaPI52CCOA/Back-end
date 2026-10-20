package com.lumina.backend.domain.usuario;

import java.util.Objects;

public class UsuarioId {
    private final Long value;

    public UsuarioId(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public static UsuarioId of(Long value) {
        if (value == null) {
            return null;
        }
        return new UsuarioId(value);
    }

    public static UsuarioId generate() {
        return new UsuarioId(null); // Será gerado pelo banco de dados
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioId usuarioId = (UsuarioId) o;
        return Objects.equals(value, usuarioId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}