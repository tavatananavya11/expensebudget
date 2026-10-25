package in.expensebudget.security;

import in.expensebudget.model.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record AccountPrincipal(Long id, String email, String name, String passwordHash) implements UserDetails {
    public static AccountPrincipal from(UserAccount account) {
        return new AccountPrincipal(account.getId(), account.getEmail(), account.getName(),
                account.getPasswordHash());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
