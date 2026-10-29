package skyware.product;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.skywareapi.company.entity.CompanyEntity;
import skyware.product.skywareapi.company.repository.CompanyRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductApplicationTests {

	@Autowired
	private CompanyRepository companyRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Disabled("Disabled to prevent consuming MySQL auto-increment IDs on the local database")
	@Transactional
	void testCompanyEntityAndTable() {
		String testEmail = "test_" + System.currentTimeMillis() + "@acme.com";
		String testPhone = "8" + (System.currentTimeMillis() % 1000000000L);

		CompanyEntity company = CompanyEntity.builder()
				.name("Acme Corp")
				.email(testEmail)
				.phone(testPhone)
				.otp(null)
				.verifyotp(0)
				.domain("acme.com")
				.status(0)
				.adminApprove(0)
				.build();

		CompanyEntity saved = companyRepository.save(company);
		assertNotNull(saved.getId());

		Optional<CompanyEntity> found = companyRepository.findByEmail(testEmail);
		assertTrue(found.isPresent());
		assertEquals("Acme Corp", found.get().getName());
		assertEquals(testPhone, found.get().getPhone());
		assertEquals(0, found.get().getVerifyotp());
		assertEquals(0, found.get().getStatus());
		assertEquals(0, found.get().getAdminApprove());
		assertNotNull(found.get().getCreatedAt());
	}

	@Autowired
	private javax.sql.DataSource dataSource;

	@Test
	void testTeachersTableExistsAndWorks() throws Exception {
		try (var conn = dataSource.getConnection();
		     var stmt = conn.createStatement()) {
			var rs = stmt.executeQuery("DESCRIBE teachers");
			boolean hasName = false, hasEmail = false, hasPhone = false, hasOtp = false, hasVerifyotp = false, hasStatus = false;
			while (rs.next()) {
				String field = rs.getString("Field");
				if ("name".equalsIgnoreCase(field)) hasName = true;
				if ("email".equalsIgnoreCase(field)) hasEmail = true;
				if ("phone".equalsIgnoreCase(field)) hasPhone = true;
				if ("otp".equalsIgnoreCase(field)) hasOtp = true;
				if ("verifyotp".equalsIgnoreCase(field)) hasVerifyotp = true;
				if ("status".equalsIgnoreCase(field)) hasStatus = true;
			}
			assertTrue(hasName);
			assertTrue(hasEmail);
			assertTrue(hasPhone);
			assertTrue(hasOtp);
			assertTrue(hasVerifyotp);
			assertTrue(hasStatus);
		}
	}

	@Test
	void testTeacherCompanyMapsTableExists() throws Exception {
		try (var conn = dataSource.getConnection();
		     var stmt = conn.createStatement()) {
			var rs = stmt.executeQuery("DESCRIBE teacher_company_maps");
			boolean hasId = false, hasTeacherId = false, hasCompanyId = false, hasStatus = false, hasCreatedAt = false, hasUpdatedAt = false;
			while (rs.next()) {
				String field = rs.getString("Field");
				if ("id".equalsIgnoreCase(field)) hasId = true;
				if ("teacher_id".equalsIgnoreCase(field)) hasTeacherId = true;
				if ("company_id".equalsIgnoreCase(field)) hasCompanyId = true;
				if ("status".equalsIgnoreCase(field)) hasStatus = true;
				if ("created_at".equalsIgnoreCase(field)) hasCreatedAt = true;
				if ("updated_at".equalsIgnoreCase(field)) hasUpdatedAt = true;
			}
			assertTrue(hasId);
			assertTrue(hasTeacherId);
			assertTrue(hasCompanyId);
			assertTrue(hasStatus);
			assertTrue(hasCreatedAt);
			assertTrue(hasUpdatedAt);
		}
	}

	@Test
	void testSpecialisationsTableExists() throws Exception {
		try (var conn = dataSource.getConnection();
		     var stmt = conn.createStatement()) {
			var rs = stmt.executeQuery("DESCRIBE specialisations");
			boolean hasId = false, hasName = false, hasShortName = false, hasStatus = false, hasCreatedAt = false, hasUpdatedAt = false;
			while (rs.next()) {
				String field = rs.getString("Field");
				if ("id".equalsIgnoreCase(field)) hasId = true;
				if ("name".equalsIgnoreCase(field)) hasName = true;
				if ("short_name".equalsIgnoreCase(field)) hasShortName = true;
				if ("status".equalsIgnoreCase(field)) hasStatus = true;
				if ("created_at".equalsIgnoreCase(field)) hasCreatedAt = true;
				if ("updated_at".equalsIgnoreCase(field)) hasUpdatedAt = true;
			}
			assertTrue(hasId);
			assertTrue(hasName);
			assertTrue(hasShortName);
			assertTrue(hasStatus);
			assertTrue(hasCreatedAt);
			assertTrue(hasUpdatedAt);
		}
	}

	@Test
	void testTeacherSpecialisationMapsTableExists() throws Exception {
		try (var conn = dataSource.getConnection();
		     var stmt = conn.createStatement()) {
			var rs = stmt.executeQuery("DESCRIBE teacher_specialisation_maps");
			boolean hasId = false, hasTeacherId = false, hasSpId = false, hasStatus = false, hasCreatedAt = false, hasUpdatedAt = false;
			while (rs.next()) {
				String field = rs.getString("Field");
				if ("id".equalsIgnoreCase(field)) hasId = true;
				if ("teacher_id".equalsIgnoreCase(field)) hasTeacherId = true;
				if ("sp_id".equalsIgnoreCase(field)) hasSpId = true;
				if ("status".equalsIgnoreCase(field)) hasStatus = true;
				if ("created_at".equalsIgnoreCase(field)) hasCreatedAt = true;
				if ("updated_at".equalsIgnoreCase(field)) hasUpdatedAt = true;
			}
			assertTrue(hasId);
			assertTrue(hasTeacherId);
			assertTrue(hasSpId);
			assertTrue(hasStatus);
			assertTrue(hasCreatedAt);
			assertTrue(hasUpdatedAt);
		}
	}

	@Autowired
	private skyware.product.skywareapi.specialisation.service.SpecialisationService specialisationService;

	@Test
	@Transactional
	void testSpecialisationCrudAndValidations() {
		// 1. Validation: Name and short_name cannot be the same
		var sameNameDto = skyware.product.skywareapi.specialisation.dto.SpecialisationRequestDto.builder()
				.name("Computer Science")
				.shortName("Computer Science")
				.build();
		assertThrows(skyware.product.error.ValidationError.class, () -> specialisationService.addSpecialisation(sameNameDto));

		// 2. Add: valid specialisation
		String uniqueSuffix = "_" + System.currentTimeMillis();
		var validDto = skyware.product.skywareapi.specialisation.dto.SpecialisationRequestDto.builder()
				.name("Computer Science" + uniqueSuffix)
				.shortName("CS" + uniqueSuffix)
				.build();
		var created = specialisationService.addSpecialisation(validDto);
		assertNotNull(created.getId());
		assertEquals("Computer Science" + uniqueSuffix, created.getName());
		assertEquals("CS" + uniqueSuffix, created.getShortName());

		// 3. Get all
		var list = specialisationService.getAllSpecialisations();
		assertFalse(list.isEmpty());

		// 4. Update: Non-existent ID should throw ValidationError
		var updateDto = skyware.product.skywareapi.specialisation.dto.SpecialisationRequestDto.builder()
				.name("Updated Name" + uniqueSuffix)
				.shortName("UN" + uniqueSuffix)
				.build();
		assertThrows(skyware.product.error.ValidationError.class, () -> specialisationService.updateSpecialisation(999999L, updateDto));

		// 5. Update: Same name and short name should throw ValidationError
		var invalidUpdateDto = skyware.product.skywareapi.specialisation.dto.SpecialisationRequestDto.builder()
				.name("SameName" + uniqueSuffix)
				.shortName("SameName" + uniqueSuffix)
				.build();
		assertThrows(skyware.product.error.ValidationError.class, () -> specialisationService.updateSpecialisation(created.getId(), invalidUpdateDto));

		// 6. Update: Valid update succeeds
		var updated = specialisationService.updateSpecialisation(created.getId(), updateDto);
		assertEquals("Updated Name" + uniqueSuffix, updated.getName());
		assertEquals("UN" + uniqueSuffix, updated.getShortName());

		// 7. Delete: Valid delete succeeds
		specialisationService.deleteSpecialisation(created.getId());
		assertThrows(skyware.product.error.ValidationError.class, () -> specialisationService.getSpecialisationById(created.getId()));
	}

	@Autowired
	private skyware.product.skywareapi.company.service.CompanyService companyService;

	@Test
	@Disabled("Disabled to prevent consuming MySQL auto-increment IDs on the local database")
	@Transactional
	void testCompanyRegistrationAndVerifyOtp() {
		String testEmail = "tech_" + System.currentTimeMillis() + "@solutions.com";
		String testPhone = "9" + (System.currentTimeMillis() % 1000000000L);

		var registerDto = skyware.product.skywareapi.company.dto.CompanyRegisterRequestDto.builder()
				.name("Tech Solutions")
				.email(testEmail)
				.phone(testPhone)
				.domain("techsolutions.com")
				.build();

		var registered = companyService.register(registerDto);
		assertNotNull(registered.getId());
		assertEquals("Tech Solutions", registered.getName());
		assertEquals(0, registered.getVerifyotp());
		assertEquals(0, registered.getStatus());

		var entityOpt = companyRepository.findByEmail(testEmail);
		assertTrue(entityOpt.isPresent());
		Integer generatedOtp = entityOpt.get().getOtp();
		assertNotNull(generatedOtp);

		// Verify OTP
		var verifyDto = skyware.product.skywareapi.company.dto.CompanyVerifyOtpRequestDto.builder()
				.email(testEmail)
				.otp(generatedOtp)
				.build();

		var verified = companyService.verifyOtp(verifyDto);
		assertEquals(1, verified.getVerifyotp());
		assertEquals(0, verified.getStatus());

		var updatedOpt = companyRepository.findByEmail(testEmail);
		assertTrue(updatedOpt.isPresent());
		assertEquals(1, updatedOpt.get().getVerifyotp());
		assertEquals(0, updatedOpt.get().getStatus());
		assertEquals(generatedOtp, updatedOpt.get().getOtp());

		// Test activation via token
		String token = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(testEmail.getBytes(java.nio.charset.StandardCharsets.UTF_8));
		var activationResult1 = companyService.activateCompany(token);
		assertTrue(activationResult1.isNewlyActivated());
		assertEquals("activated successfully", activationResult1.getMessage());
		assertEquals(1, activationResult1.getCompany().getStatus());

		var activatedEntityOpt = companyRepository.findByEmail(testEmail);
		assertTrue(activatedEntityOpt.isPresent());
		assertEquals(1, activatedEntityOpt.get().getStatus());

		// Test clicking activate button again when already status 1 -> already activated
		var activationResult2 = companyService.activateCompany(token);
		assertFalse(activationResult2.isNewlyActivated());
		assertEquals("already activated", activationResult2.getMessage());
		assertEquals(1, activationResult2.getCompany().getStatus());

		// Test activateCompany via raw email directly
		var activationResultEmail = companyService.activateCompany(testEmail);
		assertFalse(activationResultEmail.isNewlyActivated());
		assertEquals("already activated", activationResultEmail.getMessage());
	}
}

