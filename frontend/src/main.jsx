import React, { useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

const modules = [
  { name: "Treasury", detail: "Transfers, balances, and on-chain settlement" },
  { name: "Payroll", detail: "Runs, employee entries, and tax archives" },
  { name: "Subscriptions", detail: "Plans, renewals, and cancellations" },
  { name: "Event index", detail: "Confirmed contract events and reorganization recovery" }
];

function App() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [token, setToken] = useState("");
  const [message, setMessage] = useState("");

  async function login(event) {
    event.preventDefault();
    setMessage("");
    const response = await fetch("/api/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ username, password })
    });
    if (!response.ok) {
      setMessage("Login failed");
      return;
    }
    const result = await response.json();
    setToken(result.accessToken);
    setMessage("Authenticated");
  }

  return (
    <main>
      <header>
        <div>
          <span className="brand-mark">C</span>
          <strong>chainERP</strong>
        </div>
        <span className={token ? "status connected" : "status"}>{token ? "Connected" : "Signed out"}</span>
      </header>
      <section className="hero">
        <div>
          <p className="eyebrow">OPERATIONS WORKSPACE</p>
          <h1>Finance and workforce,<br />one auditable ledger.</h1>
          <p className="subtitle">Manage treasury activity, payroll runs, subscriptions, and indexed blockchain events.</p>
        </div>
        <form onSubmit={login}>
          <label>Username<input value={username} onChange={(event) => setUsername(event.target.value)} /></label>
          <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          <button type="submit">Sign in</button>
          {message && <p className="message">{message}</p>}
        </form>
      </section>
      <section className="grid">
        {modules.map((module) => (
          <article key={module.name}>
            <div className="module-icon">{module.name.slice(0, 1)}</div>
            <h2>{module.name}</h2>
            <p>{module.detail}</p>
          </article>
        ))}
      </section>
    </main>
  );
}

createRoot(document.getElementById("root")).render(<App />);
