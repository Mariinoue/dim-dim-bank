package com.dimdim.banco.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacao")
@Getter
@Setter
@NoArgsConstructor
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Selecione a conta")
    @ManyToOne(optional = false)
    @JoinColumn(name = "conta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_transacao_conta"))
    private Conta conta;

    @NotNull(message = "Selecione o tipo da transação")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoTransacao tipo;

    @NotNull(message = "Informe o valor")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Digits(integer = 13, fraction = 2, message = "Valor inválido")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres")
    @Column(length = 200)
    private String descricao;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @PrePersist
    void aoCriar() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now();
        }
    }

    public BigDecimal getEfeito() {
        return tipo.isCredito() ? valor : valor.negate();
    }
}
