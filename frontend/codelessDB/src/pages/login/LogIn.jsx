import { useState, useEffect, useContext } from "react";
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
      document.title = "Log in | CodeLess";

      const existingToken = localStorage.getItem('authToken');
      if (existingToken) {
         navigate('/', { replace: true });
         return;
      }

      const token = searchParams.get('token');
      const oauthError = searchParams.get('error');

      if (token) {
         localStorage.setItem('authToken', token);
         navigate('/');
      } else if (oauthError) {
         setError('Google login failed. Please try again.');
      }

      window.history.replaceState({}, document.title, "/login");
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
         navigate("/");
      } catch (err) {
         const serverMsg = err.response?.data?.message
            || err.response?.data
            || err.message
            || "Server unavailable. Please try again later.";

         setError(String(serverMsg));
      }
   };

   const handleForgotPassword = async () => {
      if (!mail) {
         setError("Please enter your email");
         return;
      }

      try {
         const checkResponse = await axios.post(`http://localhost:8080/user/login/forgot-password/${mail}`);

         if (checkResponse.status === 200) {
            const token = checkResponse.data;
            localStorage.setItem("token for_reset", token);

            try {
               const otpResponse = await axios.post(`http://localhost:8080/user/signup/send-otp/${mail}`);

               const otp = otpResponse.data;

               // console.log(otp);

               localStorage.setItem("otp", otp);
               localStorage.setItem("email", mail);
               localStorage.setItem("otpPurpose", "reset");
               setEmail(mail);

               // alert("OTP sent to your email!");
               navigate("/otp");
            } catch (err) {
               const serverMsg = err.response?.data?.message
                  || err.response?.data
                  || err.message
                  || "Server unavailable. Please try again later.";

               setError(String(serverMsg));
            }
         }

      } catch (err) {
         const serverMsg = err.response?.data?.message
            || err.response?.data
            || err.message
         || "Server unavailable. Please try again later.";

         setError(String(serverMsg));
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
                        <p onClick={handleForgotPassword} style={{ cursor: "pointer", margin: "6px" }}>
                           Forgot Password?
                        </p>
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
                        Log in with Google
                     </button>

                     <div className="register">
                        <p>
                           Don't Have Account ? <Link to="/SignUp">Sign Up</Link>
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