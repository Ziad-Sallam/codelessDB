import axios from "axios";

const getDeleteSignature = async (publicId) => {
  const response = await axios.get("http://localhost:8080/user/signature/delete", {
    params: { publicId }
  });
  return response.data;
};

export const deleteFromCloudinary = async (imageUrl) => {
  try {
    // Extract public_id from Cloudinary URL
    // Example URL: https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg
    // Public ID: sample
    const urlParts = imageUrl.split('/');
    const uploadIndex = urlParts.indexOf('upload');
    if (uploadIndex === -1) return false;
    
    // Get everything after 'upload/vXXXXXXXXXX/' or 'upload/'
    const filePathWithExtension = urlParts.slice(uploadIndex + 2).join('/');
    // Remove file extension
    const publicId = filePathWithExtension.substring(0, filePathWithExtension.lastIndexOf('.'));

    const { signature, timestamp, apiKey, cloudName } = await getDeleteSignature(publicId);
    
    const deleteUrl = `https://api.cloudinary.com/v1_1/${cloudName}/image/destroy`;
    
    const formData = new FormData();
    formData.append("public_id", publicId);
    formData.append("api_key", apiKey);
    formData.append("timestamp", timestamp);
    formData.append("signature", signature);

    const response = await axios.post(deleteUrl, formData);
    
    return response.data.result === "ok";

  } catch (error) {
    console.error("Error deleting from Cloudinary:", error);
    return false;
  }
};