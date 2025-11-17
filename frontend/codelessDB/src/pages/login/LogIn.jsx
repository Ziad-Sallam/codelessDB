import React, { useState ,useEffect,useContext } from "react";
import emailjs from '@emailjs/browser';
import "./LogIn.css";

import { FaUser } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { FaEye, FaEyeSlash } from "react-icons/fa";

import { Link, useNavigate } from "react-router-dom";
import { RecoveryContext } from "../../App";

const LogIn = () => {
  const navigate = useNavigate();
  const { setEmail } = useContext(RecoveryContext);
  const [mail, setMail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  useEffect(() => {
    document.title = "Login | CodeLess";
  }, []);
  const handleforgotPassword = async () => {
    if(!mail){
      setError("Please enter your email to reset password");
      return;
    }
    const generatedOTP = Math.floor(10000 + Math.random() * 90000).toString();
    try {
      const response = await emailjs.send(
        "service_hnqs4gv",
        "template_pvxxkx3",
        {
          user_email: mail,
          otp: generatedOTP,
        },
        "tfheOwRas0U6Mibcz"
      );
      console.log("Email sent successfully:", response.status, response.text);
   if (response.status === 200) {
      console.log("OTP email sent!");
    }
  } catch (err) {
    console.error("EmailJS Error:", err);
    setError("Failed to send OTP. Try again.");
    return;
  }
  setEmail(mail);
  localStorage.setItem("otp", generatedOTP);
  navigate("/otp");
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

            {/* Username */}
            <div className="input-box">
              <FaUser className="icon" />
              <input
                type="text"
                placeholder="Email"
                value={mail}
                onChange={(e) => {
                  setMail(e.target.value)
                  setError('');}
                }
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
               <p onClick={handleforgotPassword} style={{ cursor: "pointer" }}>
                Forgot Password?
              </p>
            </div>

            <div className="remember-forget">
              <label>
                <input type="checkbox" /> Remember me
              </label>
            </div>

            <button type="submit" className="submit">Log In</button>
             {error && <p style={{ color: "red" }}>{error}</p>}

            <div className="register">
              <p>
                Don't Have Account <Link to="/SignUp">signUp</Link>
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
