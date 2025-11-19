import React, { useState, useEffect, useContext } from "react";
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

  useEffect(() => {
    // Get the purpose from localStorage
    const purpose = localStorage.getItem("otpPurpose");
    setOtpPurpose(purpose);
  }, []);

  const verifyOTP = async () => {
    const entered = otpInput.join("");
    const saved = localStorage.getItem("otp");

    if (entered === saved) {
      // Check the purpose and handle accordingly
      if (otpPurpose === "signup") {
        // Complete signup process
        try {
          const signupData = JSON.parse(localStorage.getItem("signupData"));

          const response = await axios.post("http://localhost:8080/user/signup", {
            username: signupData.username,
            email: signupData.email,
            password: signupData.password,
            picture: signupData.picture
          });

          const token = response.data;
          localStorage.setItem("authToken", token);

          // Clean up temporary data
          localStorage.removeItem("signupData");
          localStorage.removeItem("otp");
          localStorage.removeItem("otpPurpose");
          localStorage.removeItem("email");

          alert("Account Created Successfully!");
          navigate("/login");

        } catch (error) {
          console.error("Signup Error:", error);
          alert("Failed to create account. Please try again."); // take error from back
        }
      } else if (otpPurpose === "reset") {
        navigate("/reset");
      }
    } else {
      alert("Invalid OTP. Try again.");
    }
  };

  const resendOTP = async () => {
    if (disable) return;

    const emailToUse = email || localStorage.getItem("email");
    console.log("Resending OTP to email:", emailToUse);

    try {
      const response = await axios.post("http://localhost:8080/auth/send-otp", {
        email: emailToUse
      });

      const otp = response.data;
      localStorage.setItem("otp", otp);
      alert("OTP resent to your email!");
      setTimer(60);
      setDisable(true);

    } catch (err) {
      console.error(err);
      alert("Failed to resend OTP.");
    }
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

          <button onClick={verifyOTP} className="submit">Verify Code</button>

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