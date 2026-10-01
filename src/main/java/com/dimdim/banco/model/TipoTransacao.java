package com.dimdim.banco.model;

public enum TipoTransacao {
    DEPOSITO("Depósito", true),
    SAQUE("Saque", false),
    TRANSFERENCIA("Transferência enviada", false),
    PAGAMENTO_BOLETO("Pagamento de boleto", false);

    private final String label;
    private final boolean credito;

    TipoTransacao(String label, boolean credito) {
        this.label = label;
        this.credito = credito;
    }

    public String getLabel() {
        return label;
    }

    public boolean isCredito() {
        return credito;
    }
}
