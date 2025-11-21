import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import "./Recovered.css";

export default function Recovered() {
  const navigate = useNavigate();

  useEffect(() => {
    const existingToken = localStorage.getItem("authToken");

    if (existingToken) {
      navigate('/', { replace: true });
    }
    
    const isRecovered = localStorage.getItem('recovered')
    
    if (!isRecovered) {
      navigate('/login', { replace: true });
    }
    
  }, [navigate]);

  return (
    <div className="bg">
      <div className="title-section">
        <h1 className="Title">CodeLess</h1>
        <p className="slogon">Skip the code. Draw your data</p>
      </div>

      <div className="main">
        <div className="wrapper text-center">
          <h1 className="mb-4 text-2xl font-bold">Password Successfully Reset!</h1>
          <p style={{ fontSize: "14px", color: "#475569", marginTop: "10px", marginBottom: "20px" }}>
            Your password has been changed successfully. You can now log in with your new password.
          </p>
          <button
            className="submit"
            onClick={() =>{
              window.location.href = "/login"
              localStorage.removeItem('recovered')
            }}
            style={{ marginTop: '20px' }}
          >
            Go to Login
          </button>
        </div>
      </div>
    </div>
  );
}