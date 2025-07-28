package com.talimhire.jobportal.services;

import com.talimhire.jobportal.entity.JobSeekerProfile;
import com.talimhire.jobportal.entity.RecruiterProfile;
import com.talimhire.jobportal.entity.Users;
import com.talimhire.jobportal.repository.JobSeekerProfileRepository;
import com.talimhire.jobportal.repository.RecruiterProfileRepository;
import com.talimhire.jobportal.repository.UsersRepository;
import org.apache.catalina.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final JobSeekerProfileRepository jobSeekerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public UsersService(UsersRepository usersRepository,PasswordEncoder passwordEncoder,RecruiterProfileRepository recruiterProfileRepository,JobSeekerProfileRepository jobSeekerProfileRepository) {
        this.usersRepository = usersRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.jobSeekerProfileRepository = jobSeekerProfileRepository;
        this.passwordEncoder=passwordEncoder;

    }
    public Users addNew(Users users){
        users.setActive(true);
        users.setRegistrationDate(new Date(System.currentTimeMillis()));
        users.setPassword(passwordEncoder.encode(users.getPassword()));
        Users savedUser=usersRepository.save(users);
        int userTypeId=users.getUserTypeId().getUserTypeId();
        if(userTypeId==1){
                    recruiterProfileRepository.save(new RecruiterProfile(savedUser));
        }else{
            jobSeekerProfileRepository.save(new JobSeekerProfile(savedUser));
        }
        return savedUser;
    }/*
    public Optional<Users> findByEmail(String email){
        return usersRepository.findByEmail(email);
    }*/

    public Object getCurrentUserProfile() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String username=authentication.getName();
          Users users=  usersRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Could not found "+username+" user"));
          int userId=users.getUserId();
          if(authentication.getAuthorities().contains(new SimpleGrantedAuthority("Recruiter"))){
             RecruiterProfile recruiterProfile= recruiterProfileRepository.findById(userId).orElse(new RecruiterProfile());
             return recruiterProfile;
          }
          else{
             JobSeekerProfile jobSeekerProfile= jobSeekerProfileRepository.findById(userId).orElse(new JobSeekerProfile());
              return jobSeekerProfile;

          }
        }
        return null;
    }

    public Users getCurrentUser() {
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String username=authentication.getName();
            Users user=  usersRepository.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Could not found "+username+" user"));
            return user;

        }
        return null;
    }

    public Users findByEmail(String currentUsername) {
        return usersRepository.findByEmail(currentUsername).orElseThrow(() -> new UsernameNotFoundException("User not " +
                "found"));
    }

    public Optional<Users> getUserByEmail(String email) {
        return usersRepository.findByEmail(email);
    }
}
