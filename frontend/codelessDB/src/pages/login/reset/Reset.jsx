import React, { useContext, useState } from "react";
import { RecoveryContext } from "../../../App";
import "./Reset.css";

export default function Reset() {
  const { setPage } = useContext(RecoveryContext);
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  function changePassword() {
    if (!password || !confirmPassword) {
      alert("Please fill in both fields");
      return;
    }
    if (password !== confirmPassword) {
      alert("Passwords do not match!");
      return;
    }
    setPage("recovered");
  }

  return (
    <div className="bg">
      <div className="title-section">
        <h1 className="Title">CodeLess</h1>
        <p className="slogon">Skip the code. Draw your data</p>
      </div>

      <div className="main">
        <div className="wrapper">
          <h1>Reset Password</h1>

          <div className="input-box">
            <input
              type="password"
              placeholder="New Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>

          <div className="input-box">
            <input
              type="password"
              placeholder="Confirm Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
          </div>

          <div className="remember-forget" style={{ justifyContent: "center" }}>
            <label>
              <input type="checkbox" />
              <span style={{ marginLeft: "5px" }}>
                I accept the{" "}
                <a href="#" style={{ textDecoration: "underline", color: "#000" }}>
                  Terms and Conditions
                </a>
              </span>
            </label>
          </div>

          <button onClick={changePassword}>Reset Password</button>
        </div>
      </div>
    </div>
  );
}
