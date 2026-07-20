CREATE UNIQUE INDEX idx_consulta_medico_horario_ativo
ON TB_CONSULTAS (medico_id, dataConsulta)
where status != 'CANCELADA';

CREATE UNIQUE INDEX idx_consulta_cliente_horario_ativo
    ON tb_consultas (cliente_id, data_consulta)
    WHERE status != 'CANCELADA';