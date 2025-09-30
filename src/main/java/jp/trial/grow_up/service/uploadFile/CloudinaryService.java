package jp.trial.grow_up.service.uploadFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("folder", folder) // フォルダ分けも可能
        );
        return uploadResult.get("secure_url").toString(); // これをDBに保存
    }

    public String getPublicIdFromUrl ( String url){
        int start = url.indexOf("profile");
        int end = url.length()-4;
        String result = url.substring(start,end);
        return result;
    }
    public boolean deleteOldImage(String publicId) throws IOException {
        Map resutl = cloudinary.uploader().destroy(publicId,ObjectUtils.emptyMap());
        if(resutl.isEmpty()){
            return false;
        }
        return true;
    }
}

