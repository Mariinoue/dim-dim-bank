package com.dimdim.banco.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conta", uniqueConstraints = @UniqueConstraint(name = "uk_conta_agencia_numero", columnNames = {"agencia", "numero"}))
@Getter
@Setter
@NoArgsConstructor
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o titular")
    @Size(max = 100, message = "O titular deve ter no máximo 100 caracteres")
    @Column(nullable = false, length = 100)
    private String titular;

    @NotBlank(message = "Informe o CPF ou CNPJ")
    @Pattern(regexp = "\\d{11}|\\d{14}", message = "Use somente números: 11 dígitos (CPF) ou 14 dígitos (CNPJ)")
    @Column(nullable = false, length = 14)
    private String documento;

    @NotNull(message = "Selecione o tipo da conta")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoConta tipo;

    @NotBlank(message = "Informe a agência")
    @Pattern(regexp = "\\d{4}", message = "A agência deve ter 4 dígitos")
    @Column(nullable = false, length = 4)
    private String agencia;

    @NotBlank(message = "Informe o número da conta")
    @Pattern(regexp = "\\d{5,10}", message = "O número da conta deve ter de 5 a 10 dígitos")
    @Column(nullable = false, length = 10)
    private String numero;

    @NotNull(message = "Informe o saldo inicial")
    @PositiveOrZero(message = "O saldo inicial não pode ser negativo")
    @Digits(integer = 13, fraction = 2, message = "Valor inválido")
    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial = BigDecimal.ZERO;

    @NotNull(message = "Selecione o status")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusConta status = StatusConta.ATIVA;

    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDate dataAbertura;

    @OneToMany(mappedBy = "conta")
    private List<Transacao> transacoes = new ArrayList<>();

    @Transient
    private BigDecimal saldoAtual;

    @PrePersist
    void aoCriar() {
        if (dataAbertura == null) {
            dataAbertura = LocalDate.now();
        }
    }

    public String getDescricao() {
        return titular + " - Ag " + agencia + " / Cc " + numero;
    }
}
