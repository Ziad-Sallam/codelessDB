import { useState, useEffect } from "react";
import "./SignUp.css";
import axios from "axios";

import { FaUser, FaEye, FaEyeSlash } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { IoIosMail } from "react-icons/io";
import { FcGoogle } from "react-icons/fc";
import { Link } from "react-router-dom";
import { useNavigate, useSearchParams } from "react-router-dom";

const SignUp = () => {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [mail, setMail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPass, setConfirmPass] = useState("");
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [userImage, setUserImage] = useState(null);
  const [preview, setPreview] = useState(null);
  const [imageError, setImageError] = useState("");
  const [searchParams] = useSearchParams();

  useEffect(() => {
    document.title = "Sign Up | CodeLess";

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
      setError('Google signup failed. Please try again.');
    }

    window.history.replaceState({}, document.title, "/SignUp");
  }, [searchParams, navigate]);

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setImageError("Only image files are allowed.");
      return;
    }

    if (file.size > 1024 * 1024) {
      setImageError("Image must be less than 1MB.");
      return;
    }

    setImageError("");

    const reader = new FileReader();
    reader.onloadend = () => {
      setUserImage(reader.result);
      setPreview(URL.createObjectURL(file));
    };

    reader.readAsDataURL(file);
  };

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

  const handleGoogleSignUp = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!checkStrength(password)) {
      setError("Password is weak.");
      return;
    }
    if (password !== confirmPass) {
      setError("Passwords do not match.");
      return;
    }
    setError("");

    try {
      const response1 = await axios.post("http://localhost:8080/user/signup/validate", {
        email: mail,
        username: username
      });

      if (response1.status === 200) {
        try {
          const response = await axios.post(`http://localhost:8080/user/signup/send-otp/${mail}`);

          const otp = response.data;

          localStorage.setItem("otp", otp);
          localStorage.setItem("email", mail);
          localStorage.setItem("otpPurpose", "signup");
          localStorage.setItem("signupData", JSON.stringify({
            username,
            email: mail,
            password,
            picture: userImage
          }));

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
    <div className="signup">
      <div className="bg">
        <div className="title-section">
          <h1 className="Title">CodeLess</h1>
          <p className="slogon">Skip the code. Draw your data</p>
        </div>

        <div className="main">
          <div className="wrapper">
            <form onSubmit={handleSubmit}>
              <h1>Sign Up</h1>

              <div className="image-upload-container">
                {userImage != null && <button type="button" className="remove-image-button" onClick={() => {
                  setUserImage(null);
                  setPreview(null);
                }}>x</button>}
                <label htmlFor="userImage" className="image-label">
                  {preview ? (
                    <img src={preview} alt="Preview" className="profile-preview" />
                  ) : (
                    <div className="upload-placeholder">
                      + Add Profile Picture
                    </div>
                  )}
                </label>

                <input
                  type="file"
                  id="userImage"
                  accept="image/*"
                  onChange={handleImageUpload}
                  style={{ display: "none" }}
                />
              </div>

              {imageError && <p className="error">{imageError}</p>}

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
                  placeholder="Mail"
                  value={mail}
                  onChange={(e) => setMail(e.target.value)}
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
                  {checks.symbol ? "✓" : "✗"} One special character (!@#$%^&*)
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

              <button type="submit" className="submit">Sign Up</button>

              {error && <p className="error-message">{error}</p>}

              <div className="divider">
                <span>OR</span>
              </div>

              <button
                type="button"
                className="google-button"
                onClick={handleGoogleSignUp}
              >
                <FcGoogle className="google-icon" />
                Sign up with Google
              </button>

              <div className="register">
                <p>
                  Already Have Account ? <Link to="/login">Log In</Link>
                </p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignUp;