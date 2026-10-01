SELECT * FROM dbo.conta;
SELECT * FROM dbo.transacao;

SELECT c.id, c.titular, c.agencia, c.numero,
       c.saldo_inicial + COALESCE(SUM(CASE WHEN t.tipo = 'DEPOSITO' THEN t.valor ELSE -t.valor END), 0) AS saldo_atual
FROM dbo.conta c
LEFT JOIN dbo.transacao t ON t.conta_id = c.id
GROUP BY c.id, c.titular, c.agencia, c.numero, c.saldo_inicial;
