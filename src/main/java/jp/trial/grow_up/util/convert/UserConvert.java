package jp.trial.grow_up.util.convert;

import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.dto.auth.SignupRequestDTO;
import jp.trial.grow_up.dto.auth.SignupResponseDTO;
import jp.trial.grow_up.dto.client.ResponseUserProfileDTO;
import jp.trial.grow_up.dto.client.ResponseWorkshopHostDTO;

public class UserConvert {


    //convert request
    public static User convertFromSignupRequestDTO(SignupRequestDTO signupRequestDTO) {
        User user = new User();
        user.setName(signupRequestDTO.getName());
        user.setEmail(signupRequestDTO.getEmail());
        user.setPassword(signupRequestDTO.getPassword());
        return user;

    }

    //auth module
    public static SignupResponseDTO convertToSignupUserDTO (User user){
        SignupResponseDTO signupResponseDTO = new SignupResponseDTO();
        signupResponseDTO.setId(user.getId());
        signupResponseDTO.setName(user.getName());
        signupResponseDTO.setEmail(user.getEmail());
        signupResponseDTO.setRole(user.getRole().toString());

        return signupResponseDTO;
    }



    //user module
    public static ResponseUserProfileDTO convertToResponseUserProfileDTO(User user){
        ResponseUserProfileDTO responseUserProfileDTO = new ResponseUserProfileDTO();
        responseUserProfileDTO.setId(user.getId());
        responseUserProfileDTO.setName(user.getName());
        responseUserProfileDTO.setRole(user.getRole().toString());
        responseUserProfileDTO.setEmail(user.getEmail());
        responseUserProfileDTO.setDepartment(user.getDepartment());
        responseUserProfileDTO.setPosition(user.getPosition());
        responseUserProfileDTO.setIntroduction(user.getIntroduction());
        responseUserProfileDTO.setProfileImageUrl(user.getProfileImageUrl());
        responseUserProfileDTO.setBackgroundImageUrl(user.getBackgroundImageUrl());
        responseUserProfileDTO.setFollowerCount(user.getFollowerCount());
        responseUserProfileDTO.setFollowingCount(user.getFollowingCount());
        responseUserProfileDTO.setLearningSkills(user.getLearningSkills());
        responseUserProfileDTO.setTeachableSkills(user.getTeachableSkills());

        return responseUserProfileDTO;
    }

    //convert to responseWorkshopHostResponse


    public static ResponseWorkshopHostDTO convertToWorkshopHostDTO(User user){
        ResponseWorkshopHostDTO res = new ResponseWorkshopHostDTO();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());

        return res;
    }

}
