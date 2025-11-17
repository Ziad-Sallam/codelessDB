import React, { useState, useEffect, useContext } from "react";
import emailjs from "@emailjs/browser";
import { useNavigate } from "react-router-dom";
import { RecoveryContext } from "../../../App";
import "./OTPInput.css";

const OTPInput = () => {
  const navigate = useNavigate();
  const { email, otp, setOTP } = useContext(RecoveryContext);
  const [otpInput, setOtpInput] = useState(["", "", "", "", ""]);
  const [timer, setTimer] = useState(60);
  const [disable, setDisable] = useState(true);

  const sendOTP = (newOTP) => {
    return emailjs.send(
        "service_hnqs4gv",
        "template_pvxxkx3",
      {
        user_email: email,
        otp: newOTP,
      },
      "tfheOwRas0U6Mibcz"
    );
  };
  const resendOTP = async () => {
    if (disable) return;
    const newOTP = Math.floor(10000 + Math.random() * 90000);
    try {
      await sendOTP(newOTP);
      alert("OTP sent successfully!");

      setOTP(newOTP);
      localStorage.setItem("otp", newOTP);

      setDisable(true);
      setTimer(60);

    } catch (err) {
      console.error(err);
      alert("Failed to send OTP");
    }
  };
  const verifyOTP = () => {
    const entered = otpInput.join("");
    const saved = localStorage.getItem("otp");
    if (entered === saved) {
      navigate("/reset");
      console.log("OTP verified!");
    } else {
      alert("Invalid OTP. Try again.");
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
          <p style={{ fontSize: "14px", textAlign: "center", marginBottom: "30px" }}>
            We sent a 6-digit code to: <b>{email}</b>
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

                  if (e.target.value && idx < 5) {
                    e.target.nextSibling.focus();
                  }
                }}
              />
            ))}
          </div>

          <button onClick={verifyOTP} className="submit">Verify Code</button>

          <div style={{ marginTop: "20px", textAlign: "center" }}>
            <p style={{ fontSize: "14px" }}>
              Didn’t receive the code?{" "}
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
