import React, { useState, useEffect } from "react";
import "./SignUp.css";
import axios from "axios";

import { FaUser, FaEye, FaEyeSlash } from "react-icons/fa";
import { TbLockPassword } from "react-icons/tb";
import { IoIosMail } from "react-icons/io";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";

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

  useEffect(() => {
    document.title = "SignUp | CodeLess";
  }, []);

  function checkStrength(pass) {
    const c = getPasswordChecks(pass);
    return c.length && c.upper && c.lower && c.number && c.symbol;
  }

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
      // Send OTP first
      const response = await axios.post("http://localhost:8080/auth/send-otp", {
        email: mail
      });

      const otp = response.data;
      console.log("Received OTP:", otp);

      // Store OTP and signup data temporarily
      localStorage.setItem("otp", otp);
      localStorage.setItem("email", mail);
      localStorage.setItem("otpPurpose", "signup"); // Track purpose
      localStorage.setItem("signupData", JSON.stringify({
        username,
        email: mail,
        password,
        picture: userImage
      }));

      alert("OTP sent to your email!");
      navigate("/otp");

    } catch (err) {
      console.error(err);
      setError("Failed to send OTP. Try again.");
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

              <div className="register">
                <p>
                  Already Have Account? <Link to="/login">Login</Link>
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