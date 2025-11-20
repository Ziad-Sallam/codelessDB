import React, { useState, useEffect, useContext } from "react";
import "./LogIn.css";
import axios from "axios";

import { FaUser } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { FaEye, FaEyeSlash } from "react-icons/fa";
import { FcGoogle } from "react-icons/fc";

import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { RecoveryContext } from "../../App";

const LogIn = () => {
   const navigate = useNavigate();
   const { setEmail } = useContext(RecoveryContext);
   const [mail, setMail] = useState('');
   const [password, setPassword] = useState('');
   const [error, setError] = useState('');
   const [showPassword, setShowPassword] = useState(false);
   const [searchParams] = useSearchParams();

   useEffect(() => {
      document.title = "Login | CodeLess";

      const token = searchParams.get('token');
      const oauthError = searchParams.get('error');

      if (token) {
         localStorage.setItem('authToken', token);
         console.log('OAuth login successful');

         window.history.replaceState({}, document.title, "/login");
         navigate('/');

      } else if (oauthError) {
         setError('Google login failed. Please try again.');

         window.history.replaceState({}, document.title, "/login");
      }
   }, [searchParams, navigate]);

   const handleGoogleLogin = () => {
      window.location.href = "http://localhost:8080/oauth2/authorization/google";
   };

   const handleLogin = async () => {
      if (!mail || !password) {
         setError("Email and password are required");
         return;
      }

      try {
         const response = await axios.post("http://localhost:8080/user/login", {
            email: mail,
            password: password
         });
         const token = response.data;
         localStorage.setItem("authToken", token);
         alert("Login successful!");
         navigate("/");
      }
      catch (err) {
         console.error(err);
         setError("Invalid email or password");
      }
   };

   const handleForgotPassword = async () => {
      if (!mail) {
         setError("Please enter your email");
         return;
      }

      try {
         // First, check if email exists and get token
         const checkResponse = await axios.post("http://localhost:8080/user/forgot-password", {
            email: mail
         });

         if (checkResponse.status === 200) {
            const token = checkResponse.data;
            console.log("Email verified. Token received. Sending OTP to:", mail);

            // Store the token for password reset
            localStorage.setItem("authToken", token);

            // Email exists, now send OTP
            const otpResponse = await axios.post("http://localhost:8080/auth/send-otp", {
               email: mail
            });

            const otp = otpResponse.data;
            console.log("Received OTP:", otp);

            // Store OTP and mark purpose as reset
            localStorage.setItem("otp", otp);
            localStorage.setItem("email", mail);
            localStorage.setItem("otpPurpose", "reset");
            setEmail(mail);

            alert("OTP sent to your email!");
            navigate("/otp");
         }

      } catch (err) {
         console.error(err);
         if (err.response && err.response.data) {
            setError(err.response.data);
         } else {
            setError("Failed to send OTP. Try again.");
         }
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
                              setMail(e.target.value)
                              setError('');
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
                        <p onClick={handleForgotPassword} style={{ cursor: "pointer" }}>
                           Forgot Password?
                        </p>
                     </div>

                     <div className="remember-forget">
                        <label>
                           <input type="checkbox" /> Remember me
                        </label>
                     </div>

                     <button type="submit" className="submit" onClick={handleLogin}>Log In</button>
                     {error && <p style={{ color: "red" }}>{error}</p>}

                     <div className="divider">
                        <span>OR</span>
                     </div>

                     <button
                        type="button"
                        className="google-button"
                        onClick={handleGoogleLogin}
                     >
                        <FcGoogle className="google-icon" />
                        Sign in with Google
                     </button>

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