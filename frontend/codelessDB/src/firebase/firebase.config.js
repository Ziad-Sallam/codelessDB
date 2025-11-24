// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
// import { getAnalytics } from "firebase/analytics";
import { getStorage } from "firebase/storage";
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
	apiKey: "AIzaSyCu1z0qgwZfEN6cQ7MNjh__IUW_7pgTMI0",
	authDomain: "codelessdb.firebaseapp.com",
	projectId: "codelessdb",
	storageBucket: "codelessdb.firebasestorage.app",
	messagingSenderId: "907688520854",
	appId: "1:907688520854:web:1161eebf206f307c5466b7",
	measurementId: "G-H9PHJ4YLFT"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
// const analytics = getAnalytics(app);
export const storage = getStorage(app);