package br.com.sales.code.bug.iam.repository;

import br.com.sales.code.bug.iam.domain.Permission;
import br.com.sales.code.bug.iam.domain.Role;
import br.com.sales.code.bug.iam.domain.exception.EntityNotFoundException;
import br.com.sales.code.bug.iam.domain.exception.RoleNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class RoleRepositoryTest {

    @Test
    public void shouldSaveRole() {
        var roleRepository = new RoleRepository();

        var initRoleList = roleRepository.findAll();

        assertNotNull(initRoleList);
        assertEquals(0, initRoleList.size());

        var role = new Role("role", Set.of(new Permission("write"), new Permission("read")));
        roleRepository.save(role);

        var endRoleList = roleRepository.findAll();

        assertNotNull(endRoleList);
        assertEquals(1, endRoleList.size());
    }

    @Test
    public void shouldFindRoleById() {
        var roleRepository = new RoleRepository();
        var role = new Role("role", Set.of(new Permission("write"), new Permission("read")));
        roleRepository.save(role);

        var foundRole = roleRepository.findById("role");

        assertNotNull(foundRole);
        assertTrue(foundRole.isPresent());
        assertEquals("role", foundRole.get().getName());
    }

    @Test
    public void shouldGetRoleById() {
        var roleRepository = new RoleRepository();
        var role = new Role("role", Set.of(new Permission("write"), new Permission("read")));
        roleRepository.save(role);

        var foundRole = roleRepository.getById("role");

        assertNotNull(foundRole);
        assertEquals("role", foundRole.getName());
    }

    @Test
    public void shouldThrowRoleNotFoundException() {
        var roleRepository = new RoleRepository();

        var exception = assertThrows(RoleNotFoundException.class, () -> {
            roleRepository.getById("role");
        });
        assertTrue(exception instanceof EntityNotFoundException);
    }
}
