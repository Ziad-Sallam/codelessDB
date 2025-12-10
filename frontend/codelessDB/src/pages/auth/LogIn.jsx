import { useState, useEffect } from "react";
import "./LogIn.css";

import { FaUser } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { FcGoogle } from "react-icons/fc";

import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { login, redirectToGoogleAuth, parseApiError, validateToken } from "./fetch.js";
import { useAuth } from "../../components/AuthProvider.jsx";

const LogIn = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const { user, setUser } = useAuth();

  useEffect(() => {
    document.title = "Log in | CodeLess";
  }, []);

  useEffect(() => {
    const handleOAuthCallback = async () => {
      const token = searchParams.get("token");

      const oauthError = searchParams.get("error");

      if (token) {
        try {
          // Save token
          localStorage.setItem("authToken", token);
          

          // Validate token and update auth context
          const userData = await validateToken();
          setUser(userData);

          // Now navigate
          navigate("/diagrams", { replace: true });
        } catch (err) {
          setError("Authentication failed. Please try again.");
          localStorage.removeItem("authToken");
        }
        return;
      }

      if (oauthError) {
        setError("Google login failed. Please try again.");
        window.history.replaceState({}, document.title, "/login");
      }
    };

    handleOAuthCallback();
  }, [searchParams, navigate, setUser]);

  const handleGoogleLogin = () => {
    redirectToGoogleAuth(false);
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    if (!email || !password) {
      setError("Email and password are required");
      return;
    }

    setLoading(true);
    try {
      const data = await login(email, password);
      const token = data?.token ?? data;
      
      console.log(data);

      if (!token) {
        throw new Error("Login did not return an auth token.");
      }

      localStorage.setItem("authToken", token);
      const userData = await validateToken();
      setUser(userData);
      navigate("/diagrams", { replace: true });
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = () => {
    // Navigate to register page with forgot password flow
    navigate("/register?flow=forgot");
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
            <form onSubmit={handleLogin}>
              <h1>Log In</h1>

              <div className="input-box">
                <FaUser className="icon" />
                <input
                  type="text"
                  placeholder="Email"
                  value={email}
                  onChange={(e) => {
                    setEmail(e.target.value);
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

              <button type="submit" className="submit" disabled={loading}>
                {loading ? "Please wait..." : "Log In"}
              </button>

              {error && <p style={{ color: "red", textAlign: "center", marginTop: "10px" }}>{error}</p>}

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
                  Don't Have Account? <Link to="/register">Sign Up</Link>
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