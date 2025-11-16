import React, { useState, useEffect, useContext } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { RecoveryContext } from "../../../App";
import "./OTPInput.css";

const OTPInput = () => {
  const navigate = useNavigate();
  const { email, otp } = useContext(RecoveryContext);
  const [otpInput, setOtpInput] = useState(["", "", "", ""]);
  const [timer, setTimer] = useState(60);
  const [disable, setDisable] = useState(true);

  function resendOTP() {
    if (disable) return;
    axios
      .post("http://localhost:5000/send_recovery_email", {
        OTP: otp,
        recipient_email: email,
      })
      .then((res) => {
        alert("OTP resent successfully!");
        setDisable(true);
        setTimer(60);
      })
      .catch((err) => console.error(err));
  }

  function verifyOTP() {
    const enteredOTP = otpInput.join("");
    if (parseInt(enteredOTP) === otp) {
      navigate("/reset");
    } else {
      alert("Invalid OTP. Please try again.");
    }
  }

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
  }, []);

  return (
    <div className="bg">
      <div className="title-section">
        <h1 className="Title">CodeLess</h1>
        <p className="slogon">Skip the code. Draw your data</p>
      </div>

      <div className="main">
        <div className="wrapper">
          <h1>Email Verification</h1>
          <p style={{ fontSize: "14px", color: "#555", textAlign: "center", marginBottom: "30px" }}>
            We have sent a code to your email: {email}
          </p>

          <div className="otp-container">
            {otpInput.map((val, idx) => (
              <input
                key={idx}
                type="text"
                maxLength="1"
                value={val}
                onChange={(e) => {
                  const newOtp = [...otpInput];
                  newOtp[idx] = e.target.value;
                  setOtpInput(newOtp);
                  if (e.target.value && idx < 3) e.target.nextSibling.focus();
                }}
              />
            ))}
          </div>

          <button onClick={verifyOTP}>Verify Account</button>

          <div className="register" style={{ marginTop: "20px", textAlign: "center" }}>
            <p style={{ fontSize: "14px", color: "#555" }}>
              Didn't receive the code?{" "}
              <span
                style={{
                  color: disable ? "gray" : "#000",
                  cursor: disable ? "default" : "pointer",
                  textDecoration: disable ? "none" : "underline",
                }}
                onClick={resendOTP}
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
