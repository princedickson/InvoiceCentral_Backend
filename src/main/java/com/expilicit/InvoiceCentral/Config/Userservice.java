package com.expilicit.InvoiceCentral.Config;

import com.expilicit.InvoiceCentral.Entity.UserRegistration;
import com.expilicit.InvoiceCentral.Repository.CustomerInvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

@Service
@AllArgsConstructor
public class Userservice implements UserDetailsService {
    private final CustomerInvoiceRepository customerInvoiceRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return customerInvoiceRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException("No user found" + email));
    }

   /* private UserDetails CreateSpringUser(UserRegistration userRegistration) {
        return org.springframework.security.core.userdetails.User.withUsername(userRegistration.getEmail())
                .password(userRegistration.getPassword())
                .username(userRegistration.getUsername())
                .authorities(grantedAuthorities(userRegistration.getAppRole().name()))
                .build();
    }*/
    private Collection<? extends GrantedAuthority> grantedAuthorities( String role){
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

}
