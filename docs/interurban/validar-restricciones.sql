-- Ejecutar después de V129, exclusivamente en PostgreSQL desechable.
-- Fixtures efímeras; no requiere tablas del servicio Córdoba.
BEGIN;
DO $$
DECLARE
    t UUID := gen_random_uuid();
    f UUID := gen_random_uuid();
    l UUID := gen_random_uuid();
    r UUID;
    first_r UUID;
    e UUID := gen_random_uuid();
    d UUID := gen_random_uuid();
    i INTEGER;
BEGIN
    INSERT INTO interurban.interurban_fares VALUES (f,0,1,CURRENT_DATE,100,20,'ARS');
    INSERT INTO interurban.trips(id,service_date,departure_at,closes_at,direction,status)
        VALUES(t,CURRENT_DATE,CURRENT_TIMESTAMP + INTERVAL '1 day',CURRENT_TIMESTAMP,1,'OPEN');
    INSERT INTO interurban.trip_legs(id,trip_id,corridor_leg_id)
        VALUES(l,t,'00000000-0000-0000-0000-000000000001');
    FOR i IN 1..5 LOOP
        r := gen_random_uuid();
        INSERT INTO interurban.reservations(id,booking_id,trip_id,origin_stop,destination_stop,
            passenger_name,phone,pickup_address,dropoff_address,fare_id,fare,commission,status,hold_expires_at)
        VALUES(r,r,t,0,1,'Fixture','000','Origen','Destino',f,100,20,'HELD',CURRENT_TIMESTAMP);
        IF i = 1 THEN first_r := r; END IF;
        IF i <= 4 THEN
            INSERT INTO interurban.leg_seats VALUES(t,l,i,r);
        END IF;
    END LOOP;
    BEGIN
        INSERT INTO interurban.leg_seats VALUES(t,l,5,r);
        RAISE EXCEPTION 'ERROR: aceptó quinta plaza';
    EXCEPTION WHEN check_violation THEN NULL;
    END;
    BEGIN
        INSERT INTO interurban.leg_seats VALUES(t,l,1,r);
        RAISE EXCEPTION 'ERROR: aceptó plaza duplicada';
    EXCEPTION WHEN unique_violation THEN NULL;
    END;
    BEGIN
        UPDATE interurban.trip_legs SET capacity=5 WHERE id=l;
        RAISE EXCEPTION 'ERROR: aceptó capacidad cinco';
    EXCEPTION WHEN check_violation THEN NULL;
    END;
    INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key)
        VALUES(e,d,first_r,'EARNED',80,'fixture-credit');
    BEGIN
        INSERT INTO interurban.driver_ledger(id,driver_id,reservation_id,entry_type,amount,event_key)
            VALUES(gen_random_uuid(),d,first_r,'EARNED',80,'fixture-credit-duplicate');
        RAISE EXCEPTION 'ERROR: aceptó doble crédito';
    EXCEPTION WHEN unique_violation THEN NULL;
    END;
    BEGIN
        UPDATE interurban.driver_ledger SET amount=0 WHERE id=e;
        RAISE EXCEPTION 'ERROR: ledger mutable';
    EXCEPTION WHEN raise_exception THEN
        IF SQLERRM <> 'driver_ledger es inmutable; registrar un asiento compensatorio' THEN RAISE; END IF;
    END;
    RAISE NOTICE 'OK: máximo cuatro, unicidad de plaza/crédito y ledger inmutable';
END;
$$;
ROLLBACK;
