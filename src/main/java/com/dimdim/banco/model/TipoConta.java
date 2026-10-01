package com.dimdim.banco.model;

public enum TipoConta {
    CORRENTE("Conta corrente"),
    POUPANCA("Poupança"),
    PESSOA_JURIDICA("Conta PJ");

    private final String label;

    TipoConta(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
