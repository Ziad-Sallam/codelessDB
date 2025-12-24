import { useEffect, useState } from "react";
import "./LogIn.css";

import { FaEye, FaEyeSlash, FaUser } from "react-icons/fa";
import { FcGoogle } from "react-icons/fc";
import { TbLockPassword } from "react-icons/tb";

import { Link, useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../components/AuthProvider.jsx";
import { login, parseApiError, redirectToGoogleAuth, validateToken } from "./fetch.js";

const LogIn = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState(location.state?.email || "");
  const [password, setPassword] = useState(location.state?.password || "");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [searchParams] = useSearchParams();
  const [loading, setLoading] = useState(false);
  const { user, setUser } = useAuth();

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
      if (err.response?.status === 401) {
        setError("Oops! That login didn’t work.\nCheck your password and try again.");
      } else if (err.response?.status === 404) {
        setError("Oops! Email not found.\nPlease check the email or sign up.");
      } else {
        setError(parseApiError(err));
      }
    } finally {
      setLoading(false);
    }
  };

  const handleForgotPassword = () => {
    // Navigate to register page with forgot password flow
    navigate(`/register?flow=forgot&email=${encodeURIComponent(email)}`, {
      state: { email, password }
    });
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

              {error && <p style={{ color: "red", textAlign: "center", marginTop: "10px", whiteSpace: "pre-line" }}>{error}</p>}

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