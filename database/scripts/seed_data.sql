-- Sample Seed Data for Smart Grid Load Balancing Simulator
-- PostgreSQL

-- Insert sample grid nodes
INSERT INTO grid_nodes (node_id, region, capacity, last_updated) VALUES
    ('NODE-1', 'North', 120.5, CURRENT_TIMESTAMP),
    ('NODE-2', 'North', 115.0, CURRENT_TIMESTAMP),
    ('NODE-3', 'South', 130.2, CURRENT_TIMESTAMP),
    ('NODE-4', 'South', 125.8, CURRENT_TIMESTAMP),
    ('NODE-5', 'East', 110.0, CURRENT_TIMESTAMP),
    ('NODE-6', 'East', 118.5, CURRENT_TIMESTAMP),
    ('NODE-7', 'West', 135.0, CURRENT_TIMESTAMP),
    ('NODE-8', 'West', 122.3, CURRENT_TIMESTAMP),
    ('NODE-9', 'Central', 140.0, CURRENT_TIMESTAMP),
    ('NODE-10', 'Central', 128.7, CURRENT_TIMESTAMP);

-- Insert sample sensor readings (last 24 hours)
INSERT INTO sensor_readings (sensor_id, node_id, timestamp, load_reading, voltage, frequency)
SELECT 
    'SENSOR-NODE-' || g.node_id,
    g.node_id,
    CURRENT_TIMESTAMP - (random() * INTERVAL '24 hours'),
    g.capacity * (0.3 + random() * 0.6), -- Random load between 30-90% of capacity
    400 + (random() * 20), -- Voltage between 400-420 kV
    60 + (random() * 0.5) -- Frequency around 60 Hz
FROM grid_nodes g
CROSS JOIN generate_series(1, 100) -- 100 readings per node
ORDER BY random();

-- Insert sample load events
INSERT INTO load_events (node_id, timestamp, load_value, utilization_percent, event_type)
SELECT 
    node_id,
    timestamp,
    load_reading,
    (load_reading / capacity) * 100,
    CASE 
        WHEN (load_reading / capacity) > 0.85 THEN 'OVERLOAD'
        WHEN (load_reading / capacity) < 0.40 THEN 'UNDERLOAD'
        ELSE 'NORMAL'
    END
FROM sensor_readings sr
JOIN grid_nodes gn ON sr.node_id = gn.node_id
WHERE sr.id % 10 = 0; -- Sample every 10th reading

-- Insert sample optimization actions
INSERT INTO optimization_actions (from_node_id, to_node_id, amount, action_type, timestamp)
VALUES
    ('NODE-1', 'NODE-2', 15.5, 'LOAD_TRANSFER', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
    ('NODE-3', 'NODE-4', 12.3, 'LOAD_TRANSFER', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
    ('NODE-5', 'NODE-6', 8.7, 'LOAD_TRANSFER', CURRENT_TIMESTAMP - INTERVAL '6 hours'),
    ('NODE-7', 'NODE-8', 20.1, 'LOAD_TRANSFER', CURRENT_TIMESTAMP - INTERVAL '8 hours'),
    ('NODE-9', 'NODE-10', 18.4, 'LOAD_TRANSFER', CURRENT_TIMESTAMP - INTERVAL '10 hours');

-- Display summary
SELECT 'Grid Nodes:', COUNT(*) FROM grid_nodes
UNION ALL
SELECT 'Sensor Readings:', COUNT(*) FROM sensor_readings
UNION ALL
SELECT 'Load Events:', COUNT(*) FROM load_events
UNION ALL
SELECT 'Optimization Actions:', COUNT(*) FROM optimization_actions;
