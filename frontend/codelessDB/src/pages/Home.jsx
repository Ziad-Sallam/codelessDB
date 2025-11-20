import React from "react";

export default function Home() {
  return (
    <main style={styles.container}>
      <section style={styles.card}>
        <h1 style={styles.title}>Welcome to Codeless DB</h1>
        <p style={styles.subtitle}>
          A simple, no-code way to build and manage your data quickly.
        </p>
      </section>
    </main>
  );
}

const styles = {
  container: {
    minHeight: "100vh",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    background: "linear-gradient(180deg,#f7f9fc,#ffffff)",
    padding: "2rem",
    boxSizing: "border-box",
  },
  card: {
    maxWidth: 720,
    width: "100%",
    textAlign: "center",
    padding: "3rem 2rem",
    borderRadius: 12,
    boxShadow: "0 6px 30px rgba(20,30,50,0.08)",
    background: "#fff",
  },
  title: {
    margin: 0,
    fontSize: "2rem",
    lineHeight: 1.1,
    color: "#0f172a",
  },
  subtitle: {
    marginTop: "0.75rem",
    color: "#475569",
    fontSize: "1rem",
  },
};