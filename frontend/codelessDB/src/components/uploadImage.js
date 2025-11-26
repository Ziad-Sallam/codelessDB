import axios from "axios";

const getSignature = async (publicId) => {
  const response = await axios.get(
    `http://localhost:8080/user/signature/upload?publicId=${publicId}`
  );
  return response.data;
};

export const uploadToCloudinary = async (file, publicId) => {
  try {
    const { signature, timestamp, apiKey, cloudName, uploadPreset } =
      await getSignature(publicId);

    const formData = new FormData();

    formData.append("file", file);
    formData.append("public_id", publicId);
    formData.append("timestamp", timestamp);
    formData.append("signature", signature);
    formData.append("api_key", apiKey);
    formData.append("upload_preset", uploadPreset);
    formData.append("overwrite", "true");
    formData.append("invalidate", "true");

    const uploadUrl = `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`;

    const res = await axios.post(uploadUrl, formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });

    return res.data.secure_url;
  } catch (error) {
    console.log(error.response?.data || error);
  }
};