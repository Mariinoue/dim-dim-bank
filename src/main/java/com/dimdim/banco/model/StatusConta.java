package com.dimdim.banco.model;

public enum StatusConta {
    ATIVA("Ativa"),
    BLOQUEADA("Bloqueada"),
    ENCERRADA("Encerrada");

    private final String label;

    StatusConta(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
