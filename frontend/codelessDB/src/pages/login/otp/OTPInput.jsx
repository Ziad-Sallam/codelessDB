import React, { useState, useContext, useEffect } from "react";
import axios from "axios";
import { RecoveryContext } from "../../../App";
import { useNavigate } from "react-router-dom";

export const OTPInput = () => {
  const navigate = useNavigate();
  const { email, otp } = useContext(RecoveryContext);
  const [otpInput, setOtpInput] = useState(["", "", "", ""]);
  const [timer, setTimer] = useState(60);
  const [disable, setDisable] = useState(true);

  function resendOTP() {
    if (disable) return;

    axios
      .post("http://localhost:5000/api/resend-otp", {
        OTP: otp,
        recipient_email: email,
      })
      .then((response) => {
        console.log(response.data);
        setDisable(true);
        setTimer(60);
        alert("OTP resent successfully!");
      })
      .catch((error) => {
        console.error("There was an error resending the OTP!", error);
      });
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
    <div className="flex justify-center items-center w-screen h-screen bg-gray-50">
      <div className="bg-white px-6 pt-10 pb-9 shadow-xl w-full max-w-lg rounded-2xl">

        <div className="flex flex-col items-center text-center space-y-2">
          <p className="font-semibold text-3xl">Email Verification</p>
          <p className="text-sm text-gray-500">
            We have sent a code to your email: {email}
          </p>
        </div>

        <form onSubmit={(e) => e.preventDefault()} className="mt-10 space-y-10">
          
          {/* OTP INPUT BOXES */}
          <div className="flex justify-between max-w-xs mx-auto">
            {otpInput.map((value, index) => (
              <input
                key={index}
                maxLength="1"
                className="w-16 h-16 text-center border border-gray-300 rounded-xl text-xl outline-none focus:ring-2 ring-blue-500"
                onChange={(e) => {
                  const val = e.target.value;
                  const newOtp = [...otpInput];
                  newOtp[index] = val;
                  setOtpInput(newOtp);

                  // Auto-move to next input
                  if (val && index < 3) {
                    e.target.nextSibling?.focus();
                  }
                }}
              />
            ))}
          </div>

          {/* VERIFY BUTTON */}
          <button
            onClick={verifyOTP}
            className="w-full py-4 bg-blue-700 text-white rounded-xl shadow-md hover:bg-blue-800 transition"
          >
            Verify Account
          </button>

          {/* RESEND OTP */}
          <div className="text-center text-sm">
            <p className="text-gray-600">
              Didnt receive the code?
              <span
                onClick={resendOTP}
                className={`ml-1 ${
                  disable
                    ? "text-gray-400"
                    : "text-blue-600 underline cursor-pointer"
                }`}
              >
                {disable ? `Resend in ${timer}s` : "Resend OTP"}
              </span>
            </p>
          </div>

        </form>
      </div>
    </div>
  );
};
