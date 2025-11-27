import { useState, useEffect, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { RecoveryContext } from "../../../App";
import "./OTPInput.css";
import axios from "axios";

const OTPInput = () => {
  const navigate = useNavigate();
  const { email } = useContext(RecoveryContext);
  const [otpInput, setOtpInput] = useState(["", "", "", "", ""]);
  const [timer, setTimer] = useState(60);
  const [disable, setDisable] = useState(true);
  const [otpPurpose, setOtpPurpose] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const otp = localStorage.getItem("otp");
    const storedEmail = localStorage.getItem("email");
    const purpose = localStorage.getItem("otpPurpose");

    if (!otp || !storedEmail || !purpose) {
      navigate('/login', { replace: true });
      return;
    }

    setOtpPurpose(purpose);
  }, [navigate]);

  const verifyOTP = async () => {
    const entered = otpInput.join("");
    const saved = localStorage.getItem("otp");

    if (entered === saved) {
      if (otpPurpose === "signup") {
        try {
          const signupData = JSON.parse(localStorage.getItem("signupData"));

          const response = await axios.post("http://localhost:8080/user/signup", {
            username: signupData.username,
            email: signupData.email,
            password: signupData.password,
            picture: null
          });

          const token = response.data;
          localStorage.setItem("authToken", token);

          localStorage.removeItem("signupData");
          localStorage.removeItem("otp");
          localStorage.removeItem("otpPurpose");
          localStorage.removeItem("email");

          alert("Account Created Successfully!");
          navigate("/login");

        } catch (err) {
          const serverMsg = err.response?.data?.message
            || err.response?.data
            || err.message
            || "Server unavailable. Please try again later.";

          setError(String(serverMsg));
        }
      } else if (otpPurpose === "reset") {
        navigate("/reset");
      }
    } else {
 
      setError("Invalid OTP. Try again.");
    }
  };

  const resendOTP = async () => {
    if (disable) return;

    const emailToUse = email || localStorage.getItem("email");

    try {
      const response = await axios.post(`http://localhost:8080/user/signup/send-otp/${emailToUse}`);

      const otp = response.data;

      localStorage.setItem("otp", otp);

      setTimer(60);
      setDisable(true);

    } catch (err) {
      const serverMsg = err.response?.data?.message
        || err.response?.data
        || err.message
        || "Server unavailable. Please try again later.";

      setError(String(serverMsg));
    }
  };

  const handleCancel = () => {
    localStorage.removeItem("otp");
    localStorage.removeItem("email");
    localStorage.removeItem("otpPurpose");
    localStorage.removeItem("signupData");
    navigate("/login");
  };

  useEffect(() => {
    let interval = setInterval(() => {
      setTimer((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          setDisable(false);
        }
        return prev > 0 ? prev - 1 : 0;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [timer]);

  return (
    <div className="bg">
      <div className="title-section">
        <h1 className="Title">CodeLess</h1>
        <p className="slogon">Skip the code. Draw your data</p>
      </div>

      <div className="main">
        <div className="wrapper">
          <h1>Email Verification</h1>
          <p style={{ fontSize: "14px", textAlign: "center", marginBottom: "30px" }}>
            We sent a 5-digit code to: <b>{email || localStorage.getItem("email")}</b>
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
              />
            ))}
          </div>

          {error && <p className="error-message" style={{marginBottom: "25px"}}>{error}</p>}

          <button onClick={verifyOTP} className="submit">Verify Code</button>

          <button
            onClick={handleCancel}
            className="submit"
            style={{
              backgroundColor: "#6c757d",
              marginTop: "10px"
            }}
          >
            Cancel
          </button>

          <div style={{ marginTop: "20px", textAlign: "center" }}>
            <p style={{ fontSize: "14px" }}>
              Didn't receive the code?{" "}
              <span
                onClick={resendOTP}
                style={{
                  color: disable ? "gray" : "black",
                  cursor: disable ? "default" : "pointer",
                  textDecoration: disable ? "none" : "underline",
                }}
              >
                {disable ? `Resend in ${timer}s` : "Resend OTP"}
              </span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default OTPInput;