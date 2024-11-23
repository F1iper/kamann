package pl.kamann.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pl.kamann.auth.role.model.Role;
import pl.kamann.auth.role.repository.RoleRepository;
import pl.kamann.config.exception.handler.ApiException;
import pl.kamann.config.global.Codes;
import pl.kamann.user.dto.AppUserDto;
import pl.kamann.user.mapper.AppUserMapper;
import pl.kamann.user.model.AppUser;
import pl.kamann.user.model.AppUserStatus;
import pl.kamann.user.repository.AppUserRepository;
import pl.kamann.utility.EntityLookupService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final AppUserMapper appUserMapper;
    private final EntityLookupService entityLookupService;

    public List<AppUserDto> getAllUsers() {
        return appUserMapper.toDtoList(appUserRepository.findAll());
    }

    public AppUserDto getUserById(Long id) {
        AppUser user = entityLookupService.findUserById(id);
        return appUserMapper.toDto(user);
    }

    public AppUserDto createUser(AppUserDto userDto) {
        entityLookupService.validateEmailNotTaken(userDto.getEmail());

        Set<Role> roles = entityLookupService.findRolesByNameIn(userDto.getRoles());

        AppUser user = appUserMapper.toEntity(userDto, roles);

        if (user.getStatus() == null) {
            user.setStatus(AppUserStatus.ACTIVE);
        }

        return appUserMapper.toDto(appUserRepository.save(user));
    }

    public AppUserDto updateUser(Long id, AppUserDto userDto) {
        AppUser existingUser = entityLookupService.findUserById(id);

        Set<Role> roles = entityLookupService.findRolesByNameIn(userDto.getRoles());
        if (roles.isEmpty()) {
            throw new ApiException("No valid roles provided for the user.", HttpStatus.BAD_REQUEST, Codes.INVALID_ROLE);
        }

        existingUser.setEmail(userDto.getEmail());
        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setRoles(roles);

        return appUserMapper.toDto(appUserRepository.save(existingUser));
    }

    public void deleteUser(Long id) {
        appUserRepository.delete(entityLookupService.findUserById(id));
    }

    public AppUserDto updateUserRoles(Long userId, Set<Role> roleNames) {
        AppUser user = entityLookupService.findUserById(userId);
        Set<Role> roles = entityLookupService.findRolesByNameIn(roleNames);

        user.setRoles(roles);
        return appUserMapper.toDto(appUserRepository.save(user));
    }

    public List<AppUserDto> getUsersByRole(String roleName) {
        Role role = entityLookupService.findRoleByName(roleName);
        return appUserMapper.toDtoList(appUserRepository.findByRolesContaining(role));
    }

    public List<AppUserDto> getUsersWithExpiringMembershipCards(LocalDate expiryDate) {
        return appUserMapper.toDtoList(appUserRepository.findUsersWithExpiringMembershipCards(expiryDate));
    }

    public AppUserDto changeUserStatus(Long userId, AppUserStatus status) {
        AppUser user = entityLookupService.findUserById(userId);
        user.setStatus(status);
        return appUserMapper.toDto(appUserRepository.save(user));
    }
}