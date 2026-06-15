CREATE TABLE business_parameters (
    parameter_key VARCHAR(100) PRIMARY KEY,
    parameter_value VARCHAR(100) NOT NULL
);

INSERT INTO business_parameters(parameter_key, parameter_value)
VALUES ('ONE_WAY_EXTRA_AMOUNT', '8000');

INSERT INTO business_parameters(parameter_key, parameter_value)
VALUES ('PRICE_PER_KM', '1000');