// src/components/auth/LogIn.jsx (or wherever you keep it)
import { useState, useEffect, useContext } from "react";
import "./LogIn.css";

import { FaUser } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { FcGoogle } from "react-icons/fc";

import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { RecoveryContext } from "../../App";

import {
  login,
  requestPasswordReset,
  sendOtp,
  parseApiError,
} from "./fetch.js";

const LogIn = () => {
  const navigate = useNavigate();
  const { setEmail } = useContext(RecoveryContext);

  const [mail, setMail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);

  // Run once: check token, set title, clean url
  useEffect(() => {
    document.title = "Log in | CodeLess";
    // Clean URL (only once)
    window.history.replaceState({}, "LogIn | CodeLess", "/login");

    const token = localStorage.getItem("authToken");
    if (token) {
      // If any token exists (even invalid), redirect to diagrams.
      navigate("/diagrams", { replace: true });
    }
    // navigate is stable from react-router; include it to satisfy hooks rules.
  }, [navigate]);

  // Watch OAuth error param separately
  useEffect(() => {
    const oauthError = searchParams.get("error");
    if (oauthError) {
      setError("Google login failed. Please try again.");
    }
  }, [searchParams]);

  const handleGoogleLogin = () => {
    // redirect user to backend oauth endpoint
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleLogin = async () => {
    setError("");
    if (!mail || !password) {
      setError("Email and password are required");
      return;
    }

    setLoading(true);
    try {
      const data = await login(mail, password);

      // backend might return token string or { token: '...' }
      const token = data?.token ?? data;

      if (!token) {
        // defensive: if backend didn't return token but status is OK, still handle gracefully
        throw new Error("Login did not return an auth token.");
      }

      localStorage.setItem("authToken", token);
      navigate("/diagrams", { replace: true });
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = async () => {
    setError("");
    if (!mail) {
      setError("Please enter your email");
      return;
    }

    setLoading(true);

    try {
      const resetToken = await requestPasswordReset(mail);
      // store whatever the backend returns
      localStorage.setItem("token_for_reset", resetToken);

      const otp = await sendOtp(mail);
      localStorage.setItem("otp", otp);
      localStorage.setItem("email", mail);
      localStorage.setItem("otpPurpose", "reset");
      setEmail(mail);

      navigate("/otp");
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login">
      <div className="bg">
        <div className="title-section">
          <h1 className="Title">CodeLess</h1>
          <p className="slogon">Skip the code. Draw your data</p>
        </div>

        <div className="main">
          <div className="wrapper">
            <form onSubmit={(e) => e.preventDefault()}>
              <h1>Log In</h1>

              <div className="input-box">
                <FaUser className="icon" />
                <input
                  type="text"
                  placeholder="Email"
                  value={mail}
                  onChange={(e) => {
                    setMail(e.target.value);
                    setError("");
                  }}
                  required
                />
              </div>

              <div className="input-box">
                <TbLockPassword className="icon" />
                <input
                  type={showPassword ? "text" : "password"}
                  placeholder="Password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <span
                  className="toggle-password"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? <FaEyeSlash /> : <FaEye />}
                </span>
              </div>

              <div className="forgot-password">
                <p
                  onClick={handleForgotPassword}
                  style={{ cursor: "pointer", margin: "6px" }}
                >
                  Forgot Password?
                </p>
              </div>

              <button
                type="button"
                className="submit"
                onClick={handleLogin}
                disabled={loading}
              >
                {loading ? "Please wait..." : "Log In"}
              </button>
              {error && <p style={{ color: "red" }}>{error}</p>}

              <div className="divider">
                <span>OR</span>
              </div>

              <button
                type="button"
                className="google-button"
                onClick={handleGoogleLogin}
                disabled={loading}
              >
                <FcGoogle className="google-icon" />
                Log in with Google
              </button>

              <div className="register">
                <p>
                  Don't Have Account? <Link to="/signup">Sign Up</Link>
                </p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LogIn;
