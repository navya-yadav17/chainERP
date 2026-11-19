CREATE TABLE treasury_transactions (
    id UUID PRIMARY KEY,
    organization_id UUID,
    tx_hash VARCHAR(66),
    from_account VARCHAR(255) NOT NULL,
    to_account VARCHAR(255) NOT NULL,
    amount NUMERIC(38, 18) NOT NULL CHECK (amount > 0),
    currency VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_treasury_transactions_created
    ON treasury_transactions (organization_id, created_at DESC);

CREATE TABLE payroll_runs (
    id UUID PRIMARY KEY,
    organization_id UUID,
    run_date DATE NOT NULL,
    total_amount NUMERIC(38, 18) NOT NULL CHECK (total_amount >= 0),
    currency VARCHAR(20) NOT NULL,
    status VARCHAR(30) NOT NULL,
    generated_by VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE payroll_entries (
    id UUID PRIMARY KEY,
    payroll_run_id UUID NOT NULL REFERENCES payroll_runs(id) ON DELETE CASCADE,
    employee_id VARCHAR(255) NOT NULL,
    gross_amount NUMERIC(38, 18) NOT NULL CHECK (gross_amount >= 0),
    net_amount NUMERIC(38, 18) NOT NULL CHECK (net_amount >= 0),
    taxes NUMERIC(38, 18) NOT NULL CHECK (taxes >= 0),
    status VARCHAR(30) NOT NULL
);

CREATE INDEX idx_payroll_entries_run ON payroll_entries (payroll_run_id);

CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,
    organization_id UUID,
    subscriber VARCHAR(255) NOT NULL,
    plan VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    next_billing_date DATE,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
