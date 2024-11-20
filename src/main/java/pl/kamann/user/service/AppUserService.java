package pl.kamann.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.user.dto.AppUserDto;
import pl.kamann.user.mapper.AppUserMapper;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.model.AppUserStatus;
import pl.kamann.user.repository.AppUserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final AppUserMapper appUserMapper;

    public List<AppUserDto> getAllUsers() {
        List<AppUser> users = appUserRepository.findAll();
        return appUserMapper.toDtoList(users);
    }

    public AppUserDto getUserById(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        return appUserMapper.toDto(user);
    }

    public AppUserDto createUser(AppUserDto userDto) {
        Set<Role> roles = roleRepository.findByNameIn(userDto.getRoles());
        AppUser user = appUserMapper.toEntity(userDto, roles);
        AppUser savedUser = appUserRepository.save(user);
        return appUserMapper.toDto(savedUser);
    }

    public AppUserDto updateUser(Long id, AppUserDto userDto) {
        AppUser existingUser = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));

        Set<Role> roles = roleRepository.findByNameIn(userDto.getRoles());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setRoles(roles);

        AppUser updatedUser = appUserRepository.save(existingUser);
        return appUserMapper.toDto(updatedUser);
    }

    public void deleteUser(Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
        appUserRepository.delete(user);
    }

    public AppUserDto updateUserRoles(Long userId, Set<Role> roleNames) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Set<Role> roles = roleRepository.findByNameIn(roleNames);
        if (roles.isEmpty()) {
            throw new RuntimeException("No roles found for the provided names");
        }

        user.setRoles(roles);

        AppUser updatedUser = appUserRepository.save(user);
        return appUserMapper.toDto(updatedUser);
    }

    public List<AppUserDto> getUsersByRole(String roleName) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found with name: " + roleName));
        List<AppUser> users = appUserRepository.findByRolesContaining(role);
        return appUserMapper.toDtoList(users);
    }

    public List<AppUserDto> getUsersWithExpiringMembershipCards(LocalDate expiryDate) {
        List<AppUser> users = appUserRepository.findUsersWithExpiringMembershipCards(expiryDate);
        return appUserMapper.toDtoList(users);
    }

    public AppUserDto changeUserStatus(Long userId, AppUserStatus status) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        user.setStatus(status);
        AppUser updatedUser = appUserRepository.save(user);
        return appUserMapper.toDto(updatedUser);
    }
}