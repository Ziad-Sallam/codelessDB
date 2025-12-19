import { useState, useEffect } from "react";
import "./Register.css";
import { FaUser, FaEye, FaEyeSlash } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { IoIosMail } from "react-icons/io";
import { FcGoogle } from "react-icons/fc";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import {
  validateSignup,
  sendOtp,
  completeSignup,
  requestPasswordReset,
  updatePassword,
  redirectToGoogleAuth,
  parseApiError,
  validateToken,
} from "./fetch.js";
import { useAuth } from "../../components/AuthProvider.jsx";

// Step constants
const STEPS = {
  SIGNUP_FORM: "signup_form",
  SIGNUP_OTP: "signup_otp",
  FORGOT_PASSWORD: "forgot_password",
  FORGOT_OTP: "forgot_otp",
  RESET_PASSWORD: "reset_password",
  SUCCESS: "success",
};

const Register = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();

  // Current step
  const [step, setStep] = useState(STEPS.SIGNUP_FORM);

  // Form data
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPass, setConfirmPass] = useState("");

  // OTP
  const [otpInput, setOtpInput] = useState(["", "", "", "", ""]);
  const [sentOtp, setSentOtp] = useState("");
  const [otpTime, setOtpTime] = useState(null);
  const [otpTimer, setOtpTimer] = useState(60);
  const [canResendOtp, setCanResendOtp] = useState(false);

  // Reset password token
  const [resetToken, setResetToken] = useState("");

  // UI state
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const { setUser } = useAuth();

  useEffect(() => {
    document.title = "Register | CodeLess";
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
        setError("Google signup failed. Please try again.");
        window.history.replaceState({}, document.title, "/register");
      }

      const flow = searchParams.get("flow");
      const otpParam = searchParams.get("otp");
      const emailParam = searchParams.get("email");

      if (otpParam && emailParam) {
        setEmail(emailParam);
        const digits = otpParam.split("").slice(0, 5);
        const newOtp = ["", "", "", "", ""];
        digits.forEach((d, i) => (newOtp[i] = d));
        setOtpInput(newOtp);
        setSentOtp(otpParam);
        setOtpTime(Date.now());

        if (flow === "otp") {
          setStep(STEPS.SIGNUP_OTP);
        } else if (flow === "forgot") {
          setStep(STEPS.FORGOT_OTP);
        }
        return;
      }

      if (flow === "forgot") {
        setStep(STEPS.FORGOT_PASSWORD);
      } else if (flow === "reset") {
        const storedToken = localStorage.getItem("token_for_reset");
        const storedEmail = localStorage.getItem("email");
        const resetSource = localStorage.getItem("resetSource");

        if (!storedToken || !storedEmail) {
          navigate("/login", { replace: true });
          return;
        }

        setResetToken(storedToken);
        setEmail(storedEmail);

        if (resetSource === "profile") {
          setStep(STEPS.RESET_PASSWORD);
        } else {
          const storedOtp = localStorage.getItem("otp");
          setSentOtp(storedOtp);
          setOtpTimer(60);
          setStep(STEPS.FORGOT_OTP);
        }
      }
    }
    handleOAuthCallback();
  }, [navigate, searchParams, setUser]);


  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData("text").trim();
    if (!/^\d+$/.test(pastedData)) return;

    const digits = pastedData.split("").slice(0, 5);
    const newOtp = [...otpInput];

    digits.forEach((digit, index) => {
      newOtp[index] = digit;
    });

    setOtpInput(newOtp);


    const focusIndex = Math.min(digits.length, 4);
    const inputs = document.querySelectorAll(".otp-container input");
    if (inputs[focusIndex]) {
      inputs[focusIndex].focus();
    }
  };

  // OTP Timer
  useEffect(() => {
    if (step === STEPS.SIGNUP_OTP || step === STEPS.FORGOT_OTP) {
      const interval = setInterval(() => {
        setOtpTimer((prev) => {
          if (prev <= 1) {
            setCanResendOtp(true);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [step, otpTimer]);

  // Password validation
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

  // Username validation - check if contains only alphanumeric and underscores
  function isValidUsername(value) {
    // Returns true if username contains only letters, numbers, and underscores
    return /^[a-zA-Z0-9_]+$/.test(value);
  }

  // Step 1: Submit signup form
  const handleSignupSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!username || !username.trim()) {
      setError("Username is required.");
      return;
    }

    if (!isValidUsername(username)) {
      setError("Username can only contain letters, numbers, and underscores (no spaces or special characters).");
      return;
    }

    if (!checkStrength(password)) {
      setError("Password is weak.");
      return;
    }

    if (password !== confirmPass) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      await validateSignup(email, username);
      const otp = await sendOtp(email, username);
      setSentOtp(otp);
      setOtpTime(Date.now());
      setOtpTimer(60);
      setCanResendOtp(false);
      setStep(STEPS.SIGNUP_OTP);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Verify OTP and complete signup
  const handleVerifySignupOtp = async () => {
    const entered = otpInput.join("");

    if (entered != sentOtp) {
      setError("Invalid OTP. Try again.");
      return;
    }

    // Check expiration (5 minutes = 300000 ms)
    if (Date.now() - otpTime > 5 * 60 * 1000) {
      setError("OTP has expired. Please request a new one.");
      return;
    }

    setLoading(true);

    try {
      const token = await completeSignup(username, email, password);
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

  // Forgot Password: Step 1
  const handleForgotPassword = async () => {
    setError("");
    if (!email) {
      setError("Please enter your email");
      return;
    }

    setLoading(true);

    try {
      const token = await requestPasswordReset(email);
      setResetToken(token);

      const otp = await sendOtp(email);
      setSentOtp(otp);
      setOtpTime(Date.now());
      setOtpTimer(60);
      setCanResendOtp(false);
      setStep(STEPS.FORGOT_OTP);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  // Forgot Password: Step 2 - Verify OTP
  const handleVerifyForgotOtp = () => {
    const entered = otpInput.join("");

    if (entered != sentOtp) {
      setError("Invalid OTP. Try again.");
      return;
    }

    // Check expiration (5 minutes = 300000 ms)
    if (Date.now() - otpTime > 5 * 60 * 1000) {
      setError("OTP has expired. Please request a new one.");
      return;
    }

    setStep(STEPS.RESET_PASSWORD);
  };

  // Forgot Password: Step 3 - Reset password
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError("");

    if (!checkStrength(password)) {
      setError("Password is weak.");
      return;
    }

    if (password !== confirmPass) {
      setError("Passwords do not match!");
      return;
    }

    setLoading(true);

    try {
      await updatePassword(password, resetToken);

      // Check if user came from profile page
      const resetSource = localStorage.getItem("resetSource");

      // Clean up localStorage
      localStorage.removeItem("token_for_reset");
      localStorage.removeItem("otp");
      localStorage.removeItem("email");
      localStorage.removeItem("otpPurpose");
      localStorage.removeItem("resetSource");

      if (resetSource === "profile") {
        // User came from profile - set success flag and navigate back
        localStorage.setItem("passwordResetSuccess", "true");
        navigate("/profile");
      } else {
        // User came from forgot password flow - show success screen
        setStep(STEPS.SUCCESS);
      }
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  // Resend OTP
  const handleResendOtp = async () => {
    if (!canResendOtp || loading) return;

    setLoading(true);

    try {
      const user = step === STEPS.SIGNUP_OTP ? username : null;
      const otp = await sendOtp(email, user);
      setSentOtp(otp);
      setOtpTime(Date.now());
      setOtpTimer(60);
      setCanResendOtp(false);
      setError("");
      setOtpInput(["", "", "", "", ""]);
    } catch (err) {
      setError(parseApiError(err));
    } finally {
      setLoading(false);
    }
  };

  // Cancel and go back to login
  const handleCancel = () => {
    navigate("/login");
  };

  // Render content based on current step
  const renderContent = () => {
    switch (step) {
      case STEPS.SIGNUP_FORM:
        return (
          <form onSubmit={handleSignupSubmit}>
            <h1>Sign Up</h1>

            <div className="input-box">
              <FaUser className="icon" />
              <input
                type="text"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
              />
            </div>

            <div className="input-box">
              <IoIosMail className="icon" />
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
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
                {checks.symbol ? "✓" : "✗"} One special character
              </li>
            </ul>

            <div className="input-box">
              <TbLockPassword className="icon" />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Confirm Password"
                value={confirmPass}
                onChange={(e) => setConfirmPass(e.target.value)}
                required
              />
              <span
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </span>
            </div>

            <button type="submit" className="submit" disabled={loading}>
              {loading ? "Please wait..." : "Sign Up"}
            </button>

            {error && <p className="error-message">{error}</p>}

            <div className="divider">
              <span>OR</span>
            </div>

            <button
              type="button"
              className="google-button"
              onClick={() => redirectToGoogleAuth(true)}
              disabled={loading}
            >
              <FcGoogle className="google-icon" />
              Sign up with Google
            </button>

            <div className="register">
              <p>
                Already Have Account? <Link to="/login">Log In</Link>
              </p>
            </div>
          </form>
        );

      case STEPS.SIGNUP_OTP:
        return (
          <>
            <h1>Email Verification</h1>
            <p style={{ fontSize: "14px", textAlign: "center", marginBottom: "30px" }}>
              We sent a 5-digit code to: <b>{email}</b>
            </p>

            <div className="otp-container">
              {otpInput.map((val, idx) => (
                <input
                  key={idx}
                  type="text"
                  maxLength="1"
                  value={val}
                  onChange={(e) => {
                    const newArr = [...otpInput];
                    newArr[idx] = e.target.value;
                    setOtpInput(newArr);

                    if (e.target.value && idx < 4) {
                      e.target.nextSibling?.focus();
                    }
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Backspace" && !val && idx > 0) {
                      e.target.previousSibling?.focus();
                    }
                  }}
                  onPaste={handlePaste}
                />
              ))}
            </div>

            {error && <p className="error-message" style={{ marginBottom: "25px" }}>{error}</p>}

            <button onClick={handleVerifySignupOtp} className="submit" disabled={loading}>
              {loading ? "Verifying..." : "Verify Code"}
            </button>

            <button
              onClick={handleCancel}
              className="submit"
              style={{ backgroundColor: "#6c757d", marginTop: "10px" }}
            >
              Cancel
            </button>

            <div style={{ marginTop: "20px", textAlign: "center" }}>
              <p style={{ fontSize: "14px" }}>
                Didn't receive the code?{" "}
                <span
                  onClick={handleResendOtp}
                  style={{
                    color: canResendOtp && !loading ? "black" : "gray",
                    cursor: canResendOtp && !loading ? "pointer" : "default",
                    textDecoration: canResendOtp && !loading ? "underline" : "none",
                  }}
                >
                  {loading ? "Sending..." : (canResendOtp ? "Resend OTP" : `Resend in ${otpTimer}s`)}
                </span>
              </p>
            </div>
          </>
        );

      case STEPS.FORGOT_PASSWORD:
        return (
          <>
            <h1>Forgot Password</h1>
            <p style={{ fontSize: "14px", textAlign: "center", marginBottom: "20px" }}>
              Enter your email to receive a verification code
            </p>

            <div className="input-box">
              <IoIosMail className="icon" />
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>

            {error && <p className="error-message">{error}</p>}

            <button onClick={handleForgotPassword} className="submit" disabled={loading}>
              {loading ? "Sending..." : "Send Code"}
            </button>

            <button
              onClick={() => navigate("/login")}
              className="submit"
              style={{ backgroundColor: "#6c757d", marginTop: "10px" }}
            >
              Back to Login
            </button>
          </>
        );

      case STEPS.FORGOT_OTP:
        return (
          <>
            <h1>Verify Email</h1>
            <p style={{ fontSize: "14px", textAlign: "center", marginBottom: "30px" }}>
              We sent a 5-digit code to: <b>{email}</b>
            </p>

            <div className="otp-container">
              {otpInput.map((val, idx) => (
                <input
                  key={idx}
                  type="text"
                  maxLength="1"
                  value={val}
                  onChange={(e) => {
                    const newArr = [...otpInput];
                    newArr[idx] = e.target.value;
                    setOtpInput(newArr);

                    if (e.target.value && idx < 4) {
                      e.target.nextSibling?.focus();
                    }
                  }}
                  onKeyDown={(e) => {
                    if (e.key === "Backspace" && !val && idx > 0) {
                      e.target.previousSibling?.focus();
                    }
                  }}
                  onPaste={handlePaste}
                />
              ))}
            </div>

            {error && <p className="error-message" style={{ marginBottom: "25px" }}>{error}</p>}

            <button onClick={handleVerifyForgotOtp} className="submit">
              Verify Code
            </button>

            <button
              onClick={handleCancel}
              className="submit"
              style={{ backgroundColor: "#6c757d", marginTop: "10px" }}
            >
              Cancel
            </button>

            <div style={{ marginTop: "20px", textAlign: "center" }}>
              <p style={{ fontSize: "14px" }}>
                Didn't receive the code?{" "}
                <span
                  onClick={handleResendOtp}
                  style={{
                    color: canResendOtp && !loading ? "black" : "gray",
                    cursor: canResendOtp && !loading ? "pointer" : "default",
                    textDecoration: canResendOtp && !loading ? "underline" : "none",
                  }}
                >
                  {loading ? "Sending..." : (canResendOtp ? "Resend OTP" : `Resend in ${otpTimer}s`)}
                </span>
              </p>
            </div>
          </>
        );

      case STEPS.RESET_PASSWORD:
        return (
          <form onSubmit={handleResetPassword}>
            <h1>Reset Password</h1>

            <div className="input-box">
              <TbLockPassword className="icon" />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="New Password"
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
                {checks.symbol ? "✓" : "✗"} One special character
              </li>
            </ul>

            <div className="input-box">
              <TbLockPassword className="icon" />
              <input
                type={showPassword ? "text" : "password"}
                placeholder="Confirm Password"
                value={confirmPass}
                onChange={(e) => setConfirmPass(e.target.value)}
                required
              />
              <span
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}
              >
                {showPassword ? <FaEyeSlash /> : <FaEye />}
              </span>
            </div>

            {error && <p className="error-message">{error}</p>}

            <button type="submit" className="submit" disabled={loading}>
              {loading ? "Resetting..." : "Reset Password"}
            </button>
          </form>
        );

      case STEPS.SUCCESS:
        return (
          <>
            <h1 className="mb-4 text-2xl font-bold">Password Successfully Reset!</h1>
            <p style={{ fontSize: "14px", color: "#475569", marginTop: "10px", marginBottom: "20px" }}>
              Your password has been changed successfully. You can now log in with your new password.
            </p>
            <button
              className="submit"
              onClick={() => navigate("/login")}
              style={{ marginTop: "20px" }}
            >
              Go to Login
            </button>
          </>
        );

      default:
        return null;
    }
  };

  return (
    <div className="register">
      <div className="bg">
        <div className="title-section">
          <h1 className="Title">CodeLess</h1>
          <p className="slogon">Skip the code. Draw your data</p>
        </div>

        <div className="main">
          <div className="wrapper">{renderContent()}</div>
        </div>
      </div>
    </div>
  );
};

export default Register;