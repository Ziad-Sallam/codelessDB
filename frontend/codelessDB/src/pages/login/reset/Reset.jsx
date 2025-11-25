import { useState, useEffect } from "react";
import "./Reset.css";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { useNavigate } from "react-router-dom";
import axios from "axios";

export default function Reset() {
  const navigate = useNavigate();
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    const otpPurpose = localStorage.getItem("otpPurpose");

    if (otpPurpose !== "reset") {
      navigate('/login', { replace: true });
    }
  }, [navigate]);

  async function changePassword() {

    if (!password || !confirmPassword) {
      setError("Please fill in both fields");
      return;
    }

    if (!checkStrength(password)) {
      setError("Password is weak.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match!");
      return;
    }
    setError("");

    const token = localStorage.getItem("token for_reset");
    const resetSource = localStorage.getItem("resetSource"); // Check where user came from

    try {
      const response = await axios.put(
        "http://localhost:8080/user/update",
        {
          password: password
        },
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      // Clean up reset-specific tokens
      localStorage.removeItem('token for_reset');
      localStorage.removeItem('otpPurpose');
      localStorage.removeItem('resetSource');

      // Navigate based on source
      if (resetSource === "profile") {
        // User came from profile page - go back to profile
        localStorage.setItem('passwordResetSuccess', 'true');
        navigate("/userprofile");
      } else {
        // User came from login/forgot password flow - go to recovered
        localStorage.removeItem('otp');
        localStorage.removeItem('email');
        localStorage.setItem('recovered', true);
        navigate("/recovered");
      }

    } catch (err) {
      const serverMsg = err.response?.data?.message
        || err.response?.data
        || err.message
        || "Server unavailable. Please try again later.";

      setError(String(serverMsg));
    }
  }

  function getPasswordChecks(pass) {
    return {
      length: pass.length >= 8,
      upper: /[A-Z]/.test(pass),
      lower: /[a-z]/.test(pass),
      number: /[0-9]/.test(pass),
      symbol: /[^A-Za-z0-9]/.test(pass),
    };
  }

  const checks = getPasswordChecks(password);

  function checkStrength(pass) {
    const c = getPasswordChecks(pass);
    return c.length && c.upper && c.lower && c.number && c.symbol;
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
            <TbLockPassword className="icon" />
            <input
              type={showPassword ? "text" : "password"}
              placeholder="New Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            <span
              className="toggle-password"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <FaEyeSlash /> : <FaEye />}
            </span>
          </div>
          <ul className="password-rules">
            <li className={checks.length ? "valid" : "invalid"}>
              {checks.length ? "✓" : "✗"} At least 8 characters
            </li>
            <li className={checks.upper ? "valid" : "invalid"}>
              {checks.upper ? "✓" : "✗"} One uppercase letter (A-Z)
            </li>
            <li className={checks.lower ? "valid" : "invalid"}>
              {checks.lower ? "✓" : "✗"} One lowercase letter (a-z)
            </li>
            <li className={checks.number ? "valid" : "invalid"}>
              {checks.number ? "✓" : "✗"} One number (0-9)
            </li>
            <li className={checks.symbol ? "valid" : "invalid"}>
              {checks.symbol ? "✓" : "✗"} One special character (!@#$%^&*)
            </li>
          </ul>

          <div className="input-box">
            <TbLockPassword className="icon" />
            <input
              type={showPassword ? "text" : "password"}
              placeholder="Confirm Password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
            />
            <span
              className="toggle-password"
              onClick={() => setShowPassword(!showPassword)}
            >
              {showPassword ? <FaEyeSlash /> : <FaEye />}
            </span>
          </div>

          <div className="remember-forget" style={{ justifyContent: "center" }}>
            <label>
            </label>
          </div>

          <button onClick={changePassword} className="submit">Reset Password</button>
          {error && <p className="error-message">{error}</p>}
        </div>
      </div>
    </div>
  );
}