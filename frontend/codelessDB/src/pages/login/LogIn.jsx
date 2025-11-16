import React, { useState ,useEffect} from "react";
import "./LogIn.css";

import { FaUser } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { FaEye, FaEyeSlash } from "react-icons/fa";

import { Link, useNavigate } from "react-router-dom";

const LogIn = () => {
  const [mail, setMail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    document.title = "Login | CodeLess";
  }, []);

  return (
    <div className="bg">
      <div className="title-section">
        <h1 className="Title">CodeLess</h1>
        <p className="slogon">Skip the code. Draw your data</p>
      </div>

      <div className="main">
        <div className="wrapper">
          <form>
            <h1>Log In</h1>

            {/* Username */}
            <div className="input-box">
              <FaUser className="icon" />
              <input
                type="text"
                placeholder="Username"
                value={mail}
                onChange={(e) => setMail(e.target.value)}
                required
              />
            </div>

            {/* Password */}
            <div className="input-box">
              <TbLockPassword className="icon" />

              <input
                type={showPassword ? "text" : "password"}
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />

              {/* 👁️ Show/Hide icon */}
              <span
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </span>
            </div>

            <div className="forgot-password">
              <p>
                <Link to="/ForgotPassword">Forgot Password?</Link>
              </p>
            </div>

            <div className="remember-forget">
              <label>
                <input type="checkbox" /> Remember me
              </label>
            </div>

            <button type="submit">Log In</button>
            {error && <p>{error}</p>}

            <div className="register">
              <p>
                Don't Have Account <Link to="/SignUp">signUp</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default LogIn;
