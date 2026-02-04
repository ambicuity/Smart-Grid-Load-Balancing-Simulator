-- Smart Grid Load Balancing Simulator Database Schema
-- PostgreSQL 12+

-- Drop existing tables if they exist
DROP TABLE IF EXISTS optimization_actions CASCADE;
DROP TABLE IF EXISTS load_events CASCADE;
DROP TABLE IF EXISTS sensor_readings CASCADE;
DROP TABLE IF EXISTS grid_nodes CASCADE;

-- Grid Nodes Table
-- Represents substations in the smart grid
CREATE TABLE grid_nodes (
    id SERIAL PRIMARY KEY,
    node_id VARCHAR(50) NOT NULL UNIQUE,
    region VARCHAR(100) NOT NULL,
    capacity DOUBLE PRECISION NOT NULL,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity CHECK (capacity > 0)
);

CREATE INDEX idx_grid_nodes_region ON grid_nodes(region);
CREATE INDEX idx_grid_nodes_last_updated ON grid_nodes(last_updated);

-- Sensor Readings Table
-- Stores real-time sensor data from grid nodes
CREATE TABLE sensor_readings (
    id SERIAL PRIMARY KEY,
    sensor_id VARCHAR(50) NOT NULL,
    node_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    load_reading DOUBLE PRECISION NOT NULL,
    voltage DOUBLE PRECISION NOT NULL,
    frequency DOUBLE PRECISION NOT NULL,
    CONSTRAINT chk_load_reading CHECK (load_reading >= 0)
);

CREATE INDEX idx_sensor_readings_node_timestamp ON sensor_readings(node_id, timestamp DESC);
CREATE INDEX idx_sensor_readings_timestamp ON sensor_readings(timestamp DESC);
CREATE INDEX idx_sensor_readings_sensor_id ON sensor_readings(sensor_id);

-- Load Events Table
-- Records significant load changes and events
CREATE TABLE load_events (
    id SERIAL PRIMARY KEY,
    node_id VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    load_value DOUBLE PRECISION NOT NULL,
    utilization_percent DOUBLE PRECISION NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    CONSTRAINT chk_event_type CHECK (event_type IN ('NORMAL', 'OVERLOAD', 'UNDERLOAD', 'CRITICAL'))
);

CREATE INDEX idx_load_events_node_timestamp ON load_events(node_id, timestamp DESC);
CREATE INDEX idx_load_events_event_type ON load_events(event_type);
CREATE INDEX idx_load_events_timestamp ON load_events(timestamp DESC);

-- Optimization Actions Table
-- Tracks load balancing actions taken by the system
CREATE TABLE optimization_actions (
    id SERIAL PRIMARY KEY,
    from_node_id VARCHAR(50) NOT NULL,
    to_node_id VARCHAR(50) NOT NULL,
    amount DOUBLE PRECISION NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    CONSTRAINT chk_amount CHECK (amount > 0)
);

CREATE INDEX idx_optimization_actions_timestamp ON optimization_actions(timestamp DESC);
CREATE INDEX idx_optimization_actions_from_node ON optimization_actions(from_node_id);
CREATE INDEX idx_optimization_actions_to_node ON optimization_actions(to_node_id);

-- Comments
COMMENT ON TABLE grid_nodes IS 'Stores information about grid nodes (substations)';
COMMENT ON TABLE sensor_readings IS 'Real-time sensor data from grid monitoring';
COMMENT ON TABLE load_events IS 'Historical log of significant load events';
COMMENT ON TABLE optimization_actions IS 'Load balancing actions performed by the system';

-- Grant permissions (adjust as needed)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO smartgrid_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO smartgrid_user;
