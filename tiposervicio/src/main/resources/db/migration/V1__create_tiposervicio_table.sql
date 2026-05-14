CREATE TABLE IF NOT EXISTS tipos_servicio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255) NOT NULL,
    costo_base DOUBLE NOT NULL
);

INSERT INTO tipos_servicio (nombre, descripcion, costo_base) VALUES
('Limpieza Superficial', 'Cambio de pasta térmica y limpieza de ventiladores.', 25000.0),
('Limpieza Profunda', 'Desarmado total y limpieza de componentes a fondo.', 45000.0),
('Reparación', 'Corrección de fallas físicas en placas o componentes.', 60000.0),
('Mejora (Upgrade)', 'Instalación de hardware nuevo (SSD, RAM, ETC).', 80000.0),
('Optimización', 'Ajustes de software, drivers y limpieza de sistema.', 30000.0);
