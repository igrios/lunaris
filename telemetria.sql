-- ==============================================================================
-- CONSULTAS DE ANALÍTICA Y TELEMETRÍA - LUNARIS ANSENUZA (DBeaver)
-- Motor: PostgreSQL
-- Nota: En DBeaver podés ejecutar cada bloque situando el cursor sobre él y 
-- presionando Ctrl + Enter (Cmd + Enter en Mac).
-- ==============================================================================


-- ------------------------------------------------------------------------------
-- 1. EMBUDO DE CONVERSIÓN (Funnel por Hitos)
-- Muestra el volumen de usuarios y sesiones que alcanzan cada etapa del bot.
-- Rango: Últimos 7 días.
-- ------------------------------------------------------------------------------
SELECT 
    e.event_type AS hito,
    COUNT(DISTINCT e.session_id) AS total_sesiones,
    COUNT(DISTINCT s.subject_key) AS usuarios_unicos
FROM chatbot_analytics_events e
JOIN chatbot_analytics_sessions s ON s.id = e.session_id
WHERE s.started_at >= NOW() - INTERVAL '7 days'
  AND s.environment = 'prod'
  AND s.source = 'WHATSAPP'
  AND NOT s.is_test
  AND s.audience <> 'DRIVER'
  AND e.event_type IN (
      'SESSION_STARTED', 
      'PRICE_REQUESTED', 
      'PRICE_SENT', 
      'ROUTE_SELECTED', 
      'DATE_SELECTED', 
      'PASSENGER_DATA_COMPLETED', 
      'SUMMARY_SENT', 
      'BOOKING_CREATED'
  )
GROUP BY e.event_type
ORDER BY total_sesiones DESC;


-- ------------------------------------------------------------------------------
-- 2. DÓNDE ABANDONA LA GENTE (Paso Pendiente y Motivos de Cierre)
-- Identifica en qué pantalla/paso exacto el usuario dejó de responder o canceló.
-- Rango: Últimos 7 días.
-- ------------------------------------------------------------------------------
SELECT 
    COALESCE(last_milestone, 'INICIO') AS ultimo_hito_alcanzado,
    COALESCE(current_step, 'DESCONOCIDO') AS paso_donde_se_trabó,
    COALESCE(end_reason, 'SIN_MOTIVO_ESPECÍFICO') AS motivo_cierre,
    COUNT(*) AS total_abandonos
FROM chatbot_analytics_sessions
WHERE started_at >= NOW() - INTERVAL '7 days'
  AND environment = 'prod'
  AND source = 'WHATSAPP'
  AND NOT is_test
  AND audience <> 'DRIVER'
  AND status IN ('ABANDONED', 'EXPIRED', 'DECLINED')
GROUP BY last_milestone, current_step, end_reason
ORDER BY total_abandonos DESC;


-- ------------------------------------------------------------------------------
-- 3. TASA GLOBAL DE CONVERSIÓN Y TIEMPO PROMEDIO DE RESERVA
-- Métricas clave de rendimiento (KPIs) sobre usuarios e intenciones de compra.
-- Rango: Últimos 30 días.
-- ------------------------------------------------------------------------------
SELECT 
    COUNT(DISTINCT subject_key) AS usuarios_totales,
    COUNT(*) AS sesiones_totales,
    COUNT(*) FILTER (WHERE booking_started_at IS NOT NULL) AS sesiones_con_intencion,
    COUNT(DISTINCT booking_group_code) FILTER (WHERE completed_at IS NOT NULL) AS reservas_creadas,
    ROUND(
        100.0 * COUNT(DISTINCT subject_key) FILTER (WHERE completed_at IS NOT NULL) 
        / NULLIF(COUNT(DISTINCT subject_key), 0), 2
    ) AS tasa_conversion_pct,
    AVG(completed_at - booking_started_at) FILTER (WHERE completed_at IS NOT NULL) AS tiempo_promedio_para_reservar
FROM chatbot_analytics_sessions
WHERE started_at >= NOW() - INTERVAL '30 days'
  AND environment = 'prod'
  AND source = 'WHATSAPP'
  AND NOT is_test
  AND audience <> 'DRIVER';


-- ------------------------------------------------------------------------------
-- 4. MOTIVOS DE BLOQUEO OPERATIVO / TÉCNICO (Sin Cupo, Sin Tarifa, etc.)
-- Muestra las oportunidades de venta perdidas por reglas del negocio o entradas no válidas.
-- Rango: Últimos 7 días.
-- ------------------------------------------------------------------------------
SELECT 
    reason_code AS motivo_bloqueo,
    COUNT(*) AS cantidad_ocurrencias
FROM chatbot_analytics_events
WHERE event_type IN ('FLOW_BLOCKED', 'INPUT_REJECTED')
  AND occurred_at >= NOW() - INTERVAL '7 days'
GROUP BY reason_code
ORDER BY cantidad_ocurrencias DESC;


-- ------------------------------------------------------------------------------
-- 5. ÚLTIMAS 20 SESIONES REGISTRADAS (Auditoría de Datos en Vivo)
-- Permite inspeccionar el estado en tiempo real de las interacciones recientes.
-- ------------------------------------------------------------------------------
SELECT 
    id AS session_id,
    subject_key,
    status,
    current_step,
    last_milestone,
    started_at,
    last_interaction_at,
    booking_group_code
FROM chatbot_analytics_sessions
WHERE environment = 'prod'
  AND NOT is_test
ORDER BY started_at DESC
LIMIT 20;