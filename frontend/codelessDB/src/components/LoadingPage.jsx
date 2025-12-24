
import React from 'react';
import './LoadingPage.css';
import { CircularProgress } from '@mui/material';

export default function LoadingPage() {
    return (
        <div className="loading-screen" >
            <h1 className="text">CodeLessDB</h1>
            <CircularProgress sx={{ color: 'white' }} />
        </div>
    );
}