package com.lumina.backend.domain.cliente;

import java.util.Objects;

public class ClienteId {
    private final Long value;

    public ClienteId(Long value) {
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public static ClienteId of(Long value) {
        if (value == null) {
            return null;
        }
        return new ClienteId(value);
    }

    public static ClienteId generate() {
        return new ClienteId(null); // Será gerado pelo banco de dados
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteId clienteId = (ClienteId) o;
        return Objects.equals(value, clienteId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}