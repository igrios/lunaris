-- Agregamos las columnas de logística interurbana
ALTER TABLE localities ADD COLUMN kms_to_cordoba INT DEFAULT 150;
ALTER TABLE localities ADD COLUMN minutes_from_origin INT DEFAULT 0;

-- Seteamos los datos reales del folleto y traza de la Ruta 17
UPDATE localities SET kms_to_cordoba = 280, minutes_from_origin = 0 WHERE name = 'San Guillermo';
UPDATE localities SET kms_to_cordoba = 260, minutes_from_origin = 20 WHERE name = 'Suardi';
UPDATE localities SET kms_to_cordoba = 240, minutes_from_origin = 40 WHERE name = 'Morteros';
UPDATE localities SET kms_to_cordoba = 224, minutes_from_origin = 55 WHERE name = 'Brinkmann';
UPDATE localities SET kms_to_cordoba = 206, minutes_from_origin = 70 WHERE name = 'Porteña';
UPDATE localities SET kms_to_cordoba = 179, minutes_from_origin = 95 WHERE name = 'Freyre';
UPDATE localities SET kms_to_cordoba = 164, minutes_from_origin = 110 WHERE name = 'La Paquita';
UPDATE localities SET kms_to_cordoba = 149, minutes_from_origin = 125 WHERE name = 'Altos de Chipión';
UPDATE localities SET kms_to_cordoba = 124, minutes_from_origin = 150 WHERE name = 'Balnearia';
UPDATE localities SET kms_to_cordoba = 137, minutes_from_origin = 165 WHERE name = 'Miramar';