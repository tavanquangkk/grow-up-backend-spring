package jp.trial.grow_up.service.client;

import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.domain.Workshop;
import jp.trial.grow_up.dto.client.ResponseWorkshopHostDTO;
import jp.trial.grow_up.dto.workshop.WorkshopDTO;
import jp.trial.grow_up.repository.client.WorkshopRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class WorkshopService {
    private final WorkshopRepository workshopRepository;
    private final UserService userService;

    public WorkshopService(WorkshopRepository workshopRepository,UserService userService) {
        this.workshopRepository = workshopRepository;
        this.userService = userService;
    }

    //勉強会が存在しているかをチェックする
    public  boolean checkExist(Workshop workshop){
        return workshopRepository.existsByName(workshop.getName());
    }

    //convert to WorkshopDTO
    public WorkshopDTO convertToWorkshopDTO(Workshop workshop){
        WorkshopDTO wsd = new WorkshopDTO();
        wsd.setId(workshop.getId());
        wsd.setName(workshop.getName());
        wsd.setDescription(workshop.getDescription());
        wsd.setDate(workshop.getDate());
        ResponseWorkshopHostDTO host = new ResponseWorkshopHostDTO();
        host.setId(workshop.getHost().getId());
        host.setName(workshop.getHost().getName());
        host.setEmail(workshop.getHost().getEmail());
        wsd.setHost(host);
        return  wsd;
    }


    //勉強会を作成する
    public WorkshopDTO handleCreateWorkshop(Workshop workshop, String email){
        if(checkExist(workshop)){
            return null;
        }
        User host = userService.getUserByEmail(email);
        //workshop info
        Workshop ws = new Workshop();
        ws.setHost(host);
        ws.setName(workshop.getName());
        ws.setDate(workshop.getDate());
        ws.setDescription(workshop.getDescription());
        workshopRepository.save(ws);
        WorkshopDTO res = convertToWorkshopDTO(ws);

        return res;
    }


    //全ての勉強会の一覧を取得する
    public List<WorkshopDTO> handleGetAllWorkshop(){
        List<Workshop> allWs = workshopRepository.findAll();
        List<WorkshopDTO> res = new ArrayList<>();
        for(Workshop ws : allWs){
            res.add(convertToWorkshopDTO(ws));
        }

        return res;
    }
    //選択した勉強会を取得する
    public WorkshopDTO handleGetWorkshop(UUID id){
        Workshop ws = workshopRepository.findById(id).orElseThrow(()-> new UsernameNotFoundException("勉強会が見つかりませんでした"));
        return convertToWorkshopDTO(ws);
    }

    //近い開催予定勉強会取得
    public List<WorkshopDTO> getUpComingWorkshops(){
        List<Workshop> workshopList = this.workshopRepository.findTop10ByDateAfterOrderByDateAsc(Instant.now());
        List<WorkshopDTO> res = new ArrayList<>();
        for(Workshop ws : workshopList){
            res.add(convertToWorkshopDTO(ws));
        }
        return res;
    }

    //get workshop by ID　(WorkshopDTOはコントローラで実装する)
    public WorkshopDTO getWorkshopById(UUID id) {
         Workshop ws = this.workshopRepository.findById(id).orElseThrow(()-> new RuntimeException("勉強会が存在していません"));
        return convertToWorkshopDTO(ws);
    }

    public Workshop updateWorkshop(UUID id,Workshop workshop)
    {
        boolean isExist = this.workshopRepository.existsById(id);
        if(isExist)
        {
            Workshop currentWs = this.workshopRepository.findById(id).orElseThrow(()-> new RuntimeException("編集できませんでした。"));
            currentWs.setName(workshop.getName());
            currentWs.setDescription(workshop.getDescription());
            currentWs.setDate(workshop.getDate());
            this.workshopRepository.save(currentWs);
            return currentWs;
        }
        return null;
    }

    public boolean deleteWorkshop(UUID id) {
        boolean isExist = this.workshopRepository.existsById(id);
        if(!isExist){
            return false;
        }
        Workshop currentWorkshop = this.workshopRepository.findById(id).orElseThrow(()-> new RuntimeException("勉強会が見つかりませんでした"));
        this.workshopRepository.delete(currentWorkshop);
        return true;
    }

    //自分の作成した勉強会一覧取得
    public List<WorkshopDTO> getAllMyWorkshops(String email) {
        UUID myId = this.userService.getUserByEmail(email).getId();
        List<Workshop> myWorkshopList = this.workshopRepository.findByHostId(myId);
        List<WorkshopDTO> res = new ArrayList<>();
        for(Workshop ws : myWorkshopList){
            res.add(convertToWorkshopDTO(ws));
        }
        return  res;
    }

    public long getSumOfWorks() {
        return workshopRepository.count();
    }
}
