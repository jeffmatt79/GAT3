-- V1__initial_schema.sql

DROP TABLE IF EXISTS app_config CASCADE;
DROP TABLE IF EXISTS aghu_discharge_track CASCADE;
DROP TABLE IF EXISTS discharge_reports CASCADE;
DROP TABLE IF EXISTS discharges CASCADE;

CREATE TABLE app_config (
    config_key VARCHAR(255) PRIMARY KEY,
    config_value VARCHAR(255)
);

CREATE TABLE discharges (
    id BIGSERIAL PRIMARY KEY,
    aghu_discharge_id BIGINT UNIQUE NOT NULL,
    status VARCHAR(20) DEFAULT 'pendente',
    clinical_summary VARCHAR(10000),
    therapeutic_guidance VARCHAR(255),
    specialized_return_guidance VARCHAR(255),
    additional_information VARCHAR(255),
    follow_up_scheduling_suggestion VARCHAR(255),
    responsible_physician VARCHAR(255),
    responsible_physician_registration VARCHAR(255),
    report_date DATE,
    medications_json VARCHAR(10000),
    problem_list_json VARCHAR(10000),
    ambulatory_profile_adequate BOOLEAN,
    ambulatory_profile_initial_diagnosis_cid_10 VARCHAR(255),
    ambulatory_profile_discharge_reason VARCHAR(255),
    service_info_ambulatory_name VARCHAR(255),
    service_info_subspecialty_or_service_chief VARCHAR(255),
    patient_info_full_name VARCHAR(255),
    patient_info_address VARCHAR(255),
    patient_info_birth_date DATE,
    patient_info_gender VARCHAR(255),
    patient_info_contacts VARCHAR(255),
    patient_info_cns_or_cpf VARCHAR(255),
    patient_info_aghu_record VARCHAR(255),
    patient_info_mother_name VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS appointments (
    seq_atendimento BIGINT PRIMARY KEY,
    alterado_em TIMESTAMP,
    nome_paciente VARCHAR(255),
    prontuario BIGINT,
    especialidade VARCHAR(255),
    pendencia_preenchimento BOOLEAN,
    alta_ambulatorial BOOLEAN,
    status VARCHAR(50),
    last_synced_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_alterado_em ON appointments(alterado_em);

INSERT INTO app_config (config_key, config_value)
VALUES ('scheduling.discharge_check_delay', '300000');

