package com.example.albanet.staff.security;

import com.example.albanet.staff.internal.StaffEntity;
import com.example.albanet.staff.internal.StaffService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class StaffUserDetailsService implements UserDetailsService {

    private final StaffService staffService;

    public StaffUserDetailsService(StaffService staffService) {
        this.staffService = staffService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            StaffEntity staff = staffService.getActiveStaffByEmail(email);
            return new StaffUserDetails(staff);
        } catch (IllegalStateException e) {
            throw new UsernameNotFoundException("Staff not found or inactive: " + email, e);
        }
    }
}
