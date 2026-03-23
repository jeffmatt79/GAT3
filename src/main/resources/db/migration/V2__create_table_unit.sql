-- Criar a tabela de filiais
CREATE TABLE unit (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Inserir as filiais iniciais
INSERT INTO unit (name) VALUES ('meac');
INSERT INTO unit (name) VALUES ('huwc');

-- Adicionar a coluna de relacionamento na tabela de atendimentos
ALTER TABLE appointments ADD COLUMN unit_id INTEGER;

-- Criar a constraint de chave estrangeira
ALTER TABLE appointments ADD CONSTRAINT fk_appointment_unit
FOREIGN KEY (unit_id) REFERENCES unit (id);