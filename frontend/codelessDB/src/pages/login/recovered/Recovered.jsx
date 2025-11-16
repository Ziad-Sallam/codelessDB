import React from "react";
import "./Recovered.css";

export default function Recovered() {
  return (
    <div className="bg">
      {/* Left gradient section */}
      <div className="title-section">
        <h1 className="Title">CodeLess</h1>
        <p className="slogon">Skip the code. Draw your data</p>
      </div>

      {/* Right main section */}
      <div className="main">
        <div className="wrapper text-center">
          <h1 className="mb-4 text-2xl font-bold">Password Successfully Set</h1>
          <p className="text-gray-700 mb-6">Welcome HOME</p>
          <button
            className="bg-blue-700 text-white py-3 px-6 rounded-xl shadow hover:bg-blue-800 transition"
            onClick={() => window.location.href = "/login"}
          >
            Go to Login
          </button>
        </div>
      </div>
    </div>
  );
}
