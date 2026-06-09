CREATE TABLE IF NOT EXISTS computadores (
    id_pc BINARY(16) PRIMARY KEY,
    rut_dueno VARCHAR(12) NOT NULL
);

CREATE TABLE IF NOT EXISTS computador_componentes (
    computador_id BINARY(16) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    tipo VARCHAR(100) NOT NULL,
    INDEX idx_computador_id (computador_id),
    CONSTRAINT fk_computador FOREIGN KEY (computador_id) REFERENCES computadores(id_pc) ON DELETE CASCADE
);
