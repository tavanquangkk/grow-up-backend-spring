package jp.trial.grow_up.service.client;

import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.dto.client.RequestUpdateInfor;
import jp.trial.grow_up.dto.client.ResponseUserProfileDTO;
import jp.trial.grow_up.repository.client.UserRepository;
import jp.trial.grow_up.repository.client.WorkshopRepository;
import jp.trial.grow_up.util.convert.UserConvert;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final WorkshopRepository workshopRepository;

    public UserService(UserRepository userRepository, WorkshopRepository workshopRepository) {
        this.userRepository = userRepository;
        this.workshopRepository = workshopRepository;
    }

    //依存関係注入end

    //save user
    public User handleSaveUser(User user){
        return  this.userRepository.save(user);
    }

    // get user by name
    public User getUserByName(String name) {
        return this.userRepository.findByName(name).orElseThrow(
                ()-> new UsernameNotFoundException("ユーザーが見つかりません: \"" + name));
    }
    //get user by email
    public User getUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("ユーザーが見つかりません: \"" + email));
    }

    // check user exist
    public boolean userExists(String email) {
        return this.userRepository.existsByEmail(email);
    }

    //create UserDetail Object with current user info
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = getUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + email);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRole().toString()) // 必要に応じてロール設定
                .build();
    }

    public Optional<User> getUserById(UUID userId) {
        return this.userRepository.findById(userId);
    }

    //オススメユーザー一覧取得
    public List<ResponseUserProfileDTO> getRecommendedUsers(User me){
        List<ResponseUserProfileDTO> userList = new ArrayList<>();
        List<User> tempList = this.userRepository.findTop10ByOrderByFollowerCountDesc();
        for(User user: tempList){
            if(!user.equals(me)){
                userList.add(UserConvert.convertToResponseUserProfileDTO(user));
            }
        }
        return userList;
    }


    public long getSumOfUsers() {
        return userRepository.count();
    }

    public boolean deleteUser(User currentUser) {
        this.workshopRepository.deleteAllByHostId(currentUser.getId());
        this.userRepository.delete(currentUser);
        return true;
    }

    public User updateUserInfo(String email,RequestUpdateInfor requestUpdateInfor) {
        User currentUser = this.userRepository.findUserByEmail(email).orElseThrow(()-> new RuntimeException("ユーザーが見つかりませんでした"));
        currentUser.setName(requestUpdateInfor.getName());
        currentUser.setDepartment(requestUpdateInfor.getDepartment());
        currentUser.setPosition(requestUpdateInfor.getPosition());
        currentUser.setIntroduction(requestUpdateInfor.getIntroduction());
        this.userRepository.save(currentUser);
        return currentUser;
    }
}
