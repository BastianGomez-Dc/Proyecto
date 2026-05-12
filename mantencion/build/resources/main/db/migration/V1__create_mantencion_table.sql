CREATE TABLE IF NOT EXISTS mantenciones (
	id_ticket BINARY(16) PRIMARY KEY,
	id_pc BINARY(16) NOT NULL,
	motivo VARCHAR(500) NOT NULL,
	tipo_servicio VARCHAR(50) NOT NULL,
	costo_total DOUBLE NOT NULL,
	fecha_entrada DATETIME NOT NULL,
	estado VARCHAR(50) NOT NULL
);
