package jp.trial.grow_up.service.client;

import jp.trial.grow_up.domain.Follow;
import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.repository.client.FollowRepository;
import jp.trial.grow_up.repository.client.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository,UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    //フォローしているかどうかをチェック
    public boolean isFollowed(UUID followerId, UUID followingId){
        return this.followRepository.existsByFollowerIdAndFollowingId(followerId,followingId);
    }

    //フォロー１行にフォローする人のIDとフォローされる人のID
    public Follow follow(UUID followerId, UUID followingId){

        Follow followData = new Follow();
        User follower = this.userRepository.findById(followerId).orElseThrow(()-> new RuntimeException(followerId + "のユーザーが見つかりませんでした"));
        User following = this.userRepository.findById(followingId).orElseThrow(()-> new RuntimeException(followingId + "のユーザーが見つかりませんでした"));
        followData.setFollower(follower);
        followData.setFollowing(following);
        followData = this.followRepository.save(followData);
        return followData;
    }

    //フォローを外す
    public boolean unFollow(UUID followerId,UUID followingId){
        Follow currentFollow = this.followRepository.findByFollowerIdAndFollowingId(followerId,followingId);
        if(currentFollow == null){
            throw new RuntimeException("フォローしていないので、外せません");
        }
        this.followRepository.delete(currentFollow);
        return true;
    }

    //フォロワー一覧を取得する　followingId==自分のID
    public List<User> getFollowings(UUID myID){
        User currentUser = userRepository.findById(myID).orElseThrow(()-> new RuntimeException("ユーザー取得に失敗しました"));
        currentUser.setFollowingCount(getFollowingNum(myID));
        this.userRepository.save(currentUser);
     return this.followRepository.findAllFollowings(myID);
    }
    //フォロー中一覧を取得する　followerId==自分のID
    public List<User> getFollowers(UUID myID){
        User currentUser = userRepository.findById(myID).orElseThrow(()-> new RuntimeException("ユーザー取得に失敗しました"));
        currentUser.setFollowerCount(getFollowerNum(myID));
        this.userRepository.save(currentUser);
        return this.followRepository.findAllFollowers(myID);
    }
    //フォロー中数を取得する
    public long getFollowingNum(UUID userId){
        User currentUser = this.userRepository.findById(userId).orElseThrow(()-> new RuntimeException("ユーザーのフォロー中数の取得が出来ません"));
        long followings = this.followRepository.countFollowings(userId);
        currentUser.setFollowingCount(followings);
        this.userRepository.save(currentUser);
        return followings;
    }
    //フォロワー数を取得する
    public long getFollowerNum(UUID userId){
        User currentUser = this.userRepository.findById(userId).orElseThrow(()-> new RuntimeException("フォロワー数の取得ができません"));
        long followers = this.followRepository.countFollowers(userId);
        currentUser.setFollowerCount(followers);
        return followers;
    }
}
