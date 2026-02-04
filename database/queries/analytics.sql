-- Analytics Queries for Smart Grid Load Balancing Simulator
-- PostgreSQL

-- ========================================
-- 1. Real-time Grid Health Dashboard
-- ========================================
-- Shows current load status of all nodes with latest readings
SELECT 
    gn.node_id,
    gn.region,
    gn.capacity,
    sr.load_reading AS current_load,
    ROUND((sr.load_reading / gn.capacity * 100)::numeric, 2) AS utilization_percent,
    CASE 
        WHEN sr.load_reading / gn.capacity > 0.85 THEN 'OVERLOADED'
        WHEN sr.load_reading / gn.capacity < 0.40 THEN 'UNDERUTILIZED'
        ELSE 'NORMAL'
    END AS status,
    sr.timestamp AS last_reading_time
FROM grid_nodes gn
LEFT JOIN LATERAL (
    SELECT * FROM sensor_readings 
    WHERE node_id = gn.node_id 
    ORDER BY timestamp DESC 
    LIMIT 1
) sr ON true
ORDER BY utilization_percent DESC;

-- ========================================
-- 2. Historical Load Trends by Region
-- ========================================
-- Average load per region over the last 24 hours
SELECT 
    gn.region,
    DATE_TRUNC('hour', sr.timestamp) AS hour,
    ROUND(AVG(sr.load_reading)::numeric, 2) AS avg_load,
    ROUND(AVG(sr.load_reading / gn.capacity * 100)::numeric, 2) AS avg_utilization_percent,
    COUNT(*) AS reading_count
FROM sensor_readings sr
JOIN grid_nodes gn ON sr.node_id = gn.node_id
WHERE sr.timestamp > CURRENT_TIMESTAMP - INTERVAL '24 hours'
GROUP BY gn.region, DATE_TRUNC('hour', sr.timestamp)
ORDER BY gn.region, hour DESC;

-- ========================================
-- 3. Peak Load Analysis
-- ========================================
-- Identifies peak load times and nodes
SELECT 
    sr.node_id,
    gn.region,
    sr.load_reading AS peak_load,
    gn.capacity,
    ROUND((sr.load_reading / gn.capacity * 100)::numeric, 2) AS peak_utilization,
    sr.timestamp AS peak_time
FROM sensor_readings sr
JOIN grid_nodes gn ON sr.node_id = gn.node_id
WHERE sr.timestamp > CURRENT_TIMESTAMP - INTERVAL '7 days'
  AND sr.load_reading = (
      SELECT MAX(load_reading) 
      FROM sensor_readings 
      WHERE node_id = sr.node_id
  )
ORDER BY peak_utilization DESC;

-- ========================================
-- 4. Overload Event Frequency
-- ========================================
-- Count of overload events per node
SELECT 
    node_id,
    COUNT(*) AS overload_count,
    MIN(timestamp) AS first_overload,
    MAX(timestamp) AS last_overload,
    ROUND(AVG(utilization_percent)::numeric, 2) AS avg_utilization_during_overload
FROM load_events
WHERE event_type = 'OVERLOAD'
  AND timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY node_id
ORDER BY overload_count DESC;

-- ========================================
-- 5. Optimization Effectiveness
-- ========================================
-- Analyzes the impact of optimization actions
SELECT 
    oa.from_node_id,
    oa.to_node_id,
    COUNT(*) AS transfer_count,
    ROUND(SUM(oa.amount)::numeric, 2) AS total_load_transferred,
    ROUND(AVG(oa.amount)::numeric, 2) AS avg_transfer_amount,
    MIN(oa.timestamp) AS first_transfer,
    MAX(oa.timestamp) AS last_transfer
FROM optimization_actions oa
WHERE oa.timestamp > CURRENT_TIMESTAMP - INTERVAL '7 days'
GROUP BY oa.from_node_id, oa.to_node_id
ORDER BY total_load_transferred DESC;

-- ========================================
-- 6. Voltage Stability Analysis
-- ========================================
-- Monitors voltage fluctuations
SELECT 
    sr.node_id,
    DATE_TRUNC('day', sr.timestamp) AS day,
    ROUND(AVG(sr.voltage)::numeric, 2) AS avg_voltage,
    ROUND(MIN(sr.voltage)::numeric, 2) AS min_voltage,
    ROUND(MAX(sr.voltage)::numeric, 2) AS max_voltage,
    ROUND(STDDEV(sr.voltage)::numeric, 2) AS voltage_stddev
