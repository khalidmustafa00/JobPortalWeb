package com.talimhire.jobportal.controller;

import com.talimhire.jobportal.entity.RecruiterProfile;
import com.talimhire.jobportal.entity.Users;
import com.talimhire.jobportal.repository.UsersRepository;
import com.talimhire.jobportal.services.RecruiterProfileService;
import com.talimhire.jobportal.util.FileUploadUtil;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@Controller
@RequestMapping("/recruiter-profile")
public class RecruiterProfileController {
    private final UsersRepository usersRepository;
    private final RecruiterProfileService recruiterProfileService;

    public RecruiterProfileController(UsersRepository usersRepository,RecruiterProfileService recruiterProfileService) {
        this.usersRepository = usersRepository;
        this.recruiterProfileService = recruiterProfileService;
    }

    @GetMapping("/")
    public String recruiterProfile(Model model) {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String currentUserName=authentication.getName();
           Users users= usersRepository.findByEmail(currentUserName).orElseThrow(()->new UsernameNotFoundException("Could not found "+currentUserName+" users"));
          Optional<RecruiterProfile> recruiterProfile=recruiterProfileService.getOne(users.getUserId());
          if(!recruiterProfile.isEmpty()){
              model.addAttribute("profile",recruiterProfile.get());
          }


        }
        return "recruiter_profile";
    }
    @PostMapping("/addNew")
    public String addNew(RecruiterProfile recruiterProfile, @RequestParam("image")MultipartFile multipartFile,Model model){
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String currentUserName=authentication.getName();
            Users users= usersRepository.findByEmail(currentUserName)
                    .orElseThrow(()->new UsernameNotFoundException("Could not found "+currentUserName+" users"));
            recruiterProfile.setUserId(users);
            recruiterProfile.setUserAccountId(users.getUserId());
        }
        model.addAttribute("profile",recruiterProfile);
        String fileName="";
        if(!multipartFile.getOriginalFilename().equals("")){
            fileName= StringUtils.cleanPath(Objects.requireNonNull(multipartFile.getOriginalFilename()));
            recruiterProfile.setProfilePhoto(fileName);
        }
        RecruiterProfile savedUser=recruiterProfileService.addNew(recruiterProfile);
        String uploadDir="photos/recruiter/"+savedUser.getUserAccountId();
        try{
            FileUploadUtil.saveFile(uploadDir,fileName,multipartFile);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return "redirect:/dashboard";
    }
}
