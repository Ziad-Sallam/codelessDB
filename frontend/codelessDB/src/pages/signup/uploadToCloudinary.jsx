import axios from "axios";

const getSignature = async () => {
  const response = await axios.get("http://localhost:8080/user/signature");
  return response.data;
};

export const uploadToCloudinary = async (file) => {
  try {
    const { signature, timestamp, apiKey, cloudName, uploadPreset } = await getSignature();
    
    const formData = new FormData();

    formData.append("file", file);
    formData.append("api_key", apiKey);
    formData.append("timestamp", timestamp);
    formData.append("signature", signature);
    formData.append("upload_preset", uploadPreset);
  
    const uploadUrl = `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`;
  
    const response = await axios.post(uploadUrl, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  
    return response.data.secure_url; // ← The image URL you save in DB

  } catch (error) {
    console.log(error);
  }

  
// how to use 
// const url = await uploadToCloudinary(file);

};