FROM sensor_readings sr
WHERE sr.timestamp > CURRENT_TIMESTAMP - INTERVAL '7 days'
GROUP BY sr.node_id, DATE_TRUNC('day', sr.timestamp)
HAVING STDDEV(sr.voltage) > 5 -- Flag nodes with high voltage variability
ORDER BY voltage_stddev DESC;

-- ========================================
-- 7. Frequency Deviation Detection
-- ========================================
-- Identifies frequency anomalies (should be ~60 Hz)
SELECT 
    sr.node_id,
    sr.timestamp,
    sr.frequency,
    ABS(sr.frequency - 60.0) AS deviation_from_nominal,
    sr.load_reading,
    CASE 
        WHEN ABS(sr.frequency - 60.0) > 0.5 THEN 'CRITICAL'
        WHEN ABS(sr.frequency - 60.0) > 0.3 THEN 'WARNING'
        ELSE 'NORMAL'
    END AS frequency_status
FROM sensor_readings sr
WHERE sr.timestamp > CURRENT_TIMESTAMP - INTERVAL '24 hours'
  AND ABS(sr.frequency - 60.0) > 0.2
ORDER BY deviation_from_nominal DESC
LIMIT 100;

-- ========================================
-- 8. Predictive Maintenance - Node Risk Score
-- ========================================
-- Calculates risk score based on historical stress
WITH node_stress AS (
    SELECT 
        node_id,
        COUNT(*) FILTER (WHERE event_type = 'OVERLOAD') AS overload_count,
        COUNT(*) FILTER (WHERE event_type = 'CRITICAL') AS critical_count,
        AVG(utilization_percent) AS avg_utilization
    FROM load_events
    WHERE timestamp > CURRENT_TIMESTAMP - INTERVAL '30 days'
    GROUP BY node_id
)
SELECT 
    gn.node_id,
    gn.region,
    COALESCE(ns.overload_count, 0) AS overload_events,
    COALESCE(ns.critical_count, 0) AS critical_events,
    ROUND(COALESCE(ns.avg_utilization, 0)::numeric, 2) AS avg_utilization,
    ROUND((
        COALESCE(ns.overload_count, 0) * 2 + 
        COALESCE(ns.critical_count, 0) * 5 + 
        COALESCE(ns.avg_utilization, 0) / 10
    )::numeric, 2) AS risk_score
FROM grid_nodes gn
LEFT JOIN node_stress ns ON gn.node_id = ns.node_id
ORDER BY risk_score DESC;

-- ========================================
-- 9. Daily Load Profile
-- ========================================
-- Average load by hour of day (for capacity planning)
SELECT 
    EXTRACT(HOUR FROM sr.timestamp) AS hour_of_day,
    ROUND(AVG(sr.load_reading)::numeric, 2) AS avg_load,
    ROUND(MIN(sr.load_reading)::numeric, 2) AS min_load,
    ROUND(MAX(sr.load_reading)::numeric, 2) AS max_load,
    COUNT(*) AS sample_count
FROM sensor_readings sr
WHERE sr.timestamp > CURRENT_TIMESTAMP - INTERVAL '7 days'
GROUP BY EXTRACT(HOUR FROM sr.timestamp)
ORDER BY hour_of_day;

-- ========================================
-- 10. Regional Load Balance
-- ========================================
-- Compares load distribution across regions
SELECT 
    gn.region,
    COUNT(DISTINCT gn.node_id) AS node_count,
    ROUND(SUM(gn.capacity)::numeric, 2) AS total_capacity,
    ROUND(AVG(sr.load_reading)::numeric, 2) AS avg_current_load,
    ROUND((AVG(sr.load_reading) / AVG(gn.capacity) * 100)::numeric, 2) AS avg_utilization,
    ROUND(SUM(gn.capacity) - AVG(sr.load_reading) * COUNT(DISTINCT gn.node_id)::numeric, 2) AS available_capacity
FROM grid_nodes gn
LEFT JOIN LATERAL (
    SELECT load_reading 
    FROM sensor_readings 
    WHERE node_id = gn.node_id 
    ORDER BY timestamp DESC 
    LIMIT 1
) sr ON true
GROUP BY gn.region
ORDER BY avg_utilization DESC;
