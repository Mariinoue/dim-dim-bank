IF OBJECT_ID('dbo.transacao', 'U') IS NOT NULL DROP TABLE dbo.transacao;
IF OBJECT_ID('dbo.conta', 'U') IS NOT NULL DROP TABLE dbo.conta;
GO

CREATE TABLE dbo.conta (
    id             BIGINT         IDENTITY(1,1) NOT NULL,
    titular        VARCHAR(100)   NOT NULL,
    documento      VARCHAR(14)    NOT NULL,
    tipo           VARCHAR(30)    NOT NULL,
    agencia        VARCHAR(4)     NOT NULL,
    numero         VARCHAR(10)    NOT NULL,
    saldo_inicial  DECIMAL(15,2)  NOT NULL,
    status         VARCHAR(30)    NOT NULL,
    data_abertura  DATE           NOT NULL,
    CONSTRAINT pk_conta PRIMARY KEY (id),
    CONSTRAINT uk_conta_agencia_numero UNIQUE (agencia, numero),
    CONSTRAINT ck_conta_tipo   CHECK (tipo   IN ('CORRENTE', 'POUPANCA', 'PESSOA_JURIDICA')),
    CONSTRAINT ck_conta_status CHECK (status IN ('ATIVA', 'BLOQUEADA', 'ENCERRADA')),
    CONSTRAINT ck_conta_saldo_inicial CHECK (saldo_inicial >= 0)
);
GO

CREATE TABLE dbo.transacao (
    id          BIGINT         IDENTITY(1,1) NOT NULL,
    conta_id    BIGINT         NOT NULL,
    tipo        VARCHAR(30)    NOT NULL,
    valor       DECIMAL(15,2)  NOT NULL,
    descricao   VARCHAR(200)   NULL,
    data_hora   DATETIME2(6)   NOT NULL,
    CONSTRAINT pk_transacao PRIMARY KEY (id),
    CONSTRAINT fk_transacao_conta FOREIGN KEY (conta_id) REFERENCES dbo.conta (id),
    CONSTRAINT ck_transacao_tipo  CHECK (tipo IN ('DEPOSITO', 'SAQUE', 'TRANSFERENCIA', 'PAGAMENTO_BOLETO')),
    CONSTRAINT ck_transacao_valor CHECK (valor > 0)
);
GO

CREATE INDEX ix_transacao_conta_id ON dbo.transacao (conta_id);
GO
