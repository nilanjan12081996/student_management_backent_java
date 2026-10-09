package skyware.product.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
public class JwtUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String role;
    private final String firstname;
    private final String lastname;
    private final Collection<? extends GrantedAuthority> authorities;

    // Constructor matching your snippet exactly:
    // (userId, email, role, firstname, lastname, authorities)
    public JwtUserDetails(Long id, String email, String role, String firstname, String lastname, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.firstname = firstname;
        this.lastname = lastname;
        this.authorities = authorities;
    }

    // --- UserDetails Interface Implementation ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // Password is not needed for JWT authentication objects
    }

    @Override
    public String getUsername() {
        return email; // We use email as the username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
